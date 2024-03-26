package mar.artefacts;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;

import org.junit.Test;

import mar.analysis.duplicates.DuplicateFinder.DuplicationGroup;
import mar.analysis.duplicates.EcoreDuplicateFinder;
import mar.indexer.common.configuration.ModelLoader;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser.Factory;

public class EcoreDuplicateFinderTest {

	@Test
	public void testFinder() throws IOException {
		String m = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_DmvCgAwjEeWATac42w0kFQ.ecore";
		EcoreDuplicateFinder<String> finder = new EcoreDuplicateFinder<>();

		String m2 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_Q_PZgLWgEem8I_zdXKdSGw.ecore";
		String m3 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_4DA1oPokEeSE6sazzutmkA.ecore";

		String m4 = "/home/jesus/projects/mde-ml/model-mate/experiments/data/mar/repo-genmymodel-ecore/data/_u_vNsOQjEeiyGtLb2crGgA.ecore";

		
		
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();
		ILoader loader = factory.newLoader();
		
		
		finder.addResource("m1", loader.toEMF(new File(m)));
		finder.addResource("m2", loader.toEMF(new File(m2)));
		finder.addResource("m3", loader.toEMF(new File(m3)));
		finder.addResource("m4", loader.toEMF(new File(m4)));

		
		Collection<DuplicationGroup<String>> result = finder.getDuplicates(0.7, 0.8);
		System.out.println("Groups: " + result.size());
		for (DuplicationGroup<String> duplicationGroup : result) {
			System.out.println(duplicationGroup);
		}

	//	db = new DuplicationDatabase();
		
	
	}
}
