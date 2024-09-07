import { Downloader } from "./Downloader.js";

const STAGE_WIDTH = window.innerWidth * 0.8;
const STAGE_HEIGHT = window.innerHeight * 0.9;
const GRID_SIZE = Math.min(STAGE_WIDTH, STAGE_HEIGHT) / 8;
const GRID_COLUMNS = 7;
const GRID_ROWS = 7;
const jpgDownloadButton = document.getElementById("download-jpg");
const pngDownloadButton = document.getElementById("download-png");
const jpegDownloadButton = document.getElementById("download-jpeg");

const instance = getInstance();

// Calculate the total grid width and height
const GRID_WIDTH = GRID_COLUMNS * GRID_SIZE;
const GRID_HEIGHT = GRID_ROWS * GRID_SIZE;

const OFFSET_X = (STAGE_WIDTH - GRID_WIDTH) / 2;
const OFFSET_Y = (STAGE_HEIGHT - GRID_HEIGHT) / 2;

let plantPosition = 100;
const plantName = "plant";

const originalPlantCounts = {};

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
stage.add(layer);

// Create grid
for (let i = 0; i < GRID_COLUMNS; i++) {
    for (let j = 0; j < GRID_ROWS; j++) {
        const rect = new Konva.Rect({
            x: i * GRID_SIZE + OFFSET_X,
            y: j * GRID_SIZE + OFFSET_Y,
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
 * Checks if the location is valid on the grid
 * @param {Number} x - x-coordinate of the plant
 * @param {Number} y - y-coordinate of the plant
 * @returns {Boolean} - True if the location is valid, false otherwise
 */
const validLocation = (x, y) => {
    return x >= OFFSET_X && x < OFFSET_X + GRID_WIDTH && y >= OFFSET_Y && y < OFFSET_Y + GRID_HEIGHT;
};

/**
 * Handles the adding of a plant to the stage produced by konva
 */
const handleAddPlant = (imageSrc, x, y, plantId) => {
    plantPosition -= 10;
    let currentPlantName = plantName + plantPosition.toString();
    const plantImage = new Image();
    plantImage.src = imageSrc;

    plantImage.onload = () => {
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
            let x = Math.round((plant.x() - OFFSET_X) / GRID_SIZE) * GRID_SIZE + OFFSET_X;
            let y = Math.round((plant.y() - OFFSET_Y) / GRID_SIZE) * GRID_SIZE + OFFSET_Y;

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
            plant.stroke(null);
            plant.strokeWidth(0);
        });

        plant.on("click", () => {
            if (selectedPlantInfo) return;

            if (selectedPlant) {
                selectedPlant.stroke(null);
                selectedPlant.strokeWidth(0);
            }

            selectedPlant = plant;
            plant.stroke("blue");
            plant.strokeWidth(4);
        });

        layer.add(plant);
    };
};

/**
 * Event listener for clicking on palette items
 */
document.querySelectorAll('[name="plant-item"]').forEach(item => {
    const plantName = item.getAttribute("data-plant-name");
    originalPlantCounts[plantName] = parseInt(item.getAttribute("data-plant-count"));

    item.addEventListener("click", () => {

        const currentCount = parseInt(item.getAttribute("data-plant-count"));

        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = "none";
        }

        if (currentCount < 1) return;

        item.style.border = "3px solid blue";
        highlightedPaletteItem = item;

        selectedPlantInfo = {
            name: item.getAttribute("data-plant-name"),
            image: `/${instance}` + item.getAttribute("data-plant-image"),
            id: item.getAttribute("data-plant-id"),
            count: currentCount
        };
    });
})

/**
 * Handles the clicking of any plant on the stage
 */
stage.on("click", event => {

    if (!(event.target === stage || event.target.name() === "grid-cell")) return;

    const mousePos = stage.getPointerPosition();
    const x = Math.floor((mousePos.x - OFFSET_X) / GRID_SIZE) * GRID_SIZE + OFFSET_X;
    const y = Math.floor((mousePos.y - OFFSET_Y) / GRID_SIZE) * GRID_SIZE + OFFSET_Y;

    if (selectedPlantInfo) {
        if (selectedPlantInfo.count < 1 || !validLocation(x, y)) return;

        handleAddPlant(selectedPlantInfo.image, x, y, selectedPlantInfo.id)
        selectedPlantInfo.count -= 1

        if (highlightedPaletteItem) {
            highlightedPaletteItem.setAttribute("data-plant-count", selectedPlantInfo.count);
            updatePlantCountDisplay(highlightedPaletteItem, selectedPlantInfo.count);
            highlightedPaletteItem.style.border = "none";
            highlightedPaletteItem = null;
        }

        selectedPlantInfo = null;

    } else if (selectedPlant) {

        if (validLocation(x, y)) {
            selectedPlant.position({
                x: x,
                y: y
            });
        }

        selectedPlant.stroke(null);
        selectedPlant.strokeWidth(0);

        // Deselect the plant
        selectedPlant = null;
    }
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
const clearAllButton = document.querySelector(".btn.bg-warning");

// TODO: remove this if check
if (clearAllButton) {
    clearAllButton.addEventListener("click", () => {
        layer.find("Image").forEach(node => node.destroy());

        document.querySelectorAll('[name="plant-item"]').forEach(item => {
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
};

/**
 * Event-listener to handle saving data. Is on the saveGardenFrom to update hidden variables before submission.
 */
document.getElementById("saveGardenForm").addEventListener("submit", event => {
    event.preventDefault(); // Prevent the default form submission
    let idList = [];
    let xCoordList = [];
    let yCoordList = [];

    // Assuming "layer.find("Image")" is correctly defined elsewhere
    layer.find("Image").forEach(node => {
        idList.push(node.id());
        xCoordList.push(node.x());
        yCoordList.push(node.y());
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

jpgDownloadButton.addEventListener("click", () => handleExport("jpg"));
pngDownloadButton.addEventListener("click", () => handleExport("png"));
jpegDownloadButton.addEventListener("click", () => handleExport("jpeg"));



