/**
 * Determines the instance based on the current URL path.
 * @returns {string} The instance ("", "test/" or "prod/") based on the URL path.
 */
const getInstance = () => {
    const path = window.location.pathname;
    let instance = "";
    if (path.includes("/test/")) {
        instance = "test/";
    } else if (path.includes("/prod/")) {
        instance = "prod/";
    }
    return instance;
};