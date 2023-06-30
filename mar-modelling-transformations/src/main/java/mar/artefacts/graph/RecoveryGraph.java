package mar.artefacts.graph;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.base.Preconditions;

import mar.analysis.megamodel.model.Project;
import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;
import mar.artefacts.MetamodelReference;
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

	/**
	 * Check validity constraints of the mini-graph.
	 * Fails at runtime if not satisfied.
	 */
	public void assertValid() {
		// Metamodels used by the programs are registered in the graph
		// because the graph is concerned only with metamodels in the same scope (project)
		for (FileProgram program : programs) {
			for (MetamodelReference mref : program.getMetamodels()) {
				Preconditions.checkState(metamodels.contains(mref.getMetamodel()));
			}
		}
	}
	
}
