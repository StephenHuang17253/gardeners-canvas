import * as THREE from 'three';
import { applyOutline } from './selectionManger.js';
import {loadTexture, addModelToScene, loadModel} from './utils.js';
import { createTileGrid, loadPlantsAtPositions } from './tiles.js';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';

import { EXRLoader } from 'three/addons/loaders/EXRLoader.js';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/addons/exporters/OBJExporter.js';

let scene, camera, renderer, light, raycaster, pointer, controls, gltfExporter, objExporter;

const container = document.getElementById('container');
const infoBox = document.getElementById('info-box');
const downloadGLTFButton = document.getElementById('download-gltf');
const downloadOBJButton = document.getElementById('download-obj');
const FOV = 75;

const GRID_SIZE = 7;
const TILE_SIZE = 10;

const textureLoader = new THREE.TextureLoader();
const gltfLoader = new GLTFLoader();

// link used to download the file
const link = document.createElement('a');
link.style.display = 'none';
document.body.appendChild(link);

// Initialises main components of the scene
const init = () => {
    scene = new THREE.Scene();

    camera = new THREE.PerspectiveCamera(FOV, container.clientWidth / container.clientHeight);
    camera.position.set(5, 5, 5);
    camera.lookAt(scene.position);

    renderer = new THREE.WebGLRenderer({ antialias: true });
    renderer.setPixelRatio(window.devicePixelRatio);
    renderer.setSize(container.clientWidth, container.clientHeight);
    container.appendChild(renderer.domElement);

    controls = new OrbitControls(camera, renderer.domElement);
    controls.enableDamping = true;
    // Prevent camera from going below the ground
    controls.maxPolarAngle = Math.PI / 2 - 0.1;

    raycaster = new THREE.Raycaster();
    pointer = null;

    gltfExporter = new GLTFExporter();
    objExporter = new OBJExporter();
}

// Adds a light to the scene
const addLight = () => {
    light = new THREE.AmbientLight(0xffffff);
    scene.add(light);
}

// Returns an array of objects that the raycaster intersects with
const getIntersects = () => {
    raycaster.setFromCamera(pointer, camera);
    return raycaster.intersectObjects(scene.children, true);
}

// Method that gets called every frame
const animate = () => {

    controls.update();

    light.position.copy(camera.position);

    if (pointer) {
        const intersects = getIntersects();
        let text = '';
        if (intersects.length > 0) {
            text = intersects[0].object.name;
        }
        infoBox.innerText = text;
    }

    renderer.render(scene, camera);
}

const loadHDRI = (url) => {
    const loader = new EXRLoader();
    loader.load(
        url,
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

const grassTexture = loadTexture('../textures/grass-tileable.jpg',textureLoader);

const positions = createTileGrid(scene, GRID_SIZE, GRID_SIZE, TILE_SIZE, grassTexture, 0.2, 1.56);

// loadPlantsAtPositions(scene, positions, plantFilename, 1);

// loadPlant('fiddle_leaf_plant.glb', new THREE.Vector3(0, 0, 10));
//
// loadPlant('banana_plant_with_pot.glb', new THREE.Vector3(0, 0, -10));
//

const fernModel = await loadModel('fern.glb','fern', gltfLoader);
addModelToScene(fernModel, scene, new THREE.Vector3(0, 0, 20), 2);

loadHDRI('../textures/night.exr');

renderer.setAnimationLoop(animate);

// Resize the renderer and fixes camera perspective when the window is resized
const onWindowResize = () => {
    camera.aspect = container.clientWidth / container.clientHeight;
    camera.updateProjectionMatrix();
    renderer.setSize(container.clientWidth, container.clientHeight);
}

// Move the camera in the direction of the arrow keys
const onKeyDown = (event) => {
    if (!pointer) {
        return;
    }
    const cameraDirection = new THREE.Vector3();
    camera.getWorldDirection(cameraDirection);
    const up = new THREE.Vector3(0, 1, 0);
    const movementScalar = 0.5;
    switch (event.key) {
        case 'ArrowLeft':
            event.preventDefault();
            camera.position.add(cameraDirection.cross(up).normalize().multiplyScalar(-movementScalar));
            break;
        case 'ArrowRight':
            event.preventDefault();
            camera.position.add(cameraDirection.cross(up).normalize().multiplyScalar(movementScalar));
            break;
        case 'ArrowUp':
            event.preventDefault();
            camera.position.y += movementScalar;
            break;
        case 'ArrowDown':
            event.preventDefault();
            camera.position.y -= movementScalar;
            break;
        default:
            break;
    }
    camera.updateProjectionMatrix();
}

// Updates the pointer position
const updatePointer = (event) => {
    if (!pointer) {
        pointer = new THREE.Vector2();
    }
    const bounds = container.getBoundingClientRect();
    pointer.x = ((event.clientX - bounds.left) / container.clientWidth) * 2 - 1;
    pointer.y = - ((event.clientY - bounds.top) / container.clientHeight) * 2 + 1;
}

// stops page elements from being highlighted when double clicks occur on the canvas
const onMouseMove = (event) => {
    updatePointer(event);
    document.body.style.userSelect = 'none';
}

// Resets the user selection to default, allows things to be highlighted again
const onMouseOut = () => {
    pointer = null;
    document.body.style.userSelect = 'auto';
}

// Highlight the object selected
const onClick = (event) => {
    updatePointer(event);
    const intersects = getIntersects();
    if (intersects.length > 0) {
        const object = intersects[0].object;
        applyOutline(object, scene);
    }
}

const save = (blob, filename) => {
    link.href = URL.createObjectURL(blob);
    link.download = filename;
    link.click();
}

const saveString = (text, filename) => {
    save(new Blob([text], { type: 'text/plain' }), filename);
}


const saveArrayBuffer = (buffer, filename) => {
    save(new Blob([buffer], { type: 'application/octet-stream' }), filename);
}

const onDownloadButtonClick = (fileType) => {
    switch (fileType) {
        case 'gltf':
            gltfExporter.parse(
                scene,
                result => {
                    if (result instanceof ArrayBuffer) {
                        saveArrayBuffer(result, 'scene.glb');
                    } else {
                        const output = JSON.stringify(result, null, 2);
                        saveString(output, 'scene.gltf');
                    }
                },
                error => console.log('An error happened while saving the scene')
            );
            break;
        case 'obj':
            saveString(objExporter.parse(scene), 'scene.obj');
            break;
        default:
            break;
    }
}

window.addEventListener('keydown', onKeyDown);
window.addEventListener('resize', onWindowResize);
window.addEventListener('load', updatePointer);
container.addEventListener('mousemove', onMouseMove);
container.addEventListener('mouseout', onMouseOut);
container.addEventListener('click', onClick);
downloadGLTFButton.addEventListener('click', () => onDownloadButtonClick('gltf'));
downloadOBJButton.addEventListener('click', () => onDownloadButtonClick('obj'));

console.log(scene.children);