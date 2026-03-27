package mar.analysis.backend.megamodel.stats;

import java.io.PrintStream;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.RawRepositoryDB;

public class LatexTables {

	public static void toArtefactTable(PrintStream out, RawRepositoryStats raw, ProjectStats project, DuplicationStats duplication) {
		//     \multirow{1}{*}{acceleo}      & Transformation   &  17,664 & 8.93\%   & -             \\ \midrule
	    // \multirow{1}{*}{atl}     	   & Transformation  &   9,850 & 7.25\%   & 2,463 (26\%)  \\ \midrule
	    //\multirow{1}{*}{ecore}	   & Metamodel & 199,025 & 93.21\%        & 31,968 (16\%)  \\ \midrule

		List<ArtefactType> types = Stream.of(ArtefactType.values()).sorted((a1, a2) -> a1.name().compareTo(a2.name())).collect(Collectors.toList());
		int totalArtefacts = 0;
		for (ArtefactType artefactType : types) {
			String type = artefactType.id;
			String usage = toUsage(artefactType);
			if (usage == null) {
				continue;
			}
			
			long totalOfType = raw.getCount(type);
			double projectPercentage = project.getProjectPercentage(type);
			totalArtefacts += totalOfType;
			
			out.print(String.format("\\multirow{1}{*}{%s} & ", type));
			out.print(String.format("%s & ", usage));
			out.print(String.format(Locale.US, "%,d & ", totalOfType));
			out.print(String.format("%.2f", projectPercentage) + "\\% & ");
			out.print(String.format(Locale.US, "%,d (%.2f", duplication.getUnique(type), duplication.getUniquePercentageMega(type)) + "\\%) \\\\ \\midrule");			
			out.println();
		}

		out.print(String.format("\\multirow{1}{*}{} & "));
		out.print(String.format("{\\bf Total artefacts } & "));
		out.print(String.format(Locale.US, "%,d & ", totalArtefacts));
		out.print("& \\\\ \\midrule\\midrule");
		out.println();
	}

	private static String toUsage(ArtefactType artefactType) {
		switch(artefactType) {
		case ACCELEO: return "Codegen";
		case ATL: return "Transformation";
		case ECORE: return "Metamodel";
		case EMFATIC: return "Metamodel";
		case EMFTEXT: return "Syntax";
		case EPSILON: return "Transformation";
		case GMF: return "Syntax";
		case HENSHIN: return "Transformation";
		case OCL: return "Validation";
		case QVTO: return "Tranformation";
		case SIRIUS: return "Syntax";
		case XTEXT: return "Syntax";
		default: return null;
		}
	}

	
	
}
