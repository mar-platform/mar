package mar.artefacts;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import mar.artefacts.utils.AntUtils;

@VisibleForTesting
public class Metamodel {

	@Nonnull
	private final String name;
	@CheckForNull
	private String uri;
	@CheckForNull
	private RecoveredPath file;
	@Nonnull
	private Set<Metamodel> dependents = new HashSet<>();
	
	public Metamodel(@Nonnull String  name) {
		this.name = name;
	}

	public void addDependent(Metamodel dependent) {
		this.dependents.add(dependent);
	}
	
	public Set<? extends Metamodel> getDependents() {
		return dependents;
	}

	public void setURI(String uri) {
		Preconditions.checkState(file == null);
		this.uri = uri;
	}

	public void setPath(RecoveredPath file) {
		this.file = file;
	}
	
	@CheckForNull
	public RecoveredPath getPath() {
		return this.file;
	}
	
	public String getUri() {
		return uri;
	}	
	
}