import * as THREE from 'three';
import { applyOutline } from './selectionManager.js';
import { addModelToScene } from './utils.js';
import { createTileGrid, loadPlantsAtPositions } from './tiles.js';
import { OrbitControls } from './OrbitControls.js';
import { Loader } from './Loader.js';
import { Exporter } from './Exporter.js';

let scene, camera, renderer, light, raycaster, pointer, exporter, loader;

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

    renderer = new THREE.WebGLRenderer({ antialias: true, preserveDrawingBuffer: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(container.clientWidth, container.clientHeight);
    container.appendChild(renderer.domElement);

    loader = new Loader();

    loader.setOnLoad(() => {
        loadingImg.classList.add('d-none');
        loadingDiv.classList.add('fadeOut');
        setTimeout(() => loadingDiv.parentElement.removeChild(loadingDiv), 500);
    });

    loader.setOnError(() => {
        loadingImg.classList.add('d-none');
        loadingDiv.innerText = 'There was an error loading your garden';
    });

    const controls = new OrbitControls(camera, renderer.domElement);
    controls.maxPolarAngle = Math.PI / 2 - 0.1;
    controls.minRadius = MIN_CAMERA_DIST;
    controls.maxRadius = MAX_CAMERA_DIST;

    raycaster = new THREE.Raycaster();
    pointer = null;

    exporter = new Exporter(link, gardenName);
};

init();

/**
 * Adds a light to the scene
 */
const addLight = () => {
    light = new THREE.AmbientLight(0xffffff);
    scene.add(light);
};

/**
 * Gets the objects that the raycaster intersects with
 * @returns {THREE.Object3D[]} An array of objects that the raycaster intersects with
 */
const getIntersects = () => {
    if (!pointer) {
        return [];
    }
    raycaster.setFromCamera(pointer, camera);
    return raycaster.intersectObjects(scene.children, true);
};

/**
 * Renders the scene
 */
const animate = () => {

    light.position.copy(camera.position);

    const intersects = getIntersects();
    if (intersects.length > 0 && intersects[0].object.name !== '') {
        console.log(intersects[0].object.name);
    }

    renderer.render(scene, camera);
};

renderer.setAnimationLoop(animate);


addLight();

loader.loadBackground(
    '../textures/nightbox.exr',
    texture => {
        scene.background = texture;
        scene.environment = texture;
    }
);

const grassTexture = loader.loadTexture('../textures/grass-tileable.jpg');

const positions = createTileGrid(scene, GRID_SIZE, GRID_SIZE, TILE_SIZE, grassTexture, 0.2, 1.56);

// loadPlantsAtPositions(scene, positions, plantFilename, 1);

const fernModel = await loader.loadModel('fern.glb', 'fern');

addModelToScene(fernModel, scene, new THREE.Vector3(0, 0, 20), 2);


/**
 * On window resize event, update the camera aspect ratio and renderer size
 */
const onWindowResize = () => {
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
};

/**
 * Updates the pointer position
 * @param {MouseEvent} event - mouse event that triggered the update
 */
const updatePointer = (event) => {
    if (!pointer) {
        pointer = new THREE.Vector2();
    }
    const bounds = container.getBoundingClientRect();
    pointer.x = ((event.clientX - bounds.left) / container.clientWidth) * 2 - 1;
    pointer.y = - ((event.clientY - bounds.top) / container.clientHeight) * 2 + 1;
};

/**
 * On mouse move event on the canvas, update the pointer position, and prevent user selection
 * @param {MouseEvent} event - mouse event that triggered the update
 */
const onMouseMove = (event) => {
    updatePointer(event);
    document.body.style.userSelect = 'none';
};

/**
 * On mouse out event on the canvas, set the pointer to null and allow user selection
 */
const onMouseOut = () => {
    pointer = null;
    document.body.style.userSelect = 'auto';
};

/**
 * On click event on the canvas, update the pointer position, 
 * and apply the outline effect to the first object that the pointer intersects
 * @param {MouseEvent} event - mouse event that triggered the update
 */
const onClick = (event) => {
    updatePointer(event);
    const intersects = getIntersects();
    if (intersects.length > 0) {
        const object = intersects[0].object;
        applyOutline(object, scene);
    }
};

window.addEventListener('resize', onWindowResize);
window.addEventListener('load', updatePointer);
container.addEventListener('mousemove', onMouseMove);
container.addEventListener('mouseout', onMouseOut);
container.addEventListener('click', onClick);
downloadGLTFButton.addEventListener('click', () => exporter.downloadGLTF(scene));
downloadOBJButton.addEventListener('click', () => exporter.downloadOBJ(scene));
downloadJPGButton.addEventListener('click', () => exporter.downloadJPEG(renderer));

console.log(scene.children);