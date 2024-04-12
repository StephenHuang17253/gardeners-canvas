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
  const code = inputs.map(input => input.value).join('');
  console.log(code);
  tokenString.value = code;
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
  const input = e.target;
  if (input.value) {
    input.value = '';
    updateTokenString();
    return;
  }
  const previousInput = e.target.previousElementSibling;
  if (!previousInput) return;
  previousInput.focus();
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
