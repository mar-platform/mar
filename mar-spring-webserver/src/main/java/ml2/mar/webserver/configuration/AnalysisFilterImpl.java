package ml2.mar.webserver.configuration;

import java.util.Collections;
import java.util.List;

import mar.analysis.backend.megamodel.TransformationRelationshipsAnalysis.Filter;

public class AnalysisFilterImpl implements Filter {

	private AnalysisConfiguration configuration;
	private List<? extends String> discardedProjects;
	
	public AnalysisFilterImpl(AnalysisConfiguration configuration) {
		this.configuration = configuration;
		if (this.configuration.projects() != null) {
			this.discardedProjects = this.configuration.projects().stream().map(p -> p.name()).toList();
		} else {
			this.discardedProjects = Collections.emptyList();
		}
	}

	@Override
	public boolean isAccepted(String id) {
		for (String path : discardedProjects) {
			if (id.startsWith(path)) {
				return false;
			}
		}
		return true;
	}

}
