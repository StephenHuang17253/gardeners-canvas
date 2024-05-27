var inputs = document.getElementsByTagName('input');
var inputsArray = Array.from(inputs);

for (var i = 0; i < inputsArray.length; i++) {
    inputsArray[i].maxLength = 5000;
}