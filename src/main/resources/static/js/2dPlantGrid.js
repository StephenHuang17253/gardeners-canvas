import { Downloader } from "./Downloader.js";

const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");

const errorElement = document.getElementById("error-message");
const confirmClearAllButton = document.getElementById("confirmClearAll");
const deletePlantButton = document.getElementById("deletePlant");
const saveGardenForm = document.getElementById("saveGardenForm");

const idListInput = document.getElementById("idList");
const xCoordListInput = document.getElementById("xCoordList");
const yCoordListInput = document.getElementById("yCoordList");

const plantItems = document.querySelectorAll("[name='plant-item']");
const gridItemLocations = document.querySelectorAll("[name='grid-item-location']");

const pagination = document.getElementById("pagination");
const firstPage = document.getElementById("firstPage");
const previousPage = document.getElementById("previousPage");
const currentPage = document.getElementById("currentPage");
const nextPage = document.getElementById("nextPage");
const lastPage = document.getElementById("lastPage");


const STAGE_WIDTH = window.innerWidth * 0.8;
const STAGE_HEIGHT = window.innerHeight * 0.9;
const GRID_SIZE = Math.min(STAGE_WIDTH, STAGE_HEIGHT) / 8;
const GRID_COLUMNS = 7;
const GRID_ROWS = 7;

const GRID_WIDTH = GRID_COLUMNS * GRID_SIZE;
const GRID_HEIGHT = GRID_ROWS * GRID_SIZE;

const OFFSET_X = (STAGE_WIDTH - GRID_WIDTH) / 2;
const OFFSET_Y = (STAGE_HEIGHT - GRID_HEIGHT) / 2;

const INVALID_LOCATION = "Please place on the grid";
const NO_PLANT_SELECTED = "Please select a plant to delete";
const OCCUPIED_DESTINATION = "Space is already occupied";
const FULL_GRID = "No more space left on the garden"
const ERROR_MESSAGE_DURATION = 3000;

const instance = getInstance();

const gardenName = document.getElementById("gardenName").value;
const gardenId = document.getElementById("gardenId").value;
const COUNT_PER_PAGE = document.getElementById("countPerPage").value;

let selectedPaletteItemInfo, selectedPaletteItem, selectedGridItem, stage, downloader, originalPlantCounts, layer,
    tooltipLayer, prevSelectPlantPosition;
let uniqueGridItemIDNo = Array.from(Array(GRID_COLUMNS * GRID_ROWS).keys());

// Helpers

/**
 * Converts grid item coordinates to Konva coordinates
 *
 * @param {number} gridItemX - x-coordinate of the grid item
 * @param {number} gridItemY - y-coordinate of the grid item
 * @returns {Object} - Object containing the x and y coordinates in Konva
 */
const convertToKonvaCoordinates = (gridItemX, gridItemY) => {
    const konvaX = gridItemX * GRID_SIZE + OFFSET_X;
    const konvaY = gridItemY * GRID_SIZE + OFFSET_Y;
    return { x: konvaX, y: konvaY };
};

/**
 * Converts Konva coordinates to grid item coordinates
 * @param konvaCoordX - Konva x-coordinate of grid item
 * @param konvaCoordY - Konva y-coordinate of grid item
 * @returns {{i: number, j: number}} - Object containing x and y coordinates on the grid
 */
const convertToGridCoordinates = (konvaCoordX, konvaCoordY) => {
    const gridItemX = Math.round((konvaCoordX - OFFSET_X) / GRID_SIZE);
    const gridItemY = Math.round((konvaCoordY - OFFSET_Y) / GRID_SIZE);
    return { i: gridItemX, j: gridItemY };
}

/**
 * Checks if the location is valid on the grid
 * @param {number} x - x-coordinate of the plant
 * @param {number} y - y-coordinate of the plant
 * @returns {boolean} - True if the location is valid, false otherwise
 */
const validLocation = (x, y) => x >= OFFSET_X && x < OFFSET_X + GRID_WIDTH && y >= OFFSET_Y && y < OFFSET_Y + GRID_HEIGHT;

