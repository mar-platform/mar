package mar.analysis.smells.ecore;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import mar.analysis.smells.Model;
import mar.analysis.smells.Smell;
import mar.analysis.smells.SmellDetector;

public abstract class EcoreSmellDetector extends SmellDetector {

	public List<Smell> detect(Resource res) {
		return detect(new Model(res));
	}

}
