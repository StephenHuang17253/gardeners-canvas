/**
 * Handles setting the inputted image as the preview image. 
 * Put this function in the onchange attribute of the file input element.
 * And the img tag for the image preview should have an id of "imageToChange".
 * 
 * @param {Event} event - The change event triggered by the file input element. Contains information about the selected file(s).
 * @return {void} - No return value.
 */
function handleFileSelect(event) {
    const files = event.target.files;
    if (files.length > 0) {
        const file = files[0];
        const reader = new FileReader();

        reader.onload = (e) => {
            const previewImage = document.getElementById("imageToChange");
            previewImage.src = e.target.result;
        };
        reader.readAsDataURL(file);
    }
}