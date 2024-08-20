import * as THREE from 'three';

// Fragment shader code as a string
const fragmentShader = `
    uniform float hue;
    uniform float saturation;
    uniform sampler2D map;
    varying vec2 vUv;
    uniform vec3 uBaseColor;
    uniform float uBrightness;

    vec3 rgb2hsv(vec3 c) {
        vec4 K = vec4(0.0, -1.0 / 3.0, 2.0 / 3.0, -1.0);
        vec4 p = mix(vec4(c.bg, K.wz), vec4(c.gb, K.xy), step(c.b, c.g));
        vec4 q = mix(vec4(p.xyw, c.r), vec4(c.r, p.yzx), step(p.x, c.r));

        float d = q.x - min(q.w, q.y);
        float e = 1.0e-10;
        return vec3(abs(q.z + (q.w - q.y) / (6.0 * d + e)), d / (q.x + e), q.x);
    }

    vec3 hsv2rgb(vec3 c) {
        vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
        vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
        return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
    }

    vec3 applyHueSaturation(vec3 color, float hueShift, float sat) {
        vec3 hsv = rgb2hsv(color);
        hsv.x += hueShift;
        hsv.y *= sat;
        return hsv2rgb(hsv);
    }

    void main() {
        vec3 color = texture2D(map, vUv).rgb;
        gl_FragColor = vec4(applyHueSaturation(color, hue, saturation)* uBaseColor* uBrightness, 1.0);
    }
`;

const vertexShader = `
    varying vec2 vUv;

    void main() {
        vUv = uv;
        gl_Position = projectionMatrix * modelViewMatrix * vec4(position, 1.0);
    }
`;

const createHueSaturationMaterial = (mapTexture, hueValue, saturationValue, brightness) => {
    return new THREE.ShaderMaterial({
        uniforms: {
            map: { value: mapTexture },
            hue: { value: hueValue },
            saturation: { value: saturationValue },
            uBaseColor: { value: new THREE.Color(0xffffff) },
            uBrightness: { value: brightness }
        },
        vertexShader: vertexShader,
        fragmentShader: fragmentShader
    });
}

export { createHueSaturationMaterial };
