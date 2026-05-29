import { STATS_PATH } from '$lib/constants/routes';
import { redirect } from '@sveltejs/kit';
import type { PageLoad } from './$types';

export const load: PageLoad = () => {
    redirect(302, STATS_PATH);
}
