const hideLastName = (toggle) => {
    if (toggle) {
        document.getElementById("lastNameDiv").style.display = "none";
    } else {
        document.getElementById("lastNameDiv").style.display = "block";
    }
}

// To hide or show on page load
hideLastName(document.getElementById("lastNameCheck").checked);