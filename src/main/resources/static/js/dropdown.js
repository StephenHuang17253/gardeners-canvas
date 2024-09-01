function onDropdownItemSelect(item) {
    document.getElementById("plantCategoryButton").innerHTML = item.innerHTML;
    document.getElementById("plantCategory").value = item.innerHTML;
}


document.addEventListener('DOMContentLoaded', function() {
    const plantCategoryValue = document.getElementById("plantCategory").value.toString()
    if (plantCategoryValue) {
        document.getElementById("plantCategoryButton").innerHTML = plantCategoryValue
    }

})