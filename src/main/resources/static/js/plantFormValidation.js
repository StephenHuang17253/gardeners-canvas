const plantDate = document.getElementById("plantDate");
const plantDateErrorJs = document.getElementById("plantDateJSError");

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
    const age = today.getFullYear() - dateValue.getFullYear();
    const monthDiff = today.getMonth() - dateValue.getMonth();
    const dayDiff = today.getDate() - dateValue.getDate();

    if (!plantDate.checkValidity()) {
        plantDateErrorJs.textContent = "Date is not in valid format, DD/MM/YYYY";
        displayDateError();
        return
    }

    let validAge = false;
    if (age > 400 || (age === 400 && (monthDiff > 0 || (monthDiff === 0 && dayDiff >= 0)))) {
        plantDateErrorJs.textContent = "The maximum age allowed is 400 years";
    } else {
        validAge = true;
    }


    if (!validAge) {
        displayDateError();
    } else {
        clearDateError();
    }
}

const handleFormSubmit = (event) => {
    handleDateUpdate({ target: plantDate });

    // Prevent form submission if there are any validation errors
    if (!plantDate.checkValidity()) {
        event.preventDefault();
        password.value = "";
        repeatPassword.value = "";
    }
}

submitButton.addEventListener('click', handleFormSubmit);