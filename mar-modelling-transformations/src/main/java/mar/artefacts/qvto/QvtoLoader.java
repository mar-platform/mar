package mar.artefacts.qvto;

import java.util.Collection;
import java.util.Set;

import javax.annotation.Nonnull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.m2m.internal.qvt.oml.InternalTransformationExecutor;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;

import mar.artefacts.Transformation;

public class QvtoLoader {

	public Transformation.Qvto load(@Nonnull String qvtoFile, Collection<EPackage> packages) {
		EPackageRegistryImpl registry = new EPackageRegistryImpl();
		packages.forEach(p -> registry.put(p.getNsURI(), p));
		InternalTransformationExecutor exec = new InternalTransformationExecutor(URI.createFileURI(qvtoFile), registry);
		exec.loadTransformation(new NullProgressMonitor());
		CompiledUnit unit = exec.getUnit();
		return new Transformation.Qvto(unit);
	}
	
}
