import * as THREE from "three";
import { createTileGrid } from "./tiles.js";
import { OrbitControls } from "./OrbitControls.js";
import { Loader } from "./Loader.js";
import { createHueSaturationMaterial } from "./hueSaturationShader.js";
import { Exporter } from "./Exporter.js";
import { Downloader } from "../Downloader.js";

const modelMap = {
    "Tree": ["tree.glb", 5],
    "Shrub": ["shrub.glb", 10],
    "Bush": ["shrub.glb", 20],
    "Herb": ["fern.glb", 1],
    "Creeper": ["creeper.glb", 0.5],
    "Climber": ["climber.glb", 5],
    "Flower": ["flower.glb", 10],
    "Pot Plant": ["potplant.glb", 5],
    "Background": ["background.glb", 100],
};

const decoMap = {
    "Rock": ["deco/rock.glb", 2],
    "Pond": ["deco/pond.glb", 2],
    "Gnome": ["deco/gnome.glb", 7],
    "Fountain": ["deco/fountain.glb", 3],
    "Table": ["deco/table.glb", 4.5],
};

const skyboxMap = {
    "Sunny": "sunny-day.exr",
    "Overcast": "cloudy-day.exr",
    "Rainy": "cloudy-day.exr",
    "Default": "default.exr"
};


let scene, camera, renderer, controls, loader, exporter, light, downloader, rainGeo, rainCount, rainSystem;
let rainSize = 0.20;

const container = document.getElementById("container");

const downloadGLTFButton = document.getElementById("download-gltf");
const downloadOBJButton = document.getElementById("download-obj");
const downloadJPGButton = document.getElementById("download-jpg");

const trackTimeInput = document.getElementById("trackTime");
const trackWeatherInput = document.getElementById("trackWeather");
const trackBackgroundInput = document.getElementById("toggleBackground");

const loadingDiv = document.getElementById("loading-div");
const loadingImg = document.getElementById("loading-img");

const FOV = 75;

const GRID_SIZE = 7;
const TILE_SIZE = 10;

const MIN_CAMERA_DIST = TILE_SIZE / 2;
const MAX_CAMERA_DIST = GRID_SIZE * TILE_SIZE;

const DEFAULT_TIME = 12;
const DEFAULT_WEATHER = "Default";

const RAIN_COLOR = 0x78b8c2

const INIT_CAMERA_POSITION = new THREE.Vector3(0, 45, 45);

// link used to download files
const link = document.createElement("a");

const gardenId = document.getElementById("gardenId").value;
const gardenName = document.getElementById("gardenName").value;
const currentHour = document.getElementById("currentHour").value;
const currentWeather = document.getElementById("weather").value;

const rainDropSizeInput = document.getElementById("rainDropSize");
let time = currentHour;
let weather = currentWeather;
let isRaining = weather === "Rainy";

/**
 * Updates the time of day in the scene
 *
 * @param {number} newTime
 */
const setTime = (newTime) => {
    time = newTime;
    changeSkybox(weather, time)
    // Update the time of day in the scene
    // Set moon or sun to the correct position
};

/**
 * Updates the weather in the scene
 *
 * @param {String} newWeather
 */
const setWeather = (newWeather) => {
    weather = newWeather;
    if (weather === DEFAULT_WEATHER) {
        setBackground(skyboxMap[weather]);
    } else {
        changeSkybox(weather, time);
    }
    // change clouds and rain to match the weather
    if (weather === "Rainy") {
        rainCount = 3000;
        startRain();
    } else if (isRaining) { // If rain currently exists but is not raining
        isRaining = false;
        stopRain(); //Deletes rain in rainSystem (Rain on existing scene)
    }
}

/**
 * Sets the background of the scene
 *
 * @param {string} filename
 */
const setBackground = (filename) => {
    loader.loadBackground(
        filename,
        texture => {
            scene.background = texture;
            scene.environment = texture;
        }
    );
}

