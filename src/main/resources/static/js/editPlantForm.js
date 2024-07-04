let DESCRIPTION_LIMIT = 512
// Event listener ensuring more than 512 characters not entered.

const description_limit_function = function() {
    let limit = DESCRIPTION_LIMIT;
    let input = document.getElementById('plantDescription').value

    if (input.length >= limit) {
        document.getElementById('plantDescription').value = input.slice(0, limit);
        document.getElementById('plantDescriptionError').textContent = "Plant description must be less than " + limit + " characters";
    } else {
        document.getElementById('plantDescriptionError').textContent = "";
    }
}


document.getElementById('plantDescription').addEventListener('input', description_limit_function);
window.addEventListener('load', description_limit_function);
