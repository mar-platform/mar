package mar.artefacts.epsilon;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.MetamodelReference.Kind;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.MetamodelSeacher;
import mar.artefacts.search.MetamodelSeacher.RecoveredMetamodelFile;
import mar.validation.AnalysisDB;

public class EpsilonInspector extends ProjectInspector {

	public EpsilonInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath, analysisDb);
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		EpsilonProgram program = new EpsilonProgram(new RecoveredPath(getRepositoryPath(f)));
		RecoveryGraph graph = new RecoveryGraph(getProject());
		graph.addProgram(program);
		
		String toMatch = IOUtils.toString(new FileInputStream(f), Charset.defaultCharset());
		Map<String, RecoveredMetamodelFile> classFootprints = toClassFootprints(toMatch);
		
		MetamodelSeacher ms = getMetamodelSearcher();
		Map<String, RecoveredMetamodelFile> recoveredMetamodels = ms.search(classFootprints);
		recoveredMetamodels.forEach((mi, mmFile) -> {
			Path repoFile = getRepositoryPath(mmFile.getBestMetamodel());				
			Metamodel mm = Metamodel.fromFile(mi, new RecoveredPath(repoFile));
			graph.addMetamodel(mm);
			List<Kind> kinds = toKinds(mi);
			program.addMetamodel(mm, kinds.toArray(MetamodelReference.EMPTY_KIND));
		});

		return graph;
	}

	private Map<String, RecoveredMetamodelFile> toClassFootprints(String epsilonProgram) {
		Pattern pattern = Pattern.compile("(\\S)!(\\S)");

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
