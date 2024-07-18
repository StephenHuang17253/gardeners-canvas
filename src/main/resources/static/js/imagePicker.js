const MAX_FILE_SIZE = 10; // in MB
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

    const fileSizeInMB = file.size / (1024 ** 2);

    const errorDisplayId = imageInput.getAttribute("data-errored-by");
    const errorDisplayElement = document.getElementById(errorDisplayId);

    if (fileSizeInMB > MAX_FILE_SIZE) {
        errorDisplayElement.textContent = errorMessage;
        imageInput.classList.add("border-danger");
        imageInput.value = '';
        return;
    }

    const submitIfvalid = imageInput.getAttribute("data-submit-if-valid");

    if (submitIfvalid !== null) {
        const formId = imageInput.closest("form").id;
        const formElement = document.getElementById(formId);
        formElement.submit();
        return;
    }

    imageInput.classList.remove("border-danger");
    errorDisplayElement.textContent = '';

    const reader = new FileReader();

    reader.onload = (e) => {
        const imageDisplayId = imageInput.getAttribute("data-displayed-by");
        const imageDisplayElement = document.getElementById(imageDisplayId);
        imageDisplayElement.src = e.target.result;
    };

    reader.readAsDataURL(file);
}


imageInputs.forEach((imageInput) => {
    imageInput.addEventListener("change", () => handleFileSelect(imageInput));
});