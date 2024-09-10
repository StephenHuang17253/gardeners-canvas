import { Downloader } from "./Downloader.js";

const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");
const errorElement = document.getElementById("error-message");
const deletePlantButton = document.getElementById("deletePlant");
const saveGardenForm = document.getElementById("saveGardenForm");
const confirmClearAllButton = document.getElementById("confirmClearAll");
const plantItems = document.querySelectorAll("[name='plant-item']");
const gridItemLocations = document.querySelectorAll(".grid-item-location");
const idListInput = document.getElementById("idList");
const xCoordListInput = document.getElementById("xCoordList");
const yCoordListInput = document.getElementById("yCoordList");

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
const ERROR_MESSAGE_DURATION = 3000;

const instance = getInstance();

const gardenName = document.getElementById("gardenName").value;
const gardenId = document.getElementById("gardenId").value;



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
 * Checks if the location is valid on the grid
 * @param {number} x - x-coordinate of the plant
 * @param {number} y - y-coordinate of the plant
 * @returns {boolean} - True if the location is valid, false otherwise
 */
const validLocation = (x, y) => x >= OFFSET_X && x < OFFSET_X + GRID_WIDTH && y >= OFFSET_Y && y < OFFSET_Y + GRID_HEIGHT;

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
    const placedCount = originalPlantCounts[plantItem.getAttribute("data-plant-name")] - count;

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
        });

        plant.on("dragmove", () => {
            tooltip.hide();
            const i = Math.round((plant.x() - OFFSET_X) / GRID_SIZE);
            const j = Math.round((plant.y() - OFFSET_Y) / GRID_SIZE);
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
            plant.stroke(null);
            plant.strokeWidth(0);
        });

        plant.on("click", () => {
            tooltip.hide();
            if (highlightedPaletteItem) {
                highlightedPaletteItem.style.border = "none";
                highlightedPaletteItem = null;
                selectedPlantInfo = null;
            }

            if (selectedPlant) {
                selectedPlant.stroke(null);
                selectedPlant.strokeWidth(0);
            }

            selectedPlant = plant;
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
    const plantName = plantItem.getAttribute("data-plant-name");
    const originalCount = originalPlantCounts[plantName];

    // Select the <a> elements by their ids
    const totalElement = plantItem.querySelector("#total");
    const placedElement = plantItem.querySelector("#placed");
    const remainingElement = plantItem.querySelector("#remaining");

    // Update the total <a> element
    if (totalElement) {
        totalElement.textContent = `${plantName} (x${originalCount})`;
    }

    // Update the placed <a> element
    if (placedElement) {
        placedElement.textContent = `Placed: ${originalCount - count}`;
    }

    // Update the remaining <a> element
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
    const plantName = plantItem.getAttribute("data-plant-name");
    const originalCount = originalPlantCounts[plantName];
    plantItem.setAttribute("data-plant-count", originalCount);
    updatePlantCountDisplay(plantItem, originalCount);
};



// Initialisation

const link = document.createElement("a");

const downloader = new Downloader(link);

const originalPlantCounts = {};

let selectedPlantInfo = null;
let highlightedPaletteItem = null;
let selectedPlant = null;

const stage = new Konva.Stage({
    width: STAGE_WIDTH,
    height: STAGE_HEIGHT,
    container: "container"
});

const layer = new Konva.Layer();
const tooltipLayer = new Konva.Layer();
stage.add(layer);
stage.add(tooltipLayer);

for (let i = 0; i < GRID_COLUMNS; i++) {
    for (let j = 0; j < GRID_ROWS; j++) {
        const konvaPos = convertToKonvaCoordinates(i, j);
        const rect = new Konva.Rect({
            x: konvaPos.x,
            y: konvaPos.y,
            width: GRID_SIZE,
            height: GRID_SIZE,
            fill: "green",
            stroke: "black",
            strokeWidth: 1,
            name: "grid-cell",
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
plantItems.forEach(item => {

    const plantName = item.getAttribute("data-plant-name");
    originalPlantCounts[plantName] = parseInt(item.getAttribute("data-plant-count"));

    /**
     * Handles the clicking of a plant item in the palette
     */
    const handlePlantItemClick = () => {

        const currentCount = parseInt(item.getAttribute("data-plant-count"));
        const category = item.getAttribute("data-plant-category");

        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = "none";
            highlightedPaletteItem = null;
        }

        if (currentCount < 1) return;

        item.style.border = "3px solid blue";

        highlightedPaletteItem = item;

        let plantImage = item.getAttribute("data-plant-image")

        if (instance === "test/" || instance === "prod/") {
            plantImage = `/${instance}` + plantImage;
        }

        selectedPlantInfo = {
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

    if (highlightedPaletteItem) {

        if (!validLocation(x, y)) {
            showErrorMessage(INVALID_LOCATION);
            return;
        }

        createPlant(selectedPlantInfo.image, x, y, selectedPlantInfo.id, selectedPlantInfo.name, selectedPlantInfo.category)
        selectedPlantInfo.count -= 1

        highlightedPaletteItem.setAttribute("data-plant-count", selectedPlantInfo.count);
        updatePlantCountDisplay(highlightedPaletteItem, selectedPlantInfo.count);

        highlightedPaletteItem.style.border = "none";
        highlightedPaletteItem = null;
        selectedPlantInfo = null;

    } else if (selectedPlant) {

        if (validLocation(x, y)) {
            selectedPlant.position({
                x: x,
                y: y
            });
        } else {
            showErrorMessage(INVALID_LOCATION);
        }

        selectedPlant.stroke(null);
        selectedPlant.strokeWidth(0);
        selectedPlant = null;
    }

    highlightedPaletteItem = null;
};


/**
 * Clears all items from the grid and resets the plant counts
 */
const handleClearAllButtonClick = () => {
    layer.find("Image").forEach(node => node.destroy());

    plantItems.forEach(item => {
        resetPlantCount(item);
    });

    selectedPlantInfo = null;
    if (highlightedPaletteItem) {
        highlightedPaletteItem.style.border = "none";
        highlightedPaletteItem = null;
    }
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
    downloader.saveFile(blob, `${gardenName}.${fileExtension}`);
};

/**
 * Handles the deletion of a plant from the garden
 */
const handleDeleteButtonClick = () => {
    if (!selectedPlant) {
        showErrorMessage(NO_PLANT_SELECTED);
        return;
    }

    const gridX = selectedPlant.attrs.x;
    const gridY = selectedPlant.attrs.y;

    const x_coord = Math.round((gridX - OFFSET_X) / GRID_SIZE);
    const y_coord = Math.round((gridY - OFFSET_Y) / GRID_SIZE);

    fetch(`/${instance}2D-garden/${gardenId}/delete?x_coord_delete=${x_coord}&y_coord_delete=${y_coord}`)

    let plantItem = null;
    plantItems.forEach(item => {
        if (item.getAttribute("data-plant-id") === selectedPlant.attrs.id) {
            plantItem = item;
        }
    });

    const updatedCount = parseInt(plantItem.getAttribute("data-plant-count")) + 1;
    plantItem.setAttribute("data-plant-count", updatedCount);

    updatePlantCountDisplay(plantItem, updatedCount);

    selectedPlant.destroy()
    selectedPlant.stroke(null);
    selectedPlant.strokeWidth(0);
    selectedPlant = null;
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
        const x_coord = Math.round((node.x() - OFFSET_X) / GRID_SIZE);
        const y_coord = Math.round((node.y() - OFFSET_Y) / GRID_SIZE);
        xCoordList.push(x_coord);
        yCoordList.push(y_coord);
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
    const isWithinPlantItem = !!event.target.closest("[name='plant-item']");
    if (highlightedPaletteItem && !isWithinPlantItem) showErrorMessage(INVALID_LOCATION);

    if (highlightedPaletteItem && !highlightedPaletteItem.contains(event.target)) {
        highlightedPaletteItem.style.border = "none";
        highlightedPaletteItem = null;
        selectedPlantInfo = null;
    }
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