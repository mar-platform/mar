import tailwindcss from '@tailwindcss/vite';
import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig(({ mode }) => ({
  plugins: [   
      tailwindcss(),
      sveltekit()
  ],
  esbuild: {
		// Removes console.log and console.info in production builds, but keeps them in development for debugging
		pure: mode === 'production' ? ['console.log', 'console.info'] : [],
	},
  server: {
      allowedHosts: [
        'models-lab.inf.um.es'
      ]
  }
}));
			      
