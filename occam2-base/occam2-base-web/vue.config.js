module.exports = {
    devServer: {
        proxy: {
            '/api': {
                target: 'http://localhost:8090'
            }
        }
    },
    outputDir: '../occam2-base-server/src/main/resources/static',
    productionSourceMap: false,
    configureWebpack: {
        optimization: {
            splitChunks: {
            minSize: 100000,
            maxSize: 500000,
            }
        }
    }
}