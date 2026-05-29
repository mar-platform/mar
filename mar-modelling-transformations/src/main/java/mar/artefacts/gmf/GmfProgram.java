package mar.artefacts.gmf;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class GmfProgram extends FileProgram {

	public GmfProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "gmf";
	}
	
	@Override
	public String getCategory() {
		return "graphical-syntax";
	}
}
