const tagInput = document.getElementById('tagInput');
const addTagButton = document.getElementById('addTagButton');
const appliedTagsList = document.getElementById('appliedTagsList');
const appliedTagsInputs = document.getElementById('appliedTagsInputs');
const searchTagErrorText = document.getElementById('searchTagErrorText')


const handleButtonClick = async () => {
    const value = tagInput.value;

    const tagExists = await checkTagExists(value);
    
    if (!tagExists) {
        searchTagErrorText.textContent = `No tag matching "${value}"`;
        return;
    }

    appliedTagsList.classList.remove('d-none');
    searchTagErrorText.textContent = '';

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

    const response =  await fetch(`/${instance}tag/exists?tagName=${tagName}`);
    return await response.json();
}


const hideTagSection = () => {
    if (appliedTagsList.childElementCount === 1) {
        appliedTagsList.classList.add('d-none');
    }
}


window.addEventListener("load", hideTagSection);
tagInput.addEventListener('keypress', handleKeyPress);
addTagButton.addEventListener('click', handleButtonClick);
