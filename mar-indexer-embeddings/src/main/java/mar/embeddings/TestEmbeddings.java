package mar.embeddings;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.embeddings.JVectorDatabase.QueryResult;
import mar.indexer.embeddings.EmbeddingStrategy;
import mar.indexer.embeddings.WordExtractor;
import mar.modelling.loader.ILoader;
import mar.validation.AnalyserRegistry;
import mar.validation.ResourceAnalyser.Factory;

public class TestEmbeddings {

	public static void main(String[] args) throws IOException {
		
		JVectorDatabase db = new JVectorDatabase(
				new File("/home/jesus/projects/mde-ml/mar/.output/jvector/ecore.jvector"),
				new File("/home/jesus/projects/mde-ml/mar/.output/jvector/ecore.info"));
		
		//String modelFileName = "/home/jesus/usr/mde-ml/workspace/zzzzTest/My.ecore";
		
		Factory factory = AnalyserRegistry.INSTANCE.getFactory("ecore");
		factory.configureEnvironment();
		
		/*
		String text = "package statemachine;\n"
				+ "\n"
				+ "class State {\n"
				+ "\n"
				+ "}\n"
				+ "\n"
				+ "class CompositeState {\n"
				+ "}\n"
				;
		text = "package sm;";
		EmfaticReader reader = new EmfaticReader();
		Resource r = reader.read(text);
		*/
		
		//String modelFileName = "/home/jesus/usr/mde-ml/workspace/zzzzTest/StateMachine.ecore";
		String modelFileName = "/home/jesus/usr/mde-ml/workspace/zzzzTest/FSM.ecore";
		
		ILoader loader = factory.newLoader();
		Resource r = loader.toEMF(new File(modelFileName));
		
		
		File f = new File("/home/jesus/projects/mde-ml/word2vec-mde/vectors/glove_modelling/vectors.txt");
		
		EMFQuery emfQuery = new EMFQuery.Generic(r, new EmbeddingStrategy.GloveWordE(f), WordExtractor.NAME_EXTRACTOR);
		
		List<QueryResult> results = db.search(emfQuery, 50);
		Collections.sort(results);
		for (QueryResult m : results) {
			System.out.println(m.score + " - " + m.modelId);
		}
		
		// Semantic search emf compare
		// https://ceur-ws.org/Vol-1706/paper6.pdf
		// -> for experiments https://dl.acm.org/doi/abs/10.1145/3417990.3422009
		
		// Automatic model transformation matching
		
		// PARMOREL: a framework for customizable model repair
		// ?????
		
		// Buscar algún ejemplo en el tema de los quick fixes, en plan, sugerencias
	}
	
	
}