/**
 * Checks if destination on grid is empty
 * @param {Number} x - x-coordinate of the plant on grid
 * @param {Number} y - y-coordinate of the plant on grid
 * @param plantId - id of plant being moved
 * @param gridLocationUniqueId
 * @returns {Boolean} - True if the destination is empty, false otherwise
 */
const emptyDestination = (x, y, plantId, gridLocationUniqueId) => {
    let nodes = layer.find("Image").values();
    for (let node of nodes) {
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        if (i === x && j === y) {
            if (node.id() !== plantId) {
                return false;
            } else if (node.attrs.uniqueGridId !== gridLocationUniqueId) {
                return false;
            }
        }
    }
    return true
}

/**
 * Creates a tooltip object for displaying plant names and categories
 * @returns {Object} - The tooltip object, and a function to set the text of the tooltip
 */
const createToolTip = () => {
    const tooltip = new Konva.Label({
        x: 0,
        y: 0,
        opacity: 0.75,
        visible: false,
    });

    const tooltipTag = new Konva.Tag({
        fill: 'black',
        pointerDirection: 'up',
        pointerWidth: 10,
        pointerHeight: 10,
        lineJoin: 'round',
        shadowColor: 'black',
        shadowBlur: 10,
        shadowOffsetX: 10,
        shadowOffsetY: 10,
        shadowOpacity: 0.5,
    });

    const tooltipText = new Konva.Text({
        text: '',
        fontFamily: 'Calibri',
        fontSize: 18,
        padding: 5,
        fill: 'white',
    });

    tooltip.add(tooltipTag);
    tooltip.add(tooltipText);
    tooltipLayer.add(tooltip);

    /**
     * Sets the text of the tooltip
     * @param {String} text
     */
    const setTooltipText = (text) => {
        tooltipText.text(text);
    };

    return [tooltip, setTooltipText];
};

/**
 * Updates a plant's placed & remaining counters when the saved layout loads.
 *
 * @param {number} plantId id of the plant whose counters are being updated
 */
const updateCountersOnLoad = (plantId) => {
    const plantItem = document.querySelector(`[name="plant-item"][data-plant-id="${plantId}"]`);

    const count = parseInt(plantItem.getAttribute("data-plant-count"));
    const placedCount = originalPlantCounts[plantId] - count;

    const placedElement = plantItem.querySelector("#placed");
    const remainingElement = plantItem.querySelector("#remaining");

    if (placedElement && remainingElement) {
        placedElement.textContent = `Placed: ${placedCount + 1}`;
        remainingElement.textContent = `Remaining: ${count - 1}`;
    }

    plantItem.setAttribute("data-plant-count", count - 1);
};

/**
 * Creates a new plant and adds it to the stage produced by konva
 *
 * @param {string} imageSrc - The image source of the plant
 * @param {number} x - The x-coordinate of the plant
 * @param {number} y - The y-coordinate of the plant
 * @param {number} plantId - The id of the plant
 * @param {string} plantName - The name of the plant
 * @param {string} category - The category of the plant
 * @param {(onLoad?: () => void) => void} onload - The function to call when the plant is loaded can be undefined
 */
