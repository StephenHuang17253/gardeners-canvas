// Check the pathname to see if app is deployed
function checkInstance(currentUrl) {
    // If app is deployed, add correct instance to url
    let instance = "";
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    return instance;
}

function createGarden() {
    let currentUrl = window.location.pathname;


    //if currentURL is not create new garden send them to this page
    if (currentUrl !== '/create-new-garden') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        let instance = checkInstance(currentUrl)
        window.location.href = `${instance}/create-new-garden`;

    }
    const previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}

function openRegisterPage() {
    const currentUrl = window.location.pathname;

    // if currentURL is not register send them to this page
    if (currentUrl !== '/register') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        let instance = checkInstance(currentUrl)
        window.location.href = `${instance}/register`;

    }
    const previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}

function openLoginPage() {
    const currentUrl = window.location.pathname;

    // if currentURL is not login send them to this page
    if (currentUrl !== '/login') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        let instance = checkInstance(currentUrl)
        window.location.href = `${instance}/login`;

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }
}

function openManageFriendsPage() {
    const currentUrl = window.location.pathname;

    // if currentURL is not manage-friends send them to this page
    if (currentUrl !== '/manage-friends') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to manage-friends page
        let instance = checkInstance(currentUrl)
        window.location.href = `${instance}/manage-friends#friends`;

    }
    const previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}


function openPublicGardens() {
    var currentUrl = window.location.pathname;

    // if currentURL is not /public-gardens send them to this page
    if (currentUrl !== '/public-gardens') {
        localStorage.setItem('previousUrl', window.location.href);

        // Redirect to create-new-garden page
        let instance = checkInstance(currentUrl)
        window.location.href = `${instance}/public-gardens`;

    }
    var previousUrl = localStorage.getItem('previousUrl');
    // Default the return URL to home page if not set
    if (!previousUrl) {
        localStorage.setItem('previousUrl', '/home');
    }

}
