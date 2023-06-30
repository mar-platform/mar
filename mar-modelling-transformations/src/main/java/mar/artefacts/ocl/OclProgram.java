package mar.artefacts.ocl;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class OclProgram extends FileProgram {

	public OclProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "ocl";
	}
	
	@Override
	public String getCategory() {
		return "validation";
	}
}
