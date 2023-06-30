package mar.analysis.megamodel.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Represents all the information about which artefacts are
 * duplicated and which ones form a group.
 * 
 * @author jesus
 *
 */
public class DuplicationRelationships {

	// GroupId -> List[NodeId]
	private final Multimap<String, String> groups = ArrayListMultimap.create();		
	private final Map<String, String> nodeToGroup = new HashMap<String, String>();
	
	public void addToGroup(String groupId, String nodeId) {
		groups.put(groupId, nodeId);
		nodeToGroup.put(nodeId, groupId);
	}

	public void forEachGroup(BiConsumer<String, Collection<? extends String>> consumer) {
		for (String key : groups.keySet()) {
			consumer.accept(key, groups.get(key));
		}
	}

	public void forEachArtefact(String groupId, Consumer<String> consumer) {
		Collection<String> nodes = groups.get(groupId);
		Preconditions.checkState(nodes != null && ! nodes.isEmpty());
		nodes.forEach(consumer);
	}

	public String getGroupOf(String nodeId) {
		return nodeToGroup.get(nodeId);
	}
	
}