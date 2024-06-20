package mar.artefacts;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.eclipse.m2m.internal.qvt.oml.QvtMessage;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;
import org.eclipse.m2m.internal.qvt.oml.cst.MappingModuleCS;
import org.eclipse.m2m.internal.qvt.oml.cst.ModelTypeCS;
import org.eclipse.m2m.internal.qvt.oml.cst.PackageRefCS;
import org.eclipse.m2m.internal.qvt.oml.cst.ParameterDeclarationCS;
import org.eclipse.m2m.internal.qvt.oml.cst.TypeSpecCS;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;
import org.eclipse.ocl.cst.PathNameCS;
import org.eclipse.ocl.cst.TypeCS;

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
			return getModelParameters().stream().map(TransformationParameter::getUri).collect(Collectors.toSet());
		}
		
		public Set<TransformationParameter> getModelParameters() {
			UnitCS cs = getUnit().getUnitCST();
			return getModelParameters(cs, this.fileName);
		}
		
		public static Set<TransformationParameter> getModelParameters(UnitCS cs, String fileName) {
			Set<TransformationParameter> result = new HashSet<>();
			if (cs.getTopLevelElements().isEmpty()) {
				System.out.println("No meta-models: " + fileName);
				return result;
			}
			MappingModuleCS x = (MappingModuleCS) cs.getTopLevelElements().get(0);
			for (ModelTypeCS modelTypeCS : x.getMetamodels()) {
				String modelId = modelTypeCS.getIdentifierCS().getValue();
				for (PackageRefCS packageRefCS : modelTypeCS.getPackageRefs()) {
					if (packageRefCS.getUriCS() == null) {
						System.out.println("No URI: " + fileName);
						System.out.println(packageRefCS.getPathNameCS());
						// TODO: Find out how to handle this
						continue;
					}
					
					String uri = packageRefCS.getUriCS().getStringSymbol();
					
					boolean isIn = false;
					boolean isOut = false;
					for (ParameterDeclarationCS p : x.getHeaderCS().getParameters()) {
						TypeCS type = p.getTypeSpecCS().getTypeCS();
						if (! (type instanceof PathNameCS))
							continue;
						PathNameCS path = (PathNameCS) type;
						Set<String> names = path.getSimpleNames().stream().map(s -> s.getValue()).collect(Collectors.toSet());
						if (names.contains(modelId)) {
							switch(p.getDirectionKind()) {
							case DEFAULT:
							case IN:
								isIn = true;
								break;
							case OUT:
								isOut = true;
								break;
							case INOUT:
								isIn = isOut = true;
								break;
							}
						}						
					}
					
					
					if (!isIn && !isOut) {
						// We couldn't find a parameter. Assume it is in.
						isIn = true;
					}
					
					result.add(new TransformationParameter(uri, isIn, isOut));
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

	public static class TransformationParameter {
		private String uri;
		private boolean isIn;
		private boolean isOut;

		public TransformationParameter(String uri, boolean isIn, boolean isOut) {
			this.uri = uri;
			this.isIn = isIn;
			this.isOut = isOut;
		}
		
		public boolean isIn() {
			return isIn;
		}
		
		public boolean isOut() {
			return isOut;
		}
		
		public String getUri() {
			return uri;
		}
	}
	
	public boolean  hasErrors();
		
	void addMetamodels(@Nonnull Collection<EcoreModel> metamodels);

	@Nonnull
	Set<? extends EcoreModel> getMetamodels();
	
	
	
}
