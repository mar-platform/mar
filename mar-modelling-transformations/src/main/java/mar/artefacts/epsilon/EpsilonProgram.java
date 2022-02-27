package mar.artefacts.epsilon;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class EpsilonProgram extends FileProgram {

	public EpsilonProgram(@Nonnull RecoveredPath path) {
		super(path);
	}
		
	public static enum Language {
		EOL,
		EGL,
		ETL,
		EVL
	}
	
}
