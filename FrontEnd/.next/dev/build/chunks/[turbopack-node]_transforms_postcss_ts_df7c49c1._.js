module.exports = [
"[turbopack-node]/transforms/postcss.ts { CONFIG => \"[project]/DACS2/FrontEnd/postcss.config.mjs [postcss] (ecmascript)\" } [postcss] (ecmascript, async loader)", ((__turbopack_context__) => {

__turbopack_context__.v((parentImport) => {
    return Promise.all([
  "chunks/020ed_f0a5e6af._.js",
  "chunks/[root-of-the-server]__ea70140f._.js"
].map((chunk) => __turbopack_context__.l(chunk))).then(() => {
        return parentImport("[turbopack-node]/transforms/postcss.ts { CONFIG => \"[project]/DACS2/FrontEnd/postcss.config.mjs [postcss] (ecmascript)\" } [postcss] (ecmascript)");
    });
});
}),
];