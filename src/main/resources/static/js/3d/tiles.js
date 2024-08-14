import { loadTexture, addModelToScene } from './utils.js';
import * as THREE from 'three';
const vertexShader = `
    varying vec2 vUv;
    void main() {
        vUv = uv;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    }
`;

// Fragment shader
const fragmentShader = `
    uniform sampler2D uTexture;
    uniform float uHue;
    uniform float uSaturation;
    uniform vec3 uBaseColor;

    varying vec2 vUv;

    vec3 rgb2hsv(vec3 c) {
        vec4 K = vec4(0.0, -1.0/3.0, 2.0/3.0, -1.0);
        vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
        vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));
        float d = q.x - min(q.w, q.y);
        float e = 1.0e-10;
        return vec3(abs((q.z + (q.w - q.y) / (6.0 * d + e))), d / (q.x + e), q.x);
    }

    vec3 hsv2rgb(vec3 c) {
        vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
        vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
        return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
    }

    void main() {
        vec4 color = texture2D(uTexture, vUv);
        vec3 hsv = rgb2hsv(color.rgb);

        // Adjust the hue and saturation
        hsv.x += uHue;  // Adjust hue
        hsv.y *= uSaturation; // Adjust saturation

        // Wrap hue within 0.0 - 1.0
        if (hsv.x > 1.0) hsv.x -= 1.0;
        if (hsv.x < 0.0) hsv.x += 1.0;

        // Apply the base color
        vec3 rgb = hsv2rgb(hsv) * uBaseColor;
        
        gl_FragColor = vec4(rgb, color.a);
    }
`;


// Create a tile material with custom shader
const createTileMaterial = (texture, hueShift, saturation) => {
    return new THREE.ShaderMaterial({
        uniforms: {
            uTexture: { value: texture },
            uHue: { value: hueShift },  // Adjust hue
            uSaturation: { value: saturation },  // Adjust saturation
            uBaseColor: { value: new THREE.Color(0xffffff) }// Default to white, no color tint
        },
        vertexShader: vertexShader,
        fragmentShader: fragmentShader
    });
};

// Create the tile mesh
const createTile = (texture, size, hueShift, saturation) => {
    const geometry = new THREE.PlaneGeometry(size, size);
    const material = createTileMaterial(texture, hueShift, saturation);
    const tile = new THREE.Mesh(geometry, material);
    tile.rotation.x = -Math.PI / 2; // Rotate to horizontal
    return tile;
};

// Create the grid of tiles and return their center positions
const createTileGrid = (scene, rows, cols, tileSize, texture, hueShift, saturation) => {
    const grid = new THREE.Group();
    const offset = (rows - 1) * tileSize / 2; // Center the grid

    // Array to store the positions of the tile centers
    const positions = [];

    for (let i = 0; i < rows; i++) {
        for (let j = 0; j < cols; j++) {
            const tile = createTile(texture, tileSize, hueShift, saturation);
            tile.position.set(i * tileSize - offset, 0, j * tileSize - offset);
            grid.add(tile);

            // Store the center position of the tile
            positions.push(new THREE.Vector3(i * tileSize - offset, 0, j * tileSize - offset));
        }
    }

    // Add the grid of tiles to the scene
    scene.add(grid);

    // Return the array of positions
    return positions;
};
// Load plants at the saved positions
const loadPlantsAtPositions = async (scene, positions, plantModel, plantScale = 1) => {
    const plantPromises = [];

    for (const position of positions) {
        plantPromises.push(addModelToScene(plantModel, scene, position, plantScale).then(plantModel => {
            plantModel.position.y += tileSize / 2;
            scene.add(plantModel);
        }));
    }

    // Wait for all plants to be loaded
    await Promise.all(plantPromises);
};

export {createTileGrid, loadPlantsAtPositions};