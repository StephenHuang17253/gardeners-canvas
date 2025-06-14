const firstName = document.getElementById("firstName");
const firstNameJSError = document.getElementById("firstNameJSError");

const lastName = document.getElementById("lastName");
const lastNameJSError = document.getElementById("lastNameJSError");


const email = document.getElementById("emailAddress")
const MAX_EMAIL_LENGTH = 320
const emailServerError = document.getElementById("emailError")
const emailJSError = document.getElementById("emailJSError");

const dateOfBirth = document.getElementById("dateOfBirth");
const dateOfBirthJSError = document.getElementById("dateOfBirthJSError");

const password = document.getElementById("password");
const repeatPassword = document.getElementById("repeatPassword");
const passwordJSError = document.getElementById("passwordJSError");
const repeatPasswordJSError = document.getElementById("repeatPasswordJSError");

const signUpButton = document.querySelector('button[type="submit"]');

/**
 * Handles the case where the user's input for the first/last name field is invalid.
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
 * Handles updates (char input) to the input field for names (first name or last name).
 * @param {{target: HTMLElement}} event - The input event.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const handleNameUpdate = (event, errorField) => {
    let nameValue = event.target.value;
    const validNameRegex = /^[\p{L}\p{M}][\p{L}\p{M} \-'’]*$/u;
    let firstOrLast;
    if (errorField === firstNameJSError) {
        firstOrLast = "First"
    } else {
        firstOrLast = "Last"
    }

    if (firstOrLast === "Last" && !event.target.offsetParent) {
        clearNameError(event.target, errorField);
        return;
    }

    if (nameValue.length > 64) {
        errorField.textContent = firstOrLast + " name must be 64 characters long or less";
        displayNameError(event.target, errorField);
    } else if (!validNameRegex.test(nameValue) || nameValue === "") {
        errorField.textContent = firstOrLast + " name cannot be empty and must only include letters, spaces, hyphens or apostrophes";
        displayNameError(event.target, errorField);
    } else {
        clearNameError(event.target, errorField);
    }
}

/**
 * Handles the case where the user's input for the date field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @returns {void}
 */
