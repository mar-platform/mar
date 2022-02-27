package mar.artefacts.graph;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import mar.artefacts.FileProgram;
import mar.artefacts.Metamodel;

public class RecoveryGraph {
	
	private Set<Metamodel> metamodels = new HashSet<>();
	private Set<FileProgram> programs = new HashSet<>();
	
	public void addMetamodel(@Nonnull Metamodel metamodel) {
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