const createPlant = (imageSrc, x, y, plantId, plantName, category, onload = undefined) => {
    const plantImage = new Image();
    plantImage.src = imageSrc;

    plantImage.onload = () => {

        const [tooltip, setToolTipText] = createToolTip();

        const plant = new Konva.Image({
            x: x,
            y: y,
            image: plantImage,
            width: GRID_SIZE,
            height: GRID_SIZE,
            name: plantName,
            id: plantId.toString(),
            draggable: true,
            uniqueGridId: uniqueGridItemIDNo.pop(),
        });

        plant.on("dragstart", () => {
            prevSelectPlantPosition = { x: plant.x(), y: plant.y() };
        })

        plant.on("dragmove", () => {
            tooltip.hide();
            const { i, j } = convertToGridCoordinates(plant.x(), plant.y());
            let { x, y } = convertToKonvaCoordinates(i, j);

            // Ensure the plant is within the grid
            if (x < OFFSET_X) x = OFFSET_X;
            if (y < OFFSET_Y) y = OFFSET_Y;
            if (x >= OFFSET_X + GRID_WIDTH) x = OFFSET_X + GRID_WIDTH - GRID_SIZE;
            if (y >= OFFSET_Y + GRID_HEIGHT) y = OFFSET_Y + GRID_HEIGHT - GRID_SIZE;

            plant.position({
                x: x,
                y: y
            });

            // Highlight the plant when dragging
            plant.stroke("blue");
            plant.strokeWidth(4);
        });

        plant.on("dragend", () => {
            // Unhighlight the plant when dragging ends
            tooltip.hide();
            const { i, j } = convertToGridCoordinates(plant.x(), plant.y());

            //ensure destination is empty
            if (!emptyDestination(i, j, plant.id(), plant.attrs.uniqueGridId)) {
                showErrorMessage(OCCUPIED_DESTINATION);
                plant.position(prevSelectPlantPosition);
            }
            plant.stroke(null);
            plant.strokeWidth(0);
        });

        plant.on("click", () => {
            if (selectedPaletteItem) {
                showErrorMessage(OCCUPIED_DESTINATION);
                tooltip.hide();
                deselectPaletteItem();
                deselectGridItem();
                return;
            }
            if (selectedGridItem) {
                showErrorMessage(OCCUPIED_DESTINATION);
                tooltip.hide();
                deselectPaletteItem();
                deselectGridItem();
                return;
            }
            tooltip.hide();
            deselectPaletteItem();
            deselectGridItem();

            selectedGridItem = plant;
            plant.stroke("blue");
            plant.strokeWidth(4);
        });

        plant.on('mousemove', () => {
            const mousePos = stage.getPointerPosition();
            tooltip.position({
                x: mousePos.x + 10,
                y: mousePos.y + 10,
            });
            setToolTipText(plantName + "\n" + category);
            tooltip.show()
        });

        plant.on('mouseout', () => {
            tooltip.hide();
        });

        layer.add(plant);

        if (onload) onload();
    };
};

/**
 * Displays an error message for a short period of time
 *
 * @param {string} message - The error message to display
 */
const showErrorMessage = (message) => {
    errorElement.textContent = message;
    errorElement.classList.remove("d-none");
    setTimeout(() => {
        errorElement.classList.add("d-none");
    }, ERROR_MESSAGE_DURATION);
};

/**
 * Takes the data URL generated by Konva of the stage and converts it to a Blob
 * Function adapted from https://stackoverflow.com/questions/12168909/blob-from-dataurl
 *
 * @param {String} dataURL - The data URL of the stage
 * @returns {Blob} - The Blob object
 */
const dataURLtoBlob = (dataURL) => {
    // Split the data URL to extract the MIME type and base64 data
    const [mime, base64] = dataURL.split(",");
    const mimeType = mime.split(":")[1].split(";")[0];

    // Decode the base64 string
    const byteString = atob(base64);

    // Convert the byte string to an ArrayBuffer
    const arrayBuffer = new ArrayBuffer(byteString.length);
    const uint8Array = new Uint8Array(arrayBuffer);

    for (let i = 0; i < byteString.length; i++) {
        uint8Array[i] = byteString.charCodeAt(i);
    }

    // Create a new Blob from the ArrayBuffer
    return new Blob([uint8Array], { type: mimeType });
};

/**
 * Updates the displayed plant count in the HTML
 *
 * @param {HTMLElement} plantItem - The plant item element
 * @param {number} count - The new count
 */
