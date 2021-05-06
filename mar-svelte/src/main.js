import App from './App.svelte';
import ResultItem from './components/ResultItem.svelte'
import SearchFacets from './components/SearchFacets.svelte'
import { basic_item, all_items } from '../__fixtures__/test_data.js'

let app
let mode = IS_TEST == undefined ? 'default' : IS_TEST

if (mode == 'default') {
	app = new App({
		target: document.body,
		props: {
			name: 'world',
			hydrate: true
		}
	});
} else {
	console.log("Testing with " + IS_TEST + " mode ")
	if (mode == 'ResultItem') {
		app = new ResultItem({
			target: document.body,
			props: {
				item: basic_item
			}
		})
	} else if (mode == 'SearchFacets') {
		app = new SearchFacets({
			target: document.body,
			props: {
				search_items: all_items
			}
		})		
	} else {
		throw 'Invalid UI test: ' + mode
	}
}

export default app;