// Selects all forms that need submit button disabling
const forms = document.querySelectorAll(".submit-form-once");

// Disables submit button on a form after it has been clicked once
document.addEventListener("DOMContentLoaded", () =>
    forms.forEach(form =>
        form.addEventListener("submit", () => {
            // If in a form find submit button
            const submitButton = form.querySelector(".submit-button");
            if (submitButton) submitButton.disabled = true;
        })
    )
);
