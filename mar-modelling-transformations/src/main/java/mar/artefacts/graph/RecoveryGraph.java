package mar.artefacts.graph;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.megamodel.model.Project;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.graph.RecoveryStats.PerFile;

public class RecoveryGraph {
	
	@CheckForNull
	private PerFile stats;
	
	private final Set<Metamodel> metamodels = new HashSet<>();
	private final Set<FileProgram> programs = new HashSet<>();

	private Project project;
	
	public RecoveryGraph(Project project, PerFile stats) {
		this(project);
		this.stats = stats;
	}
	
	public RecoveryGraph(Project project) {
		this.project = project;
	}
	
	@CheckForNull
	public PerFile getStats() {
		return stats;
	}
	
	public Project getProject() {
		return project;
	}
	
	public void addMetamodel(@Nonnull Metamodel metamodel) {
		Preconditions.checkNotNull(metamodel);
		this.metamodels.add(metamodel);
	}
	
	public void addProgram(@Nonnull FileProgram program) {
		this.programs.add(program);
	}

	public Set<? extends Metamodel> getMetamodels() {
		return metamodels;
	}
	
	public Set<? extends FileProgram> getPrograms() {
		return programs;
	}
	
}
