package mar.artefacts.epsilon;

import javax.annotation.Nonnull;

import com.google.common.io.Files;

import mar.artefacts.FileProgram;
import mar.artefacts.RecoveredPath;

public class EpsilonProgram extends FileProgram {

	private Language language = Language.OTHER;
	
	public EpsilonProgram(@Nonnull RecoveredPath path) {
		super(path);
		switch (Files.getFileExtension(path.getPath().toString())) {
		case "eol":
			language = Language.EOL;
			break;
		case "egl":
		case "egx":
			language = Language.EGL;
			break;
		case "etl":
			language = Language.ETL;
			break;
		case "evl":
			language = Language.EVL;
			break;
		case "epl":
			language = Language.EPL;
			break;
		case "emg":
			language = Language.EMG;
			break;
		case "ecl":
			language = Language.ECL;
			break;			
		case "eml":
			language = Language.EML;
			break;
		default:
			break;
		}
	}
		
	public static enum Language {
		EOL,
		EGL,
		ETL,
		EVL,
		EPL,
		EMG,
		ECL,
		EML,
		OTHER
	}
	
	@Override
	public String getKind() {
		return "epsilon";
	}
	
	@Override
	public String getLanguage() {
		return language.name().toLowerCase();
	}
	
	@Override
	public String getCategory() {	
		switch (language) {
		case OTHER:
		case EOL: return "transformation";
		case EGL: return "generator";
		case EML:
		case ECL:
		case EPL:
		case EMG:
		case ETL: return "transformation";
		case EVL: return "validation";
		}
		throw new IllegalStateException();
	}
}
