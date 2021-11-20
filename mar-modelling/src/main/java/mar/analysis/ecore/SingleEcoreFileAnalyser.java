package mar.analysis.ecore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EValidatorRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import mar.analysis.smells.Smell;
import mar.analysis.smells.ecore.EcoreSmellCatalog;
import mar.modelling.loader.ILoader;
import mar.validation.AnalysisMetadataDocument;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

public class SingleEcoreFileAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "ecore";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("ecore", new EcoreResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());		
		}
		
		@Override
		public String getId() {
			return ID;
		}
		
		@Override
		public SingleEcoreFileAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new SingleEcoreFileAnalyser();
		}

		@Override
		public ILoader newLoader() {
			return new EcoreLoader();
		}				
	}

	@Override
	protected boolean checkResource(String modelId, Resource r) {		
		return validate(r) == 0;
	}

	
	// Return the number of validation errors
	private int validate(Resource r) {
		EValidatorRegistryImpl registry = new org.eclipse.emf.ecore.impl.EValidatorRegistryImpl();
		registry.put(EcorePackage.eINSTANCE, new CustomEcoreValidator());
		Diagnostician diagnostician = new Diagnostician(registry);		
		for (EObject obj : r.getContents()) {					
			Diagnostic d = diagnostician.validate(obj);
			if (d.getSeverity() == Diagnostic.ERROR) {
				return 1 + d.getChildren().size();
			}
		}
		return 0;
	}

	@Override
	protected AnalysisData getAdditionalAnalysis(Resource r) {		
		List<String> uris = new ArrayList<String>();
		
		int numElements   = 0;
		int numPackages   = 0;
		int numClasses    = 0;
		int numEnums      = 0;
		int numDatatypes  = 0;
		int numAttributes = 0;
		int numReferences = 0;
		
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			numElements++;
			if (obj instanceof EPackage) {
				numPackages++;
				String nsURI = ((EPackage) obj).getNsURI();
				if (nsURI != null)
					uris.add(nsURI);
			} else if (obj instanceof EClass) {
				numClasses++;
			} else if (obj instanceof EAttribute) {
				numAttributes++;
			} else if (obj instanceof EReference) {
				numReferences++;
			} else if (obj instanceof EEnum) {
				numEnums++;
			} else if (obj instanceof EDataType) {
				numDatatypes++;
			}
		}
		
		Map<String, Integer> stats = new HashMap<String, Integer>();
		stats.put("elements", numElements);
		stats.put("packages", numPackages);
		stats.put("classes", numClasses);
		stats.put("enum", numEnums);
		stats.put("datatypes", numDatatypes);
		stats.put("attributes", numAttributes);
		stats.put("references", numReferences);
		
		// int numValidationErrors = validate(r);
		// stats.put("errors", numValidationErrors);
		
		Map<String, List<String>> metadata = null;
		if (! uris.isEmpty()) {
			metadata = new HashMap<String, List<String>>();
			metadata.put("nsURI", uris);			
		}
		
		// Metadata as a document
		//Map<Object, Object> document = new HashMap<>();
		//Map<String, Integer> smellDocument = new HashMap<>();
		//document.put("smells", smellDocument);
		
		AnalysisMetadataDocument document = new AnalysisMetadataDocument();
		document.setNumElements(numElements);
		Map<String, List<Smell>> smells = EcoreSmellCatalog.INSTANCE.detectSmells(r);
		if (! smells.isEmpty()) {
			smells.forEach((k, v) -> {
				document.addSmell(k, v);
			});
		}
		
		return new AnalysisData(stats, metadata, document);		
	}
	
}
