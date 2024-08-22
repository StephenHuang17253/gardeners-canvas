// The maximum number of characters allowed in any text area or input.
const MAX_LENGTH = 5000;
const textAreasToLimit = document.getElementsByTagName("textarea");
const inputsToLimit = document.getElementsByTagName("input");

/**
 * Handles setting the maximum number of characters allowed in any text area or input.
 * 
 * @param {Number} limit - The maximum number of characters allowed in any text area or input.
 * @return {void} - No return value.
 */
const setCharLimits = (limit) => {
    for (const textarea of textAreasToLimit) {
        textarea.maxLength = limit;
    }
    for (const input of inputsToLimit) {
        input.maxLength = limit;
    }
};

setCharLimits(MAX_LENGTH);
