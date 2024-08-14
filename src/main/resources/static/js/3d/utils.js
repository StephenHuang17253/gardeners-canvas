import { GLTFLoader } from 'three/addons/loaders/GLTFLoader.js';
import * as THREE from 'three';
// Loads a model from a url and gives it a name
const loadModel = async (path, name) => {
    const loader = new GLTFLoader(); // Assuming GLTF format
    const model = await loader.loadAsync(path);
    model.scene.traverse((child) => {
        child.name = name;
    });
    return model.scene;
}


// Load the grass texture
const loadTexture = (filenamePath) => {
    const loader = new THREE.TextureLoader();
    return loader.load(filenamePath);
};

// Loads a plant model and scales it
const loadAsset = async (filename, scene, position, scaleFactor = 1) => {
    const plantModel = await loadModel(`../models/${filename}`, filename);
    plantModel.position.copy(position); // Set the model's position

    // Scale the model
    plantModel.scale.set(scaleFactor, scaleFactor, scaleFactor);

    scene.add(plantModel); // Add the model to the scene
}

export {loadModel, loadTexture, loadAsset};