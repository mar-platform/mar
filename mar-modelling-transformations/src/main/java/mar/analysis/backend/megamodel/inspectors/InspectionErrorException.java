package mar.analysis.backend.megamodel.inspectors;

import java.nio.file.Path;

import mar.artefacts.FileProgram;

public abstract class InspectionErrorException extends Exception {

	private static final long serialVersionUID = -2366721485183025547L;
	
	private final Path path;

	public InspectionErrorException(FileProgram program) {
		this.path = program.getFilePath().getPath();
	}

	public InspectionErrorException(Path path) {
		this.path = path;
	}
	
	public Path getProgramPath() {
		return path;
	}
	
	public static class SyntaxError extends InspectionErrorException {
		private static final long serialVersionUID = -2344827262848185670L;

		public SyntaxError(FileProgram program) {
			super(program);
		}

	}
	
	public static class EmptyFile extends InspectionErrorException {
		private static final long serialVersionUID = -2344827262848185670L;

		public EmptyFile(Path path) {
			super(path);
		}

	}
	
	public static class IOError extends InspectionErrorException {	

		private static final long serialVersionUID = 5629987312175667385L;

		public IOError(Path path) {
			super(path);
		}

	}
}
