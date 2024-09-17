import * as THREE from 'three';
import { createTileGrid } from './tiles.js';
import { OrbitControls } from './OrbitControls.js';
import { Loader } from './Loader.js';
import { createHueSaturationMaterial } from "./hueSaturationShader.js";
import { Exporter } from './Exporter.js';
import { Downloader } from '../Downloader.js';
import { Sky } from 'three/addons/objects/Sky.js';
import { GUI } from 'three/addons/libs/lil-gui.module.min.js';

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
    renderer.toneMapping = THREE.ACESFilmicToneMapping;
    renderer.toneMappingExposure = 0.5;
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

// loader.loadBackground(
//     'skybox.exr',
//     texture => {
//         scene.background = texture;
//         scene.environment = texture;
//     }
// );

let updateSun;

const skyBox = () => {
    const sun = new THREE.Vector3();
    const sky = new Sky();
    sky.scale.setScalar( 10000 );
    scene.add( sky );

    const skyUniforms = sky.material.uniforms;

    skyUniforms[ 'turbidity' ].value = 10;
    skyUniforms[ 'rayleigh' ].value = 2;
    skyUniforms[ 'mieCoefficient' ].value = 0.005;
    skyUniforms[ 'mieDirectionalG' ].value = 0.8;

    let parameters = {
        elevation: 2,
        azimuth: 180
    };

    const pmremGenerator = new THREE.PMREMGenerator( renderer );
    const sceneEnv = new THREE.Scene();

    let renderTarget;

    updateSun = function() {

        const phi = THREE.MathUtils.degToRad( 90 - parameters.elevation );
        const theta = THREE.MathUtils.degToRad( parameters.azimuth );

        sun.setFromSphericalCoords( 1, phi, theta );

        sky.material.uniforms[ 'sunPosition' ].value.copy( sun );

        if ( renderTarget !== undefined ) renderTarget.dispose();

        sceneEnv.add( sky );
        renderTarget = pmremGenerator.fromScene( sceneEnv );
        scene.add( sky );

        scene.environment = renderTarget.texture;
    }
    updateSun();
    const gui = new GUI();

    const folderSky = gui.addFolder( 'Sky' );
    folderSky.add( parameters, 'elevation', 0, 90, 0.1 ).onChange( updateSun );
    folderSky.add( parameters, 'azimuth', - 180, 180, 0.1 ).onChange( updateSun );
    folderSky.open();
};
skyBox();


const grassTexture = loader.loadTexture('grass-tileable.jpg');

const { grid } = createTileGrid(GRID_SIZE, GRID_SIZE, TILE_SIZE, grassTexture, 0.2, 1.56);
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