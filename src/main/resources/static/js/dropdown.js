function onDropdownItemSelect(item) {
    document.getElementById("plantCategoryButton").innerHTML = item.innerHTML;
    document.getElementById("plantCategory").value = item.innerHTML;
}


function onDropdownInputChange(item) {
    document.getElementById("plantCategoryButton").innerHTML = item.value;
}