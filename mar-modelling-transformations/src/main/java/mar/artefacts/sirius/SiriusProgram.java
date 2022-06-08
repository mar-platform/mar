package mar.artefacts.sirius;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class SiriusProgram extends FileProgram {

	public SiriusProgram(RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "sirius";
	}

	@Override
	public String getCategory() {
		return "graphical-syntax";
	}

}
