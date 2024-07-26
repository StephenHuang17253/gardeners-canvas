// Use getAllSimilar in GardenTagService
const MIN_TAG_INPUT_LENGTH = 3;

const tagField = document.getElementById("tag")
const tagAutocompleteDropdown = document.getElementById("tagAutocompleteSuggestions")

const fillTagField = (data) => {
    tagField.value = data
}

const hideTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.add("d-none");

const showTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.remove("d-none");

const clearTagAutocompleteDropdown = () => tagAutocompleteDropdown.classList.innerHTML = "";


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

    // if (event.target.value.length < MIN_TAG_INPUT_LENGTH) {
    //     hideTagAutocompleteDropdown();
    //     return;
    // }

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

const fetchTagData = async (query) => {
    const instance = getInstance();
    return await fetch(`/${instance}tag/suggestions?query=${query}`)

}



// Event listeners

tagField.addEventListener("input", handleTagUpdate);
tagField.addEventListener("focus", handleTagUpdate);
tagField.addEventListener("blur", handleTagDeselect);
document.addEventListener("click", handleTagClick);
tagAutocompleteDropdown.addEventListener("mousedown", event => event.preventDefault());