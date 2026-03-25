import type { EdgeType } from "$lib/dto/Graph";

export const edgeTypes: Record<EdgeType, { label: string; color: string }> = {
	'typed-by': { label: 'Typed by', color: '--color-typed-by' },
	'import': { label: 'Import', color: '--color-import' },
	'duplicate': { label: 'Duplicate', color: '--color-duplicate' },
	'build_duplicate': { label: 'Build duplicate', color: '--color-build-duplicate' },
	'project-to-project': { label: 'Project to project', color: '--color-project-to-project' },
	'input-type': { label: 'Input type', color: '--color-input-type' },
	'output-type': { label: 'Output type', color: '--color-output-type' }
};
