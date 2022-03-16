package mar.artefacts.atl;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class ATLProgram extends FileProgram {

	public ATLProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "atl";
	}
	
	@Override
	public String getCategory() {
		return "transformation";
	}
}
