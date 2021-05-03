package mar.modelling.xmi;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public class LooseLoadingXMIResourceFactory extends XMIResourceFactoryImpl {

	@Override
	public LooseLoadingXMIResource createResource(URI uri) {
		return new LooseLoadingXMIResource(uri);
	}
	
}
