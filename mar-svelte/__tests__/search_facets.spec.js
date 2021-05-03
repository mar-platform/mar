import { cleanup, render } from '@testing-library/svelte';
import { basic_item, no_metadata_item } from '../__fixtures__/test_data.js'
import SearchFacets from '../src/components/SearchFacets.svelte';


describe('SearchFacets', () => {
	afterEach(cleanup);
    
    it('should render without metadata attached', () => {
		const { getByText } = render(SearchFacets, { search_items: [basic_item, no_metadata_item] });
	});
});
