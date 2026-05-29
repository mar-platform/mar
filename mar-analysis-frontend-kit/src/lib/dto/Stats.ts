export default interface Stats {
    raw: {
        artefactTypeCount: Record<string, number>;
    },
    mega: {
        artefactTypeCount: Record<string, number>;
    },
    /** Ignore this total as it is not calculated correctly */
    totalRaw: number;
    totalMega: number;
    totalCompletion: number;
    artefactRecoveryCompletion: Record<string, number>;

    megamodelAnalysisStats: {
        /** Use this value as the total count of raw artefacts */
        totalRawArtefacts: number;
        artefactStats: ArtefactStats[];
        graphStats: MegamodelStats;
        projectStats: ProjectStats;
    },

}

export interface ArtefactStats {
    type: string;
    usage: string;
    rawCount: number;
    projectPercentage: number;
    uniqueCount: number;
    uniquePercentageMega: number;
    uniquePercentageRaw: number;
}

export interface MegamodelStats {
    totalArtefacts: number;
    totalEdges: number;
    totalIsolated: number;
    percentIsolated: number;
    avgOutDegree: number;
    avgInDegree: number;
    connectedComponentSizes: number[];
}

export interface ProjectStats {
    totalProjects: number;
    totalIsolated: number;
    percentIsolated: number;
    totalEdges: number;
    avgDegree: number;
    connectedComponentSizes: number[];
}