const updatePlantCountDisplay = (plantItem, count) => {
    const plantId = plantItem.getAttribute("data-plant-id");
    const originalCount = originalPlantCounts[plantId];

    const totalElement = plantItem.querySelector("#total");
    const placedElement = plantItem.querySelector("#placed");
    const remainingElement = plantItem.querySelector("#remaining");

    const plantName = plantItem.getAttribute("data-plant-name");

    if (totalElement) {
        totalElement.textContent = `${plantName} (x${originalCount})`;
    }

    if (placedElement) {
        placedElement.textContent = `Placed: ${originalCount - count}`;
    }

    if (remainingElement) {
        remainingElement.textContent = `Remaining: ${count}`;
    }
};

/**
 * Resets the plant count to its original value
 *
 * @param {HTMLElement} plantItem - The plant item element
 */
const resetPlantCount = (plantItem) => {
    const plantId = plantItem.getAttribute("data-plant-id");
    const originalCount = originalPlantCounts[plantId];
    plantItem.setAttribute("data-plant-count", originalCount);
    updatePlantCountDisplay(plantItem, originalCount);
};

/**
 * Show plant items on the page based on the start and end indices
 *
 * @param {number} start - The start index
 * @param {number} end - The end index
 */
const showPlantItems = (start, end) => {
    plantItems.forEach((item, i) => {
        if (i >= start && i < end) {
            item.classList.remove("d-none");
        } else {
            item.classList.add("d-none");
        }
    });
};

/**
 * Deselects the highlighted palette item
 */
const deselectPaletteItem = () => {
    if (selectedPaletteItem) {
        selectedPaletteItem.style.border = "none";
        selectedPaletteItem = null;
        selectedPaletteItemInfo = null;
    }
};

/**
 * Deselects the highlighted grid item
 */
const deselectGridItem = () => {
    if (selectedGridItem) {
        selectedGridItem.stroke(null);
        selectedGridItem.strokeWidth(0);
        selectedGridItem = null;
    }
};


// Initialisation

const link = document.createElement("a");

downloader = new Downloader(link);

// maps plant id to the original count
originalPlantCounts = {};

selectedPaletteItemInfo = null;
selectedPaletteItem = null;
selectedGridItem = null;

stage = new Konva.Stage({
    width: STAGE_WIDTH,
    height: STAGE_HEIGHT,
    container: "container"
});

layer = new Konva.Layer();
tooltipLayer = new Konva.Layer();
stage.add(layer);
stage.add(tooltipLayer);

// Create grid
for (let i = 0; i < GRID_COLUMNS; i++) {
    for (let j = 0; j < GRID_ROWS; j++) {
        const konvaPos = convertToKonvaCoordinates(i, j);
        const rect = new Konva.Rect({
            x: konvaPos.x,
            y: konvaPos.y,
            width: GRID_SIZE,
            height: GRID_SIZE,
            fill: '#76ad4c',
            stroke: "black",
            strokeWidth: 1,
            name: "grid-cell",
            listening: false,
        });
        layer.add(rect);
    }
}


/**
 * Loads the persisted plants from a saved layout onto the grid.
 */
gridItemLocations.forEach(item => {
    const x_coord = parseInt(item.getAttribute("data-grid-x"));
    const y_coord = parseInt(item.getAttribute("data-grid-y"));
    const plantId = item.getAttribute("data-grid-objectid");
    const plantName = item.getAttribute("data-grid-name");
    const category = item.getAttribute("data-grid-category");

    let plantSrc = item.getAttribute("data-grid-image");
    if (instance !== "") {
        plantSrc = `/${instance}` + plantSrc;
    }
    const { x, y } = convertToKonvaCoordinates(x_coord, y_coord);

    const onloadCallback = () => updateCountersOnLoad(plantId);
    createPlant(plantSrc, x, y, plantId, plantName, category, onloadCallback);
});

/**
 * Initialise plant counts and event listeners for clicking on plant items
 */
