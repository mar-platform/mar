package mar.artefacts;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

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
	
	private Metamodel(@Nonnull String  name) {
		this.name = name;
	}
	
	@Nonnull
	public static Metamodel fromURI(@Nonnull String name, @Nonnull String uri) {
		Metamodel mm = new Metamodel(name);
		mm.setURI(uri);
		return mm;
	}

	@Nonnull
	public static Metamodel fromFile(@Nonnull String name, @Nonnull RecoveredPath file) {
		Metamodel mm = new Metamodel(name);
		mm.setPath(file);
		return mm;
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