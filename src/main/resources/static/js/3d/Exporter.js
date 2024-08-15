import * as THREE from 'three';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/addons/exporters/OBJExporter.js';

const MIME_TYPES = { APP_STREAM: 'application/octet-stream', TEXT_PLAIN: 'text/plain', IMAGE_JPEG: 'image/jpeg', IMAGE_STREAM: 'image/octet-stream' };

class Exporter {

    /**
     * Create a new Exporter object
     * @param {Element} link - link element to use for downloading files
     * @param {String} gardenName - name of the garden to use for the filename
     */
    constructor(link, gardenName) {
        this.gltfExporter = new GLTFExporter();
        this.objExporter = new OBJExporter();
        this.link = link;
        this.gardenName = gardenName;

        /**
         * 
         * @param {String} data - data to create a data url for
         * @param {String} type - mime type of the data
         * @returns {String} data url for the created data
         */
        this.createDataUrl = (data, type) => URL.createObjectURL(new Blob([data], { type: type }));

        /**
         * Saves the data to a file with the given filename
         * @param {String} dataURL - url of data to save
         * @param {String} filename - name of the file to save
         */
        this.downloadFile = (dataURL, filename) => {
            document.body.appendChild(this.link);
            this.link.href = dataURL;
            this.link.download = filename;
            this.link.click();
            document.body.removeChild(this.link);
        };

        /**
         * Download the scene as a GLTF file or a GLB file if the scene is too large
         * @param {THREE.Scene} scene - scene to download as a GLTF, or GLB file
         */
        this.downloadGLTF = (scene) => {
            this.gltfExporter.parse(
                scene,
                result => {
                    if (result instanceof ArrayBuffer) {
                        this.downloadFile(this.createDataUrl(result, MIME_TYPES.APP_STREAM), `${this.gardenName}.glb`);
                    } else {
                        const output = JSON.stringify(result, null, 1);
                        this.downloadFile(this.createDataUrl(output, MIME_TYPES.TEXT_PLAIN), `${this.gardenName}.gltf`);
                    }
                },
                undefined // error callback
            );
        };

        /**
         * Download the scene as an OBJ file
         * @param {THREE.Scene} scene - scene to download as an OBJ file
         */
        this.downloadOBJ = (scene) => {
            const objData = objExporter.parse(scene);
            this.downloadFile(this.createDataUrl(objData, MIME_TYPES.TEXT_PLAIN), `${this.gardenName}.obj`);
        }

        /**
         * Download the scene as a JPEG file
         * @param {THREE.WebGLRenderer} renderer - renderer to download the scene from 
         */
        this.downloadJPEG = (renderer) => {
            const imgData = renderer.domElement.toDataURL(MIME_TYPES.IMAGE_JPEG);
            this.downloadFile(imgData.replace(MIME_TYPES.IMAGE_JPEG, MIME_TYPES.IMAGE_STREAM), `${this.gardenName}.jpg`);
        };

    }
}

export { Exporter };