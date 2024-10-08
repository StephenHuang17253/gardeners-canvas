import * as THREE from "three";
import {createTileGrid} from "./tiles.js";
import {OrbitControls} from "./OrbitControls.js";
import {Loader} from "./Loader.js";
import {createHueSaturationMaterial} from "./hueSaturationShader.js";
import {Exporter} from "./Exporter.js";
import {Downloader} from "../Downloader.js";

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
    "Sun": ["sunObject.glb", 20],
    "Background": ["background.glb", 100],
};

const skyboxMap = {
    "Sunny": "sunny-day.exr",
    "Overcast": "cloudy-day.exr",
    "Rainy": "cloudy-day.exr",
    "Default": "sunny-day.exr",
    "Clear Night": "nightbox.exr",
    "Overcast Night": "overcast_nightbox.exr"
};

let scene, camera, renderer, controls, loader, exporter, light, downloader, moon, sun, moonParameters, rainSystem,
    rainGeo, backgroundModel;

let rainSize = 0.20;

const container = document.getElementById("container");

const downloadGLTFButton = document.getElementById("download-gltf");
const downloadOBJButton = document.getElementById("download-obj");
const downloadJPGButton = document.getElementById("download-jpg");

const trackTimeInput = document.getElementById("trackTime");
const trackWeatherInput = document.getElementById("trackWeather");
const trackBackgroundInput = document.getElementById("toggleBackground");
trackTimeInput.checked = true;
trackWeatherInput.checked = true;
trackBackgroundInput.checked = false;


const loadingDiv = document.getElementById("loading-div");
const loadingImg = document.getElementById("loading-img");

const FOV = 75;

const GRID_SIZE = 7;
const TILE_SIZE = 10;

const MIN_CAMERA_DIST = TILE_SIZE / 2;
const MAX_CAMERA_DIST = GRID_SIZE * TILE_SIZE;

const DEFAULT_TIME = 12;
const DEFAULT_WEATHER = "Default";

const MOON_ORBIT_RADIUS = 1500;
const SUN_ORBIT_RADIUS = 1500;

const RAIN_COLOR = 0x78b8c2;
const RAIN_COUNT = 3000;

const MAX_ELEVATION = 25;
const MIN_ELEVATION = 10;
const MAX_AZIMUTH = 90;
const MIN_AZIMUTH = -90;
const HALF_MOON_JOURNEY = 6;
const MORNING_START = 6;
const NIGHT_START = 18;

const INIT_CAMERA_POSITION = new THREE.Vector3(0, 45, 45);

// link used to download files
const link = document.createElement("a");

const gardenId = document.getElementById("gardenId").value;
const gardenName = document.getElementById("gardenName").value;

const currentHourInput = document.getElementById("currentHour");
const currentWeatherInput = document.getElementById("weather");
const rainDropSizeInput = document.getElementById("rainDropSize");

// Get the current time and weather from the input fields if they exist or use the default values
// For if the garden does not have a location set
const currentHour = currentHourInput.value !== "" ? currentHourInput.value : DEFAULT_TIME;
const currentWeather = currentWeatherInput.value !== "" ? currentWeatherInput.value : DEFAULT_WEATHER;

let time = currentHour;
let weather = currentWeather;
let isRaining = weather === "Rainy";

const ROTATION_SPEED = 0.005; // Speed of the camera rotation
let angle = 0; // Initial angle for rotation

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

    updateSkybox();

    const tileMapResponse = await fetch(`/${getInstance()}3D-tile-textures-grid/${gardenId}`)
    const tileMapTextures = await tileMapResponse.json();
    const grid = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, tileMapTextures, loader);
    scene.add(grid);

    const texture = loader.loadTexture("moon.jpg");

    const material = new THREE.MeshPhongMaterial(
        {
            color: 0xffffff,
            map: texture,
        }
    );

    // initializing moon
    const geometry = new THREE.SphereGeometry(16, 60, 60);
    moon = new THREE.Mesh(geometry, material);
    scene.add(moon);

    sun = await loader.loadModel(modelMap["Sun"][0], "Sun");

    const sunPosition = new THREE.Vector3(0, 50, 0);
    sun.position.copy(sunPosition);
    sun.scale.set(modelMap["Sun"][1], modelMap["Sun"][1], modelMap["Sun"][1]);
    scene.add(sun);

    light = new THREE.HemisphereLight(0xfff5bf, 0xfff5bf, 0.6);
    light.intensity = 5;
    light.position.set(0, 50, 0);
    scene.add(light);
    updateSun();

    setTime(time);

    const response = await fetch(`/${getInstance()}3D-garden-layout/${gardenId}`);
    const placedGardenObjects = await response.json();
    await placedGardenObjects.forEach(async (element) => await addObjectToScene(element));

    if (isRaining) {
        startRain();
    }

    backgroundModel = await loader.loadModel(modelMap["Background"][0], "Background");
    addModelToScene(
        backgroundModel,
        new THREE.Vector3(0, 0, 0),
        modelMap["Background"][1]);
    backgroundModel.visible = false;

    // Hide loading screen
    loadingImg.classList.add("d-none");
    loadingDiv.classList.add("fadeOut");
    loadingDiv.parentElement.removeChild(loadingDiv);
};

/**
 * Helper Methods
 */

/**
 * Finds if is daytime or not
 *
 * @returns {boolean} - true if is daytime else false
 */
