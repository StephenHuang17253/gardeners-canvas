import * as THREE from 'three';
import { createTileGrid } from './tiles.js';
import { OrbitControls } from './OrbitControls.js';
import { Loader } from './Loader.js';
import { createHueSaturationMaterial } from "./hueSaturationShader.js";
import { Exporter } from './Exporter.js';
import { Downloader } from '../Downloader.js';

const modelMap = {
    "Tree": ["tree.glb", 5],
    "Shrub": ["shrub.glb", 10],
    "Bush": ["shrub.glb", 20],
    "Herb": ["fern.glb", 1],
    "Creeper": ["creeper.glb", 0.5],
    "Climber": ["climber.glb", 5],
    "Flower": ["flower.glb", 10],
    "Pot Plant": ["potplant.glb", 5]
};

const decoMap = {
    "Rock": ["deco/rock.glb", 2],
    "Pond": ["deco/pond.glb", 2],
    "Gnome": ["deco/gnome.glb", 7],
    "Fountain": ["deco/fountain.glb", 3],
    "Table": ["deco/table.glb", 4.5],
};

let scene, camera, renderer, controls, loader, exporter, light, downloader;

const container = document.getElementById('container');

const downloadGLTFButton = document.getElementById('download-gltf');
const downloadOBJButton = document.getElementById('download-obj');
const downloadJPGButton = document.getElementById('download-jpg');

const loadingDiv = document.getElementById('loading-div');
const loadingImg = document.getElementById('loading-img');

const FOV = 75;

const GRID_SIZE = 7;
const TILE_SIZE = 10;

const MIN_CAMERA_DIST = TILE_SIZE / 2;
const MAX_CAMERA_DIST = GRID_SIZE * TILE_SIZE;

// link used to download files
const link = document.createElement('a');

const gardenId = document.getElementById("gardenId").value;
const gardenName = document.getElementById("gardenName").value;

/**
 * Initialises threejs components, e.g. scene, camera, renderer, controls
 */
const init = () => {
    scene = new THREE.Scene();

    camera = new THREE.PerspectiveCamera(FOV, container.clientWidth / container.clientHeight);
    camera.position.set(5, 5, 5);
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
        loadingImg.classList.add('d-none');
        loadingDiv.innerText = 'There was an error loading your garden';
    });

    controls = new OrbitControls(camera, renderer.domElement);
    controls.maxPolarAngle = Math.PI / 2;
    controls.minRadius = MIN_CAMERA_DIST;
    controls.maxRadius = MAX_CAMERA_DIST;

    downloader = new Downloader(link);

    exporter = new Exporter(gardenName, downloader);
};

/**
 * Adds a light to the scene
 */
const addLight = () => {
    light = new THREE.AmbientLight(0xffffff);
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

init();

addLight();

loader.loadBackground(
    'skybox.exr',
    texture => {
        scene.background = texture;
        scene.environment = texture;
    }
);

/**
 * Fetches 2D tile map data
 * @returns {Promise<any>} - A promise that resolves with the fetched data
 */
const fetchTileMap = async (gardenId) => {
    const instance = getInstance();
    const response = await fetch(`/${instance}3D-tile-textures-grid/${gardenId}`)
    return await response.json();
}

// const grassTexture = loader.loadTexture('grass-tileable.jpg');

// const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'Grass', 0.2, 1.56, loader);
const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'StonePath', 0, 1, loader);
// const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'PebblePath', 0.2, 1.56, loader);
// const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'Bark', 0, 1, loader);
// const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'Soil', 0.055, 1.06, loader);
// const { grid } = await createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, 'Concrete', 0, 1, loader);
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

    console.log(category, modelMap[category]);
    console.log(loadedModel);

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

// const rockModel = await loader.loadModel(decoMap['Rock'][0], 'Rock');
// const pondModel = await loader.loadModel(decoMap['Pond'][0], 'Pond');
// const gnomeModel = await loader.loadModel(decoMap['Gnome'][0], 'Gnome');
// const fountainModel = await loader.loadModel(decoMap['Fountain'][0], 'Fountain');
// const tableModel = await loader.loadModel(decoMap['Table'][0], 'Table');
// addModelToScene(
//     rockModel,
//     new THREE.Vector3(10, 0, 0),
//     decoMap['Rock'][1]);
// addModelToScene(
//     pondModel,
//     new THREE.Vector3(0, 0, 0),
//     decoMap['Pond'][1]);
// addModelToScene(
//     gnomeModel,
//     new THREE.Vector3(-10, 0, 0),
//     decoMap['Gnome'][1]);
// addModelToScene(
//     fountainModel,
//     new THREE.Vector3(0, 0, 10),
//     decoMap['Fountain'][1]);
// addModelToScene(
//     tableModel,
//     new THREE.Vector3(0, 0, -10),
//     decoMap['Table'][1]);

/**
 * Renders the scene
 */
const animate = () => {
    light.position.copy(camera.position);
    renderer.render(scene, camera);
};

renderer.setAnimationLoop(animate);

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
    document.body.style.userSelect = 'none';
};

/**
 * On mouse out event on the canvas allow user selection
 */
const onMouseOut = () => {
    document.body.style.userSelect = 'auto';
};

window.addEventListener('resize', onWindowResize);
container.addEventListener('mousemove', onMouseMove);
container.addEventListener('mouseout', onMouseOut);
downloadGLTFButton.addEventListener('click', () => exporter.downloadGLTF(scene));
downloadOBJButton.addEventListener('click', () => exporter.downloadOBJ(scene));
downloadJPGButton.addEventListener('click', () => exporter.downloadJPG(renderer));


console.log(scene.children);

loadingImg.classList.add('d-none');
loadingDiv.classList.add('fadeOut');
setTimeout(() => loadingDiv.parentElement.removeChild(loadingDiv), 500);