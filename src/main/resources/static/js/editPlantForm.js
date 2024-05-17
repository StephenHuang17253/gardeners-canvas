// Event listener ensuring more than 1000 characters not entered.
document.getElementById('plantDescription').addEventListener('input', function(event) {
    let input = event.target.value;
    if (input.length > 1000) {
        document.getElementById('plantDescription').value = input.slice(0, 1000);
    }
});
