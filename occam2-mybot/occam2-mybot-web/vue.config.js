module.exports = {
  devServer: {
    proxy: {
      '/api': {
        target: 'http://211.32.19.210:8088/'
      }
    },
    disableHostCheck: true
  },
  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html'
    }
  },
  outputDir: '../occam2-mybot-server/src/main/resources/static',
  productionSourceMap: false,
  pluginOptions: {
    quasar: {
      importStrategy: 'kebab',
      rtlSupport: false
    }
  },
  transpileDependencies: [
    'quasar'
  ]
}