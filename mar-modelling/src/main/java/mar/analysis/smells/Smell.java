package mar.analysis.smells;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;

import com.google.common.base.Preconditions;

/**
 * An smell that has been detected.
 * 
 * @author jesus
 *
 */
public class Smell {

	private final EObject[] smellyObjects;
	private final String smellId;
	
	public Smell(@Nonnull SmellDetector detector, EObject... smellyObjects) {
		Preconditions.checkArgument(smellyObjects.length >= 1);
		this.smellId = detector.getSmellId();
		this.smellyObjects = Arrays.copyOf(smellyObjects, smellyObjects.length);		
	}
	
	@Nonnull
	public String getSmellId() {
		return smellId;
	}
	
	@Nonnull
	public List<String> getSmellyObjectURIs() {
		List<String> result = new ArrayList<String>(smellyObjects.length);
		for (EObject eObject : smellyObjects) {
			result.add(EcoreUtil.getURI(eObject).fragment());
		}
		return result;
	}
	
	@Nonnull
	protected EObject[] getSmellyObjects_() {
		return smellyObjects;
	}
	
	@SuppressWarnings("unchecked")
	protected <T extends EObject>  T getSmellyObject(int idx, Class<T> klazz) {
		return (T) smellyObjects[idx];
	}

	@Nonnull
	protected List<EObject> getSmellyObjects() {
		return Arrays.asList(smellyObjects);
	}

}
