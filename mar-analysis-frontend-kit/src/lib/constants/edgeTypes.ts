import type { EdgeType } from "$lib/dto/Graph";

export const edgeTypes: Record<EdgeType, { label: string; color: string }> = {
	'typed-by': { label: 'Typed by', color: '#6b7280' },
	'import': { label: 'Import', color: '#22c55e' },
	'duplicate': { label: 'Duplicate', color: '#eab308' },
	'build_duplicate': { label: 'Build duplicate', color: '#f97316' },
	'project-to-project': { label: 'Project to project', color: '#a855f7' },
	'input-type': { label: 'Input type', color: '#3b82f6' },
	'output-type': { label: 'Output type', color: '#ef4444' }
};
