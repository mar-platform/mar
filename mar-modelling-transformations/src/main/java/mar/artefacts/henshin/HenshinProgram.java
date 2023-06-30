package mar.artefacts.henshin;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class HenshinProgram extends FileProgram {

	public HenshinProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "henshin";
	}
	
	@Override
	public String getCategory() {
		return "transformation";
	}
}
