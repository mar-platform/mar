package mar.artefacts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.m2m.internal.qvt.oml.QvtMessage;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;

import mar.analysis.ecore.EcoreRepository.EcoreModel;

public interface Transformation {

	public static abstract class AbstractTransformation implements Transformation {
		
		private Set<EcoreModel> metamodels = new HashSet<>();
		
		@Override
		public void addMetamodels(@Nonnull Collection<EcoreModel> metamodels) {
			this.metamodels.addAll(metamodels);
		}
		
		@Override
		public Set<? extends EcoreModel> getMetamodels() {
			return metamodels;
		}
		
	}
	
	public static class Qvto extends AbstractTransformation {

		private CompiledUnit unit;

		public Qvto(@Nonnull CompiledUnit unit) {
			this.unit = unit;
		}
		
		@Override
		public boolean hasErrors() {
			return ! unit.getErrors().isEmpty();
		}
		
		@Nonnull
		public CompiledUnit getUnit() {
			return unit;
		}

		@Nonnull
		public Collection<String> getMissingMetamodels() {
			Set<String> result = new HashSet<>();
			final String METAMODEL_FAIL = "Failed to resolve metamodel";
			for(QvtMessage e : unit.getErrors()) {
				String msg = e.getMessage();
				int idx = msg.indexOf(METAMODEL_FAIL);
				if (idx != -1) {
					String name = msg.substring(METAMODEL_FAIL.length() + idx + 1);
					if (name.startsWith("'") && name.endsWith("'")) {
						String uri = name.substring(idx + 1, name.length() - 1);
						result.add(uri);
					}
				}
			}
			
			return result;
		}
		
	}

	public boolean  hasErrors();
		
	void addMetamodels(@Nonnull Collection<EcoreModel> metamodels);

	@Nonnull
	Set<? extends EcoreModel> getMetamodels();
	
	
	
}
