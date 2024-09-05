// Error name for when the user cancels the file picker dialog
const CANCEL_SAVE = 'AbortError';
// Directory to open to when using file picker for first time
const DOWNLOADS_DIR = 'downloads';
// ID to save the selected location for repeated file picker use
const FILE_PICKER_ID = 'filePickerID';

/**
 * Downloader class to handle saving blobs to files
 */
class Downloader {

    /**
     * Creates a new Downloader object
     * @param {Element} link - link element to use for downloading files
     */
    constructor(link) {
        this.link = link;
    }

    /**
     * Save the blob to a file using a link element
     * 
     * @param {Blob} blob - blob to save
     * @param {String} filename - name of the file to save the blob to
     */
    saveWithLink = (blob, filename) => {
        const blobURL = URL.createObjectURL(blob);
        document.body.appendChild(this.link);
        this.link.href = blobURL;
        this.link.download = filename;
        this.link.click();
        setTimeout(() => {
            URL.revokeObjectURL(blobURL);
            document.body.removeChild(this.link);
        }, 1000);
    };

    /**
     * Save the blob to a file using the File System Access API
     * 
     * @param {Blob} blob - blob to save
     * @param {String} filename - name of the file to save the blob to 
     */
    saveWithFilePicker = async (blob, filename) => {
        const handle = await showSaveFilePicker({
            suggestedName: filename,
            startIn: DOWNLOADS_DIR,
            id: FILE_PICKER_ID,
        });
        const writable = await handle.createWritable();
        await writable.write(blob);
        await writable.close();
    };

    /**
     * Save the blob to a file using a file picker if it is supported, otherwise use a link element
     * 
     * @param {Blob} blob 
     * @param {String} filename
     */
    saveFile = async (blob, filename) => {
        // Checks that the browser supports the File System Access API.
        const supportsFileSystemAccess = 'showSaveFilePicker' in window && window.self === window.top;

        if (supportsFileSystemAccess) {
            try {
                await this.saveWithFilePicker(blob, filename);
                return;
            } catch (err) {
                // Fail silently if the user has simply canceled the dialog
                if (err.name === CANCEL_SAVE) return;
            }
        }
        // Fallback if the File System Access API is not supported
        this.saveWithLink(blob, filename);
    };
}

export { Downloader };