package mar.artefacts.qvto;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;

import javax.annotation.Nonnull;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.m2m.internal.qvt.oml.InternalTransformationExecutor;
import org.eclipse.m2m.internal.qvt.oml.ast.env.QvtOperationalEnvFactory;
import org.eclipse.m2m.internal.qvt.oml.ast.env.QvtOperationalFileEnv;
import org.eclipse.m2m.internal.qvt.oml.ast.parser.QvtOperationalParser;
import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;
import org.eclipse.m2m.internal.qvt.oml.cst.UnitCS;
import org.eclipse.ocl.ParserException;

import mar.artefacts.Transformation;

public class QvtoLoader {

	public Transformation.Qvto load(@Nonnull String qvtoFile, Collection<EPackage> packages) {
		EPackage.Registry registry = getRegistry(packages);
		InternalTransformationExecutor exec = new InternalTransformationExecutor(URI.createFileURI(qvtoFile), registry);
		exec.loadTransformation(new NullProgressMonitor());
		CompiledUnit unit = exec.getUnit();
		return new Transformation.Qvto(qvtoFile, unit);
	}

	private EPackage.Registry getRegistry(Collection<EPackage> packages) {
		EPackageRegistryImpl registry = new EPackageRegistryImpl();
		packages.forEach(p -> registry.put(p.getNsURI(), p));
		return registry;
	}
	
	// From QVToCompiler#parse
	public UnitCS parse(String qvtoFile, Collection<EPackage> packages) throws IOException {
		EPackage.Registry ePackageRegistry = getRegistry(packages);

		Reader reader = null;
		UnitCS unitCS = null;
		File source = new File(qvtoFile);
		try {
			reader = new FileReader(source);

			QvtOperationalFileEnv env = new QvtOperationalEnvFactory(ePackageRegistry).createEnvironment(URI.createFileURI(qvtoFile));

			/*
			if(options.isEnableCSTModelToken()) {
				env.setOption(QVTParsingOptions.ENABLE_CSTMODEL_TOKENS, true);
			}
			*/

			QvtOperationalParser qvtParser = new QvtOperationalParser();
			unitCS = qvtParser.parse(reader, source.getName(), env);

			return unitCS;			
		} finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}		
	}
}
