package mar.renderers.uml;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.OpaqueExpression;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.ValueSpecification;
import org.eclipse.uml2.uml.Vertex;

import mar.renderers.PlantUmlText;

/**
 * Docs: https://plantuml.com/en/state-diagram
 * 
 */
public class StateMachineVisualizer {
	
	public static final StateMachineVisualizer INSTANCE = new StateMachineVisualizer();
	
	public PlantUmlText render(StateMachine sm) {	
		PlantUmlText text = new PlantUmlText();
		List<Region> regions = sm.getRegions();
		
		text.start();
		
		String sep = "";
		for (Region region : regions) {
			text.line(sep);
			mapRegion(region, text);
			sep = "---";
		}
		
		text.end();
		
		return text;
	}

	// repo-uml-pruned/sm/lituss/LobbyServer/LobbyServer/master/sim.uml
	
	private void mapRegion(Region r, PlantUmlText text) {
		for (Transition transition : r.getTransitions()) {
			if (transition.getSource() == null || transition.getTarget() == null) {
				System.out.println("Malformed model, transition without source or target: " + transition);
				continue;
			}
			
			String src = toStateId(transition.getSource());
			String tgt = toStateId(transition.getTarget());
			
			 // : EvNewValue
			
			String name = transition.getName();
			String label = name == null ? "" : name + " ";
			List<Constraint> rules = transition.getOwnedRules();
			if (rules.size() > 0) {
				// Pick one just to show something
				// Maybe the rest could go in a comment?
				Constraint c = rules.get(0);
				ValueSpecification spec = c.getSpecification();
				if (spec instanceof OpaqueExpression) {
					OpaqueExpression e = (OpaqueExpression) spec;
					String value = String.join("&&", e.getBodies());
					label += value;
				}
			}
			
			String str = src + " --> " + tgt;
			if (label != null && !label.isEmpty()) {
				str = str + " : \"" + label + "\"";
			}
			text.line(str);
		}
		
		for (Vertex v : r.getSubvertices()) {
			if (v instanceof State && !(v instanceof FinalState)) {
				List<Region> subRegions = ((State) v).getRegions();
				String sep = "";
				
				text.line("state " + toStateId(v) + " {");
				for (Region region : subRegions) {
					text.line(sep);
					mapRegion(region, text);
					sep = "---";
				}
				text.line("}");
			} else if (v instanceof Pseudostate) {
				// 				state join_state <<join>>
				switch(((Pseudostate) v).getKind()) {
				case FORK_LITERAL:
					text.line("state " + toStateId(v) + " <<fork>>");
				case JOIN_LITERAL:
					text.line("state " + toStateId(v) + " <<join>>");
				case JUNCTION_LITERAL:
					text.line("state " + toStateId(v)); // TOOD: Change color
				
				}
				// Ignore rest?
			}
		}
	}
	
	private Map<Vertex, String> names = new HashMap<>();
	
	private String toStateId(Vertex vertex) {
		if (names.containsKey(vertex))
			return names.get(vertex);
		
		String name = toStateId_(vertex, names.size());
		names.put(vertex, name);
		return name;
	}

	private String toStateId_(Vertex vertex, int idx) {		
		if (vertex instanceof FinalState) {
			return "[*]";
		} else if (vertex instanceof State) {
			State state = (State) vertex;
			return normalizeName(state.getName());
		} else if (vertex instanceof Pseudostate) {
			Pseudostate state = (Pseudostate) vertex;

			switch(state.getKind()) {
			case INITIAL_LITERAL:
				return "[*]";
			case CHOICE_LITERAL:
			case DEEP_HISTORY_LITERAL:
			case SHALLOW_HISTORY_LITERAL:
				return toSynthesizedName(state.getName(), "H", idx);
			case ENTRY_POINT_LITERAL:
			case EXIT_POINT_LITERAL:
			case FORK_LITERAL:
				if (vertex.getName() == null)
					return "fork_" + idx;
				return vertex.getName() + "_" + idx;
			case JOIN_LITERAL:
				if (vertex.getName() == null)
					return "join_" + idx;
				return vertex.getName() + "_" + idx;
			case JUNCTION_LITERAL:
				return "junction_" + idx;
			case TERMINATE_LITERAL:
				// TODO: Mark this specially, with a comment?
				return "[*]";
			default:
				break;
			
			}
		}
		throw new UnsupportedOperationException("Vertex not supported: " + vertex);
	}

	private String normalizeName(String name) {
		//if (!name.matches("^[A-Za-z0-9_]+$")) {
		//	return "\"" + name + "\"";			
		//}
		return name.replaceAll("[^A-Za-z0-9_]", "_");
	}
	
	private String toSynthesizedName(String name, String hint, int idx) {
		name = normalizeName(name);
		return name == null ? (hint + "_" + idx) : (name + "_" + hint);
	}
	
}
