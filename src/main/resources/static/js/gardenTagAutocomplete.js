const MIN_TAG_INPUT_LENGTH = 1;
const form = document.getElementById("tagForm");
const tagField = document.getElementById("tagInput")
const tagAutocompleteDropdown = document.getElementById("tagAutocompleteSuggestions")
const tagRegex = /^[a-zA-Z0-9\s\-_']+$/;

/**
 * Gets the name of a tag
 * @param data the tag
 * @returns {string} the tag name
 */
const getDisplayString = (data) => {
    return data.tagName;
}

/**
 * Fills tag field with provided data
 * @param data - data containing the tag name
 */
const fillTagField = (data) => {
    tagField.value = getDisplayString(data)
    if (form) {
        form.submit();
    } else {
        document.getElementById("addTagButton").click();
    }
}

/**
 * Hides the tag autocomplete dropdown
 */
const hideTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.add("d-none");

/**
 * Shows the tag autocomplete dropdown
 */
const showTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.remove("d-none");

/**
 * Clears the autocomplete dropdown.searchPublicTagAutocomplete
 * @returns {string}
 */
const clearTagAutocompleteDropdown = () => tagAutocompleteDropdown.innerHTML = "";

/**
 * Handles a clicked tag suggestion, filling tag field and hiding dropdowns
 * @param tagSuggestion - the selected suggestion
 */
const handleTagSuggestionClicked = (tagSuggestion) => {
    fillTagField(tagSuggestion);
    hideTagAutocompleteDropdown();
}

/**
 * Updates the tag autocomplete dropdown with new suggestions
 * @param tagSuggestions - an array of suggestion tags
 */
const updateTagAutocompleteDropdown = (tagSuggestions) => {
    clearTagAutocompleteDropdown();

    tagAutocompleteDropdown.style.width = `${tagField.offsetWidth}px`
    tagSuggestions.forEach(tag => {
        const listElement = document.createElement("li");
        listElement.classList.add("list-group-item", "py-2");
        const div = document.createElement("div");
        div.textContent = getDisplayString(tag);
        div.classList.add("cursor-pointer", "darken-on-hover", "rounded", "p-2");
        div.style.overflowWrap = "break-word";
        div.style.textAlign = "left"
        div.addEventListener("click", () => handleTagSuggestionClicked(tag));
        listElement.appendChild(div);
        tagAutocompleteDropdown.appendChild(listElement);

    });
}

/**
 * Handles updates to the input field such as char input and the field being selected,
 *      fetching suggestions and updating the UI accordingly
 * @param event - The input event
 */
const handleTagUpdate = async (event) => {
    const value = event.target.value.trim();

    if (value === "") {
        hideTagAutocompleteDropdown();
        return;
    }

    if (value.length < MIN_TAG_INPUT_LENGTH) {
        hideTagAutocompleteDropdown();
        return;
    }

    if (!tagRegex.test(value)) {
        hideTagAutocompleteDropdown();
        return;
    }

    const tagSuggestions = await fetchTagData(event.target.value);

    if (tagSuggestions === undefined || tagSuggestions.length === 0) {
        hideTagAutocompleteDropdown();
        return;
    }

    updateTagAutocompleteDropdown(tagSuggestions);
    showTagAutocompleteDropdown();
}

/**
 * Handles deselecting an input focus, hiding dropdowns
 */
const handleTagDeselect = () => {
    hideTagAutocompleteDropdown();
}

/**
 * Handles click events, hding dropdowns if the click is outside the input field
 * @param event - The input event
 */
const handleTagClick = (event) => {
    if (event.target !== tagField && event.target !== tagAutocompleteDropdown) {
        handleTagDeselect();
    }
}

/**
 * Fetches tag data
 * @param query - the query string to search for
 * @returns {Promise<any>} - A promise that resolves with the fetched data
 */
const fetchTagData = async (query) => {
    const instance = getInstance();
    const response = await fetch(`/${instance}tag/suggestions?query=${query}`)
    return await response.json();
}

const handleEnterKeyPress = (event) => {
    if (event.key === 'Enter') {
        hideTagAutocompleteDropdown();
    }
}

// Event listeners
tagField.addEventListener("keypress", handleEnterKeyPress);
tagField.addEventListener("input", handleTagUpdate);
tagField.addEventListener("focus", handleTagUpdate);
tagField.addEventListener("blur", handleTagDeselect);
document.addEventListener("click", handleTagClick);
tagAutocompleteDropdown.addEventListener("mousedown", event => event.preventDefault());