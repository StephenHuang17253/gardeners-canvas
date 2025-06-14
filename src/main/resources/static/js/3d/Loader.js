import * as THREE from 'three';
import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import { EXRLoader } from 'three/addons/loaders/EXRLoader.js';

const BASE_URL = window.location.origin;

/**
 * Class to handle loading of textures and 3D models
 */
class Loader {

    /**
     * Constructor for the Loader class.
     * @constructor
     */
    constructor() {
        this.manager = new THREE.LoadingManager();
        this.manager.setURLModifier(url => url.startsWith('blob:') ? url : `${BASE_URL}/${getInstance()}${url}`);
        this.textureLoader = new THREE.TextureLoader(this.manager);
        this.gltfLoader = new GLTFLoader(this.manager);
        this.exrLoader = new EXRLoader(this.manager);
    }

    /**
     * Set the function to be called when loading is complete for each item
     * @param {() => void} callback - function to be called when loading is complete
     */
    setOnLoad = (callback) => this.manager.onLoad = callback;

    /**
     * Set the function to be called when an error occurs during loading an item
     * @param {() => void} callback - function to be called when an error occurs during loading
     */
    setOnError = (callback) => this.manager.onError = callback;

    /**
     * Load texture to be used in scene
     * 
     * @param {String} path - path to the texture 
     * @returns {THREE.Texture} - The loaded texture
     */
    loadTexture = (path) => this.textureLoader.load(`textures/${path}`);

    /**
     * Load gltf 3d model, gives all parts the same name
     * 
     * @param {string} path - The filename of the 3D model to be loaded.
     * @param {string} name - The name to be assigned to all parts of the loaded model.
     * @returns {Promise<Object>} - A promise that resolves to the loaded model scene.
     */
    loadModel = async (path, name) => {
        const model = await this.gltfLoader.loadAsync(`models/${path}`);
        model.scene.traverse((child) => {
            child.name = name;
        });
        return model.scene;
    };

    /**
     * Load background texture
     * 
     * @param {String} path - path to the texture
     * @param {(texture: THREE.Texture) => void} onLoad - The callback to be called when the texture is loaded
     */
    loadBackground = (path, onLoad) => {
        this.exrLoader.load(
            `textures/${path}`,
            texture => {
                texture.mapping = THREE.EquirectangularReflectionMapping;
                onLoad(texture);
            },
            undefined, // the onProgress callback
            undefined // the onError callback
        );
    };
}

export { Loader };