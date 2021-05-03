package mar.analysis.inference;

import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMIHandler;
import org.eclipse.emf.ecore.xmi.impl.XMIHelperImpl;
import org.eclipse.emf.ecore.xmi.impl.XMILoadImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLHandler;

public class InferringResource extends XMIResourceImpl {

	public InferringResource(URI uri) {
		super(uri);
	}

	@Override
	protected XMLLoad createXMLLoad(Map<?, ?> options) {
		return new InferringXMILoad(new XMIHelperImpl(this));
	}
	
	public static class InferringXMILoad extends XMILoadImpl {
		
		public InferringXMILoad(XMLHelper helper) {
			super(helper);
		}

		protected XMLHandler makeDefaultHandler() {
			return new InferringXMLHandler(resource, helper, options);
		}
	}
	
	@Override
	protected boolean useUUIDs() {
		return true;
	}
	
	private static class InferringXMLHandler extends SAXXMIHandler {

		public InferringXMLHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
			super(xmiResource, helper, options);
		}
		
		@Override
		protected void handleUnknownFeature(String prefix, String name, boolean isElement, EObject peekObject, String value) {
			if (isElement && value == null) {
				// This means it is a containment feature
				throw new UnknownContainmentException(name, peekObject.eClass(), getLineNumber(), getColumnNumber());
			}
			throw new UnknownFeature(prefix, name, peekObject, value, getLineNumber(), getColumnNumber());
		}
		
	}
	
	public static class UnknownFeature extends RuntimeException {

		private String prefix;
		private String name;
		private EObject object;
		private String value;
		private int line;
		private int column;

		public UnknownFeature(String prefix, String name, EObject object, String value, int line, int column) {
			super("Unknown feature: " + name);
			this.prefix = prefix;
			this.name = name;
			this.object = object;
			this.value = value;
			this.line = line;
			this.column = column;
		}
		
		public int getLine() {
			return line;
		}
		
		public int getColumn() {
			return column;
		}
		
		public String getName() {
			return name;
		}
		
		public EObject getObject() {
			return object;
		}
		
		public String getValue() {
			return value;
		}
		
	}
}
