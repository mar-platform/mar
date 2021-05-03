export const basic_item = {
	name: 'relational.ecore',
	description: 'This is a test relational model',
	url: 'http://github.com/jesusc/rubytl/raw/relational.ecore',
	score: 100.24,
	metadata: {
		origin: 'github',
		numElements: 10,
		smells: {
		},
		topics: ['sql', 'relational']		
	}
}

export const no_metadata_item = {
	name: 'dummy.ecore',
	url: 'http://github.com/jesusc/test/nometadata.ecore',
	score: 13.4
}

export const all_items = [basic_item, no_metadata_item]