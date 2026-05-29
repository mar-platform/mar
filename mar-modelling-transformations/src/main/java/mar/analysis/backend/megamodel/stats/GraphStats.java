package mar.analysis.backend.megamodel.stats;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.backend.megamodel.stats.ResultAnalyser.GraphLevelStats;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.ComponentGraph;
import mar.analysis.megamodel.model.ComponentGraph.SingleComponentGraph;
import mar.analysis.megamodel.model.DuplicationGraph.ArtefactGroup;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class GraphStats {

	private long totalArtefacts;
	private long totalEdges;
	private long totalIsolated;
	private double avgOutDegree;
	private double avgInDegree;
	private List<Integer> componentSizes = new ArrayList<>();

	public GraphStats(MegamodelDB megamodelDb, RawRepositoryDB rawDb, Set<String> artefactTypes) {
		TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(megamodelDb, rawDb, TransformationRelationshipsAnalysis.ALL_ACCEPTED);
		graphStats(analysis, artefactTypes);
	}

	public long getTotalArtefacts() { return totalArtefacts; }
	public long getTotalEdges() { return totalEdges; }
	public long getTotalIsolated() { return totalIsolated; }
	public double getPercentIsolated() { return totalArtefacts == 0 ? 0 : 100.0 * totalIsolated / totalArtefacts; }
	public double getAvgOutDegree() { return avgOutDegree; }
	public double getAvgInDegree() { return avgInDegree; }
	public List<Integer> getComponentSizes() { return componentSizes; }

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

			if (inDegree == 0 && outDegree == 0) {
				totalIsolatedArtefacts++;
				isolatedByType.put(type, id);
			}

		}

		PrintStream out = System.out;

		out.println();
		out.println("Isolated nodes:");
		isolatedByType.asMap().forEach((type, artefacts) -> {
			out.println("- Type: " + type + "  " + artefacts.size() + " isolated artefacts");
		});

		long nonIsolated = totalArtefactNodes - totalIsolatedArtefacts;
		double computedAvgOut = nonIsolated == 0 ? 0 : 1.0 * totalOutDegree / nonIsolated;
		double computedAvgIn  = nonIsolated == 0 ? 0 : 1.0 * totalInDegree  / nonIsolated;

		out.println();
		out.println("Graph-level stats:");
		out.println("  " + String.format("%-8s", "# Artefacts") + " " + String.format("%d", totalArtefactNodes));
		out.println("  " + String.format("%-8s", "# Edges") + " " + String.format("%d", graph.getEdges().size()));
		out.println("  " + String.format("%-8s", "# Isolated") + " " + String.format("%d", totalIsolatedArtefacts));
		out.println("  " + String.format("%-8s", "% Isolated") + " " + String.format("%.2f%s", 100.0 * totalIsolatedArtefacts / totalArtefactNodes, "%"));
		out.println("  " + String.format("%-8s", "Avg. out-degree") + " " + String.format("%.2f", computedAvgOut));
		out.println("  " + String.format("%-8s", "Avg. in-degree") + " " + String.format("%.2f", computedAvgIn));

		ComponentGraph component = new ComponentGraph(graph);
		List<SingleComponentGraph> groups = component.getSubgraphsThatAreGroups();
		System.out.println("#connected components: " + groups.size());
		for(int i = 0; i < Math.min(3, groups.size()); i++) {
			SingleComponentGraph subgraph = groups.get(i);
			System.out.println(" - " + subgraph.getNodes().size());
		}

		// Store results
		this.totalArtefacts = totalArtefactNodes;
		this.totalEdges = graph.getEdges().size();
		this.totalIsolated = totalIsolatedArtefacts;
		this.avgOutDegree = computedAvgOut;
		this.avgInDegree = computedAvgIn;
		for (SingleComponentGraph subgraph : groups) {
			this.componentSizes.add(subgraph.getNodes().size());
		}
	}

}
