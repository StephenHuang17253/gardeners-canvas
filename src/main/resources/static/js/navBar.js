
/**
 * Handles setting checking if the server is being run on test or prod.
 * 
 * @param {String} currentUrl - The current url of the page.
 * @return {String} - The base url of the page.
 */
const getBaseUrl = (currentUrl) => {
    let instance = '';
    if (currentUrl.includes('/test/')) {
        instance = '/test';
    } else if (currentUrl.includes('/prod/')) {
        instance = '/prod';
    }
    return instance;
}

/**
 * Handles navigating to the inputted url, will not change pages to the same page.
 * 
 * @param {String} toUrl - The current url of the page.
 * @return {void} - No return value.
 */
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