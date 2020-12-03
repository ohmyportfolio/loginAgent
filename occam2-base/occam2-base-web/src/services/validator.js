export default {
  associatedPasswrod(e) {
    if (0 == e.value.length) return !0;
    var value = e.value.toLowerCase();
    var b = !1; - 1 != value.search(/^((?!shilla|tlsfk|eksrl|test|eam|wktks).)*$/) && (b = !0);
    return b
  },
  defaultSpecialChar(e) {
    if (0 == e.value.length) return !0;
    var b = !1; - 1 != e.value.search(/^[a-zA-Z0-9\~\`\!\@\\\#\$\%\^\&\*\(\)\-\_\=\+\\\|\[\]\{\}\;\:\'\"\,\.\/\<\>\?]+$/) && (b = !0);
    return b
  },
  mixPassword(e) {
    if (0 == e.value.length) return !0;
    var b = !1; - 1 == e.value.search(/\d/g) || -1 == e.value.search(/^.*(?=[\~\`\!\@\\\#\$\%\^\&\*\(\)\-\_\=\+\\\|\[\]\{\}\;\:\'\"\,\.\/\<\>\?]).*$/gm) || -1 == e.value.search(/[a-z]/g) && -1 == e.value.search(/[A-Z]/g) || (b = !0);
    return b
  },
  sameCharPassword(e) {
    var a = e.value;
    if (3 > a.length) return !0;
    for (var b = a.split(""), c = a = 1; c < b.length && !(b[c - 1].toUpperCase() == b[c].toUpperCase() ? a++ : a = 1, 3 <= a); c++);
    b = !1;
    3 > a && (b = !0);
    return b
  },
  ascDescPassword(e) {
    if (3 > e.value.length) return !0;
    var b = e.value.split("");
    var a = 1;
    for (var c = "", d = 1; d < b.length; d++) {
        var f = " 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(b[d - 1].toUpperCase()),
            e = " 1234567890 ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(b[d].toUpperCase());
        f + 1 == e ? ("ASC" == c ? a++ : a = 2, c = "ASC") : f - 1 == e ? ("DESC" == c ? a++ : a = 2, c = "DESC") : a = 1;
        if (3 <= a) break
    }
    b = !1;
    3 > a && (b = !0);
    return b
  }
}
