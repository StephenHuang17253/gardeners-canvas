// the dropdown is based on CHAT GPT as I couldn't figure out how to make the bar appear using thymeleaf
document.addEventListener('DOMContentLoaded', function() {
    const addFriendButton = document.getElementById('addFriendButton');
    const searchBarContainer = document.getElementById('searchBarContainer');

    addFriendButton.addEventListener('click', function() {
        // Check if the search bar is already present
        if (!document.getElementById('friendSearchBar')) {
            const searchBar = document.createElement('input');
            searchBar.setAttribute('type', 'text');
            searchBar.setAttribute('id', 'friendSearchBar');
            searchBar.setAttribute('placeholder', 'Search');
            searchBar.classList.add('search-bar');
            searchBarContainer.appendChild(searchBar);
            const search = document.createElement('button');
            search.setAttribute('id', 'friendSearchSubmitButton');
            search.textContent = 'Search';
            search.classList.add('user_button');
            searchBarContainer.appendChild(search);
        }
    });
});