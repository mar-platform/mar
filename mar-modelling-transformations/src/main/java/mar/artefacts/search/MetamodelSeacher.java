package mar.artefacts.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.uml2.uml.UMLPackage;

import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Collections;
import mar.analysis.ecore.FootprintComputation;
import mar.artefacts.Metamodel;
import mar.artefacts.RecoveredPath;
import mar.validation.AnalysisDB;
import mar.validation.AnalysisDB.Model;

public class MetamodelSeacher {

	private final FileSearcher searcher;
	private final Set<Path> validModels;

	// TODO: Possibly identify the builtinMetamodels in some shared class
	private Map<Metamodel, Set<String>> builtinMetamodelsFootprints = new HashMap<>();
	private Function<Path, Path> toProjectPathNormalizer;
	@CheckForNull
	private SearchCache cache;
	private AnalysisDB analysisDb;
	
	public MetamodelSeacher(FileSearcher searcher, AnalysisDB analysisDb, Function<Path, Path> toProjectPathNormalizer) {
		this.searcher = searcher;
		this.analysisDb = analysisDb;
		this.toProjectPathNormalizer = toProjectPathNormalizer;
		try {
			this.validModels = analysisDb.getValidModels(p -> p).stream().map(m -> m.getRelativePath()).collect(Collectors.toSet());
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		
		builtinMetamodelsFootprints.put(Metamodel.fromURI(EcorePackage.eINSTANCE.getName(), EcorePackage.eINSTANCE.getNsURI()), toClassNames(EcorePackage.eINSTANCE.eResource()));
		builtinMetamodelsFootprints.put(Metamodel.fromURI(UMLPackage.eINSTANCE.getName(), UMLPackage.eINSTANCE.getNsURI()), toClassNames(UMLPackage.eINSTANCE.eResource()));
	}

	public void setCache(SearchCache cache) {
		this.cache = cache;
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
				Preconditions.checkState(! path.isAbsolute());
				if (! validModels.contains(path))
					continue;
				
				Set<String> names = null;
				File f = searcher.getRepoRoot().resolve(path).toFile();
				if (cache != null) {
					names = cache.getClassNamesOf(f);
				}
				
				try {
					if (names == null) {
						names = toClassNames(path, f);
						if (cache != null)
							cache.putClassNames(f, names);
					}
				} catch (Throwable e) {
					// This may happen for files which doesn't validate (it is probably a bit better check the existence of this file in the db and check that it is valid
					continue;
				}		
				
				compareSimilarities(classFootprints, names, Metamodel.fromFile("recovered", new RecoveredPath(toProjectPathNormalizer.apply(f.toPath()))));
			}
			
			builtinMetamodelsFootprints.forEach((mm, names) -> {
				compareSimilarities(classFootprints, names, mm);		
			});
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}		
	}

	private <T> void compareSimilarities(Map<T, RecoveredMetamodelFile> classFootprints, Set<String> names, Metamodel m) {
		classFootprints.forEach((metamodel, recoveredMetamodel) -> {
			int coincidences = checkSimilarity(names, recoveredMetamodel.footprint);
			if (coincidences > 0 && coincidences > recoveredMetamodel.bestCoincidenceCount) {
				recoveredMetamodel.accuracy = (100.0 * coincidences) / recoveredMetamodel.footprint.size();
				recoveredMetamodel.bestCoincidenceCount = coincidences;
				recoveredMetamodel.bestMetamodel = m;
			}
		});
	}
	

	private int checkSimilarity(Set<String> names, Set<String> footprintNames) {
		int count = 0;
		for (String str : footprintNames) {
			if (names.contains(str))
				count++;
		}
		return count;
	}
	
	private Set<String> toClassNames(Path idPath, File f) throws IOException {
		Model model = analysisDb.getModelByPath(idPath.toString(), p -> p);
		if (model == null) {
			return FootprintComputation.INSTANCE_CROSS_REFS.toClassNames(f);
		}
		String footprint = model.getKeyValueMetadata("footprint");
		Set<String> set = new HashSet<>();
		Collections.addAll(set, footprint.split(","));
		return set;
	}

	private Set<String> toClassNames(Resource resource) {
		return FootprintComputation.INSTANCE_CROSS_REFS.toClassNames(resource);
	}
	
	
	public static class RecoveredMetamodelFile {
		private double accuracy;
		private Set<String> footprint = new HashSet<String>();
		private int bestCoincidenceCount = -1;
		private Metamodel bestMetamodel;
	
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
		
		public Metamodel getBestMetamodel() {
			return bestMetamodel;
		}
		
		public boolean isValid() {
			return bestMetamodel != null;
		}
	}
}
