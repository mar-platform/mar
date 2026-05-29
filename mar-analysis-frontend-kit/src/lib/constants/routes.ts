import { PUBLIC_WEB_BASE_URL } from '$env/static/public';
console.log('WEB:', PUBLIC_WEB_BASE_URL);
export const BASE_URL = PUBLIC_WEB_BASE_URL;

export const GITHUB_PATH = 'https://github.com/mar-platform/mar';

export const STATS_PATH = `${BASE_URL}/stats`;
export const MEGAMODEL_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/megamodel`;
export const INTERPROJECT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/interproject`;
export const PROJECT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/projects`;
export const SELECTED_PROJECT_GRAPHS_EXPLORATION_PATH = (projectId: string) => `${BASE_URL}/graphs/projects?q=${encodeURIComponent(projectId)}`;
export const COMPONENT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/components`;
