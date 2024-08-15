import * as THREE from 'three';

const OUTLINE_THICKNESS = 0.05;
const OUTLINE_COLOR = new THREE.Color(0xffa500);
const OUTLINE_ALPHA = 0.25; // Set transparency (0.0 is fully transparent, 1.0 is fully opaque)
const OUTLINE_DURATION = 1000; // Duration of the outline effect in milliseconds

/**
 * Shader code for the outline effect.
 */
const outlineShader = {
    vertexShader: `
        uniform float outlineThickness;
        void main() {
            // Adjust position by adding the scaled normal
            vec4 pos = modelViewMatrix * vec4(position + normal * outlineThickness, 1.0);
            gl_Position = projectionMatrix * pos;
        }
    `,
    fragmentShader: `
        uniform vec3 outlineColor;
        uniform float outlineAlpha; // New uniform for alpha transparency
        
        void main() {
            gl_FragColor = vec4(outlineColor, outlineAlpha); // Use alpha for transparency
        }
`
};

/**
 * Applies an outline effect to the given object in the scene.
 * 
 * @param {THREE.Object3D} object - The object to apply the outline effect to.
 * @param {THREE.Scene} scene - The scene where the object is located.
 */
const applyOutline = (object, scene) => {
    const scale = object.scale;
    const maxScale = Math.max(scale.x, scale.y, scale.z);
    const adjustedThickness = OUTLINE_THICKNESS / maxScale;

    const outlineMaterial = new THREE.ShaderMaterial({
        uniforms: {
            outlineThickness: { value: adjustedThickness },
            outlineColor: { value: OUTLINE_COLOR },
            outlineAlpha: { value: OUTLINE_ALPHA }
        },
        vertexShader: outlineShader.vertexShader,
        fragmentShader: outlineShader.fragmentShader,
        // side: THREE.BackSide, // Render the outline behind the original object
        depthTest: false,
        depthWrite: false,
        transparent: true
    });

    object.traverse(child => {
        if (child.isMesh) {
            const outlineMesh = new THREE.Mesh(child.geometry.clone(), outlineMaterial);
            const worldPosition = new THREE.Vector3();
            const worldScale = new THREE.Vector3();
            child.getWorldPosition(worldPosition);
            child.getWorldScale(worldScale);
            outlineMesh.position.copy(worldPosition);
            outlineMesh.scale.copy(worldScale);
            outlineMesh.rotation.copy(child.rotation);
            scene.add(outlineMesh);
            setTimeout(() => scene.remove(outlineMesh), OUTLINE_DURATION);
        }
    });
};


export { applyOutline };