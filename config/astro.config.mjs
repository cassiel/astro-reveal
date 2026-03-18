import { defineConfig } from "astro/config"
import react from "@astrojs/react"
import remarkMath from "remark-math"
import rehypeKatex from "rehype-katex"
import mdx from "@astrojs/mdx"
import tailwindcss from "@tailwindcss/vite"

// https://astro.build/config
export default defineConfig({
  /*site: "https://jsulpis.github.io",*/
  base: "/",

  vite: {
    plugins: [tailwindcss()]
  },

  markdown: {
    // Not sure these two are being used: we have
    // a LaTeX component that uses KaTeX directly.
    remarkPlugins: [remarkMath],
    rehypePlugins: [rehypeKatex]
  },

  trailingSlash: "never",
  integrations: [react(), mdx()]
})
