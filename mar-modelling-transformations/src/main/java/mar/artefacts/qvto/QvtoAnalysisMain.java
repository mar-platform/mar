package mar.artefacts.qvto;

import java.io.File;

import mar.analysis.ecore.EcoreRepository;
import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.analysis.uml.UMLAnalyser;
import mar.ingestion.CrawlerDB;
import mar.ingestion.IngestedModel;
import mar.validation.AnalysisDB;

public class QvtoAnalysisMain {

	public QvtoAnalysisMain() {
		
		
		AnalysisDB analysisDb = new AnalysisDB(new File("/home/jesus/projects/mde-ml/mar/.output/repo-github-ecore/analysis.db"));
		EcoreRepository repo  = new EcoreRepository(analysisDb, new File("/home/jesus/projects/mde-ml/mde-datasets/download/repo-github-ecore"));
		QvtoProcessor processor = new QvtoProcessor(repo);
				
		
		String root = "/home/jesus/projects/mde-ml/mde-datasets/download/repo-github-qvto";
		String file = root + File.separator + "data/elite-se/mdd-puml-to-uml/qvto/transformation/PlantUMLToUMLTransformation.qvto";

		CrawlerDB crawler = new CrawlerDB("qvto", "github", root, new File(root + File.separator + "crawler.db"));
		for (IngestedModel m : crawler.getModels()) {
			processor.load(m.getAbsolutePath());			
		}
		
		
		
	}
	
	public static void main(String[] args) {
		new SingleEcoreFileAnalyser.Factory().configureEnvironment();
		new UMLAnalyser.Factory().configureEnvironment();

		new QvtoAnalysisMain();
	}
	
}
