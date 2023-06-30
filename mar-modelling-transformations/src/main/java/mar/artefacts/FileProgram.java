package mar.artefacts;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;

import mar.analysis.megamodel.model.RelationshipsGraph;
import mar.analysis.megamodel.model.RelationshipsGraph.Attribute;
import mar.analysis.megamodel.model.RelationshipsGraph.IsInBuildFolderAttribute;

public abstract class FileProgram {

	@Nonnull
	private final RecoveredPath path;

	@Nonnull
	private final List<MetamodelReference> metamodels = new ArrayList<>();
	
	private final List<Path> importedPrograms = new ArrayList<>();
	
	public FileProgram(@Nonnull RecoveredPath path) {
		this.path = path;
	}

	public RecoveredPath getFilePath() {
		return path;
	}

	public Path getProjectPath() {
		return path.getPath().subpath(0, 2);
	}

	
	public void addMetamodel(@Nonnull Metamodel metamodel, MetamodelReference.Kind... kind) {
		this.metamodels.add(new MetamodelReference(metamodel, kind));
	}
	
	public Collection<? extends MetamodelReference> getMetamodels() {
		return metamodels;
	}

	public void addImportDependency(Path path) {
		importedPrograms.add(path);
	}
	
	public List<? extends Path> getImportedPrograms() {
		return importedPrograms;
	}

	public abstract String getKind();

	public abstract String getCategory();

	public String getLanguage() {
		return getKind();
	}

	// This is a bit weird because it references something of the RelationshipsGraph, but it is convenient and straightforward...
	private List<RelationshipsGraph.Attribute> attributes = new ArrayList<>();
	
	public void addAttribute(RelationshipsGraph.Attribute attr) {
		this.attributes.add(attr);
	}
	
	public List<RelationshipsGraph.Attribute> getAttributes() {
		return attributes;
	}

	public boolean hasAttribute(Attribute attribute) {
		return this.attributes.contains(attribute);
	}

}
