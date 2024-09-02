// Select all input elements
const inputs = document.querySelectorAll("input");

// Add event listener to each input element
inputs.forEach(input =>
    input.addEventListener("keypress",
        event => {
            if (event.key === "Enter") event.preventDefault();
        }
    )
);
