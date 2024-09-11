import {Downloader} from "./Downloader.js";

const STAGE_WIDTH = window.innerWidth * 0.8;
const STAGE_HEIGHT = window.innerHeight * 0.9;
const GRID_SIZE = Math.min(STAGE_WIDTH, STAGE_HEIGHT) / 8;
const GRID_COLUMNS = 7;
const GRID_ROWS = 7;
const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");
const errorElement = document.getElementById("error-message");

const INVALID_LOCATION = "Please place on the grid";
const OCCUPIED_DESTINATION = "Space is already occupied";
const ERROR_MESSAGE_DURATION = 3000;

const instance = getInstance();

// Calculate the total grid width and height
const GRID_WIDTH = GRID_COLUMNS * GRID_SIZE;
const GRID_HEIGHT = GRID_ROWS * GRID_SIZE;

const OFFSET_X = (STAGE_WIDTH - GRID_WIDTH) / 2;
const OFFSET_Y = (STAGE_HEIGHT - GRID_HEIGHT) / 2;

const originalPlantCounts = {};
let uniqueGridItemIDNo = Array.from(Array(GRID_COLUMNS * GRID_ROWS).keys());

let prevSelectPlantPosition;

const stage = new Konva.Stage({
    width: STAGE_WIDTH,
    height: STAGE_HEIGHT,
    container: "container"
});

const gardenName = document.getElementById("gardenName").value;

// link used to download files
const link = document.createElement("a");

const downloader = new Downloader(link);

const layer = new Konva.Layer();
const tooltipLayer = new Konva.Layer();
stage.add(layer);
stage.add(tooltipLayer);

/**
 * Converts grid item coordinates to Konva coordinates
 *
 * @param {Number} gridItemX - x-coordinate of the grid item
 * @param {Number} gridItemY - y-coordinate of the grid item
 * @returns {Object} - Object containing the x and y coordinates in Konva
 */
const convertToKonvaCoordinates = (gridItemX, gridItemY) => {
    const konvaX = gridItemX * GRID_SIZE + OFFSET_X;
    const konvaY = gridItemY * GRID_SIZE + OFFSET_Y;
    return {x: konvaX, y: konvaY};
}

// Create grid
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
            listening: false,
        });
        layer.add(rect);
    }
}

let selectedPlantInfo = null;
let highlightedPaletteItem = null;
let selectedPlant = null;

/**
 * Checks if the location is valid on the grid
 * @param {Number} x - x-coordinate of the plant
 * @param {Number} y - y-coordinate of the plant
 * @returns {Boolean} - True if the location is valid, false otherwise
 */
