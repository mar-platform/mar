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
	private Set<Metamodel> subpackages = new HashSet<>();
	
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

	public void addSubpackage(Metamodel mm) {
		this.subpackages.add(mm);
	}	

	public Set<? extends Metamodel> getSubpackages() {
		return subpackages;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file == null) ? 0 : file.hashCode());
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	public String getName() {
		return name;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Metamodel other = (Metamodel) obj;
		if (file == null) {
			if (other.file != null)
				return false;
		} else if (!file.equals(other.file))
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}
	
	
	
}