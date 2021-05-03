package common.test;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.IntSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Consumer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.internal.resource.UMLResourceFactoryImpl;
import org.jgrapht.alg.scoring.ClusteringCoefficient;
import org.junit.Ignore;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.Test;
import org.junit.Test;

import mar.model2graph.AbstractModel2Graph;
import mar.model2graph.AbstractPathComputation;
import mar.model2graph.MetaFilter;
import mar.model2graph.Model2GraphAllpaths;
import mar.paths.PathFactory;
import mar.paths.PathFactory.DefaultPathFactory;
import mar.paths.PathFactory.EcoreTokenizer;
import mar.paths.stemming.UMLPathFactory;

import org.jgrapht.Graph;
import org.jgrapht.GraphMetrics;

public class Model2GraphAllpathsTest {
	
	@Ignore
	@Test
	public void testGetListOfPaths() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
		File dir = new File("/home/antolin/wakame/randomStuff/testM2G/");
		 File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	if (!child.getName().equals("mutations.csv")) {
			    	ResourceSet rs = new ResourceSetImpl();
					Resource resource = rs.getResource(URI.createFileURI(child.getAbsolutePath()), true);
					AbstractPathComputation pathComputation = null;
					pathComputation = new Model2GraphAllpaths(4)
			    			.withPathFactory(new PathFactory.DefaultPathFactory());
					pathComputation.withFilter(MetaFilter.getEcoreFilter());
					
					System.out.println(pathComputation.getListOfPaths(resource).toMapParticionedPaths());
					
		    	}
		      }
		  } 
	}
	
	//@Test
	public void testUMLSM() throws FileNotFoundException, IOException {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
		EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		
		
		File dir = new File("/home/antolin/repositories/EXAMPLESM/");
		 File[] directoryListing = dir.listFiles();
		  if (directoryListing != null) {
		    for (File child : directoryListing) {
		    	if (!child.getName().equals("mutations.csv")) {
		    		
		    		UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
            		Resource resource = factory.createResource(URI.createFileURI("ex.uml"));
		    		
			    	ResourceSet rs = new ResourceSetImpl();
			    	resource.load(new FileInputStream(child.getAbsolutePath()), null);
					Model2GraphAllpaths m2g = new Model2GraphAllpaths(4);
					m2g.withPathFactory(new PathFactory.WSandCCTokenizerSWStemming());
					m2g.withFilter(MetaFilter.getUMLStateMachineFilter());
					
					System.out.println(m2g.getListOfPaths(resource).toMapParticionedPaths());
		    	}
		      }
		  } 
	}
	
	//@Test
	public void testGenMyModel() throws FileNotFoundException {
		Scanner s = new Scanner(new File("/home/antolin/mde-datasets/repo-genmymodel-uml/analysis/valid.txt"));
		ArrayList<String> list = new ArrayList<String>();
		while (s.hasNext()){
		    list.add(s.next());
		}
		s.close();
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
		EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		
        String prefix = "/home/antolin/mde-datasets/repo-genmymodel-uml/";
		
		for (String string : list) {
			File child = new File(prefix+string);
			long fileSize = child.length();
    		if (fileSize > 50000)
    			continue;
    		
    		UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
    		Resource resource = factory.createResource(URI.createFileURI("ex.uml"));
    		
    		
    		try {
				resource.load(new FileInputStream(child.getAbsolutePath()), null);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}
    		
    		//resource.load(new FileInputStream(child.getAbsolutePath()));
    		
    		System.out.println(child.getName());
    		
			Model2GraphAllpaths m2g = new Model2GraphAllpaths(4);
			m2g.withFilter(MetaFilter.getUMLEasyFilter());
			m2g.withPathFactory(new DefaultPathFactory());
			//long startTime = System.currentTimeMillis();
			Map<String, Map<String, Integer>> m1 = m2g.getListOfPaths(resource).toMapParticionedPaths();
    		
		}
		
//		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
//		EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
//		int number = 0;
//		File dir = new File("/home/antolin/mde-datasets/repo-genmymodel-uml/data");
//		 File[] directoryListing = dir.listFiles();
//		  if (directoryListing != null) {
//		    for (File child : directoryListing) {
//		    	
//		    	if (!child.getName().equals("mutations.csv")) {
//		    		
//		    		long fileSize = child.length();
//		    		if (fileSize > 50000)
//		    			continue;
//		    		if (child.getName().equals("_Iwg1wBnREeig8qiayYYCew.xmi" ))
//		    			continue;
//
//		    		UMLResourceFactoryImpl factory = new UMLResourceFactoryImpl();
//            		Resource resource = factory.createResource(URI.createFileURI("ex.uml"));
//            		
//            		
//            		try {
//						resource.load(new FileInputStream(child.getAbsolutePath()), null);
//					} catch (FileNotFoundException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						continue;
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//						continue;
//					}
//            		
//		    		//resource.load(new FileInputStream(child.getAbsolutePath()));
//		    		
//		    		System.out.println(child.getName());
//		    		
//					Model2GraphAllpaths m2g = new Model2GraphAllpaths(4);
//					m2g.withFilter(MetaFilter.getUMLEasyFilter());
//					m2g.withPathFactory(new DefaultPathFactory());
//					//long startTime = System.currentTimeMillis();
//					Map<String, Map<String, Integer>> m1 = m2g.getListOfPaths(resource).toMapParticionedPaths();
//					//long endTime = System.currentTimeMillis();
//					//long time1 = endTime - startTime;
//					
//					//System.out.println(time1);
//					number = number + 1;
//					
//		    	}
//		      }
//		  }
//		  System.out.println(number);
	}
	
	
	

	@Ignore
	@Test
	public void testAllEcore() throws IOException {
		
		Scanner s = new Scanner(new File("/home/antolin/wakame/mde-datasets/repo-ecore-all/analysis/valid.txt"));
		ArrayList<String> list = new ArrayList<String>();
		while (s.hasNext()){
		    list.add(s.next());
		}
		s.close();
		
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap( ).put("*", new XMIResourceFactoryImpl());
    	EPackage.Registry.INSTANCE.put(UMLPackage.eINSTANCE.getNsURI(), UMLPackage.eINSTANCE);
		
		String prefix = "/home/antolin/wakame/mde-datasets/repo-ecore-all/";
		List<String> bigModels = new LinkedList<String>();
		
		int i = 0;
		for (String string : list) {
			
			Model2GraphAllpaths pathComputation = null;
			pathComputation = (Model2GraphAllpaths) new Model2GraphAllpaths(3)
	    			.withPathFactory(new PathFactory.EcoreTokenizer());
			pathComputation.withFilter(MetaFilter.getEcoreFilter());
			
			ResourceSet rs = new ResourceSetImpl();
			Resource resource = null;
			try {
				resource = rs.getResource(URI.createFileURI(prefix+string), true);
			} catch (Exception e) {
				continue;
			}
			//ClusteringCoefficient c = new ClusteringCoefficient<>(pathComputation.createParallelGraph(resource));
			System.out.println(i);
			Graph g = pathComputation.createParallelGraph(resource);
			
			
			
			if (g.vertexSet().size()>= 2000) {
				Map<String,Map<String,Integer>> mapTokens = pathComputation.getListOfPaths(resource).toMapParticionedPaths();
				i = i + 1;
			} else {
				pathComputation = (Model2GraphAllpaths) new Model2GraphAllpaths(4)
		    			.withPathFactory(new PathFactory.EcoreTokenizer());
				pathComputation.withFilter(MetaFilter.getEcoreFilter());
				Map<String,Map<String,Integer>> mapTokens = pathComputation.getListOfPaths(resource).toMapParticionedPaths();
				i = i + 1;
			}
			
			
//			if (string.equals("data/swmuir/xxxxx/ncpdp/models/org.ncpdp.uml.telecom/model/ECL.ecore") ||
//					string.equals("data/patins1/ifc4emf/plugins/org.ifc4emf.metamodel.ifc/model/ifc2x3.ecore") ||
//					string.equals("data/max-kramer/geko-model-weaver/examples/ifc/lu.uni.geko.examples.ifc/metamodel/ifc2x3_advice.ecore") ||
//					string.contains("ifc2x3")) {
//				
//				Model2GraphAllpaths pathComputation = null;
//				pathComputation = (Model2GraphAllpaths) new Model2GraphAllpaths(3)
//		    			.withPathFactory(new PathFactory.EcoreTokenizer());
//				pathComputation.withFilter(MetaFilter.getEcoreFilter());
//				
//				ResourceSet rs = new ResourceSetImpl();
//				Resource resource = null;
//				try {
//					resource = rs.getResource(URI.createFileURI(prefix+string), true);
//				} catch (Exception e) {
//					continue;
//				}
//				//ClusteringCoefficient c = new ClusteringCoefficient<>(pathComputation.createParallelGraph(resource));
//				System.out.println(string);
//				Graph g = pathComputation.createParallelGraph(resource);
////				List<Integer> l = new LinkedList<>();
////				for (Object n : g.vertexSet()) {
////					l.add(g.degreeOf(n));
////				}
////				IntSummaryStatistics stats = l.stream()
////                        .mapToInt((x) -> x)
////                        .summaryStatistics();
////				System.out.println(stats.getAverage());
////				System.out.println(g.vertexSet().size());
//				
//				if (g.vertexSet().size()>= 1000) {
//					bigModels.add(string);
//				}
//				
//				continue;
//			} else {
//				Model2GraphAllpaths pathComputation = null;
//				pathComputation = (Model2GraphAllpaths) new Model2GraphAllpaths(3)
//		    			.withPathFactory(new PathFactory.EcoreTokenizer());
//				pathComputation.withFilter(MetaFilter.getEcoreFilter());
//				
//				ResourceSet rs = new ResourceSetImpl();
//				Resource resource = null;
//				try {
//					resource = rs.getResource(URI.createFileURI(prefix+string), true);
//				} catch (Exception e) {
//					continue;
//				}
//				
//				//ClusteringCoefficient c = new ClusteringCoefficient<>(pathComputation.createParallelGraph(resource));
//				System.out.println(string);
//				Graph g = pathComputation.createParallelGraph(resource);
//				
////				List<Integer> l = new LinkedList<>();
////				for (Object n : g.vertexSet()) {
////					l.add(g.degreeOf(n));
////				}
////				IntSummaryStatistics stats = l.stream()
////                        .mapToInt((x) -> x)
////                        .summaryStatistics();
////				System.out.println(stats.getAverage());
////				System.out.println(g.vertexSet().size());
//				
//				if (g.vertexSet().size()>= 1000) {
//					bigModels.add(string);
//				}
//			}
//			continue;
			//System.out.println(string);
//			AbstractPathComputation pathComputation = null;
//			pathComputation = new Model2GraphAllpaths(3)
//	    			.withPathFactory(new PathFactory.EcoreTokenizer());
//			pathComputation.withFilter(MetaFilter.getEcoreFilter());
//			
//			ResourceSet rs = new ResourceSetImpl();
//			try {
//				Resource resource = rs.getResource(URI.createFileURI(prefix+string), true);
//				Map<String,Map<String,Integer>> mapTokens = pathComputation.getListOfPaths(resource).toMapParticionedPaths();
//			}catch (Exception e) {
//				//e.printStackTrace();
//    	    	continue;
//    	    }	
			
		}
		
		//System.out.println(bigModels);
		System.out.println(bigModels.size());
		
		
	}

}
