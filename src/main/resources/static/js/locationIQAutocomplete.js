const locationiqKey = document.getElementById("locationIqAccessToken").value;

$(document).ready(function () {

    $('#streetAddress').autocomplete({
        minChars: 3,
        deferRequestBy: 250,
        serviceUrl: 'https://api.locationiq.com/v1/autocomplete.php',
        paramName: 'q',
        params: {
            key: locationiqKey,
            format: "json",
            limit: 5
        },
        ajaxSettings: {
            dataType: 'json'
        },
        transformResult: function (response) {
            var suggestions = $.map(response, function (dataItem) {
                return {
                    value: dataItem.display_name,
                    data: dataItem
                };
            });

            return {
                suggestions: suggestions
            };
        },
        onSelect: function (suggestion) {
            fillAddressFields(suggestion.data);
        }
    });

    // Event listener for manual inputs, in the case that user does not use autocomplete
    $('#streetAddress, #suburb, #city, #postcode, #country').on('input', function() {
        updateGardenLocation();
    });

    // If a autocomplete suggestion is chosen, fill in form fields with the address components.
    function fillAddressFields(data) {
        let streetAddress = ""
        const streetNumber = data.address.house_number || "";
        const streetName = data.address.road || "";

        if (streetNumber !== "") {
            streetAddress += streetNumber;
        }
        if (streetName !== "" && streetNumber === "") {
            streetAddress += streetName
        }
        if (streetName !== "" && streetNumber !== "") {
            streetAddress += " " + streetName
        }

        $('#streetAddress').val(streetAddress);
        $('#suburb').val(data.address.suburb || "");
        $('#city').val(data.address.city || "");
        $('#postcode').val(data.address.postcode || "");
        $('#country').val(data.address.country || "");
        updateGardenLocation();
    }

    // Update my hidden gardenLocation thymeleaf variable to pass back to GardenService through Controller
    // At the moment, I'm not storing the suburb, city, postcode, and country values for a Garden.
    // They're just being used to dynamically construct a string which is the value of a Garden's location attribute.
    // Might have to add new attributes to Garden class for new location fields to enable pre-filling of edit form.
    function updateGardenLocation() {
        const streetAddressField = document.getElementById('streetAddress').value || "";
        const suburbField = document.getElementById('suburb').value || "";
        const cityField = document.getElementById('city').value || "";
        const postcodeField = document.getElementById('postcode').value || "";
        const countryField = document.getElementById('country').value || "";

        //const fieldValues = `${streetAddressField}, ${suburbField}, ${cityField} ${postcodeField}, ${countryField}`;
        let fieldValues = "";
        if (streetAddressField !== "") {
            fieldValues += `${streetAddressField}, `
        }
        if (suburbField !== "") {
            fieldValues += `${suburbField}, `
        }
        if (cityField !== "") {
            fieldValues += `${cityField}`
            if (postcodeField === "") {
                fieldValues += ", "
            } else {
                fieldValues += " "
            }
        }
        if (postcodeField !== "") {
            fieldValues += `${postcodeField}, `
        }
        if (countryField !== "") {
            fieldValues += `${countryField}`
        }

        document.getElementById('gardenLocation').value = fieldValues;
    }
});

