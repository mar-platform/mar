package mar.analysis.backend.megamodel.inspectors;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.epsilon.FileSearcher;
import mar.artefacts.graph.RecoveryGraph;

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

	public EmfaticInspector(Path repoFolder, Path projectSubPath) {
		super(repoFolder, projectSubPath);
		this.searcher = new FileSearcher(repoFolder, getProjectFolder());
	}

	public boolean isPackage(@Nonnull String line) {
		return line.stripLeading().startsWith("@namespace");
	}
	
	public String extractURI(@Nonnull String line) {
		final String uriTag = "uri=\"";
		int idx = line.indexOf(uriTag) + uriTag.length();
		Preconditions.checkState(idx != -1);
		
		int last = line.indexOf("\"", idx);
		Preconditions.checkState(last != -1);
		
		return line.substring(idx, last);
	}
	
	@Override
	public RecoveryGraph process(File f) throws Exception {
		
		List<String> uris = Files.lines(f.toPath()).
								filter(this::isPackage).
								map(this::extractURI).
								collect(Collectors.toList());
				
		RecoveryGraph graph = new RecoveryGraph();
		
		EmfaticProgram p = new EmfaticProgram(new RecoveredPath(getRepositoryPath(f)));		
		graph.addProgram(p);
		
		if (uris.isEmpty()) {
			String filename = f.getName().replace(".emf", ".ecore");
			File parent = f.getParentFile();

			RecoveredPath rp = searcher.findInFolder(parent.toPath(), filename);
			Metamodel mm = Metamodel.fromFile(filename, rp);
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.GENERATE);		
		} else {			
			// Assume that URIs between .ecore files match
			// Assume that the root package URIs the main URI, and the rest are dependent meta-models
			String root = uris.get(0);
			Metamodel rootPkg = Metamodel.fromURI(root, root);
			for (int i = 1; i < uris.size(); i++) {
				String uri = uris.get(i);
				Metamodel mm = Metamodel.fromURI(uri, uri);
				rootPkg.addSubpackage(mm);
			}
			graph.addMetamodel(rootPkg);
			/*
			for (String uri : uris) {
				Metamodel mm = Metamodel.fromURI(uri, uri);
				graph.addMetamodel(mm);
				p.addMetamodel(mm, MetamodelReference.Kind.GENERATE);
			}
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
