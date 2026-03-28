package mar.analysis.backend.megamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import edu.emory.mathcs.backport.java.util.Collections;
import mar.analysis.backend.megamodel.RawRepositoryDB.RawFile;
import mar.analysis.megamodel.model.Artefact;
import mar.analysis.megamodel.model.ComponentGraph;
import mar.analysis.megamodel.model.DuplicationGraph;
import mar.analysis.megamodel.model.DuplicationGraph.ArtefactGroup;
import mar.analysis.megamodel.model.DuplicationRelationships;
import mar.analysis.megamodel.model.InterProjectGraph;
import mar.analysis.megamodel.model.InterProjectGraph.ProjectGroup;
import mar.analysis.megamodel.model.Project;
import mar.analysis.megamodel.model.Relationship;
import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.ArtefactNode;
import mar.analysis.megamodel.model.RelationshipsGraph.Edge;
import mar.analysis.megamodel.model.RelationshipsGraph.Node;

public class TransformationRelationshipsAnalysis {

	private static Relationship[] MAIN_RELATIONSHIP_TYPES = { Relationship.TYPED_BY, Relationship.IMPORT, Relationship.GENERATE };
	
	private RawRepositoryDB raw;
	private MegamodelDB db;
	private Filter filter;
	private boolean cachesResults;

	private InterProjectGraph interprojectGraph;


	public TransformationRelationshipsAnalysis(@Nonnull MegamodelDB db, RawRepositoryDB raw, Filter filter) {
		this.db = db;		
		this.raw = raw;
		this.filter = filter;
	}
	

	public TransformationRelationshipsAnalysis withCache(boolean b) {
		this.cachesResults = b;
		return this;
	}
	