const validLocation = (x, y) => {
    return x >= OFFSET_X && x < OFFSET_X + GRID_WIDTH && y >= OFFSET_Y && y < OFFSET_Y + GRID_HEIGHT;
};

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
        const x_coord = Math.round((node.x() - OFFSET_X) / GRID_SIZE);
        const y_coord = Math.round((node.y() - OFFSET_Y) / GRID_SIZE);
        if (x_coord === x && y_coord === y) {
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
 * Handles the adding of a plant to the stage produced by konva
 */
const handleAddPlant = (imageSrc, x, y, plantId, plantName, category, onload = undefined) => {
    const plantImage = new Image();
    plantImage.src = imageSrc;

    plantImage.onload = () => {
        const tooltip = new Konva.Label({
            x: 0,
            y: 0,
            opacity: 0.75,
            visible: false,
        })
        const tooltiptag = new Konva.Tag({
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
        const tooltiptext =
            new Konva.Text({
                    text: '',
                    fontFamily: 'Calibri',
                    fontSize: 18,
                    padding: 5,
                    fill: 'white',
                }
            );
        tooltip.add(tooltiptag)
        tooltip.add(tooltiptext)
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
            prevSelectPlantPosition = {x: plant.x(), y: plant.y()};
        })

        plant.on("dragmove", () => {
            tooltip.hide();
            const i = Math.round((plant.x() - OFFSET_X) / GRID_SIZE);
            const j = Math.round((plant.y() - OFFSET_Y) / GRID_SIZE);
            let {x, y} = convertToKonvaCoordinates(i, j);

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
            const x = Math.round((plant.x() - OFFSET_X) / GRID_SIZE);
            const y = Math.round((plant.y() - OFFSET_Y) / GRID_SIZE);
            if (!emptyDestination(x, y, plant.id(), plant.attrs.uniqueGridId)) {
                showErrorMessage(OCCUPIED_DESTINATION);
                plant.position(prevSelectPlantPosition);
            }
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
            tooltiptext.text(plantName + "\n" + category);
            tooltip.show()
            tooltip.set
        });

        plant.on('mouseout', () => {
            tooltip.hide();
        })

        layer.add(plant);

        if (onload) onload();

        tooltipLayer.add(tooltip)
    };
};

/**
 * Displays an error message for a short period of time
 * @param {String} message - The error message to display
 */
const showErrorMessage = (message) => {
    errorElement.textContent = message;
    errorElement.classList.remove("d-none");
    setTimeout(() => {
        errorElement.classList.add("d-none");
    }, ERROR_MESSAGE_DURATION);
    validLocation
};

/**
 * Loads the persisted plants from a saved layout onto the grid.
 */
document.querySelectorAll(".grid-item-location").forEach(item => {
    const x_coord = parseInt(item.getAttribute("data-grid-x"));
    const y_coord = parseInt(item.getAttribute("data-grid-y"));
    const plantId = item.getAttribute("data-grid-objectid");
    const plantName = item.getAttribute("data-grid-name");
    const category = item.getAttribute("data-grid-category");

    let plantSrc = item.getAttribute("data-grid-image");
    if (instance === "test/" || instance === "prod/") {
        plantSrc = `/${instance}` + plantSrc;
    }
    const {x, y} = convertToKonvaCoordinates(x_coord, y_coord);

    const onloadCallback = () => updateCountersOnLoad(plantId);
    handleAddPlant(plantSrc, x, y, plantId, plantName, category, onloadCallback);
});

/**
 * Updates a plant's placed & remaining counters when the saved layout loads.
 * @param plantId id of the plant whose counters are being updated
 */
const updateCountersOnLoad = (plantId) => {
    const plantItem = document.querySelector(`[name="plant-item"][data-plant-id="${plantId}"]`);

    if (!plantItem) return;

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
 * Event listener for clicking on palette items
 */
document.querySelectorAll("[name='plant-item']").forEach(item => {

    const plantName = item.getAttribute("data-plant-name");
    originalPlantCounts[plantName] = parseInt(item.getAttribute("data-plant-count"));

    item.addEventListener("click", () => {

        const currentCount = parseInt(item.getAttribute("data-plant-count"));
        const category = item.getAttribute("data-plant-category")

        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = "none";
            highlightedPaletteItem = null
        }

        if (currentCount < 1) return;

        item.style.border = "3px solid blue";

        highlightedPaletteItem = item;

        let plantImage = item.getAttribute("data-plant-image")

        if (instance === "test/" || instance === "prod/") {
            plantImage = `/${instance}` + plantImage
        }

        selectedPlantInfo = {
            name: item.getAttribute("data-plant-name"),
            image: plantImage,
            id: item.getAttribute("data-plant-id"),
            count: currentCount,
            category: category
        };
    });
})

/**
 * Handles the clicking of the stage
 */
stage.on("click", event => {

    if (!(event.target === stage || event.target.name() === "grid-cell")) return;

    const mousePos = stage.getPointerPosition();
    const i = Math.floor((mousePos.x - OFFSET_X) / GRID_SIZE);
    const j = Math.floor((mousePos.y - OFFSET_Y) / GRID_SIZE);
    const {x, y} = convertToKonvaCoordinates(i, j);

    if (highlightedPaletteItem) {

        if (!validLocation(x, y)) {
            showErrorMessage(INVALID_LOCATION);
            return;
        }

        handleAddPlant(selectedPlantInfo.image, x, y, selectedPlantInfo.id, selectedPlantInfo.name, selectedPlantInfo.category)
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

        // Deselect the plant
        selectedPlant = null;
    }

    highlightedPaletteItem = null;
});

/**
 * Resets the plant count to its original value
 * @param {HTMLElement} plantItem - The plant item element
 */
const resetPlantCount = (plantItem) => {
    const plantName = plantItem.getAttribute("data-plant-name");
    const originalCount = originalPlantCounts[plantName];
    plantItem.setAttribute("data-plant-count", originalCount);
    updatePlantCountDisplay(plantItem, originalCount);
};

/**
 * Clear items from the grid and deselect items
 */
const clearAllButton = document.getElementById("confirmClearAll");
if (clearAllButton) {
    clearAllButton.addEventListener("click", () => {
        layer.find("Image").forEach(node => node.destroy());
        uniqueGridItemIDNo = Array.from(Array(GRID_COLUMNS * GRID_ROWS).keys());
        document.querySelectorAll("[name='plant-item']").forEach(item => {
            resetPlantCount(item);
        });

        selectedPlantInfo = null;
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = "none";
            highlightedPaletteItem = null;
        }
    });
}

