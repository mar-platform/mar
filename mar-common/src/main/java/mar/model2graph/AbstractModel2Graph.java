package mar.model2graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.builder.GraphTypeBuilder;


import mar.paths.ListofPaths;
import mar.paths.Path;
import mar.paths.PathFactory;

public abstract class AbstractModel2Graph extends AbstractPathComputation {
	
	private PathFactory pathFactory = new PathFactory.DefaultPathFactory();
	
	@Nonnull
	public AbstractPathComputation withPathFactory(@Nonnull PathFactory factory) {
		this.pathFactory = factory;
		return this;
	}

	@Nonnull
	protected ListofPaths newPathSet(@Nonnull List<? extends Path> paths) {
		return pathFactory.newPathSet(paths);
	}
	
	@Nonnull
	public PathFactory getPathFactory() {
		return pathFactory;
	}
	
	protected static class Node {
		protected Object element;
		private boolean terminal;
		private int id = -1;

		public Node(Object obj, boolean terminal) {
			this.element = obj;
			this.terminal = terminal;
		}

		public boolean isTerminal() {
			return terminal;
		}

		@Override
		public String toString() {
			if (terminal)
				return element.toString();
			return ((EObject) element).eClass().getName();

		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((element == null) ? 0 : element.hashCode());
			result = prime * result + id;
			result = prime * result + (terminal ? 1231 : 1237);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Node other = (Node) obj;
			if (element == null) {
				if (other.element != null)
					return false;
			} else if (!element.equals(other.element))
				return false;
			if (id != other.id)
				return false;
			if (terminal != other.terminal)
				return false;
			return true;
		}

		public int getId() {
			return id;
		}

		public void setId(int id) {
			this.id = id;
		}
		
		public Object getElement() {
			return this.element;
		}
	}

	@SuppressWarnings("serial")
	protected static class Edge extends DefaultEdge {

		private String label;

		public Edge(String label) {
			this.label = label;
		}

		public String getLabel() {
			return this.label;
		}

		public void setLabel(String label) {
			this.label = label;
		}

		@Override
		public String toString() {
			return "(" + super.toString() + " : " + "LABEL:" + label + ")";
		}
	}
	
