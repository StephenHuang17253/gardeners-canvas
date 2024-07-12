const dateOfBirth = document.getElementById('dateOfBirth');
let dateOfBirthJSError = document.getElementById('dateOfBirthJSError');

/**
 * If a date is invalid then shows an error
 */
const handleInvalidDate = () => {
    dateOfBirth.setCustomValidity(' ');
    dateOfBirthJSError.style.display = "block";
}

/**
 * Removes invalid date error when a user edits it
 */
const clearDateError = () => {
    dateOfBirth.setCustomValidity('');
    dateOfBirthJSError.style.display = "none";
    document.getElementById('dateOfBirthError').textContent = "";
}





