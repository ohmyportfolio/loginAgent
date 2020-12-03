module.exports = {
  "rules": {
    "no-console": 0,
    "no-debugger": 0
  },
  "parserOptions": {
    "parser": "babel-eslint",
    "ecmaVersion": 2017,
    "sourceType": "module",
    "allowImportExportEverywhere": true
  },
  "env": {
    "browser": true,
  },
  extends: [
    "plugin:vue/essential"
  ]
}