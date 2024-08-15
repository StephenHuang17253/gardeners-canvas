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

export { addModelToScene };
