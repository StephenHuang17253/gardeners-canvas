const zeroAscii = 48;
const nineAscii = 57;
const decimalPointAscii = 46;
const commaAscii = 44;


/**
 * Handles the blur event of the garden size input field, to limit the final number of decimal places to 2.
 * @param {Event} event - The blur event object.
 * @returns {void}
 */
const handleBlur = (event) => {
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
}

/**
 * Checks if the key pressed is a number or a decimal point.
 * If the method returns false, the key press is prevented.
 * @param {Event} event - The keydown event.
 * @returns {boolean} - Returns true if the key is a number or a decimal point, false otherwise.
 */
const handleKeyPress = (event) => {
    const keyCode = event.which || event.keyCode;
    const isDigit = keyCode >= zeroAscii && keyCode <= nineAscii;
    const isDecimalPoint = keyCode === decimalPointAscii || keyCode === commaAscii;

    const value = event.target.value;

    // Allow only one decimal point or comma and only numbers
    if (isDigit || (isDecimalPoint && !value.includes(".") && !value.includes(","))) {
        return true;
    } else {
        event.preventDefault();
        return false;
    }
}


document.getElementById('gardenSize').addEventListener('blur', handleBlur);
document.getElementById('gardenSize').addEventListener('keypress', handleKeyPress);

