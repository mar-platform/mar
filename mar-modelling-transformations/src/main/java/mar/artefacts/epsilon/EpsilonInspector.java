package mar.artefacts.epsilon;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.epsilon.common.module.ModuleElement;
import org.eclipse.epsilon.egl.internal.EglModule;
import org.eclipse.epsilon.eol.AbstractModule;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.etl.EtlModule;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;

import mar.analysis.backend.megamodel.inspectors.InspectionErrorException;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.MetamodelReference.Kind;
import mar.artefacts.RecoveredPath.MissingPath;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.MetamodelSeacher.RecoveredMetamodelFile;
import mar.validation.AnalysisDB;

public class EpsilonInspector extends ProjectInspector {

	ImmutableMap<String, Supplier<AbstractModule>> map = new ImmutableMap.Builder<String, Supplier<AbstractModule>>().
			put("eol", EolModule::new).
			put("etl", EtlModule::new).
			put("egl", EglModule::new).
			build();
	
	
	public EpsilonInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath, analysisDb);
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		EpsilonProgram program = new EpsilonProgram(new RecoveredPath(getRepositoryPath(f)));
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		// Try to parse, at least to discard invalid files downloaded by mistake
		String extension = Files.getFileExtension(f.getName());
		Supplier<AbstractModule> moduleFactory = map.get(extension);
		if (moduleFactory != null) {
			AbstractModule module = moduleFactory.get();
			try {
			    if (! module.parse(f)) {
				throw new InspectionErrorException.SyntaxError(program);
			    }
			} catch (java.lang.StackOverflowError e) {
			    // TODO: Possibly use a more semantic exception type
			    throw new InspectionErrorException.SyntaxError(program);	    
			}
		}
		
		String programText = IOUtils.toString(new FileInputStream(f), Charset.defaultCharset());
		List<String> deps = extractDependencies(programText);
		for (String string : deps) {
			Path loosyPath = getRepositoryPath(f).resolve(Paths.get(string));			
			RecoveredPath r = getFileSearcher().findFile(loosyPath);
			program.addImportDependency(r.getPath()); // FIXME: Check that that this is not using RecoveredPath
		}
		
		Map<String, RecoveredMetamodelFile> classFootprints = toClassFootprints(programText);
		
		// Treat specific files in a different way
		if (f.getName().endsWith(".egx")) {
			processEgx(programText, program, classFootprints);
		}
		
		MetamodelSeacher ms = getMetamodelSearcher();
		Map<String, RecoveredMetamodelFile> recoveredMetamodels = ms.search(classFootprints);
		recoveredMetamodels.forEach((mi, mmFile) -> {
			Metamodel mm = mmFile.getBestMetamodel();
			graph.addMetamodel(mm);
			List<Kind> kinds = toKinds(mi);
			program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
		});

		return graph;
	}

	private void processEgx(String strProgram, EpsilonProgram program, Map<String, RecoveredMetamodelFile> classFootprints) {
		matchUnqualifiedClassNames(strProgram, classFootprints);
		matchTemplateCalls(strProgram, program);
	}

	private static final Pattern TEMPLATE_FACTORY_PATTERN = Pattern.compile("TemplateFactory\\.load\\(['\"]([^'\"]*)['\"]\\)");
	private static final Pattern IMPORT_PATTERN= Pattern.compile("import\\s+['\"]([^'\"]*)['\"]");
	

	private List<String> extractDependencies(String text) {
		Matcher matcher = TEMPLATE_FACTORY_PATTERN.matcher("");
		Matcher matcher2 = IMPORT_PATTERN.matcher("");

		List<String> result = new ArrayList<String>();
		text.lines().forEach(line -> {
			line = line.stripLeading();
			if (! line.startsWith("//")) {
				matcher.reset(line);
				while (matcher.find()) {
					String content = matcher.group(1);
					result.add(content);
				}
				
				matcher2.reset(line);
				while (matcher2.find()) {
					String content = matcher2.group(1);
					result.add(content);
				}	
			}
		});
        
        return result;
	}
		
	private final String IDENTIFIER_PATTERN = "(?:\\b[_a-zA-Z]|\\B\\$)[_$a-zA-Z0-9]*+";
	
	private void matchUnqualifiedClassNames(String program, Map<String, RecoveredMetamodelFile> classFootprints) {
		Pattern pattern = Pattern.compile("transform\\s+\\S+\\s*:\\s*(" + IDENTIFIER_PATTERN + ")\\s*\\{");
		final Matcher matcher = pattern.matcher(program);
		while (matcher.find()) {
			String className = matcher.group(1);
			RecoveredMetamodelFile mm = classFootprints.computeIfAbsent("no-name", (k) -> new RecoveredMetamodelFile());
			mm.addToFootprint(className);
		}
	}

	private void matchTemplateCalls(String strProgram, EpsilonProgram program) {
		// Example: template : "table2html.egl"
		Pattern pattern = Pattern.compile("template\\s+:\\s*\"(.+)\"");
		final Matcher matcher = pattern.matcher(strProgram);
		while (matcher.find()) {
			String templateName = matcher.group(1);
			RecoveredPath recovered = getFileSearcher().findFile(Paths.get(templateName));
			if (! (recovered instanceof MissingPath)) {
				program.addImportDependency(recovered.getPath());
			}
		}
	}
	
	private Map<String, RecoveredMetamodelFile> toClassFootprints(String epsilonProgram) {
		Pattern pattern = Pattern.compile("(" + IDENTIFIER_PATTERN + ")!(" + IDENTIFIER_PATTERN + ")");

		Map<String, RecoveredMetamodelFile> classFootprints = new HashMap<>();
		final Matcher matcher = pattern.matcher(epsilonProgram);
		while (matcher.find()) {
			String pkgName = matcher.group(1);
			String className = matcher.group(2);

			RecoveredMetamodelFile mm = classFootprints.computeIfAbsent(pkgName, (k) -> new RecoveredMetamodelFile());
			mm.addToFootprint(className);
		}
		return classFootprints;
	}

	private List<MetamodelReference.Kind> toKinds(String modelInfo) {
		List<MetamodelReference.Kind> kinds = new ArrayList<>();
		kinds.add(MetamodelReference.Kind.TYPED_BY);
		// TODO: Check in which position the model is used to determine if it is input/output
		return kinds;
	}

}
