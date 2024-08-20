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
        localStorage.setItem("previousUrl", window.location.href);
        const baseUrl = getInstance();
        window.location.href = baseUrl + toUrl;
    }

    if (!localStorage.getItem("previousUrl")) {
        localStorage.setItem("previousUrl", "/home");
    }
}