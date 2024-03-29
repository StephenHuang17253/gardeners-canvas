
let autocomplete;

function initAutocomplete() {
    // Create the autocomplete object
    autocomplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */(document.getElementById('streetAddress')),
        {
            types: ['geocode', 'establishment'],
        });

    autocomplete.setFields(['address_components', 'geometry']);
    // When the user selects an address from the dropdown, populate ALL fields in the form.
    autocomplete.addListener('place_changed', fillInAddress);

    // The code below is for when the user manually fills out the fields instead of using autocomplete
    // or for when they modify the autcompleted values
    const streetAddressField = document.getElementById('streetAddress');
    const suburbField = document.getElementById('suburb');
    const cityField = document.getElementById('city');
    const postcodeField = document.getElementById('postcode');
    const countryField = document.getElementById('country');

    streetAddressField.addEventListener('input', updateGardenLocation);
    suburbField.addEventListener('input', updateGardenLocation);
    cityField.addEventListener('input', updateGardenLocation);
    postcodeField.addEventListener('input', updateGardenLocation);
    countryField.addEventListener('input', updateGardenLocation);

}


function fillInAddress() {
    const place = autocomplete.getPlace();
    let addr = "";

    let streetNumber;
    let route;
    let suburb;
    let city;
    let postcode;
    let country;

    for (const component of place.address_components) {
        const componentType = component.types[0];

        switch (componentType) {
            case "street_number":
                streetNumber = component.long_name;
                break;
            case "route":
                route = component.long_name;
                break;
            case "sublocality":
            case "sublocality_level_1":
            case "neighborhood":
                suburb = component.long_name;
                break;
            case "locality":
                city = component.long_name;
                break;
            case "postal_code":
                postcode = component.long_name;
                break;
            case "country":
                country = component.long_name;
                break;
        }
    }

    // addr = `${streetNumber} ${route}, ${suburb}, ${city} ${postcode}, ${country}`;

    document.getElementById('streetAddress').value = streetNumber + ' ' + route;
    document.getElementById('suburb').value = suburb;
    document.getElementById('city').value = city;
    document.getElementById('postcode').value = postcode;
    document.getElementById('country').value = country;

    updateGardenLocation();
}

function updateGardenLocation() {
    const streetAddressField = document.getElementById('streetAddress').value;
    const suburbField = document.getElementById('suburb').value;
    const cityField = document.getElementById('city').value;
    const postcodeField = document.getElementById('postcode').value;
    const countryField = document.getElementById('country').value;

    //const fieldValues = `${streetAddressField}, ${suburbField}, ${cityField} ${postcodeField}, ${countryField}`;
    let fieldValues = "";
    if (streetAddressField) {
        fieldValues += `${streetAddressField}, `
    }
    if (suburbField) {
        fieldValues += `${suburbField}, `
    }
    if (cityField) {
        fieldValues += `${cityField}`
        if (!postcodeField) {
            fieldValues += ", "
        } else {
            fieldValues += " "
        }
    }
    if (postcodeField) {
        fieldValues += `${postcodeField}, `
    }
    if (countryField) {
        fieldValues += `${countryField}`
    }

    document.getElementById('gardenLocation').value = fieldValues;
}

window.initAutocomplete = initAutocomplete;
