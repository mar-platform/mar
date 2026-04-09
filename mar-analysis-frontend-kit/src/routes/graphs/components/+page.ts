import type { PageLoad } from './$types';

export const load: PageLoad = ({ url }) => {
    // Extract the 'q' query parameter from the URL
    const q = url.searchParams.get('q');
    return {
        q
    };
};