const startRain = () => {
    const rainPositions = new Float32Array(rainCount * 3); // 3 coordinates per rain drop. x,y,z

    for (let i = 0; i < rainCount; i++) { // Iterate through each raindrop
        rainPositions.set([
            Math.random() * 130 - 60,  // x spawns anywhere x: -60 to 70
            Math.random() * 75, // y spawns anywhere y: 0 to 75
            Math.random() * 130 - 60   // z spawns anywhere z: -60 to 70
        ], i * 3);
    }

    rainGeo = new THREE.BufferGeometry();
    rainGeo.setAttribute('position', new THREE.BufferAttribute(rainPositions, 3));
    const rainMaterial = new THREE.PointsMaterial({
        color: RAIN_COLOR, // color of the raindrops
        size: rainSize, // size of the raindrops
    });

    rainSystem = new THREE.Points(rainGeo, rainMaterial);
    scene.add(rainSystem);
    isRaining = true;
};

const stopRain = () => {
    if (rainSystem) {
        scene.remove(rainSystem);
        rainSystem.geometry.dispose();
        rainSystem.material.dispose();
        rainSystem = null;
    }
    isRaining = false;
};

/**
 * Initialises threejs components, e.g. scene, camera, renderer, controls
 */
const init = () => {
    scene = new THREE.Scene();

    camera = new THREE.PerspectiveCamera(FOV, container.clientWidth / container.clientHeight);
    camera.position.copy(INIT_CAMERA_POSITION);
    camera.lookAt(scene.position);

    renderer = new THREE.WebGLRenderer(
        {
            antialias: true,
            preserveDrawingBuffer: true,
        }
    );
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(container.clientWidth, container.clientHeight);
    container.appendChild(renderer.domElement);
    window.createImageBitmap = undefined;

    loader = new Loader();

    loader.setOnError(() => {
        loadingImg.classList.add("d-none");
        loadingDiv.innerText = "There was an error loading your garden";
    });

    controls = new OrbitControls(camera, renderer.domElement);
    controls.maxPolarAngle = Math.PI / 2;
    controls.minRadius = MIN_CAMERA_DIST;
    controls.maxRadius = MAX_CAMERA_DIST;

    downloader = new Downloader(link);

    exporter = new Exporter(gardenName, downloader);
    changeSkybox(weather, time);
};

/**
 * Adds a light to the scene
 */
const addLight = () => {
    light = new THREE.AmbientLight(0xffffff, 0.00);
    scene.add(light);
};

/** Add model to scene
 *
 * @param {Object} model - The model to be added to the scene.
 * @param {Object} position - The position at which the model will be placed in the scene.
 * @param {number} [scaleFactor=1] - The scale factor to be applied to the model. Default value is 1.
 */
const addModelToScene = (model, position, scaleFactor = 1) => {
    model.position.copy(position);
    model.scale.set(scaleFactor, scaleFactor, scaleFactor);
    scene.add(model);
};

/**
 * Changes the skybox based on time of the day
 */
const changeSkybox = (weather, time) => {
    if (time > 6 && time < 18) {
        setBackground(skyboxMap[weather]);
    } else {
        setBackground("nightbox.exr")
        //set night backgrounds here
    }
}

init();

addLight();

setBackground(skyboxMap[weather]);

setWeather(weather);

const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "Grass", loader);

scene.add(grid);

/**
 * Adds a plant or decoration object to the scene.
 *
 * @param {Object} plantOrDecoration - The plant or decoration object to be added.
 * @returns {Promise<void>} - A promise that resolves when the object is added to the scene.
 */
