var textAreas = document.getElementsByTagName('textarea');
var inputs = document.getElementsByTagName('input');

const MAXLENGTH = 5000;

for (var i = 0; i < textAreas.length; i++) {
    textAreas[i].maxLength = MAXLENGTH;
}

for (var i = 0; i < inputs.length; i++) {
    inputs[i].maxLength = MAXLENGTH;
}