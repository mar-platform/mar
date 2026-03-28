export default interface Stats {
    raw: {
        artefactTypeCount: Record<string, number>;
    },
    mega: {
        artefactTypeCount: Record<string, number>;
    },
    totalRaw: number;
    totalMega: number;
    totalCompletion: number;
    artefactRecoveryCompletion: Record<string, number>;
}
