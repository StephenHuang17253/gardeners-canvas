/**
 * Load gltf 3d model, gives all parts the same name
 * 
 * @param {string} filename - The filename of the 3D model to be loaded.
 * @param {string} name - The name to be assigned to all parts of the loaded model.
 * @param {gltfLoader} gltfLoader - The gltfLoader instance used to load the model.
 * @returns {Promise<Object>} - A promise that resolves to the loaded model scene.
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
 * 
 * @param {string} filenamePath - The path of the texture to be loaded.
 * @param {textureLoader} textureLoader - The textureLoader instance used to load the texture.
 * @returns {Object} - The loaded texture.
 */
const loadTexture = (filenamePath, textureLoader) => textureLoader.load(filenamePath);

/** Add model to scene
* 
* @param {Object} model - The model to be added to the scene.
* @param {Object} scene - The scene to which the model will be added.
* @param {Object} position - The position at which the model will be placed in the scene.
* @param {number} [scaleFactor=1] - The scale factor to be applied to the model. Default value is 1.
*/
const addModelToScene = (model, scene, position, scaleFactor = 1) => {
    model.position.copy(position);
    model.scale.set(scaleFactor, scaleFactor, scaleFactor);
    scene.add(model);
};

export { loadModel, loadTexture, addModelToScene };
