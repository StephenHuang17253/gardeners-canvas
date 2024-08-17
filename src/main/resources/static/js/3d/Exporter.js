import * as THREE from 'three';
import { GLTFExporter } from 'three/addons/exporters/GLTFExporter.js';
import { OBJExporter } from 'three/addons/exporters/OBJExporter.js';

// https://web.dev/patterns/files/save-a-file

const MIME_TYPES = { APP_STREAM: 'application/octet-stream', TEXT_PLAIN: 'text/plain', IMAGE_JPEG: 'image/jpeg' };

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

        const saveWithLink = (blob, filename) => {
            document.body.appendChild(this.link);
            this.link.href = URL.createObjectURL(blob);
            this.link.download = filename;
            this.link.click();
            setTimeout(() => {
                URL.revokeObjectURL(blobURL);
                document.body.removeChild(this.link);
            }, 1000);
        };

        const saveWithFilePicker = async (blob, filename) => {
            const handle = await showSaveFilePicker({
                suggestedName: filename,
                startIn: 'downloads',
            });
            const writable = await handle.createWritable();
            await writable.write(blob);
            await writable.close();
        };

        const saveFile = async (blob, filename) => {

            // Checks that the browser supports the File System Access API.
            const supportsFileSystemAccess = 'showSaveFilePicker' in window && window.self === window.top;

            if (supportsFileSystemAccess) {
                try {
                    await saveWithFilePicker(blob, filename);
                    return;
                } catch (err) {
                    // Fail silently if the user has simply canceled the dialog.
                    if (err.name !== 'AbortError') {
                        console.error(err.name, err.message);
                        return;
                    }
                }
            }

            // Fallback if the File System Access API is not supported…
            saveWithLink(blob, filename);
        };

        /**
         * Download the scene as a GLTF file or a GLB file if the scene is too large
         * @param {THREE.Scene} scene - scene to download as a GLTF, or GLB file
         */
        this.downloadGLTF = (scene) => {
            this.gltfExporter.parse(
                scene,
                result => {
                    let blob, extension;
                    if (result instanceof ArrayBuffer) {
                        blob = new Blob([result], { type: MIME_TYPES.APP_STREAM });
                        extension = 'glb';
                    } else {
                        extension = 'gltf';
                        const output = JSON.stringify(result, null, 1);
                        blob = new Blob([output], { type: MIME_TYPES.TEXT_PLAIN });
                    }
                    saveFile(blob, `${this.gardenName}.${extension}`);
                }
            );
        };

        /**
         * Download the scene as an OBJ file
         * @param {THREE.Scene} scene - scene to download as an OBJ file
         */
        this.downloadOBJ = (scene) => {
            const objData = this.objExporter.parse(scene);
            const blob = new Blob([objData], { type: MIME_TYPES.TEXT_PLAIN });
            saveFile(blob, `${this.gardenName}.obj`);
        };

        /**
         * Download the scene as a JPEG file
         * @param {THREE.WebGLRenderer} renderer - renderer to download the scene from 
         */
        this.downloadJPEG = (renderer) => {
            renderer.domElement.toBlob(blob => {
                saveFile(blob, `${this.gardenName}.jpg`);
            }, MIME_TYPES.IMAGE_JPEG, 1);
        };

    }
}

export { Exporter };