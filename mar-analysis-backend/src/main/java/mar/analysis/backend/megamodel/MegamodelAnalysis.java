package mar.analysis.backend.megamodel;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Function;

import javax.annotation.Nonnull;

import mar.analysis.backend.megamodel.XtextAnalysisDB.XtextModel;
import mar.analysis.ecore.EcoreRepository;
import mar.analysis.ecore.EcoreRepository.EcoreDerivedModel;
import mar.analysis.ecore.EcoreRepository.EcoreModel;
import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.uml.UMLAnalyser;
import mar.artefacts.Transformation;
import mar.artefacts.qvto.QvtoProcessor;
import mar.ingestion.CrawlerDB;
import mar.ingestion.IngestedModel;
import mar.validation.AnalyserMain;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "analyser", mixinStandardHelpOptions = true, description = "Generates a mega-model from different sources")
public class MegamodelAnalysis implements Callable<Integer> {

	@Parameters(index = "0", description = "Repository folder.")
	private File rootFolder;
	@Parameters(index = "1", description = "Output file.")
	private File output;
	
	@Override
	public Integer call() throws Exception {
		new SingleEcoreFileAnalyser.Factory().configureEnvironment();
		new UMLAnalyser.Factory().configureEnvironment();
		
		File repositoryDataFolder = Paths.get(rootFolder.getAbsolutePath(), "repos").toFile();
		File ecoreAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "ecore" , "analysis.db").toFile();
		File xtextAnalysisDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "xtext" , "analysis.db").toFile();
		
		File qvtCrawlerDbFile  = Paths.get(rootFolder.getAbsolutePath(), "analysis", "qvto" , "crawler.db").toFile();
		
		// AnalysisDB analysisDb = new AnalysisDB(new File("/home/jesus/projects/mde-ml/mar/.output/repo-github-ecore/analysis.db"));
		AnalysisDB analysisDb = new AnalysisDB(ecoreAnalysisDbFile);
		XtextAnalysisDB xtextAnalysisDb = new XtextAnalysisDB(xtextAnalysisDbFile);
		
		EcoreRepository repo  = new EcoreRepository(analysisDb, repositoryDataFolder);
		QvtoProcessor processor = new QvtoProcessor(repo);
				
		MegamodelDB megamodelDB = new MegamodelDB(output);
		megamodelDB.setAutocommit(false);
		
		String root = "/home/jesus/projects/mde-ml/mde-datasets/download/repo-github-qvto";

		for (Model model : repo.getModels()) {
			megamodelDB.addArtefact(model.getId(), "metamodel", model.getFile().getName());
		}		
		
		Function<String, String> xtextFileMapper = (relative) -> repositoryDataFolder.getAbsolutePath() + File.separator + relative;
		for (Model m : xtextAnalysisDb.getValidModels(xtextFileMapper)) {
			XtextModel xtext = (XtextModel) m;
			megamodelDB.addArtefact(xtext.getId(), "grammar", xtext.getFile().getName());
			
			if (xtext.getGeneratedMetamodels().isEmpty()) {
				XtextEcoreDerivedModel derived = new XtextEcoreDerivedModel(xtext.getId(), xtext.getFile().getName());
				xtext.getGeneratedMetamodels().forEach(uri -> {
					repo.addDerived(uri, derived);
					
					// TODO: Add atributes to the node, like "non-materialized"
					String generatedMetamodelId = xtext.getId() + "#" + uri;
					megamodelDB.addArtefact(generatedMetamodelId, "metamodel", xtext.getFile().getName() + "#generated-metamodel");
					
					megamodelDB.addRelationship(xtext.getId(), generatedMetamodelId, Relationship.TYPED_BY);
				});				
			}
			
			// This may need to go into another loop 
			xtext.getImportedMetamodels().forEach(uri -> {
				List<EcoreModel> pointedModels = repo.findEcoreByURI(uri);
				if (! pointedModels.isEmpty()) {
					// TODO: Get the proper model somehow
					EcoreModel pm = pointedModels.get(0);
					megamodelDB.addRelationship(xtext.getId(), pm.getId(), Relationship.TYPED_BY);
				}
			});			
		}
		
		CrawlerDB crawler = new CrawlerDB("qvto", "github", root, qvtCrawlerDbFile);
		for (IngestedModel m : crawler.getModels()) {
			Transformation transformation = processor.load(m.getAbsolutePath());
			// /home/jesus/projects/mde-ml/mde-datasets/download/repo-github-qvto/data/PhilippGruber/ScalingPolicyProfile/org.palladiosimulator.parallelcatalogue.catalog/completions/ScalingPolicy.qvto
			if (! transformation.hasErrors()) {
				System.out.println("Adding " + m.getAbsolutePath());
				String id = m.getModelId();
								
				megamodelDB.addArtefact(id, "transformation", m.getRelativeFile().getName());
				
				for (EcoreModel mm : transformation.getMetamodels()) {
					megamodelDB.addArtefact(mm.getId(), "metamodel", mm.getName());
					megamodelDB.addRelationship(id, mm.getId(), Relationship.TYPED_BY);
					System.out.println("  - " + id + " => " + mm.getId());
				}
			}			
		}
		
		xtextAnalysisDb.close();
		megamodelDB.close();
		
		return 0;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new AnalyserMain()).execute(args);
		System.exit(exitCode);
	}
	
	public static class XtextEcoreDerivedModel implements EcoreDerivedModel {

		private String id;
		private String name;

		public XtextEcoreDerivedModel(@Nonnull String id, @Nonnull String name) {
			this.id = id;
			this.name = name;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getName() {
			return name;
		}
		
	}
}
