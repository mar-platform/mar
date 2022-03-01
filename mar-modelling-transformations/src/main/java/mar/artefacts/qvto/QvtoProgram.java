package mar.artefacts.qvto;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class QvtoProgram extends FileProgram {

	public QvtoProgram(@Nonnull RecoveredPath path) {
		super(path);
	}

	@Override
	public String getKind() {
		return "qvto";
	}
	
	@Override
	public String getCategory() {
		return "transformation";
	}
}