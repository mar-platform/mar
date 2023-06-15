package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Path;

import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
import mar.artefacts.ProjectInspector;
import mar.artefacts.RecoveredPath;
import mar.artefacts.db.RepositoryDB;
import mar.artefacts.graph.RecoveryGraph;
import mar.models.xtext.XtextLoader;
import mar.models.xtext.XtextLoader.XtextAnalysisResult;
import mar.models.xtext.XtextLoader.XtextInfo;
import mar.validation.AnalysisDB;

public class XtextInspector extends ProjectInspector {

	public XtextInspector(Path repoFolder, Path projectSubPath, AnalysisDB db, RepositoryDB repoDb) {
		super(repoFolder, projectSubPath, db, repoDb);
	}

	@Override
	public RecoveryGraph process(File f) throws Exception {
		XtextLoader loader = new XtextLoader();
		XtextInfo info = loader.getInfo(f);
		
		RecoveryGraph graph = new RecoveryGraph(getProject());
		
		XtextProgram p = new XtextProgram(new RecoveredPath(getRepositoryPath(f.toPath())));
		graph.addProgram(p);
		
		for (String uri : info.getGeneratedURIs()) {
			Metamodel mm = toMetamodel(uri, getRepositoryPath(f).getParent());
			graph.addMetamodel(mm);
			p.addMetamodel(mm, MetamodelReference.Kind.GENERATE, MetamodelReference.Kind.TYPED_BY);
		}
				
		for (String uri : info.getImportedURIs()) {
			Metamodel mm = toMetamodel(uri, getRepositoryPath(f).getParent());
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
