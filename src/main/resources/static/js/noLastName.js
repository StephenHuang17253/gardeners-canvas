/**
 * Handles hiding the last name div section of the page, includes the input, label and error text.
 * 
 * @param {Boolean} toggle - Boolean value to determine if the last name input field should be hidden.
 * @return {void} - No return value.
 */
const hideLastName = (toggle) => {
    if (toggle) {
        document.getElementById("lastNameDiv").style.display = "none";
    } else {
        document.getElementById("lastNameDiv").style.display = "block";
    }
}

// To hide or show on page load
hideLastName(document.getElementById("lastNameCheck").checked);