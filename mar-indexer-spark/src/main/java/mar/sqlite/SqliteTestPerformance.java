package mar.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.indexer.common.configuration.ModelLoader;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.paths.PathFactory;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public class SqliteTestPerformance {

	public static void main(String[] args) throws IOException {
		SqliteIndexDatabase db = new SqliteIndexDatabase(new File("/home/jesus/projects/mde-ml/mar/.output/ecore-index.db"));
		SqlitePathRetriever retriever = new SqlitePathRetriever(db);
		SqliteIndexJob.initModels();
		
		AbstractPathComputation model2graphTrue = new Model2GraphAllpaths(4).withPathFactory(new PathFactory.EcoreTokenizer());
		model2graphTrue.withFilter(MetaFilter.getEcoreFilter());

		String root = System.getenv("REPO_ROOT") + "/download/repo-github-ecore";
		try(AnalysisDB analysis = new AnalysisDB(new File("/home/jesus/projects/mde-ml/mar/.output/repo-github-ecore/analysis.db"))) {
			try {
				List<Model> models = analysis.getValidModels(s -> root + "/" + s);
				for (Model model : models) {
					Resource r = ModelLoader.DEFAULT.load(new File(model.getFile().getAbsolutePath()));
					//retriever.retrieve(r, model2graphTrue);					
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		//String model = "/home/jesus/projects/mde-ml/mde-datasets/repo-atlanmod/data/Pascal_0_1.ecore";
		
		
	}
	
}
