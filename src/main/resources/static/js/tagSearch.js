const tagInput = document.getElementById('tagInput');
const addTagButton = document.getElementById('addTagButton');
const appliedTags = document.getElementById('appliedTags');
const appliedTagsInputs = document.getElementById('appliedTagsInputs');
const searchTagErrorText = document.getElementById('searchTagErrorText')


const handleButtonClick = async () => {
    const value = tagInput.value

    const tagExists = await checkTagExists(value)
    
    if (!tagExists) {
        searchTagErrorText.textContent = `No tag matching "${value}"`
        return
    }

    const div1 = document.createElement('div');
    div1.classList.add('p-1');
    const div2 = document.createElement('div');
    const span1 = document.createElement('span');
    span1.classList.add('badge', 'rounded-pill', 'text-bg-success', 'p-2');
    span1.textContent = value;

    div2.appendChild(span1);
    div1.appendChild(div2);
    appliedTags.appendChild(div1);

    const input = document.createElement('input');
    input.value = value;
    input.name = 'appliedSearchTagsList';
    input.type = 'hidden';
    appliedTagsInputs.appendChild(input);


    tagInput.value = '';

}

const handleKeyPress = (event) => {
    if (event.key === 'Enter') {
        handleButtonClick()
    }
}


tagInput.addEventListener('keypress', handleKeyPress);
addTagButton.addEventListener('click', handleButtonClick);


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

/**
 * Fetches tag data
 * @param query - the query string to search for
 * @returns {Promise<any>} - A promise that resolves with the fetched data
 */
const checkTagExists = async (tagName) => {
    const instance = getInstance();

    const response =  await fetch(`/${instance}tag/exists?tagName=${tagName}`)
    console.log(response)
    const a = await response.json();
    console.log(a)
    return a

}