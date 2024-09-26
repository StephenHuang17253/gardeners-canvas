import * as THREE from 'three';

const tileMap = {
    "Grass": ["grass-tileable.jpg", null],   // Grass-Short
    "StonePath": ["stone-tileable.jpg", "stone-normal.jpg"],
    "PebblePath": ["pebbles-tileable.jpg", null],
    "Concrete": ["concrete-tileable.jpg", "concrete-normal.jpg"],
    "Soil": ["soil-tileable.jpg", null],
    "Bark": ["bark-tileable.jpg", null]
};

/**
 * Creates a tile material with the given parameters.
 *
 * @param {THREE.Texture} texture - The texture to be applied to the material.
 * @param {number} hueShift - The amount to adjust the hue.
 * @param {number} saturation - The amount to adjust the saturation.
 * @param {THREE.Vector2} uvScale - default 1,1 sets scale of textures
 * @param {THREE.Texture} normalTexture - The bump texture
 * @returns {THREE.ShaderMaterial} The created tile material.
 */
const createTileMaterial = (texture, hueShift, saturation, uvScale, normalTexture) => {
    texture.repeat.set(uvScale.x, uvScale.y);
    texture.wrapS = THREE.RepeatWrapping;
    texture.wrapT = THREE.RepeatWrapping;

    if (normalTexture) {
        normalTexture.repeat.set(uvScale.x, uvScale.y);
        normalTexture.wrapS = THREE.RepeatWrapping;
        normalTexture.wrapT = THREE.RepeatWrapping;
    }

    const material = new THREE.MeshStandardMaterial({
        map: texture,
        color: new THREE.Color(0xffffff),
    });

    material.color.offsetHSL(hueShift, saturation, 0);
    if (normalTexture) {
        material.normalMap = normalTexture;
        material.normalScale = new THREE.Vector2(10, 10);
        material.roughness = 0.8;
    }

    return material;
};

/**
 * Creates a tile mesh with the given texture, size, hue shift, and saturation.
 *
 * @param {string} tileMaterial - The tile material string
 * @param {number} size - The size of the tile.
 * @param {number} hueShift - The hue shift value.
 * @param {number} saturation - The saturation value.
 * @param {THREE.Texture} texture - diffuse texture
 * @param {THREE.Texture} normalTexture - normal texture
 * @returns {THREE.Mesh} The created tile mesh.
 */
const createTile = async (tileMaterial, size, hueShift, saturation, texture, normalTexture) => {

    const geometry = new THREE.PlaneGeometry(size, size);
    let uvScale;
    if (tileMaterial === 'Bark') {
        uvScale = new THREE.Vector2(0.25, 0.25);
    } else {
        uvScale = new THREE.Vector2(1, 1);
    }
    const material = createTileMaterial(texture, hueShift, saturation, uvScale, normalTexture);
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
 * @param {string} tileMaterial - The material to be used for the tile
 * @param {number} hueShift - The hue shift value for the tiles.
 * @param {number} saturation - The saturation value for the tiles.
 * @param loader - reference to loader instance
 * @returns {THREE.Group} - Object with the grid to add to scene and an array of tile center positions.
 */
const createTileGrid = async (rows, cols, tileSize, tileMaterial, hueShift, saturation, loader) => {
    const grid = new THREE.Group();
    const offset = (rows - 1) * tileSize / 2;
    const tileCenterpositions = [];
    const texture = loader.loadTexture(tileMap[tileMaterial][0]);

    let normalTexture = null;
    if (tileMap[tileMaterial][1] != null) {
        normalTexture = loader.loadTexture(tileMap[tileMaterial][2]);
    }
    for (let i = 0; i < rows; i++) {
        for (let j = 0; j < cols; j++) {
            const tile = createTile(tileMaterial, tileSize, hueShift, saturation, loader, texture, normalTexture);
            tile.position.set(i * tileSize - offset, 0, j * tileSize - offset);
            grid.add(tile);
            tileCenterpositions.push(new THREE.Vector3(i * tileSize - offset, 0, j * tileSize - offset));
        }
    }
    return grid;
};

export { createTileGrid };