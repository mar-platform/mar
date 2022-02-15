package mar.artefacts;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.m2m.internal.qvt.oml.QvtMessage;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;
import org.eclipse.m2m.internal.qvt.oml.cst.MappingModuleCS;
import org.eclipse.m2m.internal.qvt.oml.cst.ModelTypeCS;
import org.eclipse.m2m.internal.qvt.oml.cst.PackageRefCS;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;

import mar.analysis.ecore.EcoreRepository.EcoreModel;
import mar.artefacts.Transformation.Qvto;
import mar.artefacts.qvto.QvtoLoader;

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

		private final CompiledUnit unit;
		private final String fileName;

		public Qvto(@Nonnull String fileName, @Nonnull CompiledUnit unit) {
			this.fileName = fileName;
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
		
		public Set<String> getMetamodelURIs() {
			Set<String> result = new HashSet<>();
			UnitCS cs = getUnit().getUnitCST();
			if (cs.getTopLevelElements().isEmpty()) {
				System.out.println("No meta-models: " + fileName);
				return result;
			}
			MappingModuleCS x = (MappingModuleCS) cs.getTopLevelElements().get(0);
			for (ModelTypeCS modelTypeCS : x.getMetamodels()) {
				for (PackageRefCS packageRefCS : modelTypeCS.getPackageRefs()) {
					if (packageRefCS.getUriCS() == null) {
						System.out.println("No URI: " + fileName);
						System.out.println(packageRefCS.getPathNameCS());
						// TODO: Find out how to handle this
						continue;
					}
					String uri = packageRefCS.getUriCS().getStringSymbol();
					result.add(uri);
				}
			}			
			return result;
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
