package mar.analysis.smells.ecore;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.junit.Before;
import org.junit.Test;

import mar.analysis.smells.Smell;

public class TestEcoreSmells {

	private PetsMetamodel pets;
	
	@Before
	public void setUp() throws Exception {
		pets = new PetsMetamodel();
	}

	@Test
	public void testIsolatedClassSmellDetector() {
		EClass isolated = pets.newEClass("isolated");
		IsolatedClassSmellDetector detector = new IsolatedClassSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(isolated, ((IsolatedClassSmellDetector.IsolatedClassSmell) smells.get(0)).getSmellyEClass());
	}
	
	@Test
	public void testUninstantiableClassSmellDetector() {
		UninstantiableClassSmellDetector detector = new UninstantiableClassSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());	
	}
	
	@Test
	public void testOverLoadedClassSmellDetector() {
		EClass overloadedclass = pets.newEClass("Class");
		overloadedclass.setAbstract(true);
		for (int i = 0; i<11; ++i) {
			pets.newEAttribute("att"+Integer.toString(i), overloadedclass);
		}
		OverLoadedClassSmellDetector detector = new OverLoadedClassSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(overloadedclass, ((OverLoadedClassSmellDetector.OverLoadedClassSmell) smells.get(0)).getSmellyEClass());		
	}
	
	@Test
	public void testRefersAlotClassSmellDetector() {
		RefersAlotClassSmellDetector detector = new RefersAlotClassSmellDetector();
		detector.setThresh(1);
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.person, ((RefersAlotClassSmellDetector.RefersAlotClassSmell) smells.get(0)).getSmellyEClass());	
		
		detector.setThresh(2);
		smells = detector.detect(pets.res);
		assertEquals(0, smells.size());
	}
	
	@Test
	public void testReferredAlotClassSmellDetector() {
		ReferredAlotClassSmellDetector detector = new ReferredAlotClassSmellDetector();
		detector.setThresh(1);
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.person, ((ReferredAlotClassSmellDetector.ReferredAlotClassSmell) smells.get(0)).getSmellyEClass());	
		detector.setThresh(2);
		smells = detector.detect(pets.res);
		assertEquals(0, smells.size());
	}
	@Test
	public void testDepthHierarchySmellDetector() {
		DepthHierarchySmellDetector detector = new DepthHierarchySmellDetector();
		detector.setThresh(2);
		List<Smell> smells = detector.detect(pets.res);
		System.out.println(smells.stream().map(f->{
			return ((DepthHierarchySmellDetector.DepthHierarchySmell) f).getSmellyEClass().getName();
		}).collect(Collectors.toList()));
		assertEquals(2, smells.size());
		assertEquals(pets.specialDog, ((DepthHierarchySmellDetector.DepthHierarchySmell) smells.get(0)).getSmellyEClass());
		
		detector.setThresh(1);
		smells = detector.detect(pets.res);
		System.out.println(smells.stream().map(f->{
			return ((DepthHierarchySmellDetector.DepthHierarchySmell) f).getSmellyEClass().getName();
		}).collect(Collectors.toList()));
		assertEquals(3, smells.size());
		
		detector.setThresh(0);
		smells = detector.detect(pets.res);
		System.out.println(smells.stream().map(f->{
			return ((DepthHierarchySmellDetector.DepthHierarchySmell) f).getSmellyEClass().getName();
		}).collect(Collectors.toList()));
		assertEquals(5, smells.size());
	}
	
	@Test
	public void testTooManyChildrenSmellDetector() {
		TooManyChildrenSmellDetector detector = new TooManyChildrenSmellDetector();
		detector.setThresh(2);
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.dog, ((TooManyChildrenSmellDetector.TooManyChildrenSmell) smells.get(0)).getSmellyEClass());
		
		detector.setThresh(3);
		smells = detector.detect(pets.res);
		assertEquals(0, smells.size());
		
	}
	
	@Test
	public void testIrrelevantClassSmellDetector() {
		IrrelevantClassSmellDetector detector = new IrrelevantClassSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.specialDog, ((IrrelevantClassSmellDetector.IrrelevantClassSmell) smells.get(0)).getSmellyEClass());
	}
	
	@Test
	public void testOnlyOneClassSuperSmellDetector() {
		OnlyOneClassSuperSmellDetector detector = new OnlyOneClassSuperSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.animal, ((OnlyOneClassSuperSmellDetector.OnlyOneClassSuperSmell) smells.get(0)).getSmellyEClass());
	}
	
	@Test
	public void testTooLongNamesSmellDetector() {
		TooLongNamesSmellDetector detector = new TooLongNamesSmellDetector();
		List<Smell> smells = detector.detect(pets.res);
		assertEquals(1, smells.size());
		assertEquals(pets.specialDog, ((TooLongNamesSmellDetector.TooLongNamesSmell) smells.get(0)).getSmellyElement());
	}
	
	private static class PetsMetamodel {
		private Resource res = new ResourceImpl();
		private EPackage pkg;
		private EClass person;
		private EClass specialDog;
		private EClass dog;
		private EClass animal;
		
		public PetsMetamodel() {
			pkg = EcoreFactory.eINSTANCE.createEPackage();
			pkg.setName("test");
			
			EClass person = newEClass("Person", pkg);
			this.person = person;
			EClass animal = newEClass("Animal", pkg);
			animal.setAbstract(true);
			this.animal = animal;

			EClass dog = newEClass("Dog", pkg);
			dog.getESuperTypes().add(animal);
			this.dog = dog;
			
			EClass specialDog = newEClass("SpecialDogDogDogDogDogDogDogDogDogDogDogDogDogDogDog",pkg);
			specialDog.getESuperTypes().add(dog);
			this.specialDog = specialDog;
			specialDog.setAbstract(true);
			
			EClass specialDog2 = newEClass("SpecialDog2",pkg);
			specialDog2.getESuperTypes().add(dog);
			
			EReference person_to_animal = EcoreFactory.eINSTANCE.createEReference();
			person_to_animal.setName("pet");
			person_to_animal.setEType(animal);
			person.getEStructuralFeatures().add(person_to_animal);
			
			EReference person_to_person = EcoreFactory.eINSTANCE.createEReference();
			person_to_person.setName("slave");
			person_to_person.setEType(person);
			person.getEStructuralFeatures().add(person_to_person);
			
			EReference animal_to_person = EcoreFactory.eINSTANCE.createEReference();
			animal_to_person.setName("owner");
			animal_to_person.setEType(person);
			animal.getEStructuralFeatures().add(animal_to_person);
			
			person_to_animal.setEOpposite(animal_to_person);
			animal_to_person.setEOpposite(person_to_animal);
			
			res.getContents().add(pkg);
		}
		
		public EClass newEClass(@Nonnull String name) {
			return newEClass(name, pkg);
		}
		
		public EClass newEClass(@Nonnull String name, @Nonnull EPackage pkg) {
			EClass c = EcoreFactory.eINSTANCE.createEClass();
			c.setName(name);
			pkg.getEClassifiers().add(c);
			return c;
		}
		
		public void newEAttribute(@Nonnull String name, EClass eclass) {
			EAttribute eattribute = EcoreFactory.eINSTANCE.createEAttribute();
			eattribute.setName(name);
			eclass.getEStructuralFeatures().add(EcoreFactory.eINSTANCE.createEAttribute()); 
		}
	}

}
