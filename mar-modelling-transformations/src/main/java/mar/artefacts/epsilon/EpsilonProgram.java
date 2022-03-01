package mar.artefacts.epsilon;

import javax.annotation.Nonnull;

import com.google.common.io.Files;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class EpsilonProgram extends FileProgram {

	private Language language = Language.EOL;
	
	public EpsilonProgram(@Nonnull RecoveredPath path) {
		super(path);
		switch (Files.getFileExtension(path.getPath().toString())) {
		case "eol":
			language = Language.EOL;
			break;
		case "egl":
		case "egx":
			language = Language.EGL;
		case "etl":
			language = Language.ETL;
		case "evl":
			language = Language.EVL;					
		default:
			break;
		}
	}
		
	public static enum Language {
		EOL,
		EGL,
		ETL,
		EVL
	}
	
	@Override
	public String getKind() {
		return language.name().toLowerCase();
	}
	
	@Override
	public String getCategory() {	
		switch (language) {
		case EOL: return "transformation";
		case EGL: return "generator";
		case ETL: return "transformation";
		case EVL: return "validation";
		}
		throw new IllegalStateException();
	}
}
