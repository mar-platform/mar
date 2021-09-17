package mar.renderers.uml;

import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Message;
import org.eclipse.uml2.uml.MessageOccurrenceSpecification;

import mar.renderers.PlantUmlText;


public class InteractionDiagramVisualizer {

	public static final InteractionDiagramVisualizer INSTANCE = new InteractionDiagramVisualizer();
	
	public PlantUmlText render(Interaction interaction) {
		PlantUmlText text = new PlantUmlText();		
		text.start();
	
		for(Message message : interaction.getMessages()) {
			MessageOccurrenceSpecification source = (MessageOccurrenceSpecification) message.getReceiveEvent();
			MessageOccurrenceSpecification target = (MessageOccurrenceSpecification) message.getSendEvent();
			if(source != null && target != null) {
				String sourceLifeline = (source.getCovered() == null) ? "Unnamed" : source.getCovered().getName();
				String targetLifeline = (target.getCovered() == null) ? "Unnamed" : target.getCovered().getName();
				text.line(sourceLifeline + "->" + targetLifeline + ":" + message.getName());
			}
		}
		
		text.end();
		return text;
	}

}
