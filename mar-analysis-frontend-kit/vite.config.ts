import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
  // 1. This handles the base path for assets like JS and CSS
  base: '/modelgraph/',
  plugins: [   
      tailwindcss(),
      sveltekit()
  ],
  server: {
      allowedHosts: [
        'models-lab.inf.um.es'
      ]
  }
});
			      
