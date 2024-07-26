
export const isNumber = (input) => /^\d+$/.test(input);
export const hasSign = (input) => /(\+|\-)/.test(input);
export const hasDecimalPoint = (input) => /(\.|,)/.test(input);
export const hasExponent = (input) => /[eE]/.test(input);

/**
 * Checks if the key pressed is a number or a decimal point or e and whether it is valid.
 * If the method returns false, the key press is prevented.
 * @param {Event} event - The keydown event.
 * @returns {boolean} - Returns true if the key is a number or a decimal point, false otherwise.
 */
const handleKeyPress = (event) => {
    const pressed = event.key;
    const value = event.target.value;
    const validDecimalPoint = hasDecimalPoint(pressed) && !hasDecimalPoint(value) && !hasExponent(value);
    const validExponent = hasExponent(pressed) && !hasExponent(value);
    const validSign = hasSign(pressed) && !hasSign(value);
    if (isNumber(pressed) || validSign || validDecimalPoint || validExponent) {
        return true;
    } else {
        event.preventDefault();
        return false;
    }
};

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

    let expChar = '';
    if (gardenSize.includes('E')) {
        expChar = 'E';
    } else if (gardenSize.includes('e')) {
        expChar = 'e';
    }

    if (splitChar === '') return;

    const splitInputByDecimal = gardenSize.split(splitChar);
    if (expChar !== '') {
        const splitInputbyExp = splitInputByDecimal[1].split(expChar);
        event.target.value = splitInputByDecimal[0] + splitChar + splitInputbyExp[0].substring(0, 2) + expChar + splitInputbyExp[1];
    } else {
        event.target.value = splitInputByDecimal[0] + splitChar + splitInputByDecimal[1].substring(0, 2);
    }
};

document.getElementById('gardenSize').addEventListener('keypress', handleKeyPress);
document.getElementById('gardenSize').addEventListener('blur', handleBlur);
document.getElementById('plantCount').addEventListener('keypress', handleKeyPress);
