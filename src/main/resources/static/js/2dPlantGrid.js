const stageWidth = window.innerWidth * 0.8;
const stageHeight = window.innerHeight * 0.9;
const GRID_SIZE = 100;
const GRID_COLUMNS = 6;
const GRID_ROWS = 6;

const gridWidth = GRID_COLUMNS * GRID_SIZE;
const gridHeight = GRID_ROWS * GRID_SIZE;

const offsetX = (stageWidth - gridWidth) / 2;
const offsetY = (stageHeight - gridHeight) / 2;

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
            fill: 'green',
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

function createPlant(x, y, imageSrc, plantName, plantId) {
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

        plant.on('dragmove', function () {
            let newX = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            let newY = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;
            plant.position({x: newX, y: newY});
        });

        plant.on('click', function (e) {
            if (selectedPlant) {
                selectedPlant.stroke(null);
                selectedPlant.strokeWidth(0);
            }
            selectedPlant = plant;
            plant.stroke('blue');
            plant.strokeWidth(4);
            layer.draw();
            e.cancelBubble = true;
        });

        layer.add(plant);
        layer.draw();
    };
}

document.querySelectorAll('.plant-item').forEach(item => {
    item.addEventListener('click', function () {
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = 'none';
        }
        this.style.border = '3px solid blue';
        highlightedPaletteItem = this;

        selectedPlantInfo = {
            name: this.getAttribute('data-plant-name'),
            image: this.getAttribute('data-plant-image'),
            id: this.getAttribute('data-plant-id')
        };
    });
});

stage.on('click', function (e) {
    if (selectedPlantInfo && (e.target === stage || e.target.name() === 'grid-cell')) {
        const mousePos = stage.getPointerPosition();
        let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
        let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

        createPlant(x, y, selectedPlantInfo.image, selectedPlantInfo.name, selectedPlantInfo.id);

        selectedPlantInfo = null;
        if (highlightedPaletteItem) {
            highlightedPaletteItem.style.border = 'none';
            highlightedPaletteItem = null;
        }
    } else if (selectedPlant && (e.target === stage || e.target.name() === 'grid-cell')) {
        const mousePos = stage.getPointerPosition();
        let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
        let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

        selectedPlant.position({x: x, y: y});
        selectedPlant.stroke(null);
        selectedPlant.strokeWidth(0);
        layer.draw();

        selectedPlant = null;

        document.querySelectorAll('.plant-item')
    }
});

const clearAllButton = document.querySelector('.btn.bg-warning');
if (clearAllButton) {
    clearAllButton.addEventListener('click', function () {
        layer.find('Image').forEach(node => node.destroy());
        layer.draw();
    });
} else {
    console.error('Clear All button not found');
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
