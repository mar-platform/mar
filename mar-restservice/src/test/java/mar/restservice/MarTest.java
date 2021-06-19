package mar.restservice;

import java.util.HashMap;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mar.MarConfiguration;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.Model2GraphAllpaths;
import mar.model2graph.PathComputation;
import mar.paths.PathFactory;

public abstract class MarTest {

	static {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	}

	public static MarConfiguration getHbaseConfiguration(@Nonnull String model) {
		HashMap<String, PathComputation> pathsComputations = new HashMap<>();

		AbstractPathComputation model2graph = new Model2GraphAllpaths(4)
				.withPathFactory(new PathFactory.EcoreTokenizer());
		pathsComputations.put("ecore", model2graph);

		PathComputation pc = pathsComputations.get(model);

		if (pc == null)
			pc = new Model2GraphAllpaths(3).withPathFactory(new PathFactory.DefaultPathFactory());

		HBaseScorerFinal hsf = new HBaseScorerFinal(pc, model);
		return new MarConfiguration(model2graph, hsf);
	}

}
