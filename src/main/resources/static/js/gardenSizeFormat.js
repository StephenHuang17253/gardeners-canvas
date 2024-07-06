const isNumber = (input) => /^\d+$/.test(input);

const hasDecimalPoint = (input) => /(\.|,)/.test(input);

const hasExponent = (input) => /[eE]/.test(input);

const isSign = (input) => /(\+|\-)/.test(input);

/**
 * Checks if the key pressed is a number or a decimal point or e and wether it is valid.
 * If the method returns false, the key press is prevented.
 * @param {Event} event - The keydown event.
 * @returns {boolean} - Returns true if the key is a number or a decimal point, false otherwise.
 */
const handleKeyPress = (event) => {
    const pressed = event.key;
    const value = event.target.value;
    const validDecimalPoint = hasDecimalPoint(pressed) && !hasDecimalPoint(value) && !hasExponent(value)
    const validExponent = hasExponent(pressed) && !hasExponent(value)
    if (isNumber(pressed) || isSign(pressed) || validDecimalPoint || validExponent) {
        return true;
    } else {
        event.preventDefault();
        return false;
    }
}

document.getElementById('gardenSize').addEventListener('keypress', handleKeyPress);

