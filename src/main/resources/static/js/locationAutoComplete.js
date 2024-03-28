
let autocomplete;

function initAutocomplete() {
    // Create the autocomplete object, restricting the search to geographical
    // location types.
    autocomplete = new google.maps.places.Autocomplete(
        /** @type {!HTMLInputElement} */(document.getElementById('streetAddress')),
        {
            types: ['geocode', 'establishment'],
        });

    // When the user selects an address from the dropdown, populate the address
    // fields in the form.
    autocomplete.addListener('place_changed', fillInAddress);
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

    addr = `${streetNumber} ${route}, ${suburb}, ${city} ${postcode}, ${country}`;

    document.getElementById('streetAddress').value = streetNumber + ' ' + route;
    document.getElementById('suburb').value = suburb;
    document.getElementById('city').value = city;
    document.getElementById('postcode').value = postcode;
    document.getElementById('country').value = country;

    document.getElementById('gardenLocation').value = addr;
}


window.initAutocomplete = initAutocomplete;
