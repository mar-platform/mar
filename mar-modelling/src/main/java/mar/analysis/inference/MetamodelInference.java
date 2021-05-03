package mar.analysis.inference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.IllegalValueException;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.emf.ecore.xmi.UnresolvedReferenceException;
import org.eclipse.emf.ecore.xmi.XMIException;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;

import mar.analysis.inference.InferringResource.UnknownFeature;

public class MetamodelInference {

	private static final boolean DEBUG = true;

	private EPackage root;
	
	public MetamodelInference() {
		root = EcoreFactory.eINSTANCE.createEPackage();
	}
	
	public EPackage infer(@Nonnull List<File> files) {
		int i = 0;
		for (File file : files) {
			i++;
			//if (i > 10) break;
			tryLoad(file);
		}		
		return root;
	}

	private void tryLoad(File file) {
		int line = -1;
		int previousLine = -1;
		int column = -1;
		int previousColumn = -1;
		String currentFeature = "";
		String feature = "";
		
		if (DEBUG)
			System.out.println(file.getAbsolutePath());
		
		int iterationsWithoutProgress = 0;
		while (iterationsWithoutProgress < (2 + 1)) {
			if (DEBUG)
				System.out.println("Line: " + line + " - " + iterationsWithoutProgress);
			
			if (previousLine != line || previousColumn != column || ! currentFeature.equals(feature)) {
				previousLine = line;
				previousColumn = column;
				currentFeature = feature;
				iterationsWithoutProgress = 0;
			} else {
				iterationsWithoutProgress++;
			}
			
			ResourceSet rs = new ResourceSetImpl();
			try {				
				if (root.getNsURI() != null) {
					rs.getPackageRegistry().put(root.getNsURI(), root);
				}
				
				rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new InferringResourceFactoryImpl());				
				InferringResource r = (InferringResource) rs.getResource(URI.createFileURI(file.getAbsolutePath()), true);
								
//				for(EClassifier c : root.getEClassifiers()) {
//					if (c instanceof EClass) {
//						EClass klass = (EClass) c;
//						klass.geteall
//					}
//				}
				
				Map<Pair<EObject, EAttribute>, EObject> changes = new HashMap<>();
				TreeIterator<EObject> it = r.getAllContents();
				while (it.hasNext()) {
					EObject obj = it.next();
					for (EAttribute att : obj.eClass().getEAllAttributes()) {
						Object value = obj.eGet(att); // assume 0..1
						if (value instanceof String) {
							if (r.getIDToEObjectMap() != null && r.getIDToEObjectMap().get(value) != null) {
								// This is a reference
								changes.put(Pair.of(obj, att), r.getIDToEObjectMap().get(value));
							}
						}
					}
				}
				
				r.unload();
				changes.forEach((pair, target) -> {
					EAttribute attr = pair.getRight();
					EObject obj = pair.getLeft();
					
					// Is the attribute still connected?
					if (attr.eContainer() != null) {					
						EReference ref = EcoreFactory.eINSTANCE.createEReference();
						ref.setName(attr.getName());
						ref.setEType(target.eClass());
						// ref.setUpperBound(attr.getUpperBound());
						ref.setUpperBound(-1);
						ref.setLowerBound(attr.getLowerBound());
						
						EClass owner = obj.eClass();
						owner.getEStructuralFeatures().remove(attr);
						owner.getEStructuralFeatures().add(ref);
					}
				});
				
				
				if (DEBUG) {
					try {
						XMIResourceImpl m = new XMIResourceImpl(URI.createFileURI("/tmp/test.ecore"));
						m.getContents().add(root);
						m.save(null);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			} catch (UnknownFeature c) {
				addFeature(c);
				line = c.getLine();
				column = c.getColumn();
				currentFeature = c.getName();
			} catch (UnknownContainmentException c) {
				addFeature(c);
				line = c.getLine();
				column = c.getColumn();
				currentFeature = c.getName();
			} catch (Exception e) {
				Throwable cause = e.getCause();
				if (cause instanceof PackageNotFoundException) {
					PackageNotFoundException p = (PackageNotFoundException) cause;
					Pattern pattern = Pattern.compile("Package with uri '(.+)' not found");
					Matcher matcher = pattern.matcher(p.getMessage());
					if (matcher.find()) {
						String uri = matcher.group(1);
						root.setNsURI(uri);
					} else {
						throw new IllegalStateException("No match for: " + p.getMessage());
					}

					line = ((XMIException) cause).getLine();
					column = ((XMIException) cause).getColumn();
				} else if (cause instanceof org.eclipse.emf.ecore.xmi.ClassNotFoundException) {
					Pattern pattern = Pattern.compile("Class '(.+)' is not found or is abstract");
					Matcher matcher = pattern.matcher(cause.getMessage());
					if (matcher.find()) {
						String className = matcher.group(1);
						EClass klass = EcoreFactory.eINSTANCE.createEClass();
						klass.setName(className);
						root.getEClassifiers().add(klass);
					} else {
						throw new IllegalStateException();
					}
					line = ((XMIException) cause).getLine();
					column = ((XMIException) cause).getColumn();
				} else if (cause instanceof IllegalValueException) {
					IllegalValueException c = (IllegalValueException) cause;
					if (c.getFeature() instanceof EAttribute && c.getValue() instanceof EObject) {
						if (true)
							throw new RuntimeException(cause);
						EAttribute attr = (EAttribute) c.getFeature();
						EReference ref = EcoreFactory.eINSTANCE.createEReference();
						ref.setName(attr.getName());
						ref.setEType(((EObject) c.getValue()).eClass());
						// ref.setUpperBound(attr.getUpperBound());
						ref.setUpperBound(-1);
						ref.setLowerBound(attr.getLowerBound());
						
						EClass owner = c.getObject().eClass();
						owner.getEStructuralFeatures().remove(attr);
						owner.getEStructuralFeatures().add(ref);
					} else if (c.getFeature() instanceof EReference && c.getValue() instanceof EObject) {
						// The typical scenario is that we introduced a fake class (e.g., <elements>)
						// and we want it to be a superclass
						EObject obj = (EObject) c.getValue();
						obj.eClass().getESuperTypes().add(((EReference) c.getFeature()).getEReferenceType());
						throw new RuntimeException("Changed to superclass");
					} else {
						throw new RuntimeException(e);
					}
					line = ((XMIException) cause).getLine();
					column = ((XMIException) cause).getColumn();
				} else if (cause instanceof UnknownFeature) {
					UnknownFeature c = (UnknownFeature) cause;
					addFeature(c);
					line = c.getLine();
					column = ((XMIException) cause).getColumn();
				} else if (cause instanceof UnknownContainmentException) {
					addFeature((UnknownContainmentException) cause);
					line = ((UnknownContainmentException) cause).getLine();
					column = ((XMIException) cause).getColumn();
				} else if (cause instanceof UnresolvedReferenceException) {
					// See: repo-genmymodel-rds/data/_VCF04P_iEeeLEbIzy5aHfg.xmi, <type is Element>
					// This is typically a sign that a containment reference is non-containment
					UnresolvedReferenceException ex = (UnresolvedReferenceException) cause;
					((EReference) ex.getFeature()).setContainment(false);					
					line = ex.getLine();
					column = ex.getColumn();
				} else {
					if (e.getMessage().contains("Content is not allowed in prolog")) {
						System.out.println("Invalid model: " + file);
						return;
					}
					throw e;
				}
				
				// try again
				continue;
			} finally {
				rs.getResources().forEach(Resource::unload);
			}
		}
		
	}

	private void addFeature(UnknownFeature c) {
		String featureName = c.getName();
		EAttribute attr = EcoreFactory.eINSTANCE.createEAttribute();
		attr.setName(featureName);
		attr.setEType(EcorePackage.eINSTANCE.getEString());
		c.getObject().eClass().getEStructuralFeatures().add(attr);
		
		// Check if it contains an Ecore reference style value and convert to a reference
	}

	private void addFeature(UnknownContainmentException cause) {
		String featureName = cause.getName();
		EClass parent = cause.getParent();
		String className = featureName;
		EClass current = null;
		for (EClassifier c : root.getEClassifiers()) {
			if (c instanceof EClass) {
				EClass cl = (EClass) c;
				if (cl.getName().equals(className)) {
					current = cl;
					break;
				}
			}
		}
		
		if (current == null) {
			current = EcoreFactory.eINSTANCE.createEClass();
			current.setName(className);
			root.getEClassifiers().add(current);
		}
		
		EReference ref = EcoreFactory.eINSTANCE.createEReference();
		ref.setName(featureName);
		ref.setContainment(true);
		ref.setUpperBound(-1);
		ref.setEType(current);
		
		parent.getEStructuralFeatures().add(ref);
	}

}
