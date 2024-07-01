const hideLastName = (toggle) => {
    if (toggle) {
        document.getElementById("lastNameDiv").style.display = "none";
        document.getElementById("lastName").value = "";
    } else {
        document.getElementById("lastNameDiv").style.display = "block";
    }
}

const lol = () => {
    console.log(document.getElementById("lastName").value);
}