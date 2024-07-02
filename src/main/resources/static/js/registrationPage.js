const hideLastName = (toggle) => {
    if (toggle) {
        document.getElementById("lastNameDiv").style.display = "none";
        document.getElementById("lastName").value = "";
    } else {
        document.getElementById("lastNameDiv").style.display = "block";
    }
}

hideLastName(document.getElementById("lastNameCheck").checked);