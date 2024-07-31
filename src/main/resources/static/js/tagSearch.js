const tagInput = document.getElementById('tagInput');
const addTagButton = document.getElementById('addTagButton');
const appliedTags = document.getElementById('appliedTags');
const appliedTagsInputs = document.getElementById('appliedTagsInputs');


const handleButtonClick = () => {
    const div1 = document.createElement('div');
    div1.classList.add('p-1');
    const div2 = document.createElement('div');
    const span1 = document.createElement('span');
    span1.classList.add('badge', 'rounded-pill', 'text-bg-success', 'p-2');
    span1.textContent = tagInput.value;

    div2.appendChild(span1);
    div1.appendChild(div2);
    appliedTags.appendChild(div1);

    const input = document.createElement('input');
    input.value = tagInput.value;
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