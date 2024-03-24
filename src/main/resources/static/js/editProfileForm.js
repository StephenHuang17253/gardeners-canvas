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

function handleFileSelect(event) {
    const files = event.target.files;
    if (files.length > 0) {
        const file = files[0];
        const reader = new FileReader();

        reader.onload = (e) => {
            const previewImage = document.getElementById('profilePicture');
            previewImage.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
}