package mar.analysis.backend.megamodel.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import mar.analysis.backend.megamodel.ArtefactType;
import mar.analysis.backend.megamodel.MegamodelDB;
import mar.analysis.backend.megamodel.RawRepositoryDB;

/**
 * Computes megamodel statistics in-memory and returns DTOs suitable for JSON serialization.
 */
public class InMemoryResultAnalyser {

    // -------------------------------------------------------------------------
    // DTOs (Java records)
    // -------------------------------------------------------------------------

    public record ArtefactTypeStats(
            String type,
            String usage,
            long rawCount,
            double projectPercentage,
            int uniqueCount,
            double uniquePercentageMega,
            double uniquePercentageRaw) {}

    public record GraphStatsDTO(
            long totalArtefacts,
            long totalEdges,
            long totalIsolated,
            double percentIsolated,
            double avgOutDegree,
            double avgInDegree,
            List<Integer> connectedComponentSizes) {}

    public record ProjectStatsDTO(
            int totalProjects,
            int totalIsolated,
            double percentIsolated,
            long totalEdges,
            double avgDegree,
            List<Integer> connectedComponentSizes) {}

    public record MegamodelAnalysisStats(
            List<ArtefactTypeStats> artefactStats,
            long totalRawArtefacts,
            GraphStatsDTO graphStats,
            ProjectStatsDTO projectStats) {}

    // -------------------------------------------------------------------------
    // Analysis
    // -------------------------------------------------------------------------

    private final MegamodelDB megamodelDb;
    private final RawRepositoryDB rawDb;
    private final Set<String> artefactTypes;

    public InMemoryResultAnalyser(MegamodelDB megamodelDb, RawRepositoryDB rawDb, Set<String> artefactTypes) {
        this.megamodelDb = megamodelDb;
        this.rawDb = rawDb;
        this.artefactTypes = artefactTypes;
    }

    public MegamodelAnalysisStats compute() {
        GraphStats graphStats = new GraphStats(megamodelDb, rawDb, artefactTypes);
        DuplicationStats duplicationStats = new DuplicationStats(megamodelDb, rawDb);
        ProjectStats projectStats = new ProjectStats(megamodelDb, rawDb);
        RawRepositoryStats rawStats = rawDb.getStats();

        List<ArtefactTypeStats> artefactStatsList = buildArtefactStats(rawStats, projectStats, duplicationStats);

        long totalRawArtefacts = artefactStatsList.stream().mapToLong(ArtefactTypeStats::rawCount).sum();

        GraphStatsDTO graphDTO = new GraphStatsDTO(
                graphStats.getTotalArtefacts(),
                graphStats.getTotalEdges(),
                graphStats.getTotalIsolated(),
                graphStats.getPercentIsolated(),
                graphStats.getAvgOutDegree(),
                graphStats.getAvgInDegree(),
                graphStats.getComponentSizes());

        ProjectStatsDTO projectDTO = new ProjectStatsDTO(
                projectStats.getTotalProjects(),
                projectStats.getTotalIsolated(),
                projectStats.getPercentIsolated(),
                projectStats.getTotalEdges(),
                projectStats.getAvgDegree(),
                projectStats.getComponentSizes());

        return new MegamodelAnalysisStats(artefactStatsList, totalRawArtefacts, graphDTO, projectDTO);
    }

    private List<ArtefactTypeStats> buildArtefactStats(RawRepositoryStats rawStats,
            ProjectStats projectStats, DuplicationStats duplicationStats) {
        List<ArtefactTypeStats> result = new ArrayList<>();
        List<ArtefactType> types = Stream.of(ArtefactType.values())
                .sorted((a1, a2) -> a1.name().compareTo(a2.name()))
                .collect(Collectors.toList());

        for (ArtefactType artefactType : types) {
            String type = artefactType.id;
            String usage = toUsage(artefactType);
            if (usage == null)
                continue;

            long rawCount = rawStats.getCount(type);
            double projectPercentage = projectStats.getProjectPercentage(type);
            int uniqueCount = duplicationStats.getUnique(type);
            double uniquePercentageMega = duplicationStats.getUniquePercentageMega(type);
            double uniquePercentageRaw = duplicationStats.getUniquePercentageRaw(type);

            result.add(new ArtefactTypeStats(type, usage, rawCount, projectPercentage,
                    uniqueCount, uniquePercentageMega, uniquePercentageRaw));
        }
        return result;
    }

    private static String toUsage(ArtefactType artefactType) {
        switch (artefactType) {
            case ACCELEO:  return "Codegen";
            case ATL:      return "Transformation";
            case ECORE:    return "Metamodel";
            case EMFATIC:  return "Metamodel";
            case EMFTEXT:  return "Syntax";
            case EPSILON:  return "Transformation";
            case GMF:      return "Syntax";
            case HENSHIN:  return "Transformation";
            case OCL:      return "Validation";
            case QVTO:     return "Transformation";
            case SIRIUS:   return "Syntax";
            case XTEXT:    return "Syntax";
            default:       return null;
        }
    }
}
