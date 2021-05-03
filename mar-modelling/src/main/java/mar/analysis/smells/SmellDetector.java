package mar.analysis.smells;

import java.util.List;

public abstract class SmellDetector {

	public abstract List<Smell> detect(Model m);
	
	public String getSmellId() {
		return this.getClass().getSimpleName().replaceFirst("Detector$", "");
	}
	
}
