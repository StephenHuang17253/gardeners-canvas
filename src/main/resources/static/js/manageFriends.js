document.addEventListener('DOMContentLoaded', function () {
    const tabs = document.querySelectorAll('.tab-links a');
    const tabContents = document.querySelectorAll('.tab');


    tabs.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();
            activateTab(this);
        });
    });

    /**
    * This function handles the friend request.
    * It changes the value of the thymeleaf variable 'friendAccepted' to true when the user presses submit.
    * Then the form is submitted
    * @param tab the tab button the user pressed, for example 'Friends'
    */
    function activateTab(tab) {
        const target = tab.getAttribute('href').substring(1);
        tabs.forEach(tab => tab.parentElement.classList.remove('active'));
        tabContents.forEach(tabContent => tabContent.classList.remove('active'));

        tab.parentElement.classList.add('active');
        document.getElementById(target).classList.add('active');

        window.location.hash=target;
    }


    // Check if the URL contains '/search' so we can change the tab
    if (window.location.pathname.includes('/search')) {
        const searchTab = document.querySelector('.tab-links a[href="#search"]');
        if (searchTab) {
            activateTab(searchTab);
        }
    }

    // Function to check and activate the tab from URL hash
    function activateTabFromHash() {
        if (window.location.hash) {
            const hash = window.location.hash.substring(1);
            const hashTab = document.querySelector(`.tab-links a[href="#${hash}"]`);
            if (hashTab) {
                activateTab(hashTab);
            }
        }
    }

    // Activate tab if URL contains '/search'
    if (window.location.pathname.includes('/search')) {
        const searchTab = document.querySelector('.tab-links a[href="#search"]');
        if (searchTab) {
            activateTab(searchTab);
        }
    } else {
        // Activate the tab from the URL hash
        activateTabFromHash();
    }

    // Listen for hash change events
    window.addEventListener('hashchange', activateTabFromHash);

});


/**
 * This function handles the friend request.
 * It changes the value of the thymeleaf variable 'friendAccepted' to true when the user presses submit.
 * Then the form is submitted
 * @param button he button that was pressed
 * @param accepted true if the accept button was pressed falese otherwise
 */
function manageRequest(button, accepted) {
    console.log(accepted)
    const form = button.closest('form');
    const acceptedInput = form.querySelector('input[name="friendAccepted"][type="hidden"]');
    if (accepted) {
        acceptedInput.value = "true";
    } else {
        acceptedInput.value = "false";
    }
    form.submit();
}

function showModal(id) {
    const modal = document.getElementById("confirm-remove-form-"+id);
    modal.className = "then-show";
}


function closeModal(id) {
    const modal = document.getElementById("confirm-remove-form-"+id);
    modal.className = "initially-hidden";
}
