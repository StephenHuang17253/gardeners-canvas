const codeInputs = Array.from(document.getElementsByName("code-box"));
const tokenString = document.getElementById("tokenString");


//Constant for representing the Backspace key.
const BACKSPACE = "Backspace";

/**
 * Updates the token string value based on the current state of code inputs.
 * @return {void} - No return value.
 */
const updateTokenString = () => {
    tokenString.value = codeInputs.map(input => input.value).join("");
};

/**
 * Handles keydown events, specifically the Backspace key.
 *
 * @param {KeyboardEvent} e - The keyboard event object.
 * @return {void} - No return value.
 */
const handleKeydown = (e) => {
    if (e.key === BACKSPACE) {
        handleBackspace(e);
    }
};

/**
 * Manages the behavior when the Backspace key is pressed.
 *
 * @param {KeyboardEvent} e - The keyboard event object.
 * @return {void} - No return value.
 */
const handleBackspace = (e) => {
    const currentIndex = codeInputs.indexOf(e.target);
    if (currentIndex > 0 && e.target.value === "") {
        codeInputs[currentIndex - 1].value = "";
    }
    codeInputs[currentIndex].value = "";
    handleFocus(e);
    updateTokenString();
};

/**
 * Updates the focus within the code inputs after an input change.
 *
 * @param {Event} e - The event object.
 * @return {void} - No return value.
 */
const handleInput = (e) => {
    handleFocus(e);
    updateTokenString();
};

/**
 * Focuses on the next available input field.
 *
 * @param {Event} e - The event object.
 * @return {void} - No return value.
 */
const handleFocus = (e) => {
    for (var i = 0; i < codeInputs.length; i++) {
        if (codeInputs[i].value === "" || i === codeInputs.length - 1) {
            codeInputs[i].focus();
            break;
        }
    }
};

/**
 * Handles pasting values into the code inputs.
 *
 * @param {ClipboardEvent} e - The clipboard event object.
 * @return {void} - No return value.
 */
const handlePaste = (e) => {
    e.preventDefault();
    const paste = e.clipboardData.getData("text");
    codeInputs.forEach((input, i) => {
        input.value = paste[i] || "";
    });
    handleFocus(e);
    updateTokenString();
};

// Add event listeners to each code input.
codeInputs.forEach(input => {
    input.maxLength = 1;
    input.addEventListener("keydown", handleKeydown);
    input.addEventListener("input", handleInput);
    input.addEventListener("focus", handleFocus);
    input.addEventListener("paste", handlePaste);
});