const signUpButton = document.querySelector('button[type="submit"]');

const stageWidth = window.innerWidth * 0.59;
const stageHeight = window.innerHeight;

const GRID_SIZE = 100;  // Size of each grid cell
const GRID_COLUMNS = 6;  // Number of columns in the grid
const GRID_ROWS = 6;  // Number of rows in the grid

// Calculate the total grid width and height
const gridWidth = GRID_COLUMNS * GRID_SIZE;
const gridHeight = GRID_ROWS * GRID_SIZE;

// Calculate the offsets to center the grid
const offsetX = (stageWidth - gridWidth) / 2;
const offsetY = (stageHeight - gridHeight) / 2;

let plantPosition = 100;
const plantName = "plant";

const stage = new Konva.Stage({
    width: stageWidth,
    height: stageHeight,
    container: 'container'
});

let selected = false;

const layer = new Konva.Layer();
stage.add(layer);

// Create a grid of squares
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

let selectedPlant = null;  // Track the currently selected plant

const handleFormSubmit = (event) => {
    plantPosition -= 10;
    const plantImage = new Image();
    plantImage.src = '/images/default_plant.png';
    let currentPlantName = plantName + plantPosition.toString();

    plantImage.onload = function() {
        const plant = new Konva.Image({
            x: offsetX + 20 + plantPosition,
            y: offsetY + 20 + plantPosition,
            image: plantImage,
            width: GRID_SIZE,
            height: GRID_SIZE,
            draggable: true,
            name: currentPlantName,
        });
        layer.add(plant);

        plant.on('dragmove', function () {
            let x = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            let y = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

            plant.position({
                x: x,
                y: y,
            });
        });

        // Click event to select and highlight the plant
        plant.on('click', function (e) {
            if (selectedPlant) {
                // Deselect the previous plant
                selectedPlant.stroke(null);
                selectedPlant.strokeWidth(0);
            }

            selectedPlant = plant;
            selected = true;

            plant.stroke('blue');  // Highlight the selected plant
            plant.strokeWidth(4);
            layer.draw();

            e.cancelBubble = true;
        });

        layer.draw();
    };
};

// Click event to place the plant at the correct grid position
stage.on('click', function (e) {
    if (selected && selectedPlant) {
        const mousePos = stage.getPointerPosition();
        let x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
        let y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

        selectedPlant.position({
            x: x,
            y: y,
        });

        selectedPlant.stroke(null);  // Remove highlight after placement
        selectedPlant.strokeWidth(0);
        layer.draw();

        // Deselect the plant
        selectedPlant = null;
        selected = false;
    }
});

signUpButton.addEventListener('click', handleFormSubmit);
