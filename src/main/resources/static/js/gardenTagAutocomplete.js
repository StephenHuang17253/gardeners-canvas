// Use getAllSimilar in GardenTagService
const MIN_TAG_INPUT_LENGTH = 3;

const tagField = document.getElementById("tag")
const tagAutocompleteDropdown = document.getElementById("tagAutocompleteSuggestions")

const getDisplayString = (data) => {
    return data.tagName;
}

const fillTagField = (data) => {
    tagField.value = data
}

const hideTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.add("d-none");

const showTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.remove("d-none");

const clearTagAutocompleteDropdown = () => tagAutocompleteDropdown.innerHTML = "";


const handleTagSuggestionClicked = (tagSuggestion) => {
    fillTagField(tagSuggestion);
    hideAutocompleteDropdown();
}

const updateTagAutocompleteDropdown = (tagSuggestions) => {
    clearTagAutocompleteDropdown();
     tagSuggestions.forEach(tag => {
         const listElement = document.createElement("li");
         listElement.classList.add("list-group-item");
         listElement.classList.add("py-2");
         const div = document.createElement("div");
         div.textContent = getDisplayString(tag);
         div.classList.add("cursor-pointer");
         div.classList.add("darken-on-hover");
         div.classList.add("rounded");
         div.classList.add("p-2");
         div.addEventListener("click", () => handleTagSuggestionClicked(tag));
         listElement.appendChild(div);
         tagAutocompleteDropdown.appendChild(listElement);

     });
}


const handleTagUpdate = (async (event) => {
    if (event.target.value === "") {
        hideTagAutocompleteDropdown();
        return;
    }

    if (event.target.value.length < MIN_TAG_INPUT_LENGTH) {
        hideTagAutocompleteDropdown();
        return;
    }

    const tagSuggestions = await fetchTagData(event.target.value);

    if (tagSuggestions === undefined || tagSuggestions.length === 0) {
        hideTagAutocompleteDropdown();
        return;
    }

    updateTagAutocompleteDropdown(tagSuggestions)
    showTagAutocompleteDropdown();
})

const handleTagDeselect = () => {
    hideTagAutocompleteDropdown();
}

const handleTagClick = (event) => {
    if (event.target !== tagField && event.target !== tagAutocompleteDropdown) {
        handleTagDeselect();
    }
}

/**
 * Determines the instance based on the current URL path.
 * @returns {string} The instance ("", "test/" or "prod/") based on the URL path.
 */
const getInstance = () => {
    const path = window.location.pathname
    let instance = "";
    if (path.includes("/test/")) {
        instance = "test/";
    } else if (path.includes("/prod/")) {
        instance = "prod/";
    }
    return instance;
};

const fetchTagData = async (query) => {
    const instance = getInstance();

    const response =  await fetch(`/${instance}tag/suggestions?query=${query}`)
    return await response.json()

}



// Event listeners
console.log("Testing")
tagField.addEventListener("input", handleTagUpdate);
tagField.addEventListener("focus", handleTagUpdate);
tagField.addEventListener("blur", handleTagDeselect);
document.addEventListener("click", handleTagClick);
tagAutocompleteDropdown.addEventListener("mousedown", event => event.preventDefault());