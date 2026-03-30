package mar.analysis.backend.megamodel.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;
import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.ComponentGraph;
import mar.analysis.megamodel.model.ComponentGraph.SingleComponentGraph;
import mar.analysis.megamodel.model.Project;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class ProjectStats {
	
	private Map<String, Double> projectOcurrences = new HashMap<String, Double>();
	private int totalProjects;
	private int totalIsolated;
	
	public ProjectStats(MegamodelDB megamodel, RawRepositoryDB raw) {
		projectOcurrences(megamodel);
		generalStats(megamodel, raw);
	}
	
	private void generalStats(MegamodelDB megamodel, RawRepositoryDB rawDb) {
		TransformationRelationshipsAnalysis analysis = new TransformationRelationshipsAnalysis(megamodel, rawDb, TransformationRelationshipsAnalysis.ALL_ACCEPTED);
		
		this.totalProjects = megamodel.allProjects().size();
		this.totalIsolated = 0;
		int totalInDegree = 0;
		int totalOutDegree = 0;
		
		RelationshipsGraph interProject = analysis.getInterProjectGraph();
		System.out.println("Inteproject nodes: " + interProject.getNodes().size());
		for (Node node : interProject.getNodes()) {
			int inDegree = interProject.getGraph().inDegreeOf(node);
			int outDegree = interProject.getGraph().outDegreeOf(node);
			//if (inDegree > 1 || outDegree > 1) {
			//	System.out.println("Node " + node.getId() + " " + inDegree + "  " + outDegree);
			//}
			totalInDegree += inDegree; 
			totalOutDegree += outDegree; 
			if (inDegree == 0 && outDegree == 0) {
				totalIsolated++;
			}
		}
		
		Preconditions.checkState(totalInDegree == totalOutDegree);
		double avgDegree = 1.0* totalInDegree / (totalProjects - totalIsolated);
		
		System.out.println();
		System.out.println("Project general stats");
		System.out.println("Total projects: " + totalProjects);
		System.out.println("Total edges: " + interProject.getEdges().size());
		System.out.println("Total isolated: " + totalIsolated + " " + String.format("%.2f", 100.0*totalIsolated/totalProjects));
		System.out.println("Avg. degree " + String.format("%.2f", avgDegree));
	
		ComponentGraph component = new ComponentGraph(interProject);
		System.out.println("#connected components: " + component.getSubgraphsThatAreGroups().size());
		for(int i = 0; i < 3; i++) {
			SingleComponentGraph subgraph = component.getSubgraphsThatAreGroups().get(i);
			System.out.println(" - " + subgraph.getNodes().size());
		}
		
	}

	private void projectOcurrences(MegamodelDB megamodelDb) {
		System.out.println("\nProject info:");
		Map<String, Integer> totalCount = new HashMap<String, Integer>();
		Set<String> types = new TreeSet<String>();
		
		/*
		List<Project> allProjects = megamodelDb.allProjects();
		for (Project project : allProjects) {
			Set<String> ocurredInProject = new HashSet<String>();
			megamodelDb.getProjectArtefacts(project.getId(), (id, artefact) -> {
				if (! ocurredInProject.contains(artefact.getType())) {
					ocurredInProject.add(artefact.getType());
					totalCount.putIfAbsent(artefact.getType(), 0);
					totalCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);
					types.add(artefact.getType());
				}
			});
		}
		*/

		Map<String, Set<String>> ocurredInProjectByProjectId = new HashMap<String, Set<String>>();
		for (Artefact artefact : megamodelDb.getAllArtefacts().values()) {
			String projectId = artefact.getProject().getId();
			Set<String> ocurredInProject = ocurredInProjectByProjectId.computeIfAbsent(projectId, (k) -> new HashSet<>());
			
			if (! ocurredInProject.contains(artefact.getType())) {
				ocurredInProject.add(artefact.getType());
				totalCount.putIfAbsent(artefact.getType(), 0);
				totalCount.compute(artefact.getType(), (k, v) -> (v == null ? 0 : v) + 1);
				types.add(artefact.getType());
			}
		}
		
		int totalProjects = ocurredInProjectByProjectId.size();
		for (String type : types) {
			double projectOcurrence = totalCount.get(type) * 1.0;
			double ocurrencePercentage = 100.0 * (projectOcurrence / totalProjects);
			System.out.println("  " + String.format("%-8s", type) + " " + String.format("%.2f", ocurrencePercentage) + " - " + projectOcurrence + " / " + totalProjects);
		
			this.projectOcurrences.put(type, ocurrencePercentage);
		}		
	}

	public double getProjectPercentage(String type) {
		return projectOcurrences.get(type);
	}
}