/**
 * Updates the displayed plant count in the HTML
 * @param {HTMLElement} plantItem - The plant item element
 * @param {number} count - The new count
 * @param adding - whether we are adding a plant or deleting one
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
 * Takes the data URL generated by Konva of the stage and converts it to a Blob
 * Function adapted from https://stackoverflow.com/questions/12168909/blob-from-dataurl
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
    return new Blob([uint8Array], {type: mimeType});
}

/**
 * Downloads image of 2D garden grid
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
 * Event-listener to handle saving data. Is on the saveGardenFrom to update hidden variables before submission.
 */
document.getElementById("saveGardenForm").addEventListener("submit", event => {
    event.preventDefault(); // Prevent the default form submission
    const idList = [];
    const xCoordList = [];
    const yCoordList = [];

    // Assuming "layer.find("Image")" is correctly defined elsewhere
    layer.find("Image").forEach(node => {
        idList.push(node.id());
        // Convert from konva coords back to grid item coords (so x, y values range from 0-6)
        const x_coord = Math.round((node.x() - OFFSET_X) / GRID_SIZE);
        const y_coord = Math.round((node.y() - OFFSET_Y) / GRID_SIZE);
        xCoordList.push(x_coord);
        yCoordList.push(y_coord);
    });

    // Ensure these elements exist
    const idListInput = document.getElementById("idList");
    const xCoordListInput = document.getElementById("xCoordList");
    const yCoordListInput = document.getElementById("yCoordList");

    if (idListInput && xCoordListInput && yCoordListInput) {
        idListInput.value = JSON.stringify(idList);
        xCoordListInput.value = JSON.stringify(xCoordList);
        yCoordListInput.value = JSON.stringify(yCoordList);
        // Manually submit the form
        event.target.submit();
    } else {
        console.error("One or more hidden inputs not found");
    }
});

window.addEventListener("resize", () => {
    const newWidth = container.clientWidth;
    const newHeight = container.clientHeight;
    stage.width(newWidth);
    stage.height(newHeight);
    stage.draw();
});

// Deselect plant in palette when clicking outside of it
window.addEventListener("click", event => {
    const isWithinPlantItem = !!event.target.closest("[name='plant-item']");
    if (highlightedPaletteItem && !isWithinPlantItem) showErrorMessage(INVALID_LOCATION);

    if (highlightedPaletteItem && !highlightedPaletteItem.contains(event.target)) {
        highlightedPaletteItem.style.border = "none";
        highlightedPaletteItem = null;
        selectedPlantInfo = null;
    }
});

jpgDownloadButton.addEventListener("click", () => handleExport("jpg"));
pngDownloadButton.addEventListener("click", () => handleExport("png"));
jpegDownloadButton.addEventListener("click", () => handleExport("jpeg"));


/**
 * Event-listener to handle deleting data.
 */
document.getElementById("deletePlant").addEventListener("click", event => {
    console.log("Delete button clicked")

    if (selectedPlant) {
        const gridX = selectedPlant.attrs.x;
        const gridY = selectedPlant.attrs.y;

        const gardenId = document.getElementById("gardenId")

        const x_coord = Math.round((gridX - OFFSET_X) / GRID_SIZE);
        const y_coord = Math.round((gridY - OFFSET_Y) / GRID_SIZE);

        fetch(`/${instance}2D-garden/${gardenId.value}/delete?x_coord_delete=${x_coord}&y_coord_delete=${y_coord}`,
            {
                method: 'GET'
            })

        let plantItem = null;
        document.querySelectorAll('[name="plant-item"]').forEach(item => {
            if (item.getAttribute("data-plant-id") === selectedPlant.attrs.id) {
                plantItem = item;
            }
        });

        let count = parseInt(plantItem.getAttribute("data-plant-count"));
        count += 1;
        plantItem.setAttribute("data-plant-count", count);
        uniqueGridItemIDNo.push(selectedPlant.attrs.uniqueGridId);

        updatePlantCountDisplay(plantItem, count);
        selectedPlant.destroy();
        selectedPlant.stroke(null);
        selectedPlant.strokeWidth(0);
        selectedPlant = null;

    }
});


