package mar.analysis.ecore;

import java.util.Map;

import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.common.util.DiagnosticChain;
import org.eclipse.emf.common.util.UniqueEList;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreValidator;

/**
 * validateEPackage_UniqueNsURIs
 * 
 * @author jesus
 *
 */
public class CustomEcoreValidator extends EcoreValidator {

	@Override
	public boolean validateEPackage_UniqueNsURIs(EPackage ePackage, DiagnosticChain diagnostics,
			Map<Object, Object> context) {
		boolean result = true;
		String nsURI = ePackage.getNsURI();
		if (nsURI != null) {
			EPackage rootEPackage = ePackage;
			for (EPackage eSuperPackage = ePackage
					.getESuperPackage(); eSuperPackage != null; eSuperPackage = eSuperPackage.getESuperPackage()) {
				// This may happen for some ill-formed models
				if (rootEPackage == eSuperPackage)
					return false;
				rootEPackage = eSuperPackage;
			}

			UniqueEList<EPackage> ePackages = new UniqueEList.FastCompare<EPackage>();
			ePackages.add(rootEPackage);
			for (int i = 0; i < ePackages.size(); ++i) {
				ePackages.addAll(ePackages.get(i).getESubpackages());
			}
			ePackages.remove(ePackage);

			for (EPackage otherEPackage : ePackages) {
				if (nsURI.equals(otherEPackage.getNsURI())) {
					if (diagnostics == null) {
						return false;
					} else {
						result = false;
						diagnostics.add(createDiagnostic(Diagnostic.ERROR, DIAGNOSTIC_SOURCE, UNIQUE_NS_URIS,
								"_UI_EPackageUniqueNsURIs_diagnostic", new Object[] { nsURI },
								new Object[] { ePackage, otherEPackage, EcorePackage.Literals.EPACKAGE__ESUBPACKAGES },
								context));
					}
				}
			}
		}
		return result;
	}
}
