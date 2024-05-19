let DESCRIPTION_LIMIT = 512
// Event listener ensuring more than 512 characters not entered.
document.getElementById('plantDescription').addEventListener('input', function(event) {
    let limit = DESCRIPTION_LIMIT;
    let input = event.target.value;
    if (input.length > limit) {
        document.getElementById('plantDescription').value = input.slice(0, limit);
        document.getElementById('plantDescriptionError').textContent = "Plant description must be less than " + limit + " characters";
    } else {
        document.getElementById('plantDescriptionError').textContent = "";
    }
});
