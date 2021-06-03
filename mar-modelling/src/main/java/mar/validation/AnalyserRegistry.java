package mar.validation;

import java.util.function.BiConsumer;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import mar.validation.bpmn.SingleGenMyModelBPMNValidator;

public class AnalyserRegistry {

	public final static AnalyserRegistry INSTANCE = new AnalyserRegistry();
	
	private ImmutableMap<String, ResourceAnalyser.Factory> analysers = new ImmutableMap.Builder<String, ResourceAnalyser.Factory>().
			put(SingleGenMyModelBPMNValidator.ID, new SingleGenMyModelBPMNValidator.Factory()).
			put(mar.analysis.ecore.SingleEcoreFileAnalyser.ID, new mar.analysis.ecore.SingleEcoreFileAnalyser.Factory()).
			put(mar.analysis.uml.UMLAnalyser.ID, new mar.analysis.uml.UMLAnalyser.Factory()).
			put(mar.modelling.xmi.XMIAnalyser.ID, new mar.modelling.xmi.XMIAnalyser.Factory()).
			put(mar.analysis.pnml.PnmlAnalyser.ID, new mar.analysis.pnml.PnmlAnalyser.Factory()).
			put(mar.analysis.sculptor.SculptorAnalyser.ID, new mar.analysis.sculptor.SculptorAnalyser.Factory()).
			put(mar.analysis.archimate.ArchimateAnalyser.ID, new mar.analysis.archimate.ArchimateAnalyser.Factory()).
			put(mar.analysis.lilypond.LilypondAnalyser.ID, new mar.analysis.lilypond.LilypondAnalyser.Factory()).
			put(mar.analysis.simulink.SimulinkAnalyser.ID, new mar.analysis.simulink.SimulinkAnalyser.Factory()).
			put(mar.analysis.xtext.XtextAnalyser.ID, new mar.analysis.xtext.XtextAnalyser.Factory()).
			put(mar.analysis.rds.RdsAnalyser.ID, new mar.analysis.rds.RdsAnalyser.Factory()).
			build();

	private AnalyserRegistry() { }
	
	@CheckForNull
	public ResourceAnalyser.Factory getFactory(@Nonnull String name) {
		return analysers.get(name);
	}

	public void forEach(@Nonnull BiConsumer<String, ResourceAnalyser.Factory> consumer) {
		analysers.forEach(consumer);
	}
	
	
}
