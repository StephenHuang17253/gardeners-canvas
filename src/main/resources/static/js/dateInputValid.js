const firstName = document.getElementById("firstName");
let firstNameJSError = document.getElementById("firstNameJSError");

const handleInvalidFirstName = () => {
    firstName.classList.add("border-danger");
    firstNameJSError.style.display = "block";
}

const clearFirstNameError = () => {
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

const lastName = document.getElementById("lastName");
let lastNameJSError = document.getElementById("lastNameJSError");

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
firstName.addEventListener("input", handleFirstNameUpdate);
lastName.addEventListener("input", handleLastNameUpdate);

const dateOfBirth = document.getElementById("dateOfBirth");
let dateOfBirthJSError = document.getElementById("dateOfBirthJSError");

/**
 * If a date is invalid then shows an error
 */
const handleInvalidDate = () => {
    dateOfBirth.setCustomValidity(" ");
    dateOfBirth.classList.add("border-danger");
    dateOfBirthJSError.style.display = "block";
}

/**
 * Removes invalid date error when a user edits it
 */
const clearDateError = () => {
    dateOfBirth.setCustomValidity("");
    dateOfBirth.classList.remove("border-danger");
    dateOfBirthJSError.style.display = "none";
    document.getElementById("dateOfBirthError").textContent = "";
}





