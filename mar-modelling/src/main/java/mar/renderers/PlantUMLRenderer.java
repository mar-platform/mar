package mar.renderers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

public abstract class PlantUMLRenderer {

	/*
	public void renderTo(@Nonnull Resource resource, @Nonnull File file) throws IOException {
		PlantUmlCollection diagrams = new PlantUmlCollection();
		render(diagrams, resource);
		PlantUmlText text = diagrams.get(0);
		text.toImage(file);
	}

	public void renderTo(@Nonnull Resource resource, @Nonnull OutputStream outputStream) throws IOException {
		text.start();
		render(text, resource);
		text.end();
		text.toImage(outputStream);	
	}
	*/
	
	public PlantUmlCollection render(@Nonnull Resource resource) {
		PlantUmlCollection diagrams = new PlantUmlCollection();
		render(diagrams, resource);
		return diagrams;
	}

	protected abstract void render(PlantUmlCollection diagrams, Resource r);
	
}
