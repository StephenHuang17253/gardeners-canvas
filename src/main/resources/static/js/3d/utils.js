import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import * as THREE from 'three';

/**
 * Load gltf 3d model with named parts to be used in scene
 * @param filename
 * @param name
 * @param gltfLoader
 * @returns {Promise<*>}
 */
const loadModel = async (filename, name, gltfLoader) => {
    const model = await gltfLoader.loadAsync(`../models/${filename}`);
    model.scene.traverse((child) => {
        child.name = name;
    });
    return model.scene;
};

/**
 * Load texture to be used in scene
 * @param filenamePath string path of texture to be loaded
 * @param textureLoader instance
 * @returns loaded texture
 */
const loadTexture = (filenamePath, textureLoader) => {
    return textureLoader.load(filenamePath);
};

// Loads a plant model and scales it
const addModelToScene = (model, scene, position, scaleFactor = 1) => {
    model.position.copy(position);
    model.scale.set(scaleFactor, scaleFactor, scaleFactor);

    scene.add(model);
};

export {loadModel, loadTexture, addModelToScene};