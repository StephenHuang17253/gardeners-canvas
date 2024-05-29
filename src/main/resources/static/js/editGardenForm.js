// Event listener ensuring more than 2d.p. is not entered.
document.getElementById('gardenSize').addEventListener('input', function(event) {
    let input = event.target.value;
    let parts = [input];
    let point = "."
    if (input.includes('.')) {
        parts = input.split('.');
    } else if (input.includes(',')) {
        parts = input.split(',');
        point = ","
    }
    let decimals = parts[parts.length - 1]
    if (decimals.length > 2 && parts.length !== 1) {
        document.getElementById('gardenSize').value = parts[0] + point + decimals.slice(0, 2);
    }
});
