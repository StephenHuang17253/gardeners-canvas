let DESCRIPTION_LIMIT = 512
// Event listener ensuring more than 512 characters not entered.
const descriptionCounter = document.getElementById('plantDescriptionCounter');
const plantDescription = document.getElementById('plantDescription');
const plantDescriptionError = document.getElementById('plantDescriptionError');

const description_limit_function = function() {
    let limit = DESCRIPTION_LIMIT;
    let input = plantDescription.value

    if (input.length > limit) {
        plantDescription.value = input.slice(0, limit);
        plantDescriptionError.textContent = "Plant description must be less than " + limit + " characters";
        plantDescription.style.borderColor = "red"
    } else {
        plantDescriptionError.textContent = "";
        plantDescription.style.borderColor = ""
    }
    descriptionCounter.textContent = plantDescription.value.length + '/' + DESCRIPTION_LIMIT;
}


document.getElementById('plantDescription').addEventListener('input', description_limit_function);
window.addEventListener('load', description_limit_function);