const addObjectToScene = async (plantOrDecoration) => {
    const x = (plantOrDecoration.xcoordinate - ((GRID_SIZE - 1) / 2)) * TILE_SIZE;
    const z = (plantOrDecoration.ycoordinate - ((GRID_SIZE - 1) / 2)) * TILE_SIZE;
    const position = new THREE.Vector3(x, 0, z);

    const category = plantOrDecoration.category;

    const loadedModel = await loader.loadModel(modelMap[category][0], category);

    if (category === "Creeper") {
        loadedModel.traverse((child) => {
            if (child.isMesh) {
                child.material = createHueSaturationMaterial(
                    child.material.map,
                    0.2,
                    1.56,
                    2
                );
            }
        });
    }

    addModelToScene(
        loadedModel,
        position,
        modelMap[category][1]);
};

const response = await fetch(`/${getInstance()}3D-garden-layout/${gardenId}`);
const placedGardenObjects = await response.json();
placedGardenObjects.forEach((element) => addObjectToScene(element));

const backgroundModel = await loader.loadModel(modelMap["Background"][0], "Background");
addModelToScene(
    backgroundModel,
    new THREE.Vector3(0,0,0),
    modelMap["Background"][1]);

/**
 * Renders the scene
 */
const animate = () => {
    light.position.copy(camera.position);
    renderer.render(scene, camera);

    if (isRaining && rainSystem) {
        const positions = rainSystem.geometry.attributes.position.array;
        for (let i = 0; i < rainCount; i++) {
            positions[i * 3 + 1] -= 0.5 + Math.random() * 0.1;  // Update y position

            if (positions[i * 3 + 1] < 0) {
                positions[i * 3 + 1] = 75; // If below tiles (y=0) Reset to top y postion
            }
        }
        rainSystem.geometry.attributes.position.needsUpdate = true; // Mark the position attribute as needing an update
    }
};

renderer.setAnimationLoop(animate);

// Hide loading screen
loadingImg.classList.add("d-none");
loadingDiv.classList.add("fadeOut");
loadingDiv.parentElement.removeChild(loadingDiv)

/**
 * Event Handlers
 */

/**
 * On window resize event, update the camera aspect ratio and renderer size
 */
const onWindowResize = () => {
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
};

/**
 * On mouse move event on the canvas prevent user selection
 */
const onMouseMove = () => {
    document.body.style.userSelect = "none";
};

/**
 * On mouse out event on the canvas allow user selection
 */
const onMouseOut = () => {
    document.body.style.userSelect = "auto";
};

/**
* On track time input change,
* update the time variable to the current hour if the input is checked,
* otherwise set it to the default time
*/
const onTrackTimeInputChange = () => {
    const newTime = trackTimeInput.checked ? currentHour : DEFAULT_TIME;
    setTime(newTime);
};

/**
 * On track weather input change,
 * update the weather variable to the current weather if the input is checked,
 * otherwise set it to the default weather
 */
const onTrackWeatherInputChange = () => {
    const newWeather = trackWeatherInput.checked ? currentWeather : DEFAULT_WEATHER;
    setWeather(newWeather);
};

const onRainDropSizeInputChange = () => {
    rainSize = +rainDropSizeInput.value;
    stopRain();
    startRain();
};

/**
 * On track background model input change,
 * update the background model variable to display/hide the background model
 */
const onBackgroundModelInputChange = () => {
    backgroundModel.visible = trackBackgroundInput.checked;
};

console.log(scene.children);

window.addEventListener("resize", onWindowResize);
container.addEventListener("mousemove", onMouseMove);
container.addEventListener("mouseout", onMouseOut);
downloadGLTFButton.addEventListener("click", () => exporter.downloadGLTF(scene));
downloadOBJButton.addEventListener("click", () => exporter.downloadOBJ(scene));
downloadJPGButton.addEventListener("click", () => exporter.downloadJPG(renderer));
trackTimeInput.addEventListener("change", onTrackTimeInputChange);
trackWeatherInput.addEventListener("change", onTrackWeatherInputChange);

if (rainDropSizeInput){
    rainDropSizeInput.addEventListener("change", onRainDropSizeInputChange);
}
trackBackgroundInput.addEventListener("change", onBackgroundModelInputChange);