const tagInput = document.getElementById('tagInput');
const addTagButton = document.getElementById('addTagButton');
const appliedTagsList = document.getElementById('appliedTagsList');
const appliedTagsInputs = document.getElementById('appliedTagsInputs');
const searchTagErrorText = document.getElementById('searchTagErrorText');


/**
 * handles when the user enters a tag, 
 * check if that tag exists, if so displays an error message, 
 * otherwise adds it to the list of applied tags for the search
 */
const handleButtonClick = async () => {
    const value = tagInput.value;

    const tagExists = await checkTagExists(value);

    if (!tagExists) {
        tagInput.classList.add('border-danger');
        searchTagErrorText.textContent = `No tag matching "${value}"`;
        return;
    }
    const tagAlreadyApplied = Array.from(appliedTagsInputs.querySelectorAll('input[name="appliedTags"]'))
        .some(input => input.value === value)
    if (tagAlreadyApplied) {
        searchTagErrorText.textContent = `"${value}" is already applied`;
        return;
    }


    appliedTagsList.classList.remove('d-none');
    searchTagErrorText.textContent = '';
    tagInput.classList.remove('border-danger');

    const div1 = document.createElement('div');
    div1.classList.add('p-1');
    const div2 = document.createElement('div');
    const span1 = document.createElement('span');
    span1.classList.add('badge', 'rounded-pill', 'text-bg-success', 'p-2');
    span1.textContent = value;
    span1.setAttribute('data-tag-name', value);
    span1.onclick = function() { removeTag(this); };
    span1.style.cursor = 'pointer';
    initializeTagHover(span1);

    div2.appendChild(span1);
    div1.appendChild(div2);
    appliedTagsList.appendChild(div1);

    const input = document.createElement('input');
    input.value = value;
    input.name = 'appliedTags';
    input.type = 'hidden';
    appliedTagsInputs.appendChild(input);
};
/**
 *  Finds and removes tag input element
 *  and removes it from appliedTagsInputs array
 *  @param element of tag to remove
 **/
const removeTag = (element) => {
    const tagName = element.getAttribute('data-tag-name');
    element.closest('.p-1').remove();
    const inputToRemove = Array.from(appliedTagsInputs.children).find(input => input.value === tagName);
    if (inputToRemove) {inputToRemove.remove();}
    hideTagSection();
};

/**
 * Checks if the user presses enter on the tag input, 
 * if so then call handleButtonClick, otherwise clear the error text
 * @param {Event} event - The keydown event.
 */
const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
        event.preventDefault();
        handleButtonClick();
    } else {
        searchTagErrorText.textContent = '';
    }
}

/**
 * Fetches tag data
 * @param query - the query string to search for
 * @returns {Promise<any>} - A promise that resolves with the fetched data
 */
const checkTagExists = async (tagName) => {
    const instance = getInstance();
    const response = await fetch(`/${instance}tag/exists?tagName=${tagName}`);
    return await response.json();
}


/**
 * Hides the applied tag section
 */
const hideTagSection = () => {
    console.log(appliedTagsList.childElementCount)
    if (appliedTagsList.childElementCount === 1) {
        appliedTagsList.classList.add('d-none');

    }
}
/**
 * Adds tag cover properties to tag, including red hover effect and cursor effect
 * @param tagElement input tag to apply hover and pointer style to
 **/
const initializeTagHover = (tagElement) => {
    tagElement.style.cursor = 'pointer';
    tagElement.onmouseover = () => tagElement.classList.replace('text-bg-success', 'text-bg-danger');
    tagElement.onmouseout = () => tagElement.classList.replace('text-bg-danger', 'text-bg-success');
};
hideTagSection()
tagInput.addEventListener('keypress', handleKeyPress);
addTagButton.addEventListener('click', handleButtonClick);
