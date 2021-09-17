package mar.renderers;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.uml2.uml.Model;

/**
 * Represents a list of diagrams extracted from a single resource. 
 * 
 * @author jesus
 *
 */
public class PlantUmlCollection implements Iterable<PlantUmlCollection.PlantUmlImage> {

	private List<PlantUmlImage> diagrams = new ArrayList<>();
	
	public PlantUmlText newDiagram(@Nonnull EObject obj, @Nonnull String name) {
		PlantUmlText diagram = new PlantUmlText();
		add(diagram, obj, name);
		return diagram;
	}

	public void add(@Nonnull PlantUmlText diagram, @Nonnull EObject obj, @Nonnull String name) {
		diagrams.add(new PlantUmlImage(diagrams.size(), diagram));
	}

	public boolean isEmpty() {
		return diagrams.isEmpty();
	}

	public int size() {
		return diagrams.size();
	}
	
	@Nonnull
	public PlantUmlImage get(int i) {
		return diagrams.get(i);
	}

	@Override
	public Iterator<PlantUmlImage> iterator() {
		return diagrams.iterator();
	}
	
	public class PlantUmlImage {
		private int idx;
		private PlantUmlText text;

		public PlantUmlImage(int idx, PlantUmlText text) {
			this.idx = idx;
			this.text = text;
		}

		public void toImage(OutputStream outputStream) throws IOException {
			text.toImage(outputStream);
		}
		
		public void toImage(File f) throws IOException {
			text.toImage(f);
		}

		public int getIndex() {
			return idx;
		}
	}
}
