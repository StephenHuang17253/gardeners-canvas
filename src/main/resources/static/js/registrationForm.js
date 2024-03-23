function hideLastName(event) {
    if (event.target.checked) {
        document.getElementById("lastName").style.display = "none";
        document.getElementById("lastNameLabel").style.display = "none";
        document.getElementById("lastName").value = "";
    } else {
        document.getElementById("lastName").style.display = "block";
        document.getElementById("lastNameLabel").style.display = "block";
    }
}

function hideLastNameOnLoad(checked) {
    if (checked) {
        document.getElementById("lastName").style.display = "none";
        document.getElementById("lastNameLabel").style.display = "none";
        document.getElementById("lastName").value = "";
    } else {
        document.getElementById("lastName").style.display = "block";
        document.getElementById("lastNameLabel").style.display = "block";
    }
}