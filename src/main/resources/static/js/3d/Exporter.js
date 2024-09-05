import * as THREE from 'three';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/addons/exporters/OBJExporter.js';

// Code for opening the file picker to save a file is based on the following article:
// https://web.dev/patterns/files/save-a-file

const MIME_TYPES = {
    APP_STREAM: 'application/octet-stream',
    TEXT_PLAIN: 'text/plain',
    IMAGE_JPG: 'image/jpeg',
};

const FILE_EXTENSIONS = {
    OBJ: 'obj',
    JPG: 'jpg',
    GLTF: 'gtlf',
    GLB: 'glb',
};
/**
 * Exporter class to handle exporting scenes as GLTF, GLB, OBJ, and JPG files
 */
class Exporter {

    /**
     * Create a new Exporter object
     * 
     * @param {String} gardenName - name of the garden to use for the filename
     * @param {Downloader} downloader - downloader object to use for saving files
     */
    constructor(gardenName, downloader) {
        this.gltfExporter = new GLTFExporter();
        this.objExporter = new OBJExporter();
        this.gardenName = gardenName;
        this.downloader = downloader;
    }

    /**
     * Download the scene as a GLTF file or a GLB file if the scene is too large
     * 
     * @param {THREE.Scene} scene - scene to download as a GLTF, or GLB file
     */
    downloadGLTF = (scene) => {
        this.gltfExporter.parse(
            scene,
            result => {
                let blob, extension;
                if (result instanceof ArrayBuffer) {
                    blob = new Blob([result], { type: MIME_TYPES.APP_STREAM });
                    extension = FILE_EXTENSIONS.GLB;
                } else {
                    const output = JSON.stringify(result, null, 1);
                    blob = new Blob([output], { type: MIME_TYPES.TEXT_PLAIN });
                    extension = FILE_EXTENSIONS.GLTF;
                }
                this.downloader.saveFile(blob, `${this.gardenName}.${extension}`);
            }
        );
    };

    /**
     * Download the scene as an OBJ file
     * 
     * @param {THREE.Scene} scene - scene to download as an OBJ file
     */
    downloadOBJ = (scene) => {
        const objData = this.objExporter.parse(scene);
        const blob = new Blob([objData], { type: MIME_TYPES.TEXT_PLAIN });
        this.downloader.saveFile(blob, `${this.gardenName}.${FILE_EXTENSIONS.OBJ}`);
    };

    /**
     * Download the scene as a JPG file
     * 
     * @param {THREE.WebGLRenderer} renderer - renderer to download the scene from 
     */
    downloadJPG = (renderer) => {
        renderer.domElement.toBlob(blob => {
            this.downloader.saveFile(blob, `${this.gardenName}.${FILE_EXTENSIONS.JPG}`);
        }, MIME_TYPES.IMAGE_JPG, 1);
    };

}

export { Exporter };