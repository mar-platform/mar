package mar.analysis.smells;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

public class Model {

	private final Resource resource;
	private final Map<EClass, List<EObject>> cache = new HashMap<>();
	
	public Model(@Nonnull Resource r) {
		this.resource = r;
	}
	
	@Nonnull
	public Resource getResource() {
		return resource;
	}

	@SuppressWarnings("unchecked")
	public <T> List<? extends T> allObjectsOf(EClass eclass, Class<T> klazz) {
		return (List<T>) allObjectsOf(eclass);
	}
	
	public List<? extends EObject> allObjectsOf(EClass eclass) {
		if (cache.containsKey(eclass))
			return cache.get(eclass);
		
		List<EObject> contents = new ArrayList<EObject>();
		cache.put(eclass, contents);
		resource.getAllContents().forEachRemaining(o -> {
			if (eclass.isInstance(o))
				contents.add(o);
		});
		return contents;
	}
	
}
