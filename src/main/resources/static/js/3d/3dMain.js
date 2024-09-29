import * as THREE from "three";
import {createTileGrid} from "./tiles.js";
import {OrbitControls} from "./OrbitControls.js";
import {Loader} from "./Loader.js";
import {createHueSaturationMaterial} from "./hueSaturationShader.js";
import {Exporter} from "./Exporter.js";
import {Downloader} from "../Downloader.js";
import {GUI} from "three/addons/libs/lil-gui.module.min.js";

const modelMap = {
    "Tree": ["tree.glb", 5],
    "Shrub": ["shrub.glb", 10],
    "Bush": ["shrub.glb", 20],
    "Herb": ["fern.glb", 1],
    "Creeper": ["creeper.glb", 0.5],
    "Climber": ["climber.glb", 5],
    "Flower": ["flower.glb", 10],
    "Pot Plant": ["potplant.glb", 5],
    "Rock": ["deco/rock.glb", 2],
    "Pond": ["deco/pond.glb", 2],
    "Gnome": ["deco/gnome.glb", 7],
    "Fountain": ["deco/fountain.glb", 3],
    "Table": ["deco/table.glb", 4.5],
};

const skyboxMap = {
    "Sunny": "sunny_skybox.exr",
    "Overcast": "cloudy_skybox.exr",
    "Rainy": "cloudy_skybox.exr",
};

let scene, camera, renderer, controls, loader, exporter, light, downloader, moon, moonParameters;

const container = document.getElementById("container");

const downloadGLTFButton = document.getElementById("download-gltf");
const downloadOBJButton = document.getElementById("download-obj");
const downloadJPGButton = document.getElementById("download-jpg");

const trackTimeInput = document.getElementById("trackTime");
const trackWeatherInput = document.getElementById("trackWeather");

const loadingDiv = document.getElementById("loading-div");
const loadingImg = document.getElementById("loading-img");

const FOV = 75;

const GRID_SIZE = 7;
const TILE_SIZE = 10;

const MIN_CAMERA_DIST = TILE_SIZE / 2;
const MAX_CAMERA_DIST = GRID_SIZE * TILE_SIZE;

const DEFAULT_TIME = 12;
const DEFAULT_WEATHER = "Sunny";

const MOON_ORBIT_RADIUS = 100;

const INIT_CAMERA_POSITION = new THREE.Vector3(0, 45, 45);

// link used to download files
const link = document.createElement("a");

const gardenId = document.getElementById("gardenId").value;
const gardenName = document.getElementById("gardenName").value;

const currentHourInput = document.getElementById("currentHour");
const currentWeatherInput = document.getElementById("weather");

// Get the current time and weather from the input fields if they exist or use the default values
// For if the garden does not have a location set
const currentHour = currentHourInput.value !== "" ? currentHourInput.value : DEFAULT_TIME;
const currentWeather = currentWeatherInput.value !== "" ? currentWeatherInput.value : DEFAULT_WEATHER;

let time = currentHour;
let weather = currentWeather;

/**
 * Initialises threejs components, e.g. scene, camera, renderer, controls
 */
const init = async () => {
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
    renderer.setAnimationLoop(animate);

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

    addLight();

    setBackground(skyboxMap[weather]);

    const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "Grass", 0.2, 1.56, loader);
    // const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "StonePath", 0, 0, loader);
    // const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "PebblePath", 0, 0, loader);
    // const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "Bark", 0, 0, loader);
    // const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "Soil", 0.055, 0.06, loader);
    // const grid = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, "Concrete", 0, 0, loader);
    scene.add(grid);

    const texture = loader.loadTexture("moon.jpg");

    const material = new THREE.MeshPhongMaterial(
        {
            color: 0xffffff,
            map: texture,
        }
    );

    const geometry = new THREE.SphereGeometry(2, 60, 60);
    moon = new THREE.Mesh(geometry, material);
    scene.add(moon);

    moonParameters = {
        elevation: 2,
        azimuth: 180
    };
    setMoonParameters();
    updateMoon();
    const gui = new GUI();
    const folderSky = gui.addFolder('Sky');
    folderSky.add(moonParameters, 'elevation', 0, 90, 0.1).onChange(updateMoon);
    folderSky.add(moonParameters, 'azimuth', -180, 180, 0.1).onChange(updateMoon);
    folderSky.open();

    const response = await fetch(`/${getInstance()}3D-garden-layout/${gardenId}`);
    const placedGardenObjects = await response.json();
    await placedGardenObjects.forEach(async (element) => await addObjectToScene(element));

    // Hide loading screen
    loadingImg.classList.add("d-none");
    loadingDiv.classList.add("fadeOut");
    loadingDiv.parentElement.removeChild(loadingDiv);
};

/**
 * Helper Methods
 */

/**
 * Updates the time of day in the scene
 *
 * @param {number} newTime
 */
const setTime = (newTime) => {
    time = newTime;
    // Update the time of day in the scene
    // Set moon or sun to the correct position 
};

/**
 * Sets moon parameters based on current time.
 */
const setMoonParameters = () => {
    const isBeforeMorning = time < 6;
    let newElevation;
    let newAzimuth;
    if (isBeforeMorning) {
        newElevation = Math.round(55 - time * ((55 - 10) / 6));
        newAzimuth = Math.round(time * -90 / 6);
    } else {
        newElevation = Math.round(10 + 55 / 6 * (time - 17));
        newAzimuth = Math.round((time - 17) * (90 / 6));
    }

    moonParameters = {
        elevation: newElevation,
        azimuth: newAzimuth
    };
}

/**
 * Updates the weather in the scene
 *
 * @param {String} newWeather
 */
const setWeather = (newWeather) => {
    weather = newWeather;
    setBackground(skyboxMap[weather]);
    // change clouds and rain to match the weather
};

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
};


/**
 * Adds a light to the scene
 */
const addLight = () => {
    light = new THREE.AmbientLight(0xffffff);
    scene.add(light);
};

/**
 * Add model to scene
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

/**
 * Renders the scene
 */
const animate = () => {
    light.position.copy(camera.position);
    renderer.render(scene, camera);
};

/**
 * Updates the position of the moon in the scene based on the moonParameters
 */
const updateMoon = () => {
    const phi = THREE.MathUtils.degToRad(90 - moonParameters.elevation);
    const theta = THREE.MathUtils.degToRad(moonParameters.azimuth);

    moon.position.setFromSphericalCoords(1, phi, theta);
    moon.position.multiplyScalar(MOON_ORBIT_RADIUS);
};


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
const onMouseMove = () => document.body.style.userSelect = "none";

/**
 * On mouse out event on the canvas allow user selection
 */
const onMouseOut = () => document.body.style.userSelect = "auto";

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

init();

window.addEventListener("resize", onWindowResize);
container.addEventListener("mousemove", onMouseMove);
container.addEventListener("mouseout", onMouseOut);
downloadGLTFButton.addEventListener("click", () => exporter.downloadGLTF(scene));
downloadOBJButton.addEventListener("click", () => exporter.downloadOBJ(scene));
downloadJPGButton.addEventListener("click", () => exporter.downloadJPG(renderer));
trackTimeInput.addEventListener("change", onTrackTimeInputChange);
trackWeatherInput.addEventListener("change", onTrackWeatherInputChange);

console.log(scene.children);
