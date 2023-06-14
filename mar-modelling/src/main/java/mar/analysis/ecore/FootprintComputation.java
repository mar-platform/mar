package mar.analysis.ecore;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;

public class FootprintComputation {
	
	public static final FootprintComputation INSTANCE_CROSS_REFS = new FootprintComputation(true);
	public static final FootprintComputation INSTANCE_NO_CROSS_REFS = new FootprintComputation(false);	
	
	private boolean accessCrossReferences;

	public FootprintComputation(boolean accessCrossReferences) {
		this.accessCrossReferences = accessCrossReferences;
	}
	
	public Set<String> toClassNames(File f) throws IOException {
		Resource resource = new EcoreLoader().toEMF(f);
		try {
			Set<String> result = new HashSet<String>();
			Set<Resource> visited = new HashSet<Resource>();
			return toClassNames(resource, result, visited);
		} finally {
			resource.unload();
		}
	}
	
	public Set<String> toClassNames(Resource resource) {
		Set<String> result = new HashSet<String>();
		Set<Resource> visited = new HashSet<Resource>();
		return toClassNames(resource, result, visited);		
	}
	
	private Set<String> toClassNames(Resource resource, Set<String> result, Set<Resource> visited) {
		if (visited.contains(resource))
			return result;
		
		visited.add(resource);
		
		Set<Resource> externalResources = new HashSet<Resource>();
		
		//TreeIterator<EObject> it = resource.getAllContents();
		TreeIterator<EObject> it = EcoreUtil.getAllContents(resource, true);
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof EClassifier) {
				result.add(((EClassifier) obj).getName());
				if (accessCrossReferences && obj instanceof EClass) {
					Map<EObject, Collection<Setting>> crossRefs = EcoreUtil.ProxyCrossReferencer.find(obj);
					if (! crossRefs.isEmpty()) {
						for (EObject proxy : crossRefs.keySet()) {
							if (proxy.eIsProxy()) {
								EObject resolved = EcoreUtil.resolve(proxy, resource);
								if (resolved.eResource() != null)
									externalResources.add(resolved.eResource());
							}
						}
					}
				}
			}
		}

		for (Resource resource2 : externalResources) {
			result = toClassNames(resource2, result, visited);
		}
		
		return result;
	}

}