	@Nonnull
	public RelationshipsGraph getRelationships() {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		});
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType((src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);

		return graph;
	}

	@Nonnull
	public RelationshipsGraph getMegamodelGraph() {
		DuplicationGraph graph = new DuplicationGraph();
		
		Map<String, String> nodeToGroup = new HashMap<String, String>();
		
		DuplicationRelationships dup = db.getDuplicates();
		dup.forEachGroup((groupId, nodeIds) -> {
			if (filter.isAccepted(groupId)) {
				String type = dup.getTypeOfGroup(groupId).toLowerCase(); //Hack
				ArtefactGroup node = new DuplicationGraph.ArtefactGroup(groupId, "duplication", type);
				node.addArtefacts(nodeIds);
				graph.addNode(node);			
				
				nodeIds.forEach(id -> nodeToGroup.put(id, groupId));
			}
		});
		
		db.getAllArtefacts().forEach((key, artefact) -> {
			if (filter.isAccepted(key)) {
				graph.addArtefact(artefact);
				if (! nodeToGroup.containsKey(key)) {
					graph.addNode(new RelationshipsGraph.ArtefactNode(key, artefact));
				}
			}
		});
		
		db.getRelationshipsByType((src, tgt, type) -> {
			String srcGroup = dup.getGroupOf(src);
			String tgtGroup = dup.getGroupOf(tgt);
			
			String sourceId = srcGroup != null ? srcGroup : src;
			String targetId = tgtGroup != null ? tgtGroup : tgt;			
				
			if (filter.isAccepted(sourceId) && filter.isAccepted(targetId)) {
				graph.addEdge(sourceId, targetId, type);
			}
		}, MAIN_RELATIONSHIP_TYPES);
		
		return graph;
	}
	
	@Nonnull
	public RelationshipsGraph getDuplicationGraph() {
		DuplicationGraph graph = new DuplicationGraph();

		List<ArtefactGroup> groups = new ArrayList<>();
		DuplicationRelationships dup = db.getDuplicates();
		dup.forEachGroup((groupId, nodeIds) -> {
			String artefactType = dup.getTypeOfGroup(groupId);
			ArtefactGroup node = new DuplicationGraph.ArtefactGroup(groupId, "duplication", artefactType);
			node.addArtefacts(nodeIds);
			graph.addNode(node);			
			groups.add(node);
		});
		
		for (ArtefactGroup artefactGroup : groups) {
			for (String id : artefactGroup.getArtefacts()) {
				Artefact artefact = db.getArtefactById(id);
				Node node = new RelationshipsGraph.ArtefactNode(id, artefact);				
				graph.addNode(node);
				graph.addEdge(artefactGroup.getId(), node.getId(), Relationship.DUPLICATE);				
			}
		}
		
		return graph;
	}
	
	
	/**
	 * The duplication graph aggregates all nodes in the same duplication group into the same
	 * node, and the relationships are redirected.
	 */
	// I don't think this is very useful
	@Nonnull
	public RelationshipsGraph getDuplicationGraphOld() {
		DuplicationGraph graph = new DuplicationGraph();
		
		DuplicationRelationships dup = db.getDuplicates();
		dup.forEachGroup((groupId, nodeIds) -> {
			String artefactType = dup.getTypeOfGroup(groupId);
			ArtefactGroup node = new DuplicationGraph.ArtefactGroup(groupId, "duplication", artefactType);
			node.addArtefacts(nodeIds);
			graph.addNode(node);			
		});
		
		
		db.getRelationshipsByType((src, tgt, type) -> {
			// The edge needs to be redirected to a duplication group
			if (type != Relationship.DUPLICATE) {
				String srcGroup = dup.getGroupOf(src);
				String tgtGroup = dup.getGroupOf(tgt);
					
				if (srcGroup != null && tgtGroup != null) {
					ArtefactGroup group1 = (ArtefactGroup) graph.getNode(srcGroup);
					ArtefactGroup group2 = (ArtefactGroup) graph.getNode(tgtGroup);
					graph.addEdge(group1.getId(), group2.getId(), type);
				} else if (srcGroup != null) {
					ArtefactGroup group = (ArtefactGroup) graph.getNode(srcGroup);
					addNodeIfNeeded(graph, tgt);
					graph.addEdge(group.getId(), tgt, type);
				} else if (tgtGroup != null) {
					ArtefactGroup group = (ArtefactGroup) graph.getNode(tgtGroup);
					addNodeIfNeeded(graph, src);
					graph.addEdge(src, group.getId(), type);		
				} else {
					// I think that this should
					// graph.addEdge(src, tgt, type);
				}
			}
		}, plus(MAIN_RELATIONSHIP_TYPES, Relationship.DUPLICATE));
		
		return graph;
	}

	@Nonnull
	public RelationshipsGraph getInterProjectGraph_NoTimestamped() {
		if (cachesResults && interprojectGraph != null) {
			return interprojectGraph;
		}
		
		InterProjectGraph graph = computeInterprojectGraph(false);
		
		if (cachesResults) {
			this.interprojectGraph = graph;
		}
		
		/*
		System.out.println("Edges\n\n");
		for (Edge edge : graph.getEdges()) {
			System.out.println(edge.getSourceId() + " -> " + edge.getTargetId());
		}
		System.out.println("\n\n");
		*/
		
		return graph;
	}

	@Nonnull
	public RelationshipsGraph getInterProjectGraph() {
		if (cachesResults && interprojectGraph != null) {
			return interprojectGraph;
		}
		
		InterProjectGraph graph = computeInterprojectGraph(true);
		
		if (cachesResults) {
			this.interprojectGraph = graph;
		}
		
		/*
		System.out.println("Edges\n\n");
		for (Edge edge : graph.getEdges()) {
			System.out.println(edge.getSourceId() + " -> " + edge.getTargetId());
		}
		System.out.println("\n\n");
		*/
		
		return graph;
	}
	
	private InterProjectGraph computeInterprojectGraph(boolean useTimestamps) {
		RelationshipsGraph megamodel = getMegamodelGraph();
		InterProjectGraph graph = new InterProjectGraph();
		Map<String, RawFile> files = useTimestamps ? raw.getFiles() : Collections.emptyMap();
		
		for (Project project : db.allProjects()) {
			ProjectGroup prj = new InterProjectGraph.ProjectGroup(project.getId(), "project");
			graph.addNode(prj);							
		}
		
		for (Node node : megamodel.getNodes()) {
			Set<Edge> edges = megamodel.getGraph().outgoingEdgesOf(node);
			Set<String> targetProjects = new HashSet<String>();
			
			for (Edge edge : edges) {
				Node targetNode = megamodel.getNode(edge.getTargetId());
				if (targetNode instanceof ArtefactGroup g) {
					Collection<? extends String> originalArtefacts;
					if (useTimestamps) {
						originalArtefacts = pickRelevantByTimestamp(g.getArtefacts(), files);
					} else {
						originalArtefacts = g.getArtefacts();
					}
					
					for (String artefactId : originalArtefacts) {
						Artefact a = db.getArtefactById(artefactId);
						targetProjects.add(a.getProject().getId());
					}
				} else {
					Artefact a = ((ArtefactNode) targetNode).getArtefact();
					targetProjects.add(a.getProject().getId());
				}
			}
			
			if (node instanceof ArtefactGroup g) {
				Set<String> sourceProjects = new HashSet<String>();
				Collection<? extends String> originalArtefacts;
				if (useTimestamps) {
					originalArtefacts = pickRelevantByTimestamp(g.getArtefacts(), files);
				} else {
					originalArtefacts = g.getArtefacts();
				}
				
				for (String artefactId : originalArtefacts) {
					Artefact a = db.getArtefactById(artefactId);
					sourceProjects.add(a.getProject().getId());
				}
				
				for (String sourceProjectId : sourceProjects) {
					for (String targetProjectId : targetProjects) {
						if (! sourceProjectId.equals(targetProjectId)) {
							graph.addEdge(sourceProjectId, targetProjectId, Relationship.PROJECT_RELATED_TO);
						}						
					}					
				}
				
				/*
				for (String artefactId : g.getArtefacts()) {
					Artefact a = db.getArtefactById(artefactId);
					String sourceProjectId = a.getProject().getId();
					for (String targetProjectId : targetProjects) {
						if (! sourceProjectId.equals(targetProjectId)) {
							graph.addEdge(sourceProjectId, targetProjectId, Relationship.PROJECT_RELATED_TO);
						}						
					}					
				}
				*/
				
				// In addition, every artefact in the group that is not deemed as original, is a copy of the original
				/*
				for (String artefactId : g.getArtefacts()) {
					if (! originalArtefacts.contains(artefactId)) {
						Artefact copyOfOriginal = db.getArtefactById(artefactId);
						String projectOfCopy = copyOfOriginal.getProject().getId();
						for (String projectOfOriginal : sourceProjects) {
							graph.addEdge(projectOfCopy, projectOfOriginal, Relationship.COPY_FROM);
						}
					}
				}
				*/
				
			} else if (node instanceof ArtefactNode a) {
				String sourceProjectId = a.getArtefact().getProject().getId();
				for (String targetProjectId : targetProjects) {
					if (! sourceProjectId.equals(targetProjectId)) {
						graph.addEdge(sourceProjectId, targetProjectId, Relationship.PROJECT_RELATED_TO);
					}						
				}
			}
		}
		return graph;
	}
	
	// TODO: This could be done passing directly the artefact group and doing a direct sql query
	private Collection<? extends String> pickRelevantByTimestamp(List<? extends String> artefacts, Map<String, RawFile> allFiles) {
		List<RawFile> files = new ArrayList<RawRepositoryDB.RawFile>();
		for (String artefactId : artefacts) {
			RawFile info = allFiles.get(artefactId);
			if (info == null) {
				// This is likely because the artefact is heuristic
				System.out.println("Not found " + artefactId);
				continue;
			}
			
			if (info.getCreatedAt() != null) {
				files.add(info);
				Preconditions.checkArgument(info.getId().equals(artefactId));
			}
		}
		
		if (files.isEmpty()) {
			return new HashSet<>(artefacts);
		}
		
		files.sort((r1, r2) -> r1.getCreatedAt().compareTo(r2.getCreatedAt()));
		return java.util.Collections.singleton(files.getFirst().getId());
	}


	@Nonnull
	public RelationshipsGraph getInterProjectGraph_Old() {
		if (cachesResults && interprojectGraph != null) {
			return interprojectGraph;
		}
		
		InterProjectGraph graph = new InterProjectGraph();
		
		Multimap<String, Artefact> projectGroups = ArrayListMultimap.create();
		DuplicationRelationships dup = db.getDuplicates();
		
		db.getAllArtefacts().forEach((name, artefact) -> {
			String projectId = artefact.getProject().getId();
			projectGroups.put(projectId, artefact);
		});
		
		for (String projectId : projectGroups.keySet()) {
			ProjectGroup prj = new InterProjectGraph.ProjectGroup(projectId, "project");
			graph.addNode(prj);
		}

		for (String projectId : projectGroups.keySet()) {
			// This node contains all the artefact ids of the project
			Collection<Artefact> projectArtefacts = projectGroups.get(projectId);
			
			for (Artefact artefact : projectArtefacts) {
				String groupId = dup.getGroupOf(artefact.getId());
				if (groupId != null) {
					// This means the artefact is in a duplication group
					// and thus the project's artifact is linked to the 
					// every project of the artefact's of the group
					dup.forEachArtefact(groupId, (nodeId) -> {
						Artefact tgtArtefact = db.getArtefactById(nodeId);
						if (! artefact.getProject().getId().equals(tgtArtefact.getProject().getId())) {
							// TODO: Add more information to the edge, like why it exists: because X and Y artefacts are duplicated
							graph.addEdge(projectId, tgtArtefact.getProject().getId(), Relationship.PROJECT_RELATED_TO);
						}
					});
				}
			}
			
		}
		
		if (cachesResults) {
			this.interprojectGraph = graph;
		}
		
		return graph;
	}
	
	private void addNodeIfNeeded(DuplicationGraph graph, String id) {
		if (! graph.hasNode(id)) {
			Artefact artefact = this.db.getAllArtefacts().get(id);
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		}
	}

	private static Relationship[] plus(Relationship[] rels, Relationship... other) {
		Relationship[] newRels = new Relationship[rels.length + other.length];
		int i = 0;
		for (; i < rels.length; i++) {
			newRels[i] = rels[i];
		}
		for (int j = 0; j < other.length; j++) {
			newRels[i + j] = other[j];
		}
		
		return newRels;
	}

	/**
	 * This also includes DUPLICATE nodes. It is probably not very useful.
	 */
	@Nonnull
	public RelationshipsGraph getFullRelationships() {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);
		});
		
		System.out.println("Getting edges...");
		db.getRelationshipsByType((src, tgt, type) -> {
			graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);

		
		DuplicationRelationships dup = db.getDuplicates();		
		dup.forEachGroup((id, nodeIds) -> {
			Node node = new RelationshipsGraph.VirtualNode(id, "duplication");
			graph.addNode(node);
			nodeIds.forEach(nodeId -> graph.addEdge(id, nodeId, Relationship.DUPLICATE));
		});
		
		return graph;
	}
	
	public RelationshipsGraph getProjectRelationship(String projectId) {
		RelationshipsGraph graph = new RelationshipsGraph();
		this.db.getProjectArtefacts(projectId, (id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			graph.addNode(node);			
		});
		
		// This traverse all relationships, which is inneficient. Not sure if a getRelationshipsByTypeAndProject could be better
		db.getRelationshipsByType((src, tgt, type) -> {
			if (graph.hasNode(src) && graph.hasNode(tgt))
				graph.addEdge(src, tgt, type);
		}, MAIN_RELATIONSHIP_TYPES);
		
		return graph;
	}
	
	public ComponentGraph getComponentGraph() {
		RelationshipsGraph megamodelGraph = getMegamodelGraph();
		return new ComponentGraph(megamodelGraph);
	}
	
	@Nonnull
	public RelationshipsGraph getRelationshipsFromSQL(String query) {
		RelationshipsGraph graph = new RelationshipsGraph();
		System.out.println("Getting artefacts...");
	
		Map<String, Node> nodes = new HashMap<>();
		this.db.getAllArtefacts().forEach((id, artefact) -> {
			Node node = new RelationshipsGraph.ArtefactNode(id, artefact);
			nodes.put(id, node);
		});
		
		Set<Node> touched = new HashSet<>();
		db.getRelationshipsFromSQL(query, (src, tgt, type) -> {
			Node srcNode = nodes.get(src);
			Node tgtNode = nodes.get(tgt);
			if (srcNode != null && tgtNode != null) {			
				if (! touched.contains(srcNode))
					graph.addNode(srcNode);
				else 
					touched.add(srcNode);
					
				if (! touched.contains(tgtNode))
					graph.addNode(tgtNode);
				else
					touched.add(tgtNode);
	
				graph.addEdge(src, tgt, type);
			} else {
				System.out.println("Invalid edge: " + src + " - " + tgt);
			}
		});
		
		System.out.println(graph);
		
		return graph;
	}	

	public static interface Filter {
		boolean isAccepted(String id);		
	}
	
	public static Filter ALL_ACCEPTED = new Filter() {

		@Override
		public boolean isAccepted(String id) {
			return true;
		}
		
	};
}
