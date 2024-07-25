const MAX_FILE_SIZE = 10; // in MB
const ERROR_DISPLAY_DURATION = 5; // in secs
const imageInputs = document.querySelectorAll("[data-image-input]");
const errorMessage = `Image must be less than ${MAX_FILE_SIZE}MB`;

/**
 * Handles setting the inputted image as the preview image. 
 * place the following attributes on the input field:
 * data-image-input - required
 * data-displayed-by="id" - required - is the id of the image element that will display the image
 * data-errored-by="id" - required - is the id of the element that will display the error message
 * data-submit-if-valid - optional - if exists, the form will be submitted if the image is valid
 * 
 * @param {Element} imageInput - The HTML element of the input field that contains the image file.
 * @return {void} - No return value.
 */
const handleFileSelect = (imageInput) => {
    const files = imageInput.files;

    if (files.length === 0) {
        return;
    }

    const file = files[0];

    const errorDisplayId = imageInput.getAttribute("data-errored-by");
    const errorDisplayElement = document.getElementById(errorDisplayId);

    const imageDisplayId = imageInput.getAttribute("data-displayed-by");
    const imageDisplayElement = document.getElementById(imageDisplayId);

    // Check if file size is greater than the max file size
    if (file.size > MAX_FILE_SIZE * (1024 ** 2)) {
        errorDisplayElement.textContent = errorMessage;
        imageDisplayElement.classList.add("border-danger");
        imageInput.value = '';

        // Clear error message and border after 5 seconds
        setTimeout(() => {
            imageDisplayElement.classList.remove("border-danger");
            errorDisplayElement.textContent = '';
        }, ERROR_DISPLAY_DURATION * 1000)
        return;
    }

    const submitIfvalid = imageInput.getAttribute("data-submit-if-valid");

    // If the input has the attribute data-submit-if-valid, submit the form
    if (submitIfvalid !== null) {
        imageInput.closest("form").submit();
        return;
    }

    imageDisplayElement.classList.remove("border-danger");
    errorDisplayElement.textContent = '';

    // Set the image as the preview image
    const reader = new FileReader();
    reader.onload = (e) => {
        imageDisplayElement.src = e.target.result;
    };

    reader.readAsDataURL(file);
}


imageInputs.forEach((imageInput) => {
    imageInput.addEventListener("change", () => handleFileSelect(imageInput));
});