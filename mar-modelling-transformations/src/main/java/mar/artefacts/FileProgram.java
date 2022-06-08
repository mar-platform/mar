package mar.artefacts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

public abstract class FileProgram {

	@Nonnull
	private final RecoveredPath path;

	@Nonnull
	private final List<MetamodelReference> metamodels = new ArrayList<>();
	
	public FileProgram(@Nonnull RecoveredPath path) {
		this.path = path;
	}

	public RecoveredPath getFilePath() {
		return path;
	}
	
	public void addMetamodel(@Nonnull Metamodel metamodel, MetamodelReference.Kind... kind) {
		this.metamodels.add(new MetamodelReference(metamodel, kind));
	}
	
	public Collection<? extends MetamodelReference> getMetamodels() {
		return metamodels;
	}
	
	public abstract String getKind();

	public abstract String getCategory();

	public String getLanguage() {
		return getKind();
	}
}
