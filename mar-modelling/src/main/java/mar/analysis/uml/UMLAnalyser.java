package mar.analysis.uml;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.CheckForNull;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Actor;
import org.eclipse.uml2.uml.Component;
import org.eclipse.uml2.uml.Interaction;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;

import mar.analysis.ecore.SingleEcoreFileAnalyser;
import mar.indexer.common.configuration.ModelLoader;
import mar.modelling.loader.ILoader;
import mar.validation.IFileInfo;
import mar.validation.ResourceAnalyser;
import mar.validation.ResourceAnalyser.OptionMap;
import mar.validation.SingleEMFFileAnalyser;

public class UMLAnalyser extends SingleEMFFileAnalyser {

	public static final String ID = "uml";

	public static class Factory implements ResourceAnalyser.Factory {

		@Override
		public void configureEnvironment() {
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("uml", new UMLResourceFactoryImpl());
			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
			EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		}
		
		@Override
		public UMLAnalyser newAnalyser(@CheckForNull OptionMap options) {
			return new UMLAnalyser();
		}				
		
		@Override
		public String getId() {
			return ID;
		}

		@Override
		public ILoader newLoader() {
			return new UMLLoader();
		}

	}
	
	@Override
	protected boolean isProperFormat(IFileInfo f) {
		try(BufferedReader reader = new BufferedReader(new FileReader(f.getFullFile()))) {
			boolean isUML = reader.lines().anyMatch(s -> s.contains("http://www.eclipse.org/uml2"));
			if (! isUML) {
				System.out.println(" -- Not a UML model");
				return false;
			}
			return true;
		} catch (FileNotFoundException e1) {
			return false;
		} catch (IOException e1) {
			return false;
		}
	}
	
	@Override
	protected Resource loadModel(IFileInfo f) throws IOException {
		return ModelLoader.UML.load(f.getFullFile());
	}
	
	@Override
	protected boolean checkResource(String modelId, Resource r) {
		boolean hasModel = false;
		for (EObject obj : r.getContents()) {					
			Diagnostic d = Diagnostician.INSTANCE.validate(obj);
			if (d.getSeverity() == Diagnostic.ERROR) {
				return false;
			}
			
			if (obj instanceof Model) {
				hasModel = true;
			}
		}
		
		return hasModel;
	}

	@Override
	protected AnalysisData getAdditionalAnalysis(Resource r) {
		Map<String, Integer> types = new HashMap<>();
		
		TreeIterator<EObject> it = r.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			String type = null;
			if (obj instanceof StateMachine) {
				type = "sm";
			} else if (obj instanceof Interaction) {
				type = "interaction";
			} else if (obj instanceof Activity) {
				type = "ad";
			} else if (obj instanceof Component) {
				type = "comp";
			} else if (obj instanceof org.eclipse.uml2.uml.Class) {
				type = "cd";
			} else if (obj instanceof Actor) {
				type = "usecase";
			} else if (obj instanceof Profile) {
				type = "profile";
			}			
			
			if (type != null) {
				String key = "diagram_" + type;
				types.putIfAbsent(key, 0);
				types.compute(key, (k, v) -> v + 1);
			}
		}
			
		return new AnalysisData(types, null, null);		
	}		
}
