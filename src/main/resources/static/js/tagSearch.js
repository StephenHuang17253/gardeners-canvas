const tagInput = document.getElementById('tagInput');
const addTagButton = document.getElementById('addTagButton');
const appliedTagsList = document.getElementById('appliedTagsList');
const appliedTagsInputs = document.getElementById('appliedTagsInputs');
const searchTagErrorText = document.getElementById('searchTagErrorText')
const maxTextLength = 50;


/**
 * handles when the user enters a tag, 
 * check if that tag exists, if so displays an error message, 
 * otherwise adds it to the list of applied tags for the search
 */
const handleButtonClick = async () => {
    const value = tagInput.value.trim();

    const tagExists = await checkTagExists(value);

    if (!tagExists) {
        tagInput.classList.add('border-danger');
        searchTagErrorText.textContent = `No tag matching "${value}"`;
        cutOffText(searchTagErrorText, maxTextLength);
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

    div2.appendChild(span1);
    div1.appendChild(div2);
    appliedTagsList.appendChild(div1);

    const input = document.createElement('input');
    input.value = value;
    input.name = 'appliedTags';
    input.type = 'hidden';
    appliedTagsInputs.appendChild(input);


    tagInput.value = '';

}

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
    if (appliedTagsList.childElementCount === 1) {
        appliedTagsList.classList.add('d-none');
    }
}

/**
 * Cuts off text if it exceeds the max length
 * @param {HTMLElement} element - The element containing the text
 * @param {number} maxLength - The maximum length of the text
 */
const cutOffText = (element, maxLength) => {
    let text = element.textContent;
    if (text.length > maxLength) {
        element.textContent = text.substring(0, maxLength - 3) + "...\"";
    }
}


window.addEventListener('load', hideTagSection);
tagInput.addEventListener('keypress', handleKeyPress);
addTagButton.addEventListener('click', handleButtonClick);
