package mar.modelling.xmi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.xmi.FeatureNotFoundException;
import org.eclipse.emf.ecore.xmi.XMLHelper;
import org.eclipse.emf.ecore.xmi.XMLLoad;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.SAXXMLHandler;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLHandler;
import org.eclipse.emf.ecore.xmi.impl.XMLHelperImpl;
import org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl;

public class LooseLoadingXMIResource extends XMIResourceImpl {
			
	public LooseLoadingXMIResource() {
		super();
	}

	public LooseLoadingXMIResource(URI uri) {
		super(uri);
	}

	@Nonnull
	public Set<? extends String> getFeatureNotFound() {
		Set<String> featureNotFound = new HashSet<>();
		for (Diagnostic error : this.errors) {
			if (error instanceof FeatureNotFoundException) {
				featureNotFound.add(((FeatureNotFoundException) error).getName());
			}
		}
		return featureNotFound;
	}

	@Override
	protected boolean useIDAttributes() {
		return false;
	}

	@Override
	protected boolean useUUIDs() {
		return true;
	}
	
	@Override
	protected XMLLoad createXMLLoad() {
		return new ApproximateXMLLoad(createXMLHelper());
	}
	
	@Override
	protected XMLLoad createXMLLoad(Map<?, ?> options) {
		if (options != null && Boolean.TRUE.equals(options.get(OPTION_SUPPRESS_XMI))) {
			return new ApproximateXMLLoad(new XMLHelperImpl(this));
		} else {
			return super.createXMLLoad(options);
		}
	}

	public static class ApproximateXMLLoad extends XMLLoadImpl {
		
		public ApproximateXMLLoad(XMLHelper helper) {
			super(helper);
		}

		protected XMLHandler makeDefaultHandler() {
			return new ApproximateXMLHandler(resource, helper, options);
		}

		@Override
		protected void handleErrors() throws IOException {
			if (!resource.getErrors().isEmpty()) {
				for (Diagnostic error : resource.getErrors()) {
					if (error instanceof org.eclipse.emf.ecore.xmi.FeatureNotFoundException) {
						//FeatureNotFoundException e = (org.eclipse.emf.ecore.xmi.FeatureNotFoundException) error;
						//((ApproximateXMIResource) resource).featureNotFound.add(e.getName());
						continue;
					} else if (error instanceof org.eclipse.emf.ecore.xmi.UnresolvedReferenceException) {
						// This typically happens in BPMN because there is xmi:id and id attributes serialized, and the system picks id
						// ((org.eclipse.emf.ecore.xmi.UnresolvedReferenceException) error).printStackTrace();
						continue;
					}

					super.handleErrors();
					return;
				}
			}
		}
	}

	private static class ApproximateXMLHandler extends SAXXMLHandler {

		public ApproximateXMLHandler(XMLResource xmiResource, XMLHelper helper, Map<?, ?> options) {
			super(xmiResource, helper, options);
		}
	}

	/**
	 * 
	 * 23:45:56.195 [main] ERROR mar.validation.FileAnalyserEMF - Crashed
	 * data/ffe727c6-2dc0-4470-8308-55d0d9b92f63.xmi
	 * org.eclipse.emf.ecore.resource.Resource$IOWrappedException: Feature
	 * 'dataModel' not found. (, 6, 90) at
	 * org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl.handleErrors(XMLLoadImpl.java:77)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl.load(XMLLoadImpl.java:185)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl.doLoad(XMLResourceImpl.java:261)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.resource.impl.ResourceImpl.load(ResourceImpl.java:1563)
	 * ~[org.eclipse.emf.ecore-2.20.0.jar:?] at
	 * mar.models.bpmn.BPMNLoader.load(BPMNLoader.java:32) ~[classes/:?] at
	 * mar.validation.bpmn.GenMyModelBPMNValidator$ValidateChecker.loadModel(GenMyModelBPMNValidator.java:59)
	 * ~[classes/:?] at
	 * mar.validation.FileAnalyserEMF.process(FileAnalyserEMF.java:29) [classes/:?]
	 * at mar.validation.ResourceAnalyser.checkFile(ResourceAnalyser.java:184)
	 * [classes/:?] at
	 * mar.validation.ResourceAnalyser.check(ResourceAnalyser.java:78) [classes/:?]
	 * at mar.validation.AnalyserMain.main(AnalyserMain.java:41) [classes/:?] Caused
	 * by: org.eclipse.emf.ecore.xmi.FeatureNotFoundException: Feature 'dataModel'
	 * not found. (, 6, 90) at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.reportUnknownFeature(XMLHandler.java:2045)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.handleUnknownFeature(XMLHandler.java:2009)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMIHandler.handleUnknownFeature(XMIHandler.java:172)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.handleFeature(XMLHandler.java:1953)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.processElement(XMLHandler.java:1048)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMIHandler.processElement(XMIHandler.java:82)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.startElement(XMLHandler.java:1026)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLHandler.startElement(XMLHandler.java:720)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * org.eclipse.emf.ecore.xmi.impl.XMIHandler.startElement(XMIHandler.java:190)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] at
	 * com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.startElement(AbstractSAXParser.java:509)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.scanStartElement(XMLDocumentFragmentScannerImpl.java:1359)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl$FragmentContentDriver.next(XMLDocumentFragmentScannerImpl.java:2784)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.impl.XMLDocumentScannerImpl.next(XMLDocumentScannerImpl.java:602)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.impl.XMLDocumentFragmentScannerImpl.scanDocument(XMLDocumentFragmentScannerImpl.java:505)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.parsers.XML11Configuration.parse(XML11Configuration.java:842)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.parsers.XML11Configuration.parse(XML11Configuration.java:771)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.parsers.XMLParser.parse(XMLParser.java:141)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.parsers.AbstractSAXParser.parse(AbstractSAXParser.java:1213)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl$JAXPSAXParser.parse(SAXParserImpl.java:643)
	 * ~[?:1.8.0_275] at
	 * com.sun.org.apache.xerces.internal.jaxp.SAXParserImpl.parse(SAXParserImpl.java:327)
	 * ~[?:1.8.0_275] at
	 * org.eclipse.emf.ecore.xmi.impl.XMLLoadImpl.load(XMLLoadImpl.java:175)
	 * ~[org.eclipse.emf.ecore.xmi-2.16.0.jar:?] ... 8 more
	 * 
	 */

}
