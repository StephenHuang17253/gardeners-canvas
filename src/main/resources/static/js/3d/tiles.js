import * as THREE from 'three';
import { Loader } from './Loader.js';

const tileMap = {
    "Default": ["grass-tileable.jpg", "texture"],   // Grass-Short
    "Grass-Medium": ["tiles/grass_medium.glb", "model"],
    "Grass-Long": ["tiles/grass_long.glb", "model"],
    "StonePath": ["grass-tileable.jpg", "texture"],
    "PebblePath": ["grass-tileable.jpg", "texture"],
    "Concrete": ["grass-tileable.jpg", "texture"],
    "Soil": ["grass-tileable.jpg", "texture"],
    "Bark": ["grass-tileable.jpg", "texture"]
};

/**
 * Custom vertex shader for the tile material
 */
const vertexShader = `
    varying vec2 vUv;
    void main() {
        vUv = uv;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    }
`;

/**
 * Custom fragment shader for the tile material
 */
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

/**
 * Creates a tile material with the given parameters.
 *
 * @param {THREE.Texture} texture - The texture to be applied to the material.
 * @param {number} hueShift - The amount to adjust the hue.
 * @param {number} saturation - The amount to adjust the saturation.
 * @returns {THREE.ShaderMaterial} The created tile material.
 */
const createTileMaterial = (texture, hueShift, saturation) => new THREE.ShaderMaterial({
    uniforms: {
        uTexture: { value: texture },
        uHue: { value: hueShift },
        uSaturation: { value: saturation },
        uBaseColor: { value: new THREE.Color(0xffffff) }
    },
    vertexShader: vertexShader,
    fragmentShader: fragmentShader
});

/**
 * Creates a tile mesh with the given texture, size, hue shift, and saturation.
 *
 * @param {string} texture - The texture of the tile.
 * @param {number} size - The size of the tile.
 * @param {number} hueShift - The hue shift value.
 * @param {number} saturation - The saturation value.
 * @param loader
 * @returns {THREE.Mesh} The created tile mesh.
 */
const createTile = async (texture, size, hueShift, saturation, loader) => {
    const tileType = tileMap[texture][1];
    let tile;
    if (tileType === 'texture') {
        const geometry = new THREE.PlaneGeometry(size, size);
        const text = loader.loadTexture(tileMap[texture][0]);
        const material = createTileMaterial(text, hueShift, saturation);
        tile = new THREE.Mesh(geometry, material);
        tile.rotation.x = -Math.PI / 2; // Rotate to horizontal
    } else {
        tile = await loader.loadModel(tileMap[texture][0], texture);
        tile.scale.set(10, 10, 10);
    }
    return tile;
};

/**
 * Creates a tile grid.
 * 
 * @param {number} rows - The number of rows in the grid.
 * @param {number} cols - The number of columns in the grid.
 * @param {number} tileSize - The size of each tile in the grid.
 * @param {THREE.Texture} texture - The texture to be applied to each tile.
 * @param {number} hueShift - The hue shift value for the tiles.
 * @param {number} saturation - The saturation value for the tiles.
 * @returns {{THREE.Group, Array<THREE.Vector3>}} - Object with the grid to add to scene and an array of tile center positions.
 */
const createTileGrid = async (rows, cols, tileSize, texture, hueShift, saturation, loader) => {
    const grid = new THREE.Group();
    const offset = (rows - 1) * tileSize / 2;
    const tileCenterpositions = [];
    for (let i = 0; i < rows; i++) {
        for (let j = 0; j < cols; j++) {
            const tile = await createTile(texture, tileSize, hueShift, saturation, loader);
            tile.position.set(i * tileSize - offset, 0, j * tileSize - offset);
            grid.add(tile);
            tileCenterpositions.push(new THREE.Vector3(i * tileSize - offset, 0, j * tileSize - offset));
        }
    }
    return {grid, tileCenterpositions};
};

export { createTileGrid };