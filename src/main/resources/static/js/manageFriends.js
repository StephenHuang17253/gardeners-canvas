/**
 * This function handles the friend request.
 * It changes the value of the thymeleaf variable 'friendAccepted' to true when the user presses submit.
 * Then the form is submitted
 * @param button he button that was pressed
 * @param accepted true if the accept button was pressed falese otherwise
 */
const manageRequest = (button, accepted) => {
    const form = button.closest('form');
    const acceptedInput = form.querySelector('input[name="friendAccepted"][type="hidden"]');
    acceptedInput.value = accepted;
    form.submit();
}


const showModal = (id) => {
    const modal = document.getElementById("confirm-remove-form-"+id);
    modal.classList.remove("d-none");
}


const closeModal = (id) => {
    const modal = document.getElementById("confirm-remove-form-"+id);
    modal.classList.add("d-none");
}
