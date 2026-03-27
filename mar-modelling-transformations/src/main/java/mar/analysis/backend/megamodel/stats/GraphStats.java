package mar.analysis.backend.megamodel.stats;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.backend.megamodel.stats.ResultAnalyser.GraphLevelStats;
import mar.analysis.backend.megamodel.stats.ResultAnalyser.Isolated;
import mar.analysis.backend.megamodel.stats.ResultAnalyser.IsolatedFile;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.ComponentGraph;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.ComponentGraph.SingleComponentGraph;
import mar.analysis.megamodel.model.DuplicationGraph.ArtefactGroup;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class GraphStats {

	public GraphStats(MegamodelDB megamodelDb, RawRepositoryDB rawDb, Set<String> artefactTypes) {
		TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(megamodelDb, rawDb, TransformationRelationshipsAnalysis.ALL_ACCEPTED);
		graphStats(analysis, artefactTypes);
	}

	private void graphStats(TransformationRelationshipsAnalysis analysis, Set<String> artefactTypes) {
		RelationshipsGraph graph = analysis.getMegamodelGraph();

		long totalArtefactNodes = 0;
		
		long totalIsolatedArtefacts = 0;
		long totalOutDegree = 0;
		long totalInDegree = 0;
		
		Multimap<String, String> isolatedByType = MultimapBuilder.hashKeys().arrayListValues().build();		
		
		for (Node node : graph.getNodes()) {
			String type;
			String id = node.getId();
			if (node instanceof ArtefactNode) {
				type = ((ArtefactNode) node).getArtefact().getType();
			} else if (node instanceof ArtefactGroup) {
				type = ((ArtefactGroup) node).getArtefactType();
			} else {
				throw new IllegalStateException();
			}
			
			if (! artefactTypes.contains(type))
				continue;
				
			totalArtefactNodes++;
			
			Graph<Node, Edge> impl = graph.getGraph();
			int outDegree = impl.outDegreeOf(node);
			int inDegree = impl.inDegreeOf(node);
			totalOutDegree += outDegree;
			totalInDegree += inDegree;
			System.out.println(inDegree + " - " + outDegree);
			System.out.println(totalInDegree + " - " + totalOutDegree);
			
			if (inDegree == 0 && outDegree == 0) {
				totalIsolatedArtefacts++;
				isolatedByType.put(type, id); // ArtefactGroupartefact);
			}
		
		}
		
		GraphLevelStats graphStats = new GraphLevelStats();
			
		PrintStream out = System.out;
		
		out.println();
		out.println("Isolated nodes:");
		isolatedByType.asMap().forEach((type, artefacts) -> {
			out.println("- Type: " + type + "  " + artefacts.size() + " isolated artefacts");
			/*
			List<Artefact> sorted = new ArrayList<>(artefacts);
			Collections.sort(sorted, (a1, a2) -> a1.getId().compareTo(a2.getId()));
			sorted.forEach(a -> {
				graphStats.addIsolated(a);
				out.println("   " + a.getId());
			});
			*/
		});
				
		out.println();
		out.println("Graph-level stats:");
		out.println("  " + String.format("%-8s", "# Artefacts") + " " + String.format("%d", totalArtefactNodes));
		out.println("  " + String.format("%-8s", "# Edges") + " " + String.format("%d", graph.getEdges().size()));
		out.println("  " + String.format("%-8s", "# Isolated") + " " + String.format("%d", totalIsolatedArtefacts));
		out.println("  " + String.format("%-8s", "% Isolated") + " " + String.format("%.2f%s", 100.0 * totalIsolatedArtefacts / totalArtefactNodes, "%"));
		
		// In/Out for non-isolated nodes only (see substraction (totalArtefactNodes - totalIsolatedArtefacts))
		out.println("  " + String.format("%-8s", "Avg. out-degree") + " " + String.format("%.2f", 1.0 * totalOutDegree / (totalArtefactNodes - totalIsolatedArtefacts)));
		out.println("  " + String.format("%-8s", "Avg. in-degree") + " " + String.format("%.2f", 1.0 * totalInDegree / (totalArtefactNodes - totalIsolatedArtefacts)));

		ComponentGraph component = new ComponentGraph(graph);
		System.out.println("#connected components: " + component.getSubgraphs().size());
		for(int i = 0; i < 3; i++) {
			SingleComponentGraph subgraph = component.getSubgraphs().get(i);
			System.out.println(" - " + subgraph.getNodes().size());
		}
	}

}