plantItems.forEach((item, i) => {

    if (i < COUNT_PER_PAGE) {
        item.classList.remove("d-none");
    }

    const plantId = item.getAttribute("data-plant-id");
    originalPlantCounts[plantId] = parseInt(item.getAttribute("data-plant-count"));

    /**
     * Handles the clicking of a plant item in the palette
     */
    const handlePlantItemClick = () => {
        let nodes = layer.find("Image");
        const currentCount = parseInt(item.getAttribute("data-plant-count"));
        const category = item.getAttribute("data-plant-category");

        deselectPaletteItem();
        deselectGridItem();

        if (nodes.length >= GRID_COLUMNS * GRID_ROWS) {
            showErrorMessage(FULL_GRID);
            return;
        }

        if (currentCount < 1) return;

        item.style.border = "3px solid blue";

        selectedPaletteItem = item;


        let plantImage = item.getAttribute("data-category-image")

        if (instance === "test/" || instance === "prod/") {
            plantImage = `/${instance}` + plantImage;
        }

        selectedPaletteItemInfo = {
            name: item.getAttribute("data-plant-name"),
            image: plantImage,
            id: item.getAttribute("data-plant-id"),
            count: currentCount,
            category: category
        };
    };

    item.addEventListener("click", handlePlantItemClick);
});

// Event Handlers

/**
 * Handles the clicking of the stage
 *
 * @param {Event} event
 */
const handleStageClick = (event) => {

    if (!(event.target === stage || event.target.name() === "grid-cell")) return;

    const mousePos = stage.getPointerPosition();
    const i = Math.floor((mousePos.x - OFFSET_X) / GRID_SIZE);
    const j = Math.floor((mousePos.y - OFFSET_Y) / GRID_SIZE);
    const { x, y } = convertToKonvaCoordinates(i, j);

    if (selectedPaletteItem) {

        if (!validLocation(x, y)) {
            showErrorMessage(INVALID_LOCATION);
            return;
        }

        createPlant(selectedPaletteItemInfo.image, x, y, selectedPaletteItemInfo.id, selectedPaletteItemInfo.name, selectedPaletteItemInfo.category)
        selectedPaletteItemInfo.count -= 1

        selectedPaletteItem.setAttribute("data-plant-count", selectedPaletteItemInfo.count);
        updatePlantCountDisplay(selectedPaletteItem, selectedPaletteItemInfo.count);

    } else if (selectedGridItem) {

        if (validLocation(x, y)) {
            selectedGridItem.position({
                x: x,
                y: y
            });
        } else {
            showErrorMessage(INVALID_LOCATION);
        }

        deselectGridItem();
    }

    deselectPaletteItem();
};


/**
 * Clears all items from the grid and resets the plant counts
 */
const handleClearAllButtonClick = () => {
    layer.find("Image").forEach(node => node.destroy());

    plantItems.forEach(item => {
        resetPlantCount(item);
    });
    uniqueGridItemIDNo = Array.from(Array(GRID_COLUMNS * GRID_ROWS).keys());

    deselectPaletteItem();
};

/**
 * Downloads image of 2D garden grid
 *
 * @param {String} fileExtension - extension of downloaded file
 */
const handleExport = async (fileExtension) => {
    const dataURL = stage.toDataURL(
        {
            mimeType: "image/" + (fileExtension === "jpg" ? "jpeg" : fileExtension),
            pixelRatio: 3
        }
    );
    const blob = dataURLtoBlob(dataURL);
    downloader.saveWithLink(blob, `${gardenName}.${fileExtension}`);
};

/**
 * Handles the deletion of a plant from the garden
 */
const handleDeleteButtonClick = () => {
    if (!selectedGridItem) {
        showErrorMessage(NO_PLANT_SELECTED);
        return;
    }

    const gridX = selectedGridItem.attrs.x;
    const gridY = selectedGridItem.attrs.y;

    const { i, j } = convertToGridCoordinates(gridX, gridY);

    let plantItem = null;
    plantItems.forEach(item => {
        if (item.getAttribute("data-plant-id") === selectedGridItem.attrs.id) {
            plantItem = item;
        }
    });

    const updatedCount = parseInt(plantItem.getAttribute("data-plant-count")) + 1;
    plantItem.setAttribute("data-plant-count", updatedCount);
    uniqueGridItemIDNo.push(selectedGridItem.attrs.uniqueGridId);

    updatePlantCountDisplay(plantItem, updatedCount);

    selectedGridItem.destroy();
    selectedGridItem = null;
};

