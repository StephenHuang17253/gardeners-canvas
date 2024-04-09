let addressInput = ''; // Store the current address entered by the user

// Function to make a LocationIQ API request
const fetchLocationIQData = async(query) => {
    try {
        const response = await fetch(`/api/location/suggestions?query=${query}`);
        const data = await response.json();

        if (data === 429) {
            showRateLimitMessage();
            hideNoMatchingLocationMessage();
        } else {
            hideRateLimitMessage();
        }
        console.log(data);
        return data
    } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
    }
}

// Call 3 requests at once to test if rate limit works.
const testRateLimit = async(query) => {
    try {
        const [response, response2, response3] = await Promise.all([
            fetch(`/api/location/suggestions?query=${query}`),
            fetch(`/api/location/suggestions?query=${query}`),
            fetch(`/api/location/suggestions?query=${query}`),
        ]);
        const data = await response.json();
        const data2 = await response2.json();
        const data3 = await response3.json();

        if (data === 429 || data2 === 429 || data3 === 429) {
            showRateLimitMessage();
            hideNoMatchingLocationMessage();
            console.log(data);
            return;
        } else {
            hideRateLimitMessage();
        }

        console.log("Rate limit exceeded")
    } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
    }
}


// Type 'Fabian Gilson' to test rate limit
document.getElementById('streetAddress').addEventListener('input', function(event) {
    const inputText = event.target.value.trim();
    if (inputText === "Fabian Gilson") {
        testRateLimit(inputText);
    }
});

// Function to update the autocomplete suggestions
function updateAutocompleteSuggestions(suggestions) {
    const autocompleteDropdown = document.getElementById('autocompleteSuggestions');
    autocompleteDropdown.innerHTML = ''; // Clear previous suggestions

    suggestions.forEach(suggestion => {
        const option = document.createElement('li');
        option.textContent = suggestion.display_name;
        autocompleteDropdown.appendChild(option);

        option.addEventListener('click', function() {
            fillAddressFields(suggestion);
            autocompleteDropdown.classList.remove('show');
            hideNoMatchingLocationMessage();
        });
    });
    autocompleteDropdown.classList.add('show'); // Add the 'show' class to display the suggestions dropdown
}

function hideAutocompleteDropdown() {
    const autocompleteDropdown = document.getElementById('autocompleteSuggestions');
    autocompleteDropdown.innerHTML = ''; // Clear the dropdown content
    autocompleteDropdown.classList.remove('show'); // Remove the 'show' class to hide the dropdown
}

function showNoMatchingLocationMessage() {
    const noMatchingLocationMessage = document.getElementById('noMatchingLocationMessage');
    noMatchingLocationMessage.classList.add('show');
}

function hideNoMatchingLocationMessage() {
    const noMatchingLocationMessage = document.getElementById('noMatchingLocationMessage');
    noMatchingLocationMessage.classList.remove('show');
}

function showRateLimitMessage() {
    const rateLimitMessage = document.getElementById('rateLimitMessage');
    rateLimitMessage.classList.add('show');
}

function hideRateLimitMessage() {
    const rateLimitMessage = document.getElementById('rateLimitMessage');
    rateLimitMessage.classList.remove('show');
}

function debounce(func, delay) {
    let timeoutId;
    return function() {
        const context = this;
        const args = arguments;
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(context, args), delay);
    };
}

// Event listener for input in the street address field and autocomplete
document.getElementById('streetAddress').addEventListener('input', debounce(async function (event) {
    addressInput = event.target.value.trim(); // Update the current address variable
    if (addressInput.length >= 3) {
        const suggestions = await fetchLocationIQData(addressInput); // Make a LocationIQ API request when address is at least 3 characters long
        if (suggestions && suggestions.length > 0) {
            updateAutocompleteSuggestions(suggestions) // Update autocomplete suggestions
        } else {
            hideAutocompleteDropdown()
            showNoMatchingLocationMessage()
        }
    }

}, 300));

// Event listener for when the street address field loses focus
document.getElementById('streetAddress').addEventListener('blur', function(event) {
    hideAutocompleteDropdown(); // Hide autocomplete suggestions when the street address field loses focus
});


// Event listener for when the autocomplete dropdown is opened
document.getElementById('autocompleteSuggestions').addEventListener('mousedown', function(event) {
    event.preventDefault(); // Prevent the dropdown from losing focus when clicking inside it
});

// Event listener for clicking anywhere outside the street address field and the autocomplete dropdown
document.addEventListener('click', function(event) {
    const streetAddressField = document.getElementById('streetAddress');
    const autocompleteDropdown = document.getElementById('autocompleteSuggestions');
    if (event.target !== streetAddressField && event.target !== autocompleteDropdown) {
        hideAutocompleteDropdown(); // Hide autocomplete suggestions when clicking outside
    }
});


// If input is too short for autocomplete, hide suggestion box.
document.getElementById('streetAddress').addEventListener('input', function(event) {
    const autocompleteDropdown = document.getElementById('autocompleteSuggestions');
    if (event.target.value.trim().length < 3) {
        hideAutocompleteDropdown();
        hideNoMatchingLocationMessage();
    } else {
        if (!autocompleteDropdown.classList.contains('show')) {
            showNoMatchingLocationMessage();
        }
    }

});

// Event listener for selecting an autocomplete suggestion from the dropdown
document.getElementById('autocompleteDropdown').addEventListener('change', function(event) {
    fillAddressFields(event.target.value); // Update fields with values from selected suggestion
});

// If a autocomplete suggestion is chosen, fill in form fields with the address components.
function fillAddressFields(data) {
    let streetAddress = ""
    const streetNumber = data.address.house_number || "";
    const streetName = data.address.road || "";

    if (streetNumber !== "") {
        streetAddress += streetNumber;
    }
    if (streetName !== "" && streetNumber === "") {
        streetAddress += streetName
    }
    if (streetName !== "" && streetNumber !== "") {
        streetAddress += " " + streetName
    }

    document.getElementById('streetAddress').value = streetAddress;
    document.getElementById('suburb').value = data.address.suburb || "";
    document.getElementById('city').value = data.address.city || "";
    document.getElementById('postcode').value = data.address.postcode || "";
    document.getElementById('country').value = data.address.country || "";
    // updateGardenLocation();
}



