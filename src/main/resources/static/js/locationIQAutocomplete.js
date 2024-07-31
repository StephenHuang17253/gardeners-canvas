const RATE_LIMIT_MESSAGE = "Exceeded limit of 2 autocomplete requests per second. Please try again";
const NO_MATCHING_LOCATION_MESSAGE = "No matching location found, location-based services may not work";
const MIN_API_CALL_INPUT_LENGTH = 3;
const API_CALL_DELAY = 300;
const TOO_MANY_REQUESTS_CODE = 429;
const MIN_INPUT_LENGTH_MESSAGE = "Please enter at least " + MIN_API_CALL_INPUT_LENGTH + " characters";

const autocompleteDropdown = document.getElementById("autocompleteSuggestions");
const autocompleteErrorMessage = document.getElementById("autocompleteError");
const streetAddressField = document.getElementById("streetAddress");

const getDisplayString = (data) => {
    const address = data.address
    const streetNumber = address.house_number || "";
    const streetName = address.road || "";
    const streetAddress = streetNumber + " " + streetName;
    const addressParts = [
        streetAddress, address.suburb || "", address.city || "", address.postcode || "", address.country || ""
    ]
    return addressParts.filter(addressPart => addressPart !== "").join(", ")
}


/**
 * Fills address fields with provided data.
 * @param {Object} data - Data containing address information.
 * @returns {void}
 */
const fillAddressFields = (data) => {
    const address = data.address
    const streetNumber = address.house_number || "";
    const streetName = address.road || "";
    const streetAddress = streetNumber + " " + streetName;

    document.getElementById("streetAddress").value = streetAddress;
    document.getElementById("suburb").value = address.suburb || "";
    document.getElementById("city").value = address.city || "";
    document.getElementById("postcode").value = address.postcode || "";
    document.getElementById("country").value = address.country || "";
    document.getElementById("longitude").value = data.lon || "";
    document.getElementById("latitude").value = data.lat || "";
}

/**
 * Hides the autocomplete dropdown.
 * @returns {void}
 */
const hideAutocompleteDropdown = () => autocompleteDropdown.classList.add("d-none");

/**
 * Shows the autocomplete dropdown.
 * @returns {void}
 */
const showAutocompleteDropdown = () => autocompleteDropdown.classList.remove("d-none");

/**
 * Clears the autocomplete dropdown.
 * @returns {void}
 */
const clearAutocompleteDropdown = () => autocompleteDropdown.innerHTML = "";

/**
 * Handles a clicked suggestion, filling address fields and hiding dropdowns.
 * @param {Object} suggestion - The selected suggestion object.
 * @returns {void}
 */
const handleSuggestionClicked = (suggestion) => {
    fillAddressFields(suggestion);
    hideAutocompleteDropdown();
    hideAutocompleteErrorMessage();
}

/**
 * Updates the autocomplete dropdown with new suggestions.
 * @param {Array} suggestions - An array of suggestion objects.
 * @returns {void}
 */
const updateAutocompleteDropdown = (suggestions) => {
    clearAutocompleteDropdown();
    suggestions.forEach(suggestion => {
        const listElement = document.createElement("li");
        listElement.classList.add("list-group-item");
        listElement.classList.add("py-2");
        const div = document.createElement("div");
        div.textContent = getDisplayString(suggestion);
        div.classList.add("cursor-pointer");
        div.classList.add("darken-on-hover");
        div.classList.add("rounded");
        div.classList.add("p-2");
        div.addEventListener("click", () => handleSuggestionClicked(suggestion));
        listElement.appendChild(div);
        autocompleteDropdown.appendChild(listElement);
    });
}

/**
 * Hides the autocomplete error message.
 * @returns {void}
 */
const hideAutocompleteErrorMessage = () => autocompleteErrorMessage.classList.add("d-none");

/**
 * Sets the text content of the autocomplete error message element.
 * @param {string} message - The error message to display.
 * @returns {void}
 */
const setAutocompleteErrorMessage = (message) => {
    autocompleteErrorMessage.textContent = message;
    autocompleteErrorMessage.classList.remove("d-none");
};

/**
 * Fetches location IQ data.
 * @param {string} query - The query string to search for.
 * @returns {Promise<Object>} A promise that resolves with the fetched data.
 */
const fetchLocationIQData = async (query) => {
    try {
        const instance = getInstance();
        const response = await fetch(`/${instance}api/location/suggestions?query=${query}`);
        const data = await response.json();
        return data
    } catch (error) {
        console.error("There was a problem with the fetch operation:", error);
    }
};

/**
 * Handles updates to the input field such as char input and the field being selected, fetching suggestions and updating the UI accordingly.
 * @param {Event} event - The input event.
 * @returns {void}
 */
const handleUpdate = debounce(async (event) => {

    hideAutocompleteErrorMessage();

    if (event.target.value === "") {
        hideAutocompleteDropdown();
        return;
    }

    if (event.target.value.length < MIN_API_CALL_INPUT_LENGTH) {
        hideAutocompleteDropdown();
        setAutocompleteErrorMessage(MIN_INPUT_LENGTH_MESSAGE);
        return;
    }

    const suggestions = await fetchLocationIQData(event.target.value);

    if (suggestions === TOO_MANY_REQUESTS_CODE) {
        hideAutocompleteDropdown();
        setAutocompleteErrorMessage(RATE_LIMIT_MESSAGE);
        return;
    }

    if (suggestions === undefined || suggestions.error !== undefined || suggestions.length === 0) {
        hideAutocompleteDropdown();
        setAutocompleteErrorMessage(NO_MATCHING_LOCATION_MESSAGE);
        return;
    }

    updateAutocompleteDropdown(suggestions);
    showAutocompleteDropdown();

}, API_CALL_DELAY);

/**
 * Handles deselecting an input focus, hiding dropdowns.
 * @returns {void}
 */
const handleDeselect = () => {
    hideAutocompleteDropdown();
    hideAutocompleteErrorMessage();
}

/**
 * Handles click events, hiding dropdowns if the click is outside the input field.
 * @param {Event} event - The input event.
 * @returns {void}
 */
const handleClick = (event) => {
    if (event.target !== streetAddressField && event.target !== autocompleteDropdown) {
        handleDeselect();
    }
}

/**
 * This is needed, don"t remove.
 * Morgan requested a debouncing implementation as a prerequisite to approval for the API.
 * Delays call of a function until after a specified time.
 * @param {Function} func The function to debounce.
 * @param {number} delay The delay in milliseconds before invoking the function after the last call.
 * @returns {Function} Returns the debounced function.
 */
function debounce(func, delay) {
    let timeoutId;
    return function () {
        const context = this;
        const args = arguments;
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => func.apply(context, args), delay);
    };
}

// Event listeners

streetAddressField.addEventListener("input", handleUpdate);

streetAddressField.addEventListener("focus", handleUpdate);

streetAddressField.addEventListener("blur", handleDeselect);

document.addEventListener("click", handleClick);

autocompleteDropdown.addEventListener("mousedown", event => event.preventDefault());