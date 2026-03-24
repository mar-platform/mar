import type Project from "./Project";

export interface Node {
    _type: 'artefact' | 'virtual';
    id: string;
    artefact: {
        id: string;
        category: string;
        fileStatus: 'EXISTS' | 'MISSING' | 'UNRESOLVED' | 'GENERATED' | 'HEURISTIC' | 'BUILTIN' | 'UNEXPECTED' | 'ERROR';
        name: string;
        project: Project['id'];
        type: string;
    };
    attributes: string[];
}

export interface Edge {
    source: string;
    target: string;
    types: ('typed-by' | 'import' | 'duplicate' | 'build_duplicate' | 'project-to-project' | 'input-type' | 'output-type')[];
}

export default interface Graph {
    edges: Edge[];
    nodes: Node[];
}
