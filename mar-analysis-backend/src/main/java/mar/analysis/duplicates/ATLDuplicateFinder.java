package mar.analysis.duplicates;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.ecore.EObject;

import anatlyzer.atl.model.ATLModel;
import anatlyzer.atl.util.ATLUtils;
import anatlyzer.atl.util.ATLUtils.ModelInfo;
import anatlyzer.atlext.ATL.Helper;
import anatlyzer.atlext.OCL.OclModelElement;
import anatlyzer.atlext.OCL.VariableDeclaration;

/**
 * For transformations we may need to add an additional constraint:
 * 
 *   - The input/output meta-models of the transformation also needs to be
 *     duplicates of the meta-models of a candidate duplicate transformation.
 *      
 * @author jesus
 *
 */
public class ATLDuplicateFinder extends DuplicateFinder<ATLModel> {

	public ATLDuplicateFinder() {
		super(new ATLTokenExtractor());
	}

	private static class ATLTokenExtractor implements ITokenExtractor<ATLModel> {

		@Override
		public List<String> extract(ATLModel resource) {
			List<String> tokens = new ArrayList<>();
			TreeIterator<EObject> it = resource.getResource().getAllContents();
			while (it.hasNext()) {
				EObject obj = it.next();
				if (obj instanceof VariableDeclaration) {
					addToken(tokens, (((VariableDeclaration) obj).getVarName()));
				} else if (obj instanceof Helper) {
					Helper h = (Helper) obj;
					try {
						// It seems that a helper may have either getDefinition() or getOperationName(), etc. null
						addToken(tokens, (ATLUtils.getHelperName(h)));
					} catch (NullPointerException e) {
						System.out.println(h);
					}
				} else if (obj instanceof OclModelElement) {
					OclModelElement me = (OclModelElement) obj;
					addToken(tokens, (me.getName()));
				} else if (obj instanceof anatlyzer.atlext.ATL.Module) {
					addToken(tokens, (((anatlyzer.atlext.ATL.Module) obj).getName()));
					((anatlyzer.atlext.ATL.Module) obj).getInModels().forEach(m -> addToken(tokens, (m.getName())));
					((anatlyzer.atlext.ATL.Module) obj).getOutModels().forEach(m -> addToken(tokens, (m.getName())));
				}
			}
			
			for (ModelInfo info : ATLUtils.getModelInfo(resource)) {
				addToken(tokens, (info.getURIorPath()));
			}
			
			return tokens;
		}

		private void addToken(List<String> tokens, String string) {
			if (string != null)
				tokens.add(string);
		}
		
	}
}
