package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Path;

import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.graph.RecoveryGraph;
import mar.models.xtext.XtextLoader;
import mar.models.xtext.XtextLoader.XtextAnalysisResult;

public class XtextInspector extends ProjectInspector {

	public XtextInspector(Path repoFolder, Path projectSubPath) {
		super(repoFolder, projectSubPath);
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		XtextLoader loader = new XtextLoader();
		XtextAnalysisResult r = loader.doAnalysis(f);
		
		RecoveryGraph graph = new RecoveryGraph();
		
		XtextProgram p = new XtextProgram(new RecoveredPath(f.toPath()));		
		graph.addProgram(p);
		
		for (String uri : r.getGeneratedURIs()) {
			Metamodel mm = Metamodel.fromURI(uri, uri);
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.GENERATE, MetamodelReference.Kind.TYPED_BY);
		}
				
		for (String uri : r.getImportedURIs()) {
			Metamodel mm = Metamodel.fromURI(uri, uri);
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.IMPORT, MetamodelReference.Kind.TYPED_BY);
		}
		
		return graph;
	}	
	
	public static class XtextProgram extends FileProgram {

		public XtextProgram(RecoveredPath path) {
			super(path);
		}
		
		@Override
		public String getKind() {
			return "xtext";
		}
		
		@Override
		public String getCategory() {
			return "textual-syntax";
		}
		
	}
}