const displayDateError = () => {
    dateOfBirth.setCustomValidity(" ");
    dateOfBirth.classList.add("border-danger");
    dateOfBirthJSError.style.display = "block";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearDateError = () => {
    dateOfBirth.setCustomValidity("");
    dateOfBirthJSError.style.display = "none";
    dateOfBirth.classList.remove("border-danger");
}

/**
 * Handles updates to the input field for date of birth.
 * @param {{target: HTMLElement}} event - The input event.
 * @returns {void}
 */
const handleDateUpdate = (event) => {
    const dateValue = new Date(event.target.value);
    const today = new Date();
    const age = today.getFullYear() - dateValue.getFullYear();
    const monthDiff = today.getMonth() - dateValue.getMonth();
    const dayDiff = today.getDate() - dateValue.getDate();

    clearDateError();

    if (!dateOfBirth.checkValidity()) {
        dateOfBirthJSError.textContent = "Date is not in valid format, DD/MM/YYYY";
        displayDateError();
        return
    }

    let validAge = false;
    if (age > 120 || (age === 120 && (monthDiff > 0 || (monthDiff === 0 && dayDiff >= 0)))) {
        dateOfBirthJSError.textContent = "The maximum age allowed is 120 years";
    } else if (age < 13 || (age === 13 && (monthDiff < 0 || (monthDiff === 0 && dayDiff < 0)))) {
        dateOfBirthJSError.textContent = "You must be 13 years or older to create an account";
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
 * Handles the case where the user's input for the email field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @returns {void}
 */
const displayEmailError = () => {
    email.setCustomValidity(" ");
    email.classList.add("border-danger");
    emailJSError.style.display = "block";
    if (emailServerError) {
        emailServerError.style.display = "none";
    }
}

/**
 * Clears the error message and removes the red border from the input field.
 * @param {HTMLElement} inputField - The input field element.
 * @param {HTMLElement} errorField - The error message element.
 * @returns {void}
 */
const clearEmailError = () => {
    email.setCustomValidity("");
    email.classList.remove("border-danger");
    emailJSError.style.display = "none";
}

/**
 * Handles updates (char input) to the input field for the user's email.
 * @param {{target: HTMLElement}} event - The input event.
 * @returns {void}
 */
const handleEmailUpdate = (event) => {
    let emailValue = event.target.value;
    const emailRegex = /^[\p{L}\p{M}\p{N}]{1,}(?:[._-][\p{L}\p{M}\p{N}]+)*@[a-zA-Z0-9-]{1,}\.[a-zA-Z]{2,}(?:\.[a-zA-Z]{2,})?$/u;
    const [localPart, domainPart] = emailValue.split('@');

    if (!emailRegex.test(emailValue)) {
        emailJSError.textContent = "Email must be in the form 'jane@doe.nz'";
        displayEmailError();
    } else if (emailValue.length > MAX_EMAIL_LENGTH || localPart.length > 64 || domainPart.length > 255) {
        emailJSError.textContent = "Email is too long, should be 320 characters or less. The local part should be max 64 characters and domain should be max 225 characters";
        displayEmailError();
    } else {
        clearEmailError();
    }
}

/**
 * Handles the case where the user's input for the password field is invalid.
 * Sets the border of the input to red, and makes the error message visible.
 * @returns {void}
 */
const displayPasswordError = () => {
    password.setCustomValidity(" ");
    password.classList.add("border-danger");
    passwordJSError.style.display = "block";
    passwordJSError.textContent = "Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @returns {void}
 */
const clearPasswordError = () => {
    password.setCustomValidity("");
    password.classList.remove("border-danger");
    passwordJSError.style.display = "none";
}

/**
 * Handles updates (char input) to the input field for the user's password.
 * @param {{target: HTMLElement}} event - The input event.
 * @returns {void}
 */
const handlePasswordInput = (event) => {
    console.log("handlePasswordInput was called")
    let passwordValue = event.target.value;
    console.log(passwordValue)
    const validPasswordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]).{8,}$/;

    if (passwordValue === "") {
        displayPasswordError();
    } else if (!validPasswordRegex.test(passwordValue)) {
        displayPasswordError();
    } else {
        clearPasswordError();
    }
}

/**
 * Handles the case where the user's input for the password field is invalid (not the same).
 * Sets the border of the input to red, and makes the error message visible.
 * @returns {void}
 */
const displayRepeatPasswordError = () => {
    repeatPassword.setCustomValidity(" ");
    repeatPassword.classList.add("border-danger");
    repeatPasswordJSError.style.display = "block";
    repeatPasswordJSError.textContent = "Passwords do not match";
}

/**
 * Clears the error message and removes the red border from the input field.
 * @returns {void}
 */
const clearRepeatPasswordError = () => {
    repeatPassword.setCustomValidity("");
    repeatPassword.classList.remove("border-danger");
    repeatPasswordJSError.style.display = "none";
}

/**
 * Handles updates (char input) to the input field for the user's repeated password.
 * @param {{target: HTMLElement}} event - The input event.
 * @returns {void}
 */
const handleRepeatPasswordInput = (event) => {
    const repeatPasswordValue = event.target.value;
    const passwordValue = password.value;

    if (repeatPasswordValue !== passwordValue) {
        displayRepeatPasswordError()
    } else {
        clearRepeatPasswordError()
    }
}

/**
 * Validates the form on the client-side when the user presses the Submit button.
 * If there is an invalid input, prevent the submission of the form.
 * @param {Event} event - The input event.
 * @returns {void}
 */
const handleFormSubmit = (event) => {
    handleNameUpdate({ target: firstName }, firstNameJSError);
    handleNameUpdate({ target: lastName }, lastNameJSError);
    handleDateUpdate({ target: dateOfBirth });
    handleEmailUpdate({ target: email });
    handlePasswordInput({ target: password });
    handleRepeatPasswordInput({ target: repeatPassword })

    // Prevent form submission if there are any validation errors
    if (!firstName.checkValidity() || !lastName.checkValidity() || !email.checkValidity()
        || !dateOfBirth.checkValidity() || !password.checkValidity() || !repeatPassword.checkValidity()) {
        event.preventDefault();
        password.value = "";
        repeatPassword.value = "";
    }
}

signUpButton.addEventListener('click', handleFormSubmit);

/**
 * TODO: Bring up real-time validation on client-side on Tuesday 23rd SCRUM meeting
 * The event listeners below are currently commented out.
 * However, I am considering getting PO approval to change the ACs to allow error messages to appear
 * before the user submits the form.
 * I think the form will feel more responsive if the user gets feedback as they type.
 * Will discuss at next Scrum, or on Mattermost.
 */
// Event Listeners .
// firstName.addEventListener("input", (event) => handleNameUpdate(event, firstNameJSError));
// lastName.addEventListener("input", (event) => handleNameUpdate(event, lastNameJSError));
// email.addEventListener("input", handleEmailUpdate);
// dateOfBirth.addEventListener("input", handleDateUpdate);

