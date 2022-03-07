package mar.artefacts.acceleo;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class AcceleoProgram extends FileProgram {

	public AcceleoProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "acceleo";
	}
	
	@Override
	public String getCategory() {
		return "generator";
	}
}
