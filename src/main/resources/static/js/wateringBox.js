// Stores a garden specific token for the watering message box which uses to compare the day it was closed with the current day
document.addEventListener('DOMContentLoaded', function () {
    const gardenId = document.getElementById('gardenId').value;
    const messageBox = document.getElementById('messageBox');
    const key = `messageBoxClosed_${gardenId}`;

    const closedDate = localStorage.getItem(key);
    const today = new Date().toISOString().split('T')[0];

    if (closedDate === today) {
        messageBox.style.display = 'none';
    }

    window.closeMessageBox = function () {
        messageBox.style.display = 'none';
        localStorage.setItem(key, today);
    };
});