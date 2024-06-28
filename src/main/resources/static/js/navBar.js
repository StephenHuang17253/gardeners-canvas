const getBaseUrl = (currentUrl) => {
    // Get the correct base url, taking into account if the app is deployed on test or prod
    let instance = '';
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    return instance;
}

const navigateTo = (toUrl) => {
    const currentUrl = window.location.pathname;

    // if currentURL is not toUrl send them to this page
    // Does not check for test and prod, see git lab issue
    if (currentUrl !== toUrl) {
        localStorage.setItem('previousUrl', window.location.href);
        const baseUrl = getBaseUrl(currentUrl);
        window.location.href = baseUrl + toUrl;
    }

    if (!localStorage.getItem('previousUrl')) {
        localStorage.setItem('previousUrl', '/home');
    }
}