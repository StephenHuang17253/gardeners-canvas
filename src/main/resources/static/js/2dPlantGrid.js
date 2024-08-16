const signUpButton = document.querySelector('button[type="submit"]');

var stageWidth = window.innerWidth;
var stageHeight = window.innerHeight;
var GRID_SIZE = 100;  // Size of each grid cell
var GRID_COLUMNS = 10;  // Number of columns in the grid
var GRID_ROWS = 10;  // Number of rows in the grid

// Calculate the total grid width and height
var gridWidth = GRID_COLUMNS * GRID_SIZE;
var gridHeight = GRID_ROWS * GRID_SIZE;

// Calculate the offsets to center the grid
var offsetX = (stageWidth - gridWidth) / 2;
var offsetY = (stageHeight - gridHeight) / 2;

var plantPosition = 100;
var plantName = "plant"

var plantList = [];

var stage = new Konva.Stage({
    width: stageWidth,
    height: stageHeight,
    container: 'container'
});

let selected = false

var layer = new Konva.Layer();
stage.add(layer);

// Create a grid of squares
for (var i = 0; i < GRID_COLUMNS; i++) {
    for (var j = 0; j < GRID_ROWS; j++) {
        var rect = new Konva.Rect({
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
    var plantImage = new Image();
    plantImage.src = 'https://perenual.com/storage/species_image/5833_petroselinum_crispum/og/24890552915_bc32127f01_b.jpg';  // Replace with your image path
    let currentPlantName = plantName + plantPosition.toString();


    plantImage.onload = function() {
        var plant = new Konva.Image({
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
            var x = Math.round((plant.x() - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
            var y = Math.round((plant.y() - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

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

            // Prevent the stage click event from firing immediately
            e.cancelBubble = true;
        });

        layer.draw();
    };
};

// Click event on the stage to place the plant at the correct grid position
stage.on('click', function (e) {
    if (selected && selectedPlant) {
        var mousePos = stage.getPointerPosition();
        var x = Math.floor((mousePos.x - offsetX) / GRID_SIZE) * GRID_SIZE + offsetX;
        var y = Math.floor((mousePos.y - offsetY) / GRID_SIZE) * GRID_SIZE + offsetY;

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