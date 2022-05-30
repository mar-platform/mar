package mar.restservice.evaluate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.indexer.common.cmd.CmdOptions;
import mar.indexer.common.configuration.IndexJobConfigurationData;
import mar.indexer.common.configuration.InvalidJobSpecification;
import mar.indexer.common.configuration.ModelLoader;
import mar.indexer.common.configuration.SingleIndexJob;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.paths.PathFactory;
import mar.restservice.HBaseScorerFinal;
import mar.restservice.Profiler;
import mar.restservice.scoring.SqliteScorer;
import mar.sqlite.SqliteIndexDatabase;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "sqlite-hbase-evaluation", mixinStandardHelpOptions = true, description = "Evaluate SQLite and HBase")
public class Evaluate implements Callable<Integer> {
	
	@Parameters(index = "0", description = "The configuration file.")
	private File configurationFile;
	@Parameters(index = "1", description = "The SQLite db file.")
	private File indexDb;
	
	public static void evaluate(List<Model> models, SqliteIndexDatabase db) throws IOException {
		AbstractPathComputation path = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.DefaultPathFactory());
		
		Profiler profilerHBase = new Profiler();
		Profiler profilerSqlite = new Profiler();
		List<Model> differences = new ArrayList<>();
		
		try(HBaseScorerFinal scorer = new HBaseScorerFinal(path, "ecore")) {			
			SqliteScorer sqliteScorer = new SqliteScorer(path, db);

			try {
				for (Model model : models) {
					//if (! (model.getId().equals("github:ecore:/data/andymcr/webgen-pim/work.andycarpenter.webgen.pims.security/model/security.ecore") ||
					//		model.getId().equals("github:ecore:/data/gssi/metamodel-dataset-analysis-toolchain/metamodels/apidesc.ecore")))
					//	continue;
					
					Resource r = ModelLoader.DEFAULT.load(new File(model.getFile().getAbsolutePath()));
					
					System.out.println("Evaluating: " + model.getId());
					
					profilerHBase.start();
					Map<String, Double> result1 = scorer.score(r);
					profilerHBase.stop("HBase: " + model.getId(), System.out);
					
					profilerSqlite.start();
					Map<String, Double> result2 = sqliteScorer.score(r);
					profilerSqlite.stop("SQLite: " + model.getId(), System.out);
					
					if (! result1.equals(result2)) {
						System.out.println("Different results for " + model.getFile().getAbsolutePath());
						differences.add(model);
					}
					
					r.unload();
				}
				
				Double hbaseTime = profilerHBase.getTimes().values().stream().collect(Collectors.averagingDouble(v -> v));
				Double sqliteTime = profilerSqlite.getTimes().values().stream().collect(Collectors.averagingDouble(v -> v));
				System.out.println();
				System.out.println("HBase time: " + hbaseTime);
				System.out.println("SQLite time: " + sqliteTime);
				System.out.println("Differences: " + Strings.join(differences, '\n'));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			
		}
	}
	
	@Override
	public Integer call() throws Exception {	
	    IndexJobConfigurationData data = CmdOptions.readConfiguration(configurationFile);
		
		SqliteIndexDatabase db = new SqliteIndexDatabase(indexDb);
		initModels();
		
		//AbstractPathComputation model2graphTrue = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.EcoreTokenizer());
		//model2graphTrue.withFilter(MetaFilter.getEcoreFilterNames());
		
		SingleIndexJob job = data.getRepo("repo-ecore-all");
		
		
		String root = job.getRootFolder();
		try(AnalysisDB analysis = new AnalysisDB(job.getModelDbFile())) {
			List<Model> models = analysis.getValidModels(s -> root + "/" + s);
			
			Random random = new Random(123);
			List<Model> selected = new ArrayList<>();
			for(int i = 0; i < 1000; i++) {
				int idx = random.nextInt(models.size());
				selected.add(models.get(idx));
			}
			
			evaluate(selected, db);
		}		
		
		return 0;
	}
	
	public static void main(String[] args) throws IOException, InvalidJobSpecification {
		int exitCode = new CommandLine(new Evaluate()).execute(args);
		System.exit(exitCode);
	}
	
	
	/* pp */ static void initModels() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("ecore", new EcoreResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("xmi", new XMIResourceFactoryImpl());
    	Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("uml", new UMLResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);	
	}
}
