package mar.analysis.backend.megamodel.inspectors;

import mar.artefacts.FileProgram;

public abstract class InspectionErrorException extends Exception {

	private static final long serialVersionUID = -2366721485183025547L;
	
	private final FileProgram program;

	public InspectionErrorException(FileProgram program) {
		this.program = program;
	}
	
	public FileProgram getProgram() {
		return program;
	}
	
	public static class SyntaxError extends InspectionErrorException {
		private static final long serialVersionUID = -2344827262848185670L;

		public SyntaxError(FileProgram program) {
			super(program);
		}

	}
}