	public Graph<Node, Edge> createParallelGraph(Resource r) {

		Graph<Node, Edge> g = GraphTypeBuilder.<Node, Edge>directed().allowingMultipleEdges(true)
				.allowingSelfLoops(true).edgeClass(Edge.class).buildGraph();

		TreeIterator<EObject> it = r.getAllContents();
		int id = 0;

		IMetaFilter mf = getFilter();
		List<Node> visited = new ArrayList<Node>();

		while (it.hasNext()) {

			EObject obj = it.next();
			if (obj.eIsProxy()) {
				continue;
			}
		
			
			if (obj != null && mf.passFilterObject(obj)) {
				Node n1 = new Node(obj, false);
				g.addVertex(n1);
				visited.add(n1);
				
				for (EStructuralFeature f : obj.eClass().getEAllStructuralFeatures()) {
					if (f.isDerived())
						continue;
					// ignore the structural feature (attribute or reference)
					if (!mf.passFilerStructural(f))
						continue;
					// System.out.println(obj.eClass().getName());
					// label of the edge is the name of the EStructuralFeature
					if (f instanceof EAttribute && f.isMany()) {
						List<Object> attributes = (List<Object>) obj.eGet(f);
						for (Object object : attributes) {
							if (object != null) {
								Node n2 = new Node(object, true);
								n2.setId(id);
								id = id + 1;
								g.addVertex(n2);
								g.addEdge(n1, n2, new Edge(f.getName()));
								g.addEdge(n2, n1, new Edge(f.getName()));
							}
						}
						continue;
					}
					// take attributes
					if (f instanceof EAttribute && !f.isMany()) {
						Object attribute = obj.eGet(f);
						if (attribute != null) {
							// System.out.println(attribute);
							Node n2 = new Node(attribute, true);
							n2.setId(id);
							id = id + 1;
							g.addVertex(n2);
							g.addEdge(n1, n2, new Edge(f.getName()));
							g.addEdge(n2, n1, new Edge(f.getName()));
						}
						continue;
					}
					// take the references
					if (f instanceof EReference && f.isMany()) {
						Collection<EObject> elements = (Collection<EObject>) obj.eGet(f);
						for (EObject e : elements) {
							// ignore the class
							if (e != null && !mf.passFilterObject(e))
								continue;
							if (e != null && e.eIsProxy())
								continue;

							if (e != null) {
								Node n2 = new Node(e, false);
								g.addVertex(n2);
								g.addEdge(n1, n2, new Edge(f.getName()));
								// g.addEdge(n2, n1, new Edge(f.getName()));
								// System.out.println(f.getName());

							}
						}
						continue;
					}
					// take the reference
					if (f instanceof EReference && !f.isMany()) {
						EObject element = (EObject) obj.eGet(f);
						// ignore the class
						if (element != null &&!mf.passFilterObject(element))
							continue;
						if (element != null && element.eIsProxy())
							continue;
						if (element != null) {
							Node n2 = new Node(element, false);
							g.addVertex(n2);
							g.addEdge(n1, n2, new Edge(f.getName()));
							// g.addEdge(n2, n1, new Edge(f.getName()));

							continue;
						}
					}

				}
			}
		}
		
		Set<Node> nodesLeft = g.vertexSet().stream().filter(n -> !n.isTerminal() 
				&& !visited.contains(n)).collect(Collectors.toSet());
		
		
		//we should consider only elements "inside the model"
		for (Node n: nodesLeft) {
				EObject obj = (EObject) n.getElement();
				
				
				if (obj.eIsProxy()) {
					continue;
				}
				
				//System.out.println(obj);
				
				for (EStructuralFeature f : obj.eClass().getEAllStructuralFeatures()) {
					if (f.isDerived())
						continue;
					// ignore the structural feature (attribute or reference)
					if (!mf.passFilerStructural(f))
						continue;
					// System.out.println(obj.eClass().getName());
					// label of the edge is the name of the EStructuralFeature
					if (f instanceof EAttribute && f.isMany()) {
						List<Object> attributes = (List<Object>) obj.eGet(f);
						for (Object object : attributes) {
							if (object != null) {
								Node n2 = new Node(object, true);
								n2.setId(id);
								id = id + 1;
								g.addVertex(n2);
								g.addEdge(n, n2, new Edge(f.getName()));
								g.addEdge(n2, n, new Edge(f.getName()));
							}
						}
						continue;
					}
					// take attributes
					if (f instanceof EAttribute && !f.isMany()) {
						Object attribute = obj.eGet(f);
						if (attribute != null) {
							// System.out.println(attribute);
							Node n2 = new Node(attribute, true);
							n2.setId(id);
							id = id + 1;
							g.addVertex(n2);
							g.addEdge(n, n2, new Edge(f.getName()));
							g.addEdge(n2, n, new Edge(f.getName()));
						}
						continue;
					}
					// take the references
					if (f instanceof EReference && f.isMany()) {
						Collection<EObject> elements = (Collection<EObject>) obj.eGet(f);
						for (EObject e : elements) {
							// ignore the class
							if (e != null && !mf.passFilterObject(e))
								continue;
							if (e != null && e.eIsProxy())
								continue;

							if (e != null) {
								Node n2 = new Node(e, false);
								
								if (g.vertexSet().contains(n2))
									g.addEdge(n, n2, new Edge(f.getName()));
								
								//g.addVertex(n2);
								
								// g.addEdge(n2, n1, new Edge(f.getName()));
								// System.out.println(f.getName());

							}
						}
						continue;
					}
					// take the reference
					if (f instanceof EReference && !f.isMany()) {
						EObject element = (EObject) obj.eGet(f);
						// ignore the class
						if (element != null &&!mf.passFilterObject(element))
							continue;
						if (element != null && element.eIsProxy())
							continue;
						if (element != null) {
							Node n2 = new Node(element, false);
							
							if (g.vertexSet().contains(n2))
								g.addEdge(n, n2, new Edge(f.getName()));
							
							//g.addVertex(n2);
							
							// g.addEdge(n2, n1, new Edge(f.getName()));

							continue;
						}
					}
				}
		}
		return g;
	}
	
	protected boolean noAttributesNode(Node n, Graph<Node, Edge> g) {
		if (n.isTerminal())
			return false;

		Set<Edge> edges = g.edgesOf(n);

		for (Edge edge : edges) {
			Node n2 = g.getEdgeTarget(edge);

			if (n2.isTerminal())
				return false;

		}

		return true;
	}
}
