import * as THREE from 'three';

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


const applyOutline = (object, scene) => {
    const baseOutlineThickness = 0.05; // Base thickness of the outline
    const outlineColor = new THREE.Color(0xffa500); // Orange color
    const outlineAlpha = 0.25; // Set transparency (0.0 is fully transparent, 1.0 is fully opaque)

    // Use a fixed thickness or adjust it based on the object's scale
    const scale = object.scale;
    const maxScale = Math.max(scale.x, scale.y, scale.z);
    const adjustedThickness = baseOutlineThickness / maxScale;

    // Create the outline material
    const outlineMaterial = new THREE.ShaderMaterial({
        uniforms: {
            outlineThickness: { value: adjustedThickness },
            outlineColor: { value: outlineColor },
            outlineAlpha: { value: outlineAlpha } //
        },
        vertexShader: outlineShader.vertexShader,
        fragmentShader: outlineShader.fragmentShader,
        // side: THREE.BackSide, // Render the outline behind the original object
        depthTest: false, // Disable depth testing to ensure the outline is always visible
        depthWrite: false, // Prevent writing to the depth buffer
        transparent: true
    });

    object.traverse((child) => {
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

            // Optionally, remove the outline after a delay
            setTimeout(() => {
                scene.remove(outlineMesh);
            }, 1000); // Outline duration in milliseconds
        }
    });
}


export { applyOutline };