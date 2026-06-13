import { defineConfig } from "astro/config"
import react from "@astrojs/react"
import remarkMath from "remark-math"
import rehypeKatex from "rehype-katex"
import mdx from "@astrojs/mdx"
import tailwindcss from "@tailwindcss/vite"
import { unified } from "@astrojs/markdown-remark"

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
    // Astro 6 deprecated markdown.remarkPlugins / rehypePlugins;
    // pass them to unified({...}) from @astrojs/markdown-remark instead.
    processor: unified({
      remarkPlugins: [remarkMath],
      rehypePlugins: [rehypeKatex]
    })
  },

  trailingSlash: "never",
  integrations: [react(), mdx()]
})
