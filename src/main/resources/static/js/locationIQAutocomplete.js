let addressInput = ''; // Store the current address entered by the user

// Function to make a LocationIQ API request
const fetchLocationIQData = async(query) => {
    try {
        const response = await fetch(`/api/location/suggestions?query=${query}`);
        const data = await response.json();
        console.log(data);
        return data
    } catch (error) {
        console.error('There was a problem with the fetch operation:', error);
    }
}

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
        });
    });
    autocompleteDropdown.classList.add('show'); // Add the 'show' class to display the suggestions dropdown
}

function hideAutocompleteDropdown() {
    const autocompleteDropdown = document.getElementById('autocompleteSuggestions');
    autocompleteDropdown.innerHTML = ''; // Clear the dropdown content
    autocompleteDropdown.classList.remove('show'); // Remove the 'show' class to hide the dropdown
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
        console.log('Suggestions:', suggestions);
        updateAutocompleteSuggestions(suggestions) // Update autocomplete suggestions
    }
}, 300));

// If input is too short for autocomplete, hide suggestion box.
// Also for cases where user initially types a valid input, then deletes it.
document.getElementById('streetAddress').addEventListener('input', function(event) {
    if (event.target.value.trim().length < 3) {
        hideAutocompleteDropdown();
    }
});

// Event listener for selecting an autocomplete suggestion from the dropdown
document.getElementById('autocompleteDropdown').addEventListener('change', function(event) {
    fillAddressFields(event.target.value); // Update fields with values from selected suggestion
});


/**
 * Was needed for updateGardenLocation to handle manual inputs when user didn't use autocomplete.
 * GardenLocation string is now being dynamically constructed in the controller.
 *
const addManualInputListeners = () => {
    const streetAddressField = document.getElementById('streetAddress');
    const suburbField = document.getElementById('suburb');
    const cityField = document.getElementById('city');
    const postcodeField = document.getElementById('postcode');
    const countryField = document.getElementById('country');

    streetAddressField.addEventListener('input', updateGardenLocation);
    suburbField.addEventListener('input', updateGardenLocation);
    cityField.addEventListener('input', updateGardenLocation);
    postcodeField.addEventListener('input', updateGardenLocation);
    countryField.addEventListener('input', updateGardenLocation);
};

addManualInputListeners();

**/



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

/** It stopped working and I don't know why
 * Handling this in GardenFormController now

function updateGardenLocation() {
    const streetAddressField = document.getElementById('streetAddress').value || "";
    const suburbField = document.getElementById('suburb').value || "";
    const cityField = document.getElementById('city').value || "";
    const postcodeField = document.getElementById('postcode').value || "";
    const countryField = document.getElementById('country').value || "";

    console.log(streetAddressField)
    console.log(suburbField)
    console.log(cityField)
    console.log(postcodeField)
    console.log(countryField)

    //const fieldValues = `${streetAddressField}, ${suburbField}, ${cityField} ${postcodeField}, ${countryField}`;
    let fieldValues = "";
    if (streetAddressField !== "") {
        fieldValues += `${streetAddressField}, `
    }
    if (suburbField !== "") {
        fieldValues += `${suburbField}, `
    }
    if (cityField !== "") {
        fieldValues += `${cityField}`
        if (postcodeField === "") {
            fieldValues += ", "
        } else {
            fieldValues += " "
        }
    }
    if (postcodeField !== "") {
        fieldValues += `${postcodeField}, `
    }
    if (countryField !== "") {
        fieldValues += `${countryField}`
    }

    console.log(fieldValues)
    document.getElementById('gardenLocation').value = fieldValues;
}
**/


