const plantDate = document.getElementById("plantDate");
const plantDateErrorJs = document.getElementById("plantDateJSError");

const plantName = document.getElementById("plantName");
const plantNameJSError = document.getElementById("plantNameJSError");

const plantDescription = document.getElementById("plantDescription");
const plantDescriptionJSError = document.getElementById("plantDescriptionJSError");

const plantCount = document.getElementById("plantCount");
const plantCountJSError = document.getElementById("plantCountJSError");

const plantCategory = document.getElementById("plantCategory");
const plantCategoryButton = document.getElementById("plantCategoryButton");
const plantCategoryJSError = document.getElementById("plantCategoryJSError");
let plantCategorySelected = false;

const variationSelector1 = 65039;
const submitButton = document.querySelector('button[type="submit"]');


/**
 * Handles the case where the user's input for the date field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @returns {void}
 */
const displayDateError = () => {
    plantDate.setCustomValidity(" ");
    plantDate.classList.add("border-danger");
    plantDateErrorJs.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearDateError = () => {
    plantDate.setCustomValidity("");
    plantDateErrorJs.style.display = "none";
    plantDate.classList.remove("border-danger");
}

/**
 * Handles updates to the input field for plant date.
 * @param {{target: HTMLElement}} event - The input event.
 * @returns {void}
 */
const handleDateUpdate = (event) => {
    const dateValue = new Date(event.target.value);
    const today = new Date();
    const timeDiff = today - dateValue;
    const age = today.getFullYear() - dateValue.getFullYear();
    const monthDiff = today.getMonth() - dateValue.getMonth();
    const dayDiff = today.getDate() - dateValue.getDate();
    const trueDayDiff = timeDiff / (1000 * 60 * 60 * 24);
    clearDateError();
    if (!plantDate.checkValidity()) {
        plantDateErrorJs.textContent = "Date is not in valid format, DD/MM/YYYY";
        displayDateError();
        return
    }

    let validAge = false;
    if (age > 400 || (age === 400 && (monthDiff > 0 || (monthDiff === 0 && dayDiff >= 0)))) {
        plantDateErrorJs.textContent = "Plant date cannot be more than 400 years ago.";
    } else if (trueDayDiff < -365) {
        plantDateErrorJs.textContent = "Plant date cannot be more than a year in the future.";
    } else {
        validAge = true;
    }
    if (!validAge) {
        displayDateError();
    } else {
        clearDateError();
    }
}

/**
 * Handles the case where the user's input for the name field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @param {HTMLElement} inputField - The input field element (firstName or lastName).
 * @param {HTMLElement} errorField - The error message element (firstNameJSError or lastNameJSError).
 * @returns {void}
 */
const displayNameError = (inputField, errorField) => {
    inputField.setCustomValidity(" "); // ignore underline, is fine (appears because I'm using a param)
    inputField.classList.add("border-danger");
    errorField.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearNameError = (inputField, errorField) => {
    inputField.setCustomValidity(""); // ignore underline, is fine (appears because I'm using a param)
    inputField.classList.remove("border-danger");
    errorField.style.display = "none";
}

/**
 * Handles updates (char input) to the input field for name.
 * @param {{target: HTMLElement}} event - The input event.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const handleNameUpdate = (event, errorField) => {
    let nameValue = event.target.value;
    const validNameRegex = /^[\p{L}\p{M}\p{N}\s,.'-]*$/u;
    if (nameValue.length > 64) {
        errorField.textContent = "Plant name cannot be greater than 64 characters in length";
        displayNameError(event.target, errorField);
    } else if (!validNameRegex.test(nameValue) || !nameValue.trim()) {
        errorField.textContent = "Plant name cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes";
        displayNameError(event.target, errorField);
    } else {
        clearNameError(event.target, errorField);
    }
}

/**
 * Handles the case where the user's input for the description field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @param {HTMLElement} inputField - The input field element (firstName or lastName).
 * @param {HTMLElement} errorField - The error message element (firstNameJSError or lastNameJSError).
 * @returns {void}
 */
const displayDescriptionError = () => {
    plantDescription.setCustomValidity(" "); // ignore underline, is fine (appears because I'm using a param)
    plantDescription.classList.add("border-danger");
    plantDescriptionJSError.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearDescriptionError = () => {
    plantDescription.setCustomValidity(""); // ignore underline, is fine (appears because I'm using a param)
    plantDescription.classList.remove("border-danger");
    plantDescriptionJSError.style.display = "none";
}

/**
 * Handles updates (char input) to the input field for description.
 * @param {{target: HTMLElement}} event - The input event.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const handleDescriptionUpdate = (event) => {
    const descriptionValue = event.target.value;
    const filteredValue = descriptionValue.replaceAll(/\s+/g, "");

    const containsLetterRegex = /[a-zA-Z]/;

    const characterCount = Array.from(descriptionValue).filter(char => !(char.match(/\s/) || char.charCodeAt(0) === variationSelector1)).length;

    if ((!containsLetterRegex.test(filteredValue) && filteredValue !== "") || characterCount > 512) {
        plantDescriptionJSError.textContent = "Description must be 512 characters or less and contain some letters";
        displayDescriptionError();
    } else {
        clearDescriptionError();
    }
}

/**
 * Handles the case where the user's input for the count is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @param {HTMLElement} inputField - The input field element (firstName or lastName).
 * @param {HTMLElement} errorField - The error message element (firstNameJSError or lastNameJSError).
 * @returns {void}
 */
const displayCountError = () => {
    plantCount.setCustomValidity(" "); // ignore underline, is fine (appears because I'm using a param)
    plantCount.classList.add("border-danger");
    plantCountJSError.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearCountError = () => {
    plantCount.setCustomValidity(""); // ignore underline, is fine (appears because I'm using a param)
    plantCount.classList.remove("border-danger");
    plantCountJSError.style.display = "none";
}

/**
 * Handles updates to the input field for count.
 * @param {{target: HTMLElement}} event - The input event.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const handleCountUpdate = (event) => {
    const countValue = event.target.value;


    let floatValue;
    try {
        if (countValue === '') {
            clearCountError();
            return;
        }

        floatValue = parseFloat(countValue.replace(",", "."));

        if (isNaN(floatValue)) {
            plantCountJSError.textContent = "Invalid number format.";
            displayCountError();
            return;
        }

        if (floatValue % 1 !== 0 || floatValue > 1000000 || floatValue <= 0) {
            plantCountJSError.textContent = "Plant count must be a positive whole number between 1 and 1,000,000";
            displayCountError();
            return;
        }
    } catch (e) {

        plantCountJSError.textContent = "Invalid number format.";
        displayCountError();
        return;
    }

    clearCountError();
}


/**
 * Handles the case where the user's input for the category is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @param {HTMLElement} inputField - The input field element (plantCategory).
 * @param {HTMLElement} errorField - The error message element (plantCategoryJSError).
 * @returns {void}
 */
const displayCategoryError = () => {
    plantCategoryButton.classList.add("border-danger");
    plantCategoryJSError.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearCategoryError = () => {
    plantCategory.setCustomValidity("");
    plantCategory.classList.remove("border-danger");
    plantCategoryJSError.style.display = "none";
}

/**
 * Handles updates to the input field for category.
 * @param {{target: HTMLElement}} event - The input event.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const handleCategoryUpdate = (event) => {
    let categoryValue = event.target.value;
    if (categoryValue.length === 0) {
        displayCategoryError();
    } else {
        clearCategoryError();
        plantCategorySelected = true
    }

}


/**
 * Validates the form on the client-side when the user presses the Submit button.
 * If there is an invalid input, prevent the submission of the form.
 * @param {Event} event - The input event.
 * @returns {void}
 */
const handleFormSubmit = (event) => {
    handleDateUpdate({ target: plantDate });
    handleNameUpdate({ target: plantName }, plantNameJSError);
    handleDescriptionUpdate({ target: plantDescription });
    handleCountUpdate({ target: plantCount });
    handleCategoryUpdate({target: plantCategory});
    // Prevent form submission if there are any validation errors
    if (!plantDate.checkValidity() || !plantName.checkValidity() || !plantDescription.checkValidity() || !plantCount.checkValidity() || !plantCategorySelected) {
        event.preventDefault();
    }
}

submitButton.addEventListener('click', handleFormSubmit);