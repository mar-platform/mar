import type Project from "./Project";

export type Node = ArtefactNode | VirtualNode;

export interface BaseNode {
    _type: 'artefact' | 'virtual' | 'duplication';
    id: string;
    attributes: string[];
}

export type ArtefactType = "qvto" | "ocl" | "ecore" | "xtext" | "emfatic" | "epsilon" | "acceleo" | "atl" | "sirius" | "henshin" | "gmf";

export interface ArtefactNode extends BaseNode {
    _type: 'artefact';
    artefact: {
        id: string;
        category: string;
        fileStatus: 'EXISTS' | 'MISSING' | 'UNRESOLVED' | 'GENERATED' | 'HEURISTIC' | 'BUILTIN' | 'UNEXPECTED' | 'ERROR';
        name: string;
        project: Project['id'];
        type: ArtefactType;
    };
}

export interface VirtualNode extends BaseNode {
    _type: 'virtual';
    kind: 'duplication' | 'project';
    artefactType?: ArtefactType;
    artefacts?: string[];
}

export type EdgeType = 'typed-by' | 'import' | 'duplicate' | 'build_duplicate' | 'project-to-project' | 'input-type' | 'output-type' | 'copy-from';

export interface Edge {
    source: string;
    target: string;
    types: EdgeType[];
}

export default interface Graph {
    edges: Edge[];
    nodes: Node[];
}
