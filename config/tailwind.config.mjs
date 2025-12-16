/** @type {import('tailwindcss').Config} */
export default {
	content: [
		'./src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}',
		'./astro-reveal/src/**/*.{astro,html,js,jsx,md,mdx,svelte,ts,tsx,vue}'
	],
	theme: {
		extend: {
			colors: {
				yellow: 'hsl(45, 80%, 70%)',
				orange: 'hsl(30, 80%, 70%)',
				blue: 'hsl(200, 80%, 70%)',
				purple: 'hsl(250, 80%, 80%)',
				pale: 'hsl(0, 0%, 70%)',
				grey: 'hsl(0, 0%, 50%)'
			},

			fontFamily: {
				display: ["Microgramma"]
			},

			fontSize: {
				"sm-1": '0.9em',
				"sm-2": '0.7em',
				"sm-3": '0.6em',
				"lg-1": '1.1em',
				'lg-2': '1.25em'
			}
		}
	},
	plugins: []
}
