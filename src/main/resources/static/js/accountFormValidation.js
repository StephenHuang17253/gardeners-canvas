const firstName = document.getElementById("firstName");
let firstNameJSError = document.getElementById("firstNameJSError");

const lastName = document.getElementById("lastName");
let lastNameJSError = document.getElementById("lastNameJSError");

const email = document.getElementById("emailAddress")
const MAX_EMAIL_LENGTH = 320
let emailJSError = document.getElementById("emailJSError");


const dateOfBirth = document.getElementById("dateOfBirth");
let dateOfBirthJSError = document.getElementById("dateOfBirthJSError");

const handleInvalidFirstName = () => {
    firstName.setCustomValidity(" ");
    firstName.classList.add("border-danger");
    firstNameJSError.style.display = "block";
}

const clearFirstNameError = () => {
    firstName.setCustomValidity("");
    firstName.classList.remove("border-danger");
    firstNameJSError.style.display = "none";
}

const handleFirstNameUpdate = (event) => {
    let firstNameValue = event.target.value
    const validNameRegex = /^[A-Za-z\s\-']+$/;
    console.log(firstNameValue);
    if (firstNameValue.length > 64) {
        firstNameJSError.textContent = 'First name must be less than or equal to 64 characters';
        handleInvalidFirstName();
    } else if (!validNameRegex.test(firstNameValue)) {
        firstNameJSError.textContent = 'First name cannot be empty and must only include letters, spaces, hyphens or apostrophes';
        handleInvalidFirstName();
    } else if (firstNameValue === "") {
        firstNameJSError.textContent = 'First name cannot be empty and must only include letters, spaces, hyphens or apostrophes';
        handleInvalidFirstName();
    } else {
        clearFirstNameError();
    }
}

const handleInvalidLastName = () => {
    lastName.classList.add("border-danger");
    lastNameJSError.style.display = "block";
}

const clearLastNameError = () => {
    lastName.classList.remove("border-danger");
    lastNameJSError.style.display = "none";
}

const handleLastNameUpdate = (event) => {
    let lastNameValue = event.target.value
    const validNameRegex = /^[A-Za-z\s\-']+$/;
    console.log(lastNameValue);
    if (lastNameValue.length > 64) {
        lastNameJSError.textContent = 'Last name must be less than or equal to 64 characters';
        handleInvalidLastName();
    } else if (!validNameRegex.test(lastNameValue)) {
        lastNameJSError.textContent = 'Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes';
        handleInvalidLastName();
    } else if (lastNameValue === "") {
        lastNameJSError.textContent = 'Last name cannot be empty and must only include letters, spaces, hyphens or apostrophes';
        handleInvalidLastName();
    } else {
        clearLastNameError();
    }
}



const handleInvalidEmail = () => {
    email.classList.add("border-danger");
    emailJSError.style.display = "block";
}

const clearEmailError = () => {
    email.classList.remove("border-danger");
    emailJSError.style.display = "none";
}

const handleEmailInput = (event) => {

    let emailValue = event.target.value;
    const emailRegex = /^[\p{L}\p{M}\p{N}]{1,}(?:[._-][\p{L}\p{M}\p{N}]+)*@[a-zA-Z0-9-]{1,}\.[a-zA-Z]{2,}(?:\.[a-zA-Z]{2,})?$/u;
    const [localPart, domainPart] = emailValue.split('@');

    if (emailValue.length > MAX_EMAIL_LENGTH) {
        emailJSError.textContent = "Email is too long, should be 320 characters or less. The local part should be max 64 characters and domain should be max 225 characters"
        handleInvalidEmail()
    } else if (!emailRegex.test(emailValue)) {
        emailJSError.textContent = "Email must be in the form 'jane@doe.nz'"
        handleInvalidEmail()
    } else if (localPart.length > 64 || domainPart.length > 255) {
        emailJSError.textContent = "Email is too long, should be 320 characters or less. The local part should be max 64 characters and domain should be max 225 characters"
        handleInvalidEmail()
    } else {
        clearEmailError();
    }

}


/**
 * If a date is invalid then shows an error
 */
const handleInvalidDate = () => {
    dateOfBirth.setCustomValidity(" ");
    dateOfBirth.classList.add("border-danger")
    dateOfBirthJSError.style.display = "block";
}

/**
 * Removes invalid date error when a user edits it
 */
const clearDateError = () => {
    dateOfBirth.setCustomValidity("");
    dateOfBirthJSError.style.display = "none";
    dateOfBirth.classList.remove("border-danger")
    document.getElementById("dateOfBirthError").textContent = "";
}

// Event Listeners
firstName.addEventListener("input", handleFirstNameUpdate);
lastName.addEventListener("input", handleLastNameUpdate);
email.addEventListener("input", handleEmailInput);




