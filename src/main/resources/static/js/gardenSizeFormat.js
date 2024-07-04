// Event listener ensuring more than 2d.p. is not entered.
document.getElementById('gardenSize').addEventListener('blur', event => {
    const gardenSize = event.target.value;

    let splitChar = '';
    if (gardenSize.includes('.')) {
        splitChar = '.';
    } else if (gardenSize.includes(',')) {
        splitChar = ',';
    }

    if (splitChar !== '') {
        const splitInput = gardenSize.split(splitChar);
        event.target.value = splitInput[0] + splitChar + splitInput[1].substring(0, 2);
    }
});
