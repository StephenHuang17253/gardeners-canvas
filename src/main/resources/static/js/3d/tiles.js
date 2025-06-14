import * as THREE from 'three';

const tileMap = {
    "GRASS": ["grass-tileable.jpg", 0.2, 1.56],
    "STONE_PATH": ["stone-tileable.jpg", 0, 1],
    "PEBBLE_PATH": ["pebbles-tileable.jpg", 0, 1],
    "CONCRETE": ["concrete-tileable.jpg", 0, 1],
    "SOIL": ["soil-tileable.jpg", 0.055, 1.06],
    "BARK": ["bark-tileable.jpg", 0, 1]
};
/**
 * Custom vertex shader for the tile material
 */
const vertexShader = `
    varying vec2 vUv;
    uniform vec2 uUvScale;
    void main() {
        vUv = uv * uUvScale;
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
 * @param uvScale
 * @returns {THREE.ShaderMaterial} The created tile material.
 */
const createTileMaterial = (texture, hueShift, saturation, uvScale) => new THREE.ShaderMaterial({
    uniforms: {
        uTexture: { value: texture },
        uHue: { value: hueShift },
        uSaturation: { value: saturation },
        uBaseColor: { value: new THREE.Color(0xffffff) },
        uUvScale: { value: uvScale },
    },
    vertexShader: vertexShader,
    fragmentShader: fragmentShader
});

/**
 * Creates a tile mesh with the given texture, size, hue shift, and saturation.
 *
 * @param {string} tileMaterial - The tile material string
 * @param {number} size - The size of the tile.
 * @param {THREE.Texture} texture - diffuse texture
 * @returns {THREE.Mesh} The created tile mesh.
 */
const createTile = (tileMaterial, size, texture) => {

    const hueShift = tileMap[tileMaterial][1];
    const saturation = tileMap[tileMaterial][2];
    const geometry = new THREE.PlaneGeometry(size, size);
    let uvScale;
    if (tileMaterial === "BARK") {
        uvScale = new THREE.Vector2(0.25, 0.25);
    } else if (tileMaterial === "PEBBLE_PATH") {
        uvScale = new THREE.Vector2(0.85, 0.85);
    } else {
        uvScale = new THREE.Vector2(1, 1);
    }
    const material = createTileMaterial(texture, hueShift, saturation, uvScale);
    const tile = new THREE.Mesh(geometry, material);
    tile.rotation.x = -Math.PI / 2; // Rotate to horizontal
    return tile;
};

/**
 * Creates a tile grid.
 *
 * @param {number} rows - The number of rows in the grid.
 * @param {number} cols - The number of columns in the grid.
 * @param {number} tileSize - The size of each tile in the grid.
 * @param {Array.<Object>} gardenTileTextureList - The list of tiles from the garden.
 * @param {Loader} loader - reference to loader instance
 * @returns {THREE.Group} - Object with the grid to add to scene and an array of tile center positions.
 */
const createTileGrid = async (rows, cols, tileSize, gardenTileTextureList, loader) => {
    const grid = new THREE.Group();
    const offset = (rows - 1) * tileSize / 2;
    gardenTileTextureList.forEach(gardenTileTexture => {
        const texture = loader.loadTexture(tileMap[gardenTileTexture.tileTexture][0]);
        const tile = createTile(gardenTileTexture.tileTexture, tileSize, texture);
        tile.position.set(gardenTileTexture.xcoordinate * tileSize - offset, 0, gardenTileTexture.ycoordinate * tileSize - offset);
        grid.add(tile);
    })
    return grid;
}

export { createTileGrid };