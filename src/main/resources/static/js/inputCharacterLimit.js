var inputs = document.getElementsByTagName('input');
var textAreas = document.getElementsByTagName('textarea');
var allInputs = Array.from(inputs).concat(Array.from(textAreas));

for (var i = 0; i < inputsArray.length; i++) {
    inputsArray[i].maxLength = 5000;
}