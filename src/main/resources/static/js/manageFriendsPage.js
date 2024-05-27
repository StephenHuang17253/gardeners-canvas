// the dropdown is based on CHAT GPT as I couldn't figure out how to make the bar appear using thymeleaf
// document.addEventListener('DOMContentLoaded', function() {
//     const addFriendButton = document.getElementById('addFriendButton');
//     const searchBarContainer = document.getElementById('searchBarContainer');
//     addFriendButton.addEventListener('click', function() {
//         searchBarContainer.style.display = 'block';
//     });
// });

function submitForm() {
    const data = document.getElementById('data').value;

    fetch('/manage-friends/search', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({data: data})
    }).then(response => response.json())
    .then(responseData => {
        document.getElementById('response').innerText = responseData.message;
    })
    .catch(error => {
        console.error('Error:', error)
    });
}

