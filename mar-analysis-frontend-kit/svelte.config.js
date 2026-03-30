import adapter from '@sveltejs/adapter-static';

/** @type {import('@sveltejs/kit').Config} */
const config = {
        kit: { 
	    adapter: adapter(),
	    paths: {
		// SvelteKit will now automatically tell Vite to use this base
		base: '/modelgraph'
	    }
	},
	vitePlugin: {
		dynamicCompileOptions: ({ filename }) =>
			filename.includes('node_modules') ? undefined : { runes: true }
	}
};

export default config;
