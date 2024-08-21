import * as THREE from 'three';
import { createTileGrid } from './tiles.js';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { Loader } from './Loader.js';
import { createHueSaturationMaterial } from "./hueSaturationShader.js";
import { Exporter } from './Exporter.js';

let scene, camera, renderer, controls, loader, exporter, light;

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
const gardenName = 'My Favourite Garden';

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

    loader = new Loader();

    loader.setOnError(() => {
        loadingImg.classList.add('d-none');
        loadingDiv.innerText = 'There was an error loading your garden';
    });

    controls = new OrbitControls(camera, renderer.domElement);
    controls.maxPolarAngle = Math.PI / 2;
    controls.minRadius = MIN_CAMERA_DIST;
    controls.maxRadius = MAX_CAMERA_DIST;

    exporter = new Exporter(link, gardenName);
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

const grassTexture = loader.loadTexture('grass-tileable.jpg');

const { grid } = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, grassTexture, 0.2, 1.56);
scene.add(grid);

const fernModel = await loader.loadModel('fern.glb', 'fern');
const creeperModel = await loader.loadModel('creeper.glb', 'creeper');
const treeModel = await loader.loadModel('tree.glb', 'tree');
const flowerModel = await loader.loadModel('flower.glb', 'flower');
const shrubModel = await loader.loadModel('shrub.glb', 'shrub');
const potplantModel = await loader.loadModel('potplant.glb', 'potplant');
const climberModel = await loader.loadModel('climber.glb', 'climber');

creeperModel.traverse((child) => {
    if (child.isMesh) {
        child.material = createHueSaturationMaterial(
            child.material.map,
            0.2,
            1.56,
            2
        );
    }
});

addModelToScene(fernModel, new THREE.Vector3(0, 0, 20), 1);
addModelToScene(creeperModel, new THREE.Vector3(0, 0, -20), 0.5);
addModelToScene(treeModel, new THREE.Vector3(10, 0, 0), 5);
addModelToScene(flowerModel, new THREE.Vector3(-10, 0, 0), 10);
addModelToScene(shrubModel, new THREE.Vector3(-20, 0, 0), 10);
addModelToScene(potplantModel, new THREE.Vector3(20, 0, 0), 5);
addModelToScene(climberModel, new THREE.Vector3(0, 0, 0), 5);

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