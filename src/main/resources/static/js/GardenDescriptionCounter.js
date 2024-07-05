const MAX_CHARS = 512;
const descriptionInput = document.getElementById('gardenDescription');
const descriptionCounter = document.getElementById('gardenDescriptionCounter');

const updateCounter = () => {
    if (descriptionInput.value.length > MAX_CHARS) {
        descriptionCounter.classList.add('text-danger');
    } else {
        descriptionCounter.classList.remove('text-danger');
    }
    descriptionCounter.textContent = descriptionInput.value.length + '/' + MAX_CHARS;
}

updateCounter();
descriptionInput.addEventListener('input', updateCounter);


