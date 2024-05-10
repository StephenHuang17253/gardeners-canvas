function createGarden() {
    let currentUrl = window.location.pathname;
    let instance = "";
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    console.log(instance)
    //if currentURL is not create new garden send them to this page
    if (currentUrl !== '/create-new-garden') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        window.location.href = `${instance}/create-new-garden`;

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}

function openRegisterPage() {
    var currentUrl = window.location.pathname;
    let instance = "";
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    console.log(instance)
    // if currentURL is not register send them to this page
    if (currentUrl !== '/register') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        window.location.href = `${instance}/register`;

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}

function openLoginPage() {
    var currentUrl = window.location.pathname;
    let instance = "";
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    console.log(instance)
    // if currentURL is not login send them to this page
    if (currentUrl !== '/login') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        window.location.href = `${instance}/login`;

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}
