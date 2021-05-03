package mar.renderers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

public abstract class PlantUMLRenderer {

	public void renderTo(@Nonnull Resource resource, @Nonnull File file) throws IOException {
		PlantUmlText text = new PlantUmlText();
		text.start();
		render(text, resource);
		text.end();
		text.toImage(file);
	}

	public void renderTo(@Nonnull Resource resource, @Nonnull OutputStream outputStream) throws IOException {
		PlantUmlText text = new PlantUmlText();
		text.start();
		render(text, resource);
		text.end();
		text.toImage(outputStream);	
	}

	protected abstract void render(PlantUmlText text, Resource r);
	
}
