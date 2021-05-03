package mar.chatbot.elements;

import java.util.Set;

public class ElementsSet implements IElement {

	private final Set<IElement> set;
	private final SetType type;

	public ElementsSet(Set<IElement> set, SetType type) {
		super();
		this.set = set;
		this.type = type;
	}

	@Override
	public ElementsSet toSet() {
		return this;
	}
	
	public Set<IElement> getSet() {
		return set;
	}

	public SetType getType() {
		return type;
	}

	public boolean isEmpty() {
		return set.isEmpty();
	}
	
	public void addElement(IElement e) {
		set.add(e);
	}
	
}
