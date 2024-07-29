// Selects all forms that need submit button disabling
const forms = document.querySelectorAll(".submit-form-once");

// Disables submit button on a form after it has been clicked once
document.addEventListener("DOMContentLoaded", function () {
    forms.forEach(function (form) {
        form.addEventListener("submit", function () {
            // In a form find submit buttons
            const submitButton = form.querySelector(".submit-button");
            if (submitButton) {
                submitButton.disabled = true;
            }
        });
    });
});
