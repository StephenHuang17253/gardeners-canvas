const form = document.getElementById("code-form");
const inputs = Array.from(document.getElementsByClassName("code-input"));
const tokenString = document.getElementById("tokenString");
const emailAddress = document.getElementById("emailAddress");

const KEYBOARDS = {
  backspace: 8,
  arrowLeft: 37,
  arrowRight: 39,
  tab: 9,
};

const updateTokenString = () => {
  tokenString.value = inputs.map(input => input.value).join('');
}

const handleInput = (e) => {
  const input = e.target;
  const nextInput = input.nextElementSibling;
  if (nextInput && input.value) {
    nextInput.focus();
    if (nextInput.value) {
      nextInput.select();
    }
  }
  updateTokenString();
}

const handlePaste = (e) => {
  e.preventDefault();
  const paste = e.clipboardData.getData('text');
  inputs.forEach((input, i) => {
    input.value = paste[i] || '';
  });
  updateTokenString();
}

const handleBackspace = (e) => {
  e.preventDefault();
  const lastFilledInput = inputs.findLast(input => input.value !== '');
  if (lastFilledInput.value) {
    lastFilledInput.value = '';
    updateTokenString();
    const previousInput = lastFilledInput.previousElementSibling;
    if (previousInput) {
      previousInput.focus();
    }
    return;
  }
  inputs[0].focus();
}

const handleArrowLeft = (e) => {
  e.preventDefault();
  const previousInput = e.target.previousElementSibling;
  if (!previousInput) return;
  previousInput.focus();
}

handleArrowRight = (e) => {
  e.preventDefault();
  const nextInput = e.target.nextElementSibling;
  if (!nextInput || e.target.value === '') return;
  nextInput.focus();
}

form.addEventListener('input', handleInput);
inputs[0].addEventListener('paste', handlePaste);

inputs.forEach(input => {

  input.maxLength = 1;

  input.addEventListener('keydown', e => {
    switch (e.keyCode) {
      case KEYBOARDS.backspace:
        handleBackspace(e);
        break;
      case KEYBOARDS.arrowLeft:
        handleArrowLeft(e);
        break;
      case KEYBOARDS.arrowRight:
        handleArrowRight(e);
        break;
      case KEYBOARDS.tab:
        handleArrowRight(e);
        break;
      default:
    }
  });
});
