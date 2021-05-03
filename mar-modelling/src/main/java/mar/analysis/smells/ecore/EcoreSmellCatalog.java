package mar.analysis.smells.ecore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;

public class EcoreSmellCatalog {

	public static final EcoreSmellCatalog INSTANCE = new EcoreSmellCatalog();
	
	public EcoreSmellCatalog() {
    	
	}

	// We allocate always to allow detector have state
	private List<EcoreSmellDetector> getSmellDetectors() {
    	UninstantiableClassSmellDetector detectorUC = new UninstantiableClassSmellDetector();
    	IsolatedClassSmellDetector detectorIC = new IsolatedClassSmellDetector();
    	OverLoadedClassSmellDetector detectorOL = new OverLoadedClassSmellDetector();
    	ReferredAlotClassSmellDetector detectorReferred = new ReferredAlotClassSmellDetector();
    	RefersAlotClassSmellDetector detectorRefers = new RefersAlotClassSmellDetector();
    	DepthHierarchySmellDetector detectorHier = new DepthHierarchySmellDetector();
    	OnlyOneClassSuperSmellDetector detectorOnly = new OnlyOneClassSuperSmellDetector();
    	TooManyChildrenSmellDetector detectorToo = new TooManyChildrenSmellDetector();
    	TooLongNamesSmellDetector detectorLong = new TooLongNamesSmellDetector();
    	IrrelevantClassSmellDetector detectorIrr = new IrrelevantClassSmellDetector();
    	return Arrays.asList(detectorUC, detectorIC, detectorOL, 
    			detectorReferred, detectorRefers, detectorHier, 
    			detectorOnly, detectorToo, detectorLong, detectorIrr);
	}
	
	@Nonnull
	public Map<String, List<Smell>> detectSmells(Resource r) {
		Model m = new Model(r);
		Map<String, List<Smell>> result = new HashMap<>();
		List<EcoreSmellDetector> detectors = getSmellDetectors();
		for (EcoreSmellDetector detector : detectors) {
			List<Smell> smells = detector.detect(m);
			if (smells.size() > 0) {
				result.put(detector.getSmellId(), smells);
			}
		}
		return result;
	}
	
}
