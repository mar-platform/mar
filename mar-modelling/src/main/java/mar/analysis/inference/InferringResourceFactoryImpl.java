package mar.analysis.inference;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mar.modelling.xmi.LooseLoadingXMIResource;

public class InferringResourceFactoryImpl  extends XMIResourceFactoryImpl {

	@Override
	public InferringResource createResource(URI uri) {
		return new InferringResource(uri);
	}
	
}