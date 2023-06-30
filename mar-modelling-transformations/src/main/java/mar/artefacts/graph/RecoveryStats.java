package mar.artefacts.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;

/**
 * Gather information about which information is expected be recovered, and how
 * much of it have been recovered.
 * 
 * @author jesus
 */
public class RecoveryStats {
	
	public static class PerFile {
		private Path path;
		private String type;
		
		private int potentialPrograms;
		private int programs;
		
		public PerFile(Path path, String type) {
			this.path = path;
			this.type = type;
		}
		
		public void setPotentialPrograms(int n) {
			this.potentialPrograms = n;
		}
		
		public void addRecoveredProgram(FileProgram program) {
			this.programs += 1;
		}
		
		public Path getPath() {
			return path;
		}
		
		public String getType() {
			return type;
		}
		
		public int getPotentialPrograms() {
			return potentialPrograms;
		}
		
		public int getPrograms() {
			return programs;
		}
	}

	public static class Composite {
		
		@Nonnull
		private final List<PerFile> stats = new ArrayList<>();
		
		public void addStats(@Nonnull PerFile stats) {
			this.stats.add(stats);
		}

		public void detailedReport() {
			for (PerFile perFile : stats) {
				System.out.println(perFile.type + " " + perFile.path + " " + perFile.potentialPrograms + " " + perFile.programs);
			}
		}

		@Nonnull
		public List<? extends PerFile> getSingleStats() {
			return stats;
		}
	}
}
