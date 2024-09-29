import { Downloader } from "./Downloader.js";

const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");
const paletteWindow = document.getElementById("palette-window");


const errorElement = document.getElementById("error-message");
const confirmClearAllButton = document.getElementById("confirmClearAll");
const deletePlantButton = document.getElementById("deletePlant");
const saveGardenForm = document.getElementById("saveGardenForm");

const idListInput = document.getElementById("idList");
const typeListInput = document.getElementById("typeList");
const xCoordListInput = document.getElementById("xCoordList");
const yCoordListInput = document.getElementById("yCoordList");
const tileTextureListInput = document.getElementById("tileTextureList");

const plantItems = document.querySelectorAll("[name='plant-item']");
const textureItems = document.querySelectorAll("[name='texture-item']");
const gridItemLocations = document.querySelectorAll("[name='grid-item-location']");
const decorationItems = document.querySelectorAll("[name='decoration-item']");
const tileItems = document.querySelectorAll("[name='grid-tile']");


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

const TEXTURE_TYPE = "TEXTURE"

let selectedPaletteItemInfo, selectedPaletteItem, selectedGridItem, stage, downloader, originalPlantCounts,
    textureLayer, gardenItemLayer,
    tooltipLayer, prevSelectPosition;
let uniqueGridItemIDNo = Array.from(Array(GRID_COLUMNS * GRID_ROWS).keys());
let preventUnload = false;

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
    const nodes = gardenItemLayer.find("Image").values();
    for (let node of nodes) {
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        if ((i === x && j === y) && (node.id() !== plantId || node.attrs.uniqueGridId !== gridLocationUniqueId)) {
            return false;
        }
    }
    return true;
};

/**
 * Removes previous texture on grid box
 * @param x x-coordinate in grid coordinate form
 * @param y y-coordinate in grid coordinate form
 */
const destroyExistingTexture = (x, y) => {
    const nodes = textureLayer.find("Image").values();
    for (let node of nodes) {
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        if ((i === x && j === y)) {
            node.destroy();
        }
    }
};

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


const createTextureOnGrid = (imageSrc, x, y, itemType, objectName) => {
    const gridItemImage = new Image();
    gridItemImage.src = imageSrc;
    gridItemImage.onload = () => {
        const texture = new Konva.Image({
            x: x,
            y: y,
            image: gridItemImage,
            width: GRID_SIZE,
            height: GRID_SIZE,
            name: objectName,
            type: itemType,
            draggable: false,
        });

        textureLayer.add(texture);
        if (onload) onload();
    };
}

/**
 * Creates a new plant or decoration and adds it to the stage produced by konva
 *
 * @param {string} imageSrc - The image source of the plant or decoration
 * @param {number} x - The x-coordinate of the plant or decoration
 * @param {number} y - The y-coordinate of the plant or decoration
 * @param {number} objectId - The id of the plant or decoration
 * @param {string} itemType - enum type (PLANT or DECORATION)
 * @param {string} objectName - The name of the plant or decoration
 * @param {string} category - The category of the plant or decoration
 * @param {(onLoad?: () => void) => void} onload - The function to call when the plant is loaded can be undefined
 */