/**
 * Handles the saving of the garden layout
 *
 * @param {Event} event - The event object
 */
const handleGardenFormSubmit = (event) => {
    event.preventDefault();

    const idList = [];
    const xCoordList = [];
    const yCoordList = [];

    layer.find("Image").forEach(node => {
        idList.push(node.id());
        // Convert from konva coords back to grid item coords (so x, y values range from 0-6)
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        xCoordList.push(i);
        yCoordList.push(j);
    });

    idListInput.value = JSON.stringify(idList);
    xCoordListInput.value = JSON.stringify(xCoordList);
    yCoordListInput.value = JSON.stringify(yCoordList);
    event.target.submit();
};

/**
 * Handle resizing the stage when the window is resized
 */
const handleWindowResize = () => {
    const newWidth = container.clientWidth;
    const newHeight = container.clientHeight;
    stage.width(newWidth);
    stage.height(newHeight);
    stage.draw();
};

/**
 * Handle clicking outside of the palette
 *
 * @param {Event} event - The event object
 */
const handleWindowClick = (event) => {

    if (!selectedPaletteItem) return;

    if (pagination.contains(event.target)) {
        deselectPaletteItem();
        return;
    }
    console.log("machine gun");

    // check is spot clicked it plant

    const isWithinPlantItem = !!event.target.closest("[name='plant-item']");
    if (!isWithinPlantItem) showErrorMessage(INVALID_LOCATION);

    if (!selectedPaletteItem.contains(event.target)) {
        deselectPaletteItem();
    }
};

/**
 * Handle clicking the first page button
 */
const handleFirstPageClick = () => {
    showPlantItems(0, COUNT_PER_PAGE);
    currentPage.textContent = 1;
};

/**
 * Handle clicking the previous page button
 */
const handlePreviousPageClick = () => {
    const currentPageNumber = parseInt(currentPage.textContent);
    if (currentPageNumber === 1) return;
    const start = (currentPageNumber - 2) * COUNT_PER_PAGE;
    const end = (currentPageNumber - 1) * COUNT_PER_PAGE;
    showPlantItems(start, end);
    currentPage.textContent = currentPageNumber - 1;
};

/**
 * Handle clicking the next page button
 */
const handleNextPageClick = () => {
    const currentPageNumber = parseInt(currentPage.textContent);
    if (plantItems.length <= currentPageNumber * COUNT_PER_PAGE) return;
    const start = currentPageNumber * COUNT_PER_PAGE;
    const end = (currentPageNumber + 1) * COUNT_PER_PAGE;
    showPlantItems(start, end);
    currentPage.textContent = currentPageNumber + 1;
};

/**
 * Handle clicking the last page button
 */
const handleLastPageClick = () => {
    const start = Math.floor(plantItems.length / COUNT_PER_PAGE) * COUNT_PER_PAGE;
    const end = plantItems.length;
    if (start === end) return;
    showPlantItems(start, end);
    currentPage.textContent = Math.ceil(end / COUNT_PER_PAGE);
};


// Event listeners

window.addEventListener("click", handleWindowClick);
window.addEventListener("resize", handleWindowResize);

stage.on("click", handleStageClick);

jpgDownloadButton.addEventListener("click", () => handleExport("jpg"));
pngDownloadButton.addEventListener("click", () => handleExport("png"));
jpegDownloadButton.addEventListener("click", () => handleExport("jpeg"));

confirmClearAllButton.addEventListener("click", handleClearAllButtonClick);
deletePlantButton.addEventListener("click", handleDeleteButtonClick);
saveGardenForm.addEventListener("submit", handleGardenFormSubmit);

firstPage.addEventListener("click", handleFirstPageClick);
previousPage.addEventListener("click", handlePreviousPageClick);
nextPage.addEventListener("click", handleNextPageClick);
lastPage.addEventListener("click", handleLastPageClick);