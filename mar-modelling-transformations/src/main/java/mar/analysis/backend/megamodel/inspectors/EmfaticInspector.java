package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.apache.commons.io.IOUtils;

import com.google.common.base.Preconditions;

import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.artefacts.search.FileSearcher;
import mar.validation.AnalysisDB;

/**
 * In .emf files we can find package URIs in lines like:
 * 
 * <pre>
 *  @namespace(uri="http://my_uri", prefix="my_prefix")
 * </pre>
 * 
 * @author jesus
 *
 */
public class EmfaticInspector extends ProjectInspector {

	private final FileSearcher searcher;

	public EmfaticInspector(Path repoFolder, Path projectSubPath, AnalysisDB analysisDb) {
		super(repoFolder, projectSubPath, analysisDb);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
	}
	
	protected String extractURI(@Nonnull String text, int startIndex) {
		final String uriTag = "uri=\"";
		int idx = text.indexOf(uriTag, startIndex) + uriTag.length();
		Preconditions.checkState(idx != -1);
		
		int last = text.indexOf("\"", startIndex + idx);
		Preconditions.checkState(last != -1);
		
		return text.substring(idx, last);
	}
	
	private List<String> getUris(File f) throws IOException {
		List<String> uris = new ArrayList<>();

		String contents = IOUtils.toString(new FileInputStream(f), Charset.defaultCharset());
		int index = 0;
		while (true) {
			final String NAMESPACE = "@namespace";
			int namespaceStart = contents.indexOf(NAMESPACE, index);
			if (namespaceStart == -1) 
				break;
			
			int namespaceEnd = namespaceStart + NAMESPACE.length();
			String uri = extractURI(contents, namespaceEnd);
			uris.add(uri);
			index = namespaceEnd + uri.length(); // an estimation
		}
		
		return uris;
	}

	
	@Override
	public RecoveryGraph process(File f) throws Exception {
		List<String> uris = getUris(f);
				
		RecoveryGraph graph = new RecoveryGraph(getProject());
		
		EmfaticProgram p = new EmfaticProgram(new RecoveredPath(getRepositoryPath(f)));		
		graph.addProgram(p);
		
		if (uris.isEmpty()) {
			// TODO: Is this even possible?
			String filename = f.getName().replace(".emf", ".ecore");
			File parent = f.getParentFile();

			RecoveredPath rp = searcher.findInFolder(parent.toPath(), filename);
			Metamodel mm = Metamodel.fromFile(filename, rp);
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.GENERATE);		
		} else {			
			// Assume that URIs between .ecore files match
			// Assume that the root package URIs the main URI, and the rest are dependent meta-models
			String rootURI = uris.get(0);
			Metamodel metamodel = toMetamodelFromURI(f.getName().replace(".emf", ""), rootURI);
			graph.addMetamodel(metamodel);
			p.addMetamodel(metamodel, MetamodelReference.Kind.GENERATE);
			
			/*
			String root = uris.get(0);
			Metamodel rootPkg = Metamodel.fromURI(root, root);
			for (int i = 1; i < uris.size(); i++) {
				String uri = uris.get(i);
				Metamodel mm = Metamodel.fromURI(uri, uri);
				rootPkg.addSubpackage(mm);
			}
			graph.addMetamodel(rootPkg);
			*/
		}
		
		return graph;
	}	
	
	public static class EmfaticProgram extends FileProgram {

		public EmfaticProgram(RecoveredPath path) {
			super(path);
		}
		
		@Override
		public String getKind() {
			return "emfatic";
		}
		
		@Override
		public String getCategory() {
			return "metamodel-syntax";
		}
		
	}

}
