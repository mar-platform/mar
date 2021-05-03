import { cleanup, render } from '@testing-library/svelte';
import { basic_item } from '../__fixtures__/test_data.js'
import Item from '../src/components/ResultItem.svelte';
import '../src/test_urls.js'

// https://github.com/agusID/boilerplate-svelte/blob/master/docs/testing.md
// https://testing-library.com/docs/svelte-testing-library/example
// https://svelte-recipes.netlify.app/testing/


describe('ResultItem', () => {

	afterEach(cleanup);

	it('should render a basic smell', () => {
		const { getByText } = render(Item, { item: basic_item });

		var itemTitle = getByText(basic_item.name)

		expect(itemTitle).toBeDefined()
		expect(getByText(basic_item.description)).toBeDefined()
		expect(itemTitle.href).toBe(basic_item.url)
	});
});

