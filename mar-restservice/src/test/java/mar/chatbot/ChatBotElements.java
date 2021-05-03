package mar.chatbot;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.google.gson.Gson;

import mar.chatbot.elements.EcoreElementId;
import mar.chatbot.elements.ElementId;
import mar.chatbot.elements.ElementsSet;
import mar.chatbot.elements.IElement;
import mar.chatbot.elements.SetType;
import mar.chatbot.elements.SingleElement;

public class ChatBotElements {

	@Test
	public void test() {
		Gson gson = new Gson();
		SingleElement se1 = new SingleElement("Professor", "name", "EClass");
		SingleElement se2 = new SingleElement("Student", "name", "EClass");
		EcoreElementId ied = new EcoreElementId("University");
		Set<IElement> set = new HashSet<IElement>();
		set.add(se1); set.add(se2);set.add(ied);
		ElementsSet eset = new ElementsSet(set, SetType.OR);
		
		
		String json = gson.toJson(eset);
		System.out.println(json);
	}

}
