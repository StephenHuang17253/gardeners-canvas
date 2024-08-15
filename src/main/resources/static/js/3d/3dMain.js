import * as THREE from 'three';
import { applyOutline } from './selectionManager.js';
import { loadTexture, addModelToScene, loadModel } from './utils.js';
import { createTileGrid, loadPlantsAtPositions } from './tiles.js';
import { OrbitControls } from './OrbitControls.js';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';

import { EXRLoader } from 'three/addons/loaders/EXRLoader.js';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/addons/exporters/OBJExporter.js';

let scene, camera, renderer, manager, light, raycaster, pointer, controls, gltfExporter, objExporter, textureLoader, gltfLoader, exrLoader;

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

    manager = new THREE.LoadingManager();

    manager.onLoad = () => {
        loadingImg.classList.add('d-none');
        loadingDiv.classList.add('fadeOut');
        setTimeout(() => loadingDiv.parentElement.removeChild(loadingDiv), 500);
    };

    manager.onError = () => {
        loadingImg.classList.add('d-none');
        loadingDiv.innerText = 'There was an error loading your garden';
    };

    controls = new OrbitControls(camera, renderer.domElement);
    controls.maxPolarAngle = Math.PI / 2 - 0.1;
    controls.minDistance = MIN_CAMERA_DIST;
    controls.maxDistance = MAX_CAMERA_DIST;
    controls.minTargetRadius = MIN_CAMERA_DIST;
    controls.maxTargetRadius = MAX_CAMERA_DIST;

    raycaster = new THREE.Raycaster();
    pointer = null;

    gltfExporter = new GLTFExporter(manager);
    objExporter = new OBJExporter(manager);

    textureLoader = new THREE.TextureLoader(manager);
    gltfLoader = new GLTFLoader(manager);
    exrLoader = new EXRLoader(manager);
};

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

/**
 * Loads an EXR image and sets it as the background and environment of the scene
 * @param {String} path 
 */
const loadEXR = (path) => {
    exrLoader.load(
        path,
        texture => {
            texture.mapping = THREE.EquirectangularReflectionMapping;
            scene.background = texture;
            scene.environment = texture;
        },
        undefined,
        error => console.error('An error occurred while loading the EXR file:', error)
    );
};

init();
addLight();
loadEXR('../textures/nightbox.exr');

const grassTexture = loadTexture('../textures/grass-tileable.jpg', textureLoader);

const positions = createTileGrid(scene, GRID_SIZE, GRID_SIZE, TILE_SIZE, grassTexture, 0.2, 1.56);

// loadPlantsAtPositions(scene, positions, plantFilename, 1);

const fernModel = await loadModel('fern.glb', 'fern', gltfLoader);

addModelToScene(fernModel, scene, new THREE.Vector3(0, 0, 20), 2);

renderer.setAnimationLoop(animate);

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

/**
 * 
 * @param {String} data - data to create a data url for
 * @param {String} type - mime type of the data
 * @returns {String} data url for the created data
 */
const createDataUrl = (data, type) => URL.createObjectURL(new Blob([data], { type: type }));

/**
 * Saves the data to a file with the given filename
 * @param {String} dataURL - url of data to save
 * @param {String} filename - name of the file to save
 */
const downloadFile = (dataURL, filename) => {
    document.body.appendChild(link);
    link.href = dataURL;
    link.download = filename;
    link.click();
    document.body.removeChild(link);
};

/**
 * On download button click, download the scene as a file with the given file extension
 * @param {String} fileType - file extension of the file to download, gltf, obj, or jpg 
 */
const onDownloadButtonClick = (fileType) => {
    switch (fileType) {
        case 'gltf': {
            gltfExporter.parse(
                scene,
                result => {
                    if (result instanceof ArrayBuffer) {
                        downloadFile(createDataUrl(result, 'application/octet-stream'), 'scene.glb');
                    } else {
                        const output = JSON.stringify(result, null, 1);
                        downloadFile(createDataUrl(output, 'text/plain'), 'scene.gltf');
                    }
                },
                error => console.log('An error happened while saving the scene')
            );
            break;
        }
        case 'obj': {
            const objData = objExporter.parse(scene);
            downloadFile(createDataUrl(objData, 'text/plain'), 'scene.obj');
            break;
        }
        case 'jpg': {
            try {
                const strMime = "image/jpeg";
                const strDownloadMime = "image/octet-stream";
                const imgData = renderer.domElement.toDataURL(strMime);
                downloadFile(imgData.replace(strMime, strDownloadMime), "scene.jpg");
            } catch (error) {
                console.log(error);
                return;
            }
            break;
        }
        default:
            break;
    }
};

window.addEventListener('resize', onWindowResize);
window.addEventListener('load', updatePointer);
container.addEventListener('mousemove', onMouseMove);
container.addEventListener('mouseout', onMouseOut);
container.addEventListener('click', onClick);
downloadGLTFButton.addEventListener('click', () => onDownloadButtonClick('gltf'));
downloadOBJButton.addEventListener('click', () => onDownloadButtonClick('obj'));
downloadJPGButton.addEventListener('click', () => onDownloadButtonClick('jpg'));

console.log(scene.children);