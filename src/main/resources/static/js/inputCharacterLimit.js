var textAreas = document.getElementsByTagName('textarea');
var inputs = document.getElementsByTagName('input');

const MAX_LENGTH = 5000;

for (var i = 0; i < textAreas.length; i++) {
    textAreas[i].maxLength = MAX_LENGTH;
}

for (var i = 0; i < inputs.length; i++) {
    inputs[i].maxLength = MAX_LENGTH;
}