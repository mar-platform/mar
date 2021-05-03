package mar.chatbot.elements;

import java.util.Set;
import java.util.TreeSet;

public class SingleElement implements IElement{
	
	private final String attribute;
	private final String value;
	private final String metaclass;
	
	public SingleElement(String attribute, String value, String metaclass) {
		this.attribute = attribute;
		this.value = value;
		this.metaclass = metaclass;
	}
	public String getAttribute() {
		return attribute;
	}

	public String getValue() {
		return value;
	}

	public String getMetaclass() {
		return metaclass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attribute == null) ? 0 : attribute.hashCode());
		result = prime * result + ((metaclass == null) ? 0 : metaclass.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		SingleElement other = (SingleElement) obj;
		if (attribute == null) {
			if (other.attribute != null)
				return false;
		} else if (!attribute.equals(other.attribute))
			return false;
		if (metaclass == null) {
			if (other.metaclass != null)
				return false;
		} else if (!metaclass.equals(other.metaclass))
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}


	public String getPrefix() {
		return "(" + value + "," + attribute + "," + metaclass;
	}

	@Override
	public ElementsSet toSet() {
		Set<IElement> set = new TreeSet<IElement>();
		set.add(this);
		return new ElementsSet(set, SetType.AND);
	}

}