const isDayTime = () => time >= MORNING_START && time < NIGHT_START;

/**
 * Starts the rain in the scene
 */
const startRain = () => {
    const rainPositions = new Float32Array(RAIN_COUNT * 3); // 3 coordinates per rain drop. x,y,z

    for (let i = 0; i < RAIN_COUNT; i++) { // Iterate through each raindrop
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

/**
 * Stops the rain in the scene
 */
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
 * Updates the time of day in the scene
 *
 * @param {number} newTime
 */
const setTime = (newTime) => {
    time = newTime;
    // Update the time of day in the scene
    // Set moon or sun to the correct position
    if (isDayTime()) {
        sun.visible = true;
        moon.visible = false;
        updateSun();
    } else {
        sun.visible = false;
        moon.visible = true;
        setMoonParameters();
        updateMoon();
    }

    updateSkybox();
};

/**
 * Updates the weather in the scene
 *
 * @param {String} newWeather
 */
const setWeather = (newWeather) => {
    weather = newWeather;
    updateSkybox();
    // change clouds and rain to match the weather
    if (weather === "Rainy") {
        startRain();
    } else if (isRaining) { // If rain currently exists but is not raining
        isRaining = false;
        stopRain(); //Deletes rain in rainSystem (Rain on existing scene)
    }
};

/**
 * Sets moon parameters based on current time.
 */
const setMoonParameters = () => {
    const isBeforeMorningAndAfterMidnight = time < MORNING_START;
    let newElevation;
    let newAzimuth;
    if (isBeforeMorningAndAfterMidnight) {
        newElevation = Math.round(MAX_ELEVATION - time * ((MAX_ELEVATION - MIN_ELEVATION) / HALF_MOON_JOURNEY));
        newAzimuth = Math.round(time * MIN_AZIMUTH / HALF_MOON_JOURNEY);
    } else {
        newElevation = Math.round(MIN_ELEVATION + MAX_ELEVATION / HALF_MOON_JOURNEY * (time - (NIGHT_START - 1)));
        newAzimuth = Math.round((time - (NIGHT_START - 1)) * (MAX_AZIMUTH / HALF_MOON_JOURNEY));
    }
    moonParameters = {
        elevation: newElevation,
        azimuth: newAzimuth
    };
};

/**
 * Updates the skybox based on the time and weather
 */
const updateSkybox = () => {
    if (isDayTime()) {
        setBackground(skyboxMap[weather]);
    } else {
        if (weather === "Rainy" || weather === "Overcast") {
            setBackground(skyboxMap["Overcast Night"])
        } else {
            setBackground(skyboxMap["Clear Night"])
        }
    }
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
    angle += ROTATION_SPEED;

    // Calculate the new camera position
    const radius = INIT_CAMERA_POSITION.length(); // Distance from the origin
    camera.position.x = radius * Math.sin(angle);
    camera.position.z = radius * Math.cos(angle);
    camera.position.y = 30;
    camera.lookAt(scene.position); // Keep looking at the center of the scene

    renderer.render(scene, camera);


    if (isRaining && rainSystem) {
        const positions = rainSystem.geometry.attributes.position.array;
        for (let i = 0; i < RAIN_COUNT; i++) {
            positions[i * 3 + 1] -= 0.5 + Math.random() * 0.1;  // Update y position

            if (positions[i * 3 + 1] < 0) {
                positions[i * 3 + 1] = 75; // If below tiles (y=0) Reset to top y position
            }
        }
        rainSystem.geometry.attributes.position.needsUpdate = true; // Mark the position attribute as needing an update
    }

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
 * Updates the movement of the sun based on the gardens time
 */
const updateSun = () => {
    const sunY = (SUN_ORBIT_RADIUS / 3) - Math.abs(SUN_ORBIT_RADIUS * (time - 12) / 18);
    const sunZ = (SUN_ORBIT_RADIUS / 6) * (time - 12);
    sun.position.z = sunZ;
    sun.position.y = sunY;
    sun.position.x = SUN_ORBIT_RADIUS;
    light.position.set(SUN_ORBIT_RADIUS - 30, sunY, sunZ);
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

/**
 * On rain drop size input change, update the rain drop size
 */
const onRainDropSizeInputChange = () => {
    rainSize = +rainDropSizeInput.value;
    stopRain();
    startRain();
};

init();
/**
 * On track background model input change,
 * update the background model variable to display/hide the background model
 */
const onBackgroundModelInputChange = () => {
    backgroundModel.visible = trackBackgroundInput.checked;
};

window.addEventListener("resize", onWindowResize);
container.addEventListener("mousemove", onMouseMove);
container.addEventListener("mouseout", onMouseOut);
downloadGLTFButton.addEventListener("click", () => exporter.downloadGLTF(scene));
downloadOBJButton.addEventListener("click", () => exporter.downloadOBJ(scene));
downloadJPGButton.addEventListener("click", () => exporter.downloadJPG(renderer));
trackTimeInput.addEventListener("change", onTrackTimeInputChange);
trackWeatherInput.addEventListener("change", onTrackWeatherInputChange);

if (rainDropSizeInput) {
    rainDropSizeInput.addEventListener("change", onRainDropSizeInputChange);
}
trackBackgroundInput.addEventListener("change", onBackgroundModelInputChange);