const MAXCHARS = 512;
const descriptionInput = document.getElementById('gardenDescription');
const descriptionCounter = document.getElementById('gardenDescriptionCounter');

const updateCounter = () => {
    if (descriptionInput.value.length > MAXCHARS) {
        descriptionCounter.classList.add('error');
    } else {
        descriptionCounter.classList.remove('error');
    }
    descriptionCounter.textContent = descriptionInput.value.length + '/' + MAXCHARS;
}

updateCounter();
descriptionInput.addEventListener('input', updateCounter);


