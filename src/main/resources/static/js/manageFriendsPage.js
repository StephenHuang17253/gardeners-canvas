// the dropdown is based on CHAT GPT as I couldn't figure out how to make the bar appear using thymeleaf
document.addEventListener('DOMContentLoaded', function() {
    const addFriendButton = document.getElementById('addFriendButton');
    const searchBarContainer = document.getElementById('searchBarContainer');
    addFriendButton.addEventListener('click', function() {
        searchBarContainer.style.display = 'block';
    });
});