const createPlantOrDecoration = (imageSrc, x, y, objectId, itemType, objectName, category, onload = undefined) => {
    const gridItemImage = new Image();
    gridItemImage.src = imageSrc;

    gridItemImage.onload = () => {

        const [tooltip, setToolTipText] = createToolTip();

        const plantOrDecoration = new Konva.Image({
            x: x,
            y: y,
            image: gridItemImage,
            width: GRID_SIZE,
            height: GRID_SIZE,
            name: objectName,
            id: objectId.toString(),
            type: itemType,
            draggable: true,
            itemCategory: category,
            uniqueGridId: uniqueGridItemIDNo.pop(),
        });

        plantOrDecoration.on("dragstart", () => {
            prevSelectPosition = { x: plantOrDecoration.x(), y: plantOrDecoration.y() };
        })

        plantOrDecoration.on("dragmove", () => {
            tooltip.hide();
            const { i, j } = convertToGridCoordinates(plantOrDecoration.x(), plantOrDecoration.y());
            let { x, y } = convertToKonvaCoordinates(i, j);

            // Ensure the plant or decoration is within the grid
            if (x < OFFSET_X) x = OFFSET_X;
            if (y < OFFSET_Y) y = OFFSET_Y;
            if (x >= OFFSET_X + GRID_WIDTH) x = OFFSET_X + GRID_WIDTH - GRID_SIZE;
            if (y >= OFFSET_Y + GRID_HEIGHT) y = OFFSET_Y + GRID_HEIGHT - GRID_SIZE;

            plantOrDecoration.position({
                x: x,
                y: y
            });

            // Highlight the plant when dragging
            plantOrDecoration.stroke("blue");
            plantOrDecoration.strokeWidth(4);
        });

        plantOrDecoration.on("dragend", () => {
            // Unhighlight the plant when dragging ends
            tooltip.hide();
            const { i, j } = convertToGridCoordinates(plantOrDecoration.x(), plantOrDecoration.y());

            //ensure destination is empty
            if (!emptyDestination(i, j, plantOrDecoration.id(), plantOrDecoration.attrs.uniqueGridId)) {
                showErrorMessage(OCCUPIED_DESTINATION);
                plantOrDecoration.position(prevSelectPosition);
            }
            plantOrDecoration.stroke(null);
            plantOrDecoration.strokeWidth(0);
        });

        plantOrDecoration.on("click", () => {
            if (selectedPaletteItem) {

                if (selectedPaletteItemInfo.type !== TEXTURE_TYPE) {
                    showErrorMessage(OCCUPIED_DESTINATION);
                    tooltip.hide();
                    deselectPaletteItem();
                    deselectGridItem();
                }

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

            selectedGridItem = plantOrDecoration;
            plantOrDecoration.stroke("blue");
            plantOrDecoration.strokeWidth(4);
        });

        plantOrDecoration.on('mousemove', () => {
            const mousePos = stage.getPointerPosition();
            tooltip.position({
                x: mousePos.x + 10,
                y: mousePos.y + 10,
            });
            setToolTipText(objectName + "\n" + category);
            tooltip.show();
        });

        plantOrDecoration.on('mouseout', () => {
            tooltip.hide();
        });

        gardenItemLayer.add(plantOrDecoration);

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

textureLayer = new Konva.Layer();
gardenItemLayer = new Konva.Layer();
tooltipLayer = new Konva.Layer();
stage.add(textureLayer);
stage.add(gardenItemLayer);
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
            fill: 'transparent',
            stroke: "black",
            strokeWidth: 1,
            name: "grid-cell",
            listening: false,
        });
        textureLayer.add(rect);
        gardenItemLayer.add(rect);
    }
}

/**
 * Loads the persisted grid items (plants & decorations) from a saved layout onto the grid.
 */
gridItemLocations.forEach(item => {
    const xCoord = parseInt(item.getAttribute("data-grid-x"));
    const yCoord = parseInt(item.getAttribute("data-grid-y"));
    const objectId = item.getAttribute("data-grid-objectid");
    const itemType = item.getAttribute("data-grid-type");
    const itemName = item.getAttribute("data-grid-name");
    const category = item.getAttribute("data-grid-category");
    let imageSrc = item.getAttribute("data-grid-image");
    if (instance !== "") {
        imageSrc = `/${instance}` + imageSrc;
    }
    const { x, y } = convertToKonvaCoordinates(xCoord, yCoord);

    const onloadCallback = () => updateCountersOnLoad(objectId);
    createPlantOrDecoration(imageSrc, x, y, objectId, itemType, itemName, category, onloadCallback);
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
        let nodes = gardenItemLayer.find("Image");
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
            type: "PLANT",
            count: currentCount,
            category: category
        };
    };

    item.addEventListener("click", handlePlantItemClick);
});

/**
 * Initialise event listeners for clicking on decoration items
 */
decorationItems.forEach((item) => {
    item.addEventListener('click', () => {
        deselectPaletteItem();
        deselectGridItem();

        item.style.border = '3px solid blue';
        selectedPaletteItem = item;

        let decorationImage = item.getAttribute('data-decoration-image');

        if (instance === 'test/' || instance === 'prod/') {
            decorationImage = `/${instance}` + decorationImage;
        }

        selectedPaletteItemInfo = {
            name: item.getAttribute('data-decoration-type'),
            image: decorationImage,
            id: item.getAttribute('data-decoration-id'),
            type: "DECORATION",
            count: 999,
            category: 'Decoration'
        };
    });
});

/**
 * Initialise event listeners for clicking on textures
 */
textureItems.forEach((item) => {

    const textureName = item.getAttribute("data-texture-name");
    let textureImage = item.getAttribute("data-texture-image")

    /**
     * Handles the clicking of a texture item in the palette
     */
    const handleTextureItemClick = () => {
        deselectPaletteItem();
        deselectGridItem();

        item.style.border = "3px solid blue";
        selectedPaletteItem = item;

        if (instance === "test/" || instance === "prod/") {
            textureImage = `/${instance}` + textureImage;
        }
        selectedPaletteItemInfo = {
            name: textureName,
            image: textureImage,
            type: TEXTURE_TYPE,
        };
    };

    item.addEventListener("click", handleTextureItemClick);
});

/**
 * Loads the persisted tile textures from a saved layout onto the grid.
 */
tileItems.forEach(item => {
    const tileX = item.getAttribute("data-tile-x");
    const tileY = item.getAttribute("data-tile-y");
    const tileTexture = item.getAttribute("data-tile-texture");
    const tileTextureImage = item.getAttribute("data-tile-image");

    let imageSrc = tileTextureImage;
    if (instance !== "") {
        imageSrc = `/${instance}` + imageSrc;
    }

    const { x, y } = convertToKonvaCoordinates(tileX, tileY);

    createTextureOnGrid(imageSrc, x, y, "TEXTURE", tileTexture);
});

// Event Handlers

/**
 * Handles the clicking of the stage
 */
const handleStageClick = () => {

    const mousePos = stage.getPointerPosition();
    const i = Math.floor((mousePos.x - OFFSET_X) / GRID_SIZE);
    const j = Math.floor((mousePos.y - OFFSET_Y) / GRID_SIZE);
    const { x, y } = convertToKonvaCoordinates(i, j);

    if (selectedPaletteItem) {
        if (!validLocation(x, y)) {
            if (selectedPaletteItemInfo.type === TEXTURE_TYPE) {
                deselectPaletteItem();
            }
            showErrorMessage(INVALID_LOCATION);
            return;
        }
        if (selectedPaletteItemInfo.type !== TEXTURE_TYPE) {
            createPlantOrDecoration(selectedPaletteItemInfo.image, x, y, selectedPaletteItemInfo.id, selectedPaletteItemInfo.type, selectedPaletteItemInfo.name, selectedPaletteItemInfo.category)
            selectedPaletteItemInfo.count -= 1

            selectedPaletteItem.setAttribute("data-plant-count", selectedPaletteItemInfo.count);
            updatePlantCountDisplay(selectedPaletteItem, selectedPaletteItemInfo.count);
        } else {
            const { i: newGridX, j: newGridY } = convertToGridCoordinates(x, y);
            createTextureOnGrid(selectedPaletteItemInfo.image, x, y, selectedPaletteItemInfo.type, selectedPaletteItemInfo.name, function () {
                destroyExistingTexture(newGridX, newGridY);
            });
            return;
        }
    } else if (selectedGridItem) {

        if (validLocation(x, y)) {
            selectedGridItem.position({
                x: x,
                y: y
            });
        } else {
            showErrorMessage(INVALID_LOCATION);
            deselectGridItem();
        }
    }
    deselectPaletteItem();
};

/**
 * Clears all items from the grid and resets the plant counts
 */
const handleClearAllButtonClick = () => {
    gardenItemLayer.find("Image").forEach(node => node.destroy());

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
    decorationItems.forEach(item => {
        if (item.getAttribute("data-decoration-id") === selectedGridItem.attrs.id) {
            plantItem = item;
        }
    })

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
    preventUnload = true;

    const idList = [];
    const typeList = [];
    const xCoordList = [];
    const yCoordList = [];

    gardenItemLayer.find("Image").forEach(node => {
        idList.push(node.id());
        typeList.push(node.attrs.type);
        // Convert from konva coords back to grid item coords (so x, y values range from 0-6)
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        xCoordList.push(i);
        yCoordList.push(j);
    });

    // assuming this is a list of length GRID_ROWS x GRID_COLUMNS of texture values e.g. GRASS
    const tileTextures = Array(GRID_ROWS * GRID_COLUMNS).fill(null);

    textureLayer.find("Image").forEach(node => {
        const { i, j } = convertToGridCoordinates(node.x(), node.y());
        tileTextures[j * GRID_ROWS + i] = node.attrs.name;
    });

    console.log(tileTextures);


    idListInput.value = JSON.stringify(idList);
    typeListInput.value = JSON.stringify(typeList);
    xCoordListInput.value = JSON.stringify(xCoordList);
    yCoordListInput.value = JSON.stringify(yCoordList);
    tileTextureListInput.value = JSON.stringify(tileTextures);
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

    // if no palette item selected, do nothing
    if (!selectedPaletteItem) return;

    // if clicking the pagination, deselect the palette item
    if (pagination.contains(event.target)) {
        deselectPaletteItem();
        return;
    }

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

/**
 * checks if 2D grid has been modified and has unsaved changes
 * 
 * @returns {boolean} are there changes on the grid (true if there are, false if not)
 */
const hasUnsavedChanges = () => {
    const originalGrid = Array.from(gridItemLocations).map(value => ({
        x: value.getAttribute("data-grid-x"),
        y: value.getAttribute("data-grid-y"),
        id: value.getAttribute("data-grid-objectid").toString(),
        name: value.getAttribute("data-grid-name"),
        category: value.getAttribute("data-grid-category")
    }));


    const currentGrid = gardenItemLayer.find("Image").map(node => {
        const { i: x, j: y } = convertToGridCoordinates(node.x(), node.y()); // Destructuring here
        return {
            x: x.toString(),
            y: y.toString(),
            id: node.id(),  // This is fine, as `id` is a method
            name: node.name(),
            category: node.attrs.itemCategory
        };
    });

    // Sort method to ensure lists are sorted
    const sortMethod = (a, b) => a.id.localeCompare(b.id) ||
        a.name.localeCompare(b.name) ||
        a.category.localeCompare(b.category) ||
        a.x.localeCompare(b.x) ||
        a.y.localeCompare(b.y);

    //ensure lists are sorted in same order
    currentGrid.sort(sortMethod);

    originalGrid.sort(sortMethod);

    // Compare the two arrays
    if (originalGrid.length !== currentGrid.length) {
        return true;
    }

    return originalGrid.some((original, index) => {
        const current = currentGrid[index];
        return (
            original.x !== current.x ||
            original.y !== current.y ||
            original.id !== current.id ||
            original.name !== current.name ||
            original.category !== current.category
        );
    });
};

/**
 * Handles exiting the page in any form
 * Shows modal if there are unsaved changes
 * 
 * @param {Event} event - The event object
 */
const handlePageExit = (event) => {
    if (hasUnsavedChanges() && !preventUnload) {
        event.preventDefault();
        event.returnValue = 'You have unsaved changes!';
        return 'You have unsaved changes!';
    }
};


// Event listeners

window.addEventListener("click", handleWindowClick);
window.addEventListener("resize", handleWindowResize);
// before unload event found at https://developer.mozilla.org/en-US/docs/Web/API/Window/beforeunload_event
window.addEventListener("beforeunload", handlePageExit);

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