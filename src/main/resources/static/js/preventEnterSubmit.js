// Select all input elements
var inputs = document.querySelectorAll('input');

// Add event listener to each input element
inputs.forEach(function(input) {
    input.addEventListener('keypress', function(event) {
        if (event.key === 'Enter') {
            event.preventDefault();
        }
    });
});