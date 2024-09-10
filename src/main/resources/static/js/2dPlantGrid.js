const stageWidth = window.innerWidth * 0.8;
const stageHeight = window.innerHeight * 0.9;
const GRID_SIZE = Math.min(stageWidth, stageHeight) / 8;
const GRID_COLUMNS = 7;
const GRID_ROWS = 7;
const TREE_SIZE = 50;


// Calculate the total grid width and height
const gridWidth = GRID_COLUMNS * GRID_SIZE;
const gridHeight = GRID_ROWS * GRID_SIZE;

const offsetX = (stageWidth - gridWidth) / 2;
const offsetY = (stageHeight - gridHeight) / 2;

let plantPosition = 100;
const plantName = "plant";

let plantCount = 0;

const originalPlantCounts = {};

const saveGardenButton = document.querySelector('.btn.bg-success');

const stage = new Konva.Stage({
    width: stageWidth,
    height: stageHeight,
    container: 'container'
});

const layer = new Konva.Layer();
stage.add(layer);

// Create grid
for (let i = 0; i < GRID_COLUMNS; i++) {
    for (let j = 0; j < GRID_ROWS; j++) {
        const rect = new Konva.Rect({
            x: i * GRID_SIZE + offsetX,
            y: j * GRID_SIZE + offsetY,
            width: GRID_SIZE,
            height: GRID_SIZE,
            fill: '#76ad4c',
            stroke: 'black',
            strokeWidth: 1,
            name: 'grid-cell',
        });
        layer.add(rect);
    }
}

let selectedPlantInfo = null;
let highlightedPaletteItem = null;
let selectedPlant = null;

/**
 * Handles the adding of a plant to the stage produced by konva
 */
const handleAddPlant = (imageSrc, x, y, plantId, plantCategory) => {
    plantPosition -= 10;
    let currentPlantName = plantName + plantPosition.toString();
    const plantImage = new Image();
    plantImage.src = imageSrc;

    plantImage.onload = function () {
        let size = GRID_SIZE;
        let offset = 0;

        if(plantCategory === 'Tree'){
            size += TREE_SIZE;
            offset += TREE_SIZE / 2;
        }
        const plant = new Konva.Image({
            x: x,
            y: y,
            image: plantImage,
            width: size,
            height: size,
            name: plantName,
            id: plantId.toString(),
            draggable: true,
        });


        plant.on('dragmove', function () {
            let x = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX - offset;
            let y = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY - offset;

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
    };
}

/**
 * Event listener for clicking on palette items
 */
document.querySelectorAll('.plant-item').forEach(item => {
    const plantName = item.getAttribute('data-plant-name');
    const plantCount = parseInt(item.getAttribute('data-plant-count'));
    originalPlantCounts[plantName] = plantCount;
    item.addEventListener('click', function() {
        const currentCount = parseInt(this.getAttribute('data-plant-count'));
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = 'none';
        }
        if (currentCount > 0) {
            if (highlightedPaletteItem) {
                highlightedPaletteItem.style.border = 'none';
            }
            this.style.border = '3px solid blue';
            highlightedPaletteItem = this;

            const inst = getInstance();

            selectedPlantInfo = {

                name: this.getAttribute('data-plant-name'),
                image: `/${inst}` + this.getAttribute('data-category-image'),
                id: this.getAttribute('data-plant-id'),
                count: currentCount,
                category: this.getAttribute('data-plant-category')
            };
        }
    });
});


/**
 * Handles the clicking of any plant on the stage
 */
stage.on('click', function (event) {
    if (selectedPlantInfo && (event.target === stage || event.target.name() === 'grid-cell')) {
        if (selectedPlantInfo.count > 0) {

            const mousePos = stage.getPointerPosition();
            let offset = 0;
            if(selectedPlantInfo.category === 'Tree'){
                offset += TREE_SIZE/2;
            }
            let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX-offset;
            let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY-offset;
            plantCount = plantCount - 1
            handleAddPlant(selectedPlantInfo.image, x, y, selectedPlantInfo.id, selectedPlantInfo.category)
            selectedPlantInfo.count -= 1

            if (highlightedPaletteItem) {
                highlightedPaletteItem.setAttribute('data-plant-count', selectedPlantInfo.count);
                updatePlantCountDisplay(highlightedPaletteItem, selectedPlantInfo.count);
                highlightedPaletteItem.style.border = 'none';
                highlightedPaletteItem = null;

            }

            selectedPlantInfo = null;
        }
    } else if (selectedPlant && (event.target === stage || event.target.name() === 'grid-cell')) {
        const mousePos = stage.getPointerPosition();
        let offset = 0;
        const plantImageObject = selectedPlant.image();
        let plantImagePath = new URL(plantImageObject.src).pathname;
        if(plantImagePath === "/" + getInstance() + 'images/2d-plant-types/tree.png'){
            offset += TREE_SIZE/2;
        }
        let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX-offset;
        let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY-offset;

        selectedPlant.position({x: x, y: y});
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
    const plantName = plantItem.getAttribute('data-plant-name');
    const originalCount = originalPlantCounts[plantName];
    plantItem.setAttribute('data-plant-count', originalCount);
    updatePlantCountDisplay(plantItem, originalCount);
}

/**
 * Clear items from the grid and deselect items
 */
const clearAllButton = document.querySelector('.btn.bg-warning');
if (clearAllButton) {
    clearAllButton.addEventListener('click', function () {
        layer.find('Image').forEach(node => node.destroy());
        layer.draw();

        document.querySelectorAll('.plant-item').forEach(item => {
            resetPlantCount(item);
        });
        selectedPlantInfo = null;
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = 'none';
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
    const plantName = plantItem.getAttribute('data-plant-name');
    const originalCount = originalPlantCounts[plantName];
    const countDisplay = plantItem.querySelector('a');

    // Select the <a> elements by their ids
    const totalElement = plantItem.querySelector('#total');
    const placedElement = plantItem.querySelector('#placed');
    const remainingElement = plantItem.querySelector('#remaining');

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
 * Event-listener to handle saving data. Is on the saveGardenFrom to update hidden variables before submission.
 */
document.getElementById('saveGardenForm').addEventListener('submit', function (event) {
    event.preventDefault(); // Prevent the default form submission
    let idList = [];
    let xCoordList = [];
    let yCoordList = [];

    // Assuming 'layer.find('Image')' is correctly defined elsewhere
    layer.find('Image').forEach(node => {
        idList.push(node.id());
        xCoordList.push(node.x());
        yCoordList.push(node.y());
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
        console.error('One or more hidden inputs not found');
    }
});

window.addEventListener('resize', () => {
    const newWidth = container.clientWidth;
    const newHeight = container.clientHeight;
    stage.width(newWidth);
    stage.height(newHeight);
    stage.draw();
});



