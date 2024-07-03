
// The maximum number of characters allowed in any text area or input.
const MAX_LENGTH = 5000;

/**
 * Handles setting the maximum number of characters allowed in any text area or input.
 * 
 * @param {Number} limit - The maximum number of characters allowed in any text area or input.
 * @return {void} - No return value.
 */
const setCharLimits = (limit) => {
    var textAreas = document.getElementsByTagName('textarea');
    var inputs = document.getElementsByTagName('input');

    for (var i = 0; i < textAreas.length; i++) {
        textAreas[i].maxLength = limit;
    }

    for (var i = 0; i < inputs.length; i++) {
        inputs[i].maxLength = limit;
    }
}

setCharLimits(MAX_LENGTH);