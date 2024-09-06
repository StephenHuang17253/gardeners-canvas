import { Downloader } from "./Downloader.js";

const stageWidth = window.innerWidth * 0.8;
const stageHeight = window.innerHeight * 0.9;
const GRID_SIZE = Math.min(stageWidth, stageHeight) / 8;
const GRID_COLUMNS = 7;
const GRID_ROWS = 7;
const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");

// Calculate the total grid width and height
const gridWidth = GRID_COLUMNS * GRID_SIZE;
const gridHeight = GRID_ROWS * GRID_SIZE;

const offsetX = (stageWidth - gridWidth) / 2;
const offsetY = (stageHeight - gridHeight) / 2;

let plantPosition = 100;
const plantName = "plant";

let plantCount = 0;

const originalPlantCounts = {};

const stage = new Konva.Stage({
    width: stageWidth,
    height: stageHeight,
    container: "container"
});

const gardenName = document.getElementById("gardenName").value;

// link used to download files
const link = document.createElement("a");

const downloader = new Downloader(link);

const layer = new Konva.Layer();
stage.add(layer);

function convertToKonvaCoordinates(gridItemX, gridItemY) {
    const konvaX = gridItemX * GRID_SIZE + offsetX;
    const konvaY = gridItemY * GRID_SIZE + offsetY;
    return { x: konvaX, y: konvaY };
}


// Create grid
for (let i = 0; i < GRID_COLUMNS; i++) {
    for (let j = 0; j < GRID_ROWS; j++) {
        const rect = new Konva.Rect({
            x: i * GRID_SIZE + offsetX,
            y: j * GRID_SIZE + offsetY,
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

let selectedPlantInfo = null;
let highlightedPaletteItem = null;
let selectedPlant = null;

/**
 * Loads the persisted plants from a saved layout onto the grid.
 */
document.querySelectorAll('.grid-item-location').forEach(item => {
    const x_coord = parseInt(item.getAttribute('data-grid-x'));
    const y_coord = parseInt(item.getAttribute('data-grid-y'));
    const plantId = item.getAttribute('data-grid-objectid');
    console.log(plantId)

    console.log(item.getAttribute('data-grid-image'))

    const plantImage = new Image();

    plantImage.src = item.getAttribute('data-grid-image');
    plantImage.onload = function () {
        const konvaPos = convertToKonvaCoordinates(x_coord, y_coord);
        const plant = new Konva.Image({
            x: konvaPos.x,
            y: konvaPos.y,
            image: plantImage,
            width: GRID_SIZE,
            height: GRID_SIZE,
            name: plantName,
            id: plantId.toString(),
            draggable: true,
        });

        plant.on('dragmove', function () {
            let x = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            let y = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

            plant.position({
                x: x,
                y: y,
            });
        });

        plant.on('click', function (event) {
            if (!selectedPlantInfo) {
                if (selectedPlant) {
                    selectedPlant.stroke(null);
                    selectedPlant.strokeWidth(0);
                }
                selectedPlant = plant;
                plant.stroke('blue');
                plant.strokeWidth(4);
                layer.draw();
                event.cancelBubble = true;
            }
        });

        layer.add(plant);
        layer.draw();

        updateCountersOnLoad(plantId);
    }

});

/**
 * Updates a plant's placed & remaining counters when the saved layout loads.
 * @param plantId id of the plant whose counters are being updated
 */
function updateCountersOnLoad(plantId) {
    const plantItem = document.querySelector(`.plant-item[data-plant-id="${plantId}"]`)

    if (plantItem) {
        const count = parseInt(plantItem.getAttribute('data-plant-count'))
        const placedCount = originalPlantCounts[plantItem.getAttribute('data-plant-name')] - count;

        const placedElement = plantItem.querySelector('#placed');
        const remainingElement = plantItem.querySelector('#remaining');

        if (placedElement && remainingElement) {
            placedElement.textContent = `Placed: ${placedCount + 1}`;
            remainingElement.textContent = `Remaining: ${count - 1}`;
        }

        plantItem.setAttribute('data-plant-count', count - 1);
    }

}

/**
 * Handles the adding of a plant to the stage produced by konva
 */
const handleAddPlant = (imageSrc, x, y, plantId) => {
    plantPosition -= 10;
    let currentPlantName = plantName + plantPosition.toString();
    const plantImage = new Image();
    plantImage.src = imageSrc;

    plantImage.onload = function () {
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


        plant.on("dragmove", function () {
            let x = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            let y = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

            plant.position({
                x: x,
                y: y,
            });
        });

        plant.on("click", function (event) {
            if (!selectedPlantInfo) {
                if (selectedPlant) {
                    selectedPlant.stroke(null);
                    selectedPlant.strokeWidth(0);
                }
                selectedPlant = plant;
                plant.stroke("blue");
                plant.strokeWidth(4);
                layer.draw();
                event.cancelBubble = true;
            }
        });

        layer.add(plant);
        layer.draw();
    };
}

/**
 * Event listener for clicking on palette items
 */
document.querySelectorAll(".plant-item").forEach(item => {
    const instance = getInstance();
    let plantImage;

    if (instance === "test/" || instance === "prod/") {
        plantImage = `/${instance}` + item.getAttribute("data-plant-image")
    } else {
        plantImage = item.getAttribute("data-plant-image")
    }

    const plantName = item.getAttribute("data-plant-name");
    const plantCount = parseInt(item.getAttribute("data-plant-count"));

    originalPlantCounts[plantName] = plantCount;

    item.addEventListener("click", function() {
        const currentCount = parseInt(this.getAttribute('data-plant-count'));
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = "none";
        }
        if (currentCount > 0) {
            if (highlightedPaletteItem) {
                highlightedPaletteItem.style.border = "none";
            }
            this.style.border = "3px solid blue";
            highlightedPaletteItem = this;

            const instance = getInstance();

            selectedPlantInfo = {
                name: this.getAttribute("data-plant-name"),
                image: plantImage,
                id: this.getAttribute("data-plant-id"),
                count: currentCount
            };
        }
    });
});

/**
 * Handles the clicking of any plant on the stage
 */
stage.on("click", function (event) {
    if (selectedPlantInfo && (event.target === stage || event.target.name() === "grid-cell")) {
        if (selectedPlantInfo.count > 0) {

            const mousePos = stage.getPointerPosition();
            let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;
            plantCount = plantCount - 1
            handleAddPlant(selectedPlantInfo.image, x, y, selectedPlantInfo.id)
            selectedPlantInfo.count -= 1

            if (highlightedPaletteItem) {
                highlightedPaletteItem.setAttribute("data-plant-count", selectedPlantInfo.count);
                updatePlantCountDisplay(highlightedPaletteItem, selectedPlantInfo.count);
                highlightedPaletteItem.style.border = "none";
                highlightedPaletteItem = null;
            }

            selectedPlantInfo = null;
        }
    } else if (selectedPlant && (event.target === stage || event.target.name() === "grid-cell")) {
        const mousePos = stage.getPointerPosition();
        let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
        let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

        selectedPlant.position({ x: x, y: y });
        selectedPlant.stroke(null);
        selectedPlant.strokeWidth(0);

        layer.draw();

        // Deselect the plant
        selectedPlant = null;

    }
});

/**
 * Resets the plant count to its original value
 * @param {HTMLElement} plantItem - The plant item element
 */
function resetPlantCount(plantItem) {
    const plantName = plantItem.getAttribute("data-plant-name");
    const originalCount = originalPlantCounts[plantName];
    plantItem.setAttribute("data-plant-count", originalCount);
    updatePlantCountDisplay(plantItem, originalCount);
}

/**
 * Clear items from the grid and deselect items
 */
const clearAllButton = document.querySelector(".btn.bg-warning");
if (clearAllButton) {
    clearAllButton.addEventListener("click", function () {
        layer.find("Image").forEach(node => node.destroy());
        layer.draw();

        document.querySelectorAll(".plant-item").forEach(item => {
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
 */
function updatePlantCountDisplay(plantItem, count) {
    const plantName = plantItem.getAttribute("data-plant-name");
    const originalCount = originalPlantCounts[plantName];
    const countDisplay = plantItem.querySelector("a");

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
}

/**
 * Downloads image of 2D garden grid
 * @param fileExtension extension of downloaded file
 */
const handleExport = async (fileExtension) => {
    const dataURL = stage.toDataURL(
        {
            mimeType: "image/" + (fileExtension === "jpg" ? "jpeg" : fileExtension),
            pixelRatio: 3
        }
    );
    const blob = await fetch(dataURL).then(res => res.blob());
    downloader.saveFile(blob, `${gardenName}.${fileExtension}`);
}

/**
 * Event-listener to handle saving data. Is on the saveGardenFrom to update hidden variables before submission.
 */
document.getElementById("saveGardenForm").addEventListener("submit", function (event) {
    event.preventDefault(); // Prevent the default form submission
    let idList = [];
    let xCoordList = [];
    let yCoordList = [];

    // Assuming "layer.find("Image")" is correctly defined elsewhere
    layer.find("Image").forEach(node => {
        idList.push(node.id());
        // Convert from konva coords back to grid item coords (so x, y values range from 0-6)
        const x_coord = Math.round((node.x() - offsetX) / GRID_SIZE);
        const y_coord = Math.round((node.y() - offsetY) / GRID_SIZE);
        xCoordList.push(x_coord);
        yCoordList.push(y_coord);
    });

    // Ensure these elements exist
    let idListInput = document.getElementById("idList");
    let xCoordListInput = document.getElementById("xCoordList");
    let yCoordListInput = document.getElementById("yCoordList");


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

jpgDownloadButton.addEventListener("click", () => handleExport("jpg"));
pngDownloadButton.addEventListener("click", () => handleExport("png"));
jpegDownloadButton.addEventListener("click", () => handleExport("jpeg"));



