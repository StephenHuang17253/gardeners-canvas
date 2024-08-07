const openTagModal = document.getElementById("openTagModalButton");
const openModal = document.getElementById("openModal")

window.addEventListener('DOMContentLoaded', () => {
    console.log("openModal value:", openModal)
    console.log("openModal.innerText:", openModal.innerText)
    if (openModal !== null) {
        openTagModal.click();
    }
})