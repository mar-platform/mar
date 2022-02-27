package mar.artefacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class FileProgram {

	@Nonnull
	private final RecoveredPath path;

	@Nonnull
	private final List<Metamodel> metamodels = new ArrayList<>();
	
	public FileProgram(@Nonnull RecoveredPath path) {
		this.path = path;
	}

	public RecoveredPath getFilePath() {
		return path;
	}
	
	public void addMetamodel(@Nonnull Metamodel metamodel) {
		this.metamodels.add(metamodel);
	}
	
	public Collection<? extends Metamodel> getMetamodels() {
		return metamodels;
	}
	
}
