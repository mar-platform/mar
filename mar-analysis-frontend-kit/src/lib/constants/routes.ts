import { PUBLIC_API_BASE_URL } from '$env/static/public';

export const BASE_URL = PUBLIC_API_BASE_URL;

export const GITHUB_PATH = 'https://github.com/mar-platform/mar';

export const STATS_PATH = `${BASE_URL}/stats`;
export const ALL_GRAPHS_PATH = `${BASE_URL}/graphs`;
export const DUPLICATION_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/duplication`;
export const MEGAMODEL_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/megamodel`;
export const INTERPROJECT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/interproject`;
export const PROJECT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/projects`;
export const COMPONENT_GRAPHS_EXPLORATION_PATH = `${BASE_URL}/graphs/components`;
