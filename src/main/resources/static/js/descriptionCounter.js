const MAX_CHARS = 512;
const descriptionInputs = document.querySelectorAll("[data-description='true']");


/**
 * Updates the character count display for a description input field.
 * Also changes the text color to red if the input exceeds the maximum allowed characters.
 * To use give the input 
 *  data-description="true"
 * and 
 *  data-counter-id="id"
 * where id is the id of the element that you want to use as your counter
 * 
 * @param {Element} descriptionInput - The HTML element of the description input field whose characters to count.
 */
const updateCounter = (descriptionInput) => {

    const counterId = descriptionInput.getAttribute("data-counter-id");

    const descriptionCounter = document.getElementById(counterId);

    if (descriptionInput.value.length > MAX_CHARS) {
        descriptionCounter.classList.add("text-danger");
    } else {
        descriptionCounter.classList.remove("text-danger");
    }

    let characterCount = 0;
    for (const char of descriptionInput.value) {
        characterCount++;
    }

    descriptionCounter.textContent = characterCount + "/" + MAX_CHARS;
}

window.addEventListener("load", () => {
    descriptionInputs.forEach((descriptionInput) => {
        updateCounter(descriptionInput);
        descriptionInput.addEventListener("input", () => updateCounter(descriptionInput));
    });
});




