package mar.artefacts.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.ecore.EcoreLoader;

public class MetamodelSeacher {

	private final FileSearcher searcher;

	public MetamodelSeacher(FileSearcher searcher) {
		this.searcher = searcher;
	}

	public <T> Map<T, RecoveredMetamodelFile> search(Map<T, RecoveredMetamodelFile> classFootprints) {
		scoreFootprints(classFootprints);
		
		Map<T, RecoveredMetamodelFile> result = new HashMap<>();
		classFootprints.forEach((k, v) -> {
			if (v.getBestMetamodel() != null && v.getAccuracy() > 95.0)
				result.put(k ,v);
		});
		
		return result;
	}

	public RecoveredMetamodelFile search(Set<String> classFootprints) {
		Map<String, RecoveredMetamodelFile> singleton = new HashMap<>();
		final String FAKE_KEY = "key";
		singleton.put(FAKE_KEY, new RecoveredMetamodelFile(classFootprints));
		scoreFootprints(singleton);
		return singleton.get(FAKE_KEY);
	}
	
	public <T> void scoreFootprints(Map<T, RecoveredMetamodelFile> classFootprints) {
		try {
			List<Path> files = searcher.findFilesByExtension("ecore");
			for (Path path : files) {
				Set<String> names;
				try {
					names = toClassNames(path.toFile());
				} catch (Throwable e) {
					// This may happen for files which doesn't validate (it is probably a bit better check the existence of this file in the db and check that it is valid
					continue;
				}		
				
				classFootprints.forEach((metamodel, recoveredMetamodel) -> {
					int coincidences = checkSimilarity(names, recoveredMetamodel.footprint);
					if (coincidences > recoveredMetamodel.bestCoincidenceCount) {
						recoveredMetamodel.accuracy = (100.0 * coincidences) / recoveredMetamodel.footprint.size();
						recoveredMetamodel.bestCoincidenceCount = coincidences;
						recoveredMetamodel.bestMetamodel = path.toFile();
					}
				});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}
	

	private int checkSimilarity(Set<String> names, Set<String> footprintNames) {
		int count = 0;
		for (String str : footprintNames) {
			if (names.contains(str))
				count++;
		}
		return count;
	}
	
	private Set<String> toClassNames(File f) throws IOException {
		Set<String> result = new HashSet<String>();
		Resource resource = new EcoreLoader().toEMF(f);
		TreeIterator<EObject> it = resource.getAllContents();
		while (it.hasNext()) {
			EObject obj = it.next();
			if (obj instanceof EClass) {
				result.add(((EClass) obj).getName());
			}
		}
		return result;
	}
	
	public static class RecoveredMetamodelFile {
		private double accuracy;
		private Set<String> footprint = new HashSet<String>();
		private int bestCoincidenceCount = -1;
		private File bestMetamodel;
	
		public RecoveredMetamodelFile(Set<String> classFootprints) {
			this.footprint.addAll(classFootprints);
		}

		public RecoveredMetamodelFile() {
		}
		
		public void addToFootprint(String name) {
			this.footprint.add(name);
		}
		
		public double getAccuracy() {
			return accuracy;
		}
		
		public int getBestCoincidenceCount() {
			return bestCoincidenceCount;
		}
		
		public File getBestMetamodel() {
			return bestMetamodel;
		}
		
		public boolean isValid() {
			return bestMetamodel != null;
		}
	}
}
