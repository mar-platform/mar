import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const API_BASE_URL = PUBLIC_API_BASE_URL;

export const STATS_ENDPOINT = `${API_BASE_URL}/stats`;
export const ALL_GRAPHS_ENDPOINT = `${API_BASE_URL}/graph`;
export const LABEL_PROPAGATION_ENDPOINT = `${API_BASE_URL}/clustering/label-propagation`;
export const DUPLICATION_GRAPH_ENDPOINT = `${API_BASE_URL}/duplication-graph`;
export const MEGAMODEL_GRAPH_ENDPOINT = `${API_BASE_URL}/megamodel-graph`;
export const INTER_PROJECT_GRAPH_ENDPOINT = `${API_BASE_URL}/interproject-graph`;
export const SEARCH_PROJECT_ENDPOINT = (value: string) => `${API_BASE_URL}/search-project?value=${encodeURIComponent(value)}`;
export const ALL_PROJECTS_ENDPOINT = `${API_BASE_URL}/all-projects`;
export const PROJECT_GRAPH_ENDPOINT = (projectId: string) => `${API_BASE_URL}/project-graph?projectId=${encodeURIComponent(projectId)}`;
export const ALL_COMPONENTS_ENDPOINT = `${API_BASE_URL}/all-components`;
export const COMPONENT_GRAPH_ENDPOINT = `${API_BASE_URL}/component-graph`;
export const ARTEFACT_INFO_ENDPOINT = (artefactId: string) => `${API_BASE_URL}/artefacts?q=${encodeURIComponent(artefactId)}`;
export const PROJECT_INFO_ENDPOINT = (projectId: string) => `${API_BASE_URL}/projects?q=${encodeURIComponent(projectId)}`;

export const GITHUB_USER_API_ENDPOINT = (userId: string) => `https://api.github.com/users/${encodeURIComponent(userId)}`;
