function createGarden() {
    var currentUrl = window.location.pathname;
    //if currentURL is not create new garden send them to this page
    if (currentUrl !== '/create-new-garden') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        window.location.href = '/create-new-garden';

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to landing page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/landing');
    }

}
