package com.example.game.render3d

import android.opengl.GLES30

class LitShaderProgram {
    val programId: Int
    val uMvpLoc: Int
    val uModelLoc: Int
    val uSunDirLoc: Int
    val uSunColorLoc: Int
    val uAmbientTopLoc: Int
    val uAmbientBottomLoc: Int
    val uCameraPosLoc: Int
    val uFogColorLoc: Int
    val uFogDensityLoc: Int
    val uTintColorLoc: Int

    init {
        val vs = compileShader(GLES30.GL_VERTEX_SHADER, VERTEX_SHADER)
        val fs = compileShader(GLES30.GL_FRAGMENT_SHADER, FRAGMENT_SHADER)
        programId = GLES30.glCreateProgram()
        GLES30.glAttachShader(programId, vs)
        GLES30.glAttachShader(programId, fs)
        GLES30.glLinkProgram(programId)
        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)

        uMvpLoc = GLES30.glGetUniformLocation(programId, "uMVP")
        uModelLoc = GLES30.glGetUniformLocation(programId, "uModel")
        uSunDirLoc = GLES30.glGetUniformLocation(programId, "uSunDir")
        uSunColorLoc = GLES30.glGetUniformLocation(programId, "uSunColor")
        uAmbientTopLoc = GLES30.glGetUniformLocation(programId, "uAmbientTop")
        uAmbientBottomLoc = GLES30.glGetUniformLocation(programId, "uAmbientBottom")
        uCameraPosLoc = GLES30.glGetUniformLocation(programId, "uCameraPos")
        uFogColorLoc = GLES30.glGetUniformLocation(programId, "uFogColor")
        uFogDensityLoc = GLES30.glGetUniformLocation(programId, "uFogDensity")
        uTintColorLoc = GLES30.glGetUniformLocation(programId, "uTintColor")
    }

    fun use() {
        GLES30.glUseProgram(programId)
    }

    fun release() {
        GLES30.glDeleteProgram(programId)
    }

    companion object {
        private const val VERTEX_SHADER = """#version 300 es
            layout(location = 0) in vec3 aPosition;
            layout(location = 1) in vec3 aNormal;
            layout(location = 2) in vec4 aColor;
            layout(location = 3) in float aSpecular;

            uniform mat4 uMVP;
            uniform mat4 uModel;

            out vec3 vWorldPos;
            out vec3 vNormal;
            out vec4 vColor;
            out float vSpec;

            void main() {
                vec4 wp = uModel * vec4(aPosition, 1.0);
                vWorldPos = wp.xyz;
                vNormal = normalize(mat3(uModel) * aNormal);
                vColor = aColor;
                vSpec = aSpecular;
                gl_Position = uMVP * vec4(aPosition, 1.0);
            }
        """

        private const val FRAGMENT_SHADER = """#version 300 es
            precision highp float;

            in vec3 vWorldPos;
            in vec3 vNormal;
            in vec4 vColor;
            in float vSpec;

            uniform vec3 uSunDir;
            uniform vec3 uSunColor;
            uniform vec3 uAmbientTop;
            uniform vec3 uAmbientBottom;
            uniform vec3 uCameraPos;
            uniform vec3 uFogColor;
            uniform float uFogDensity;
            uniform vec4 uTintColor;

            out vec4 fragColor;

            void main() {
                vec3 N = normalize(vNormal);
                vec3 L = normalize(uSunDir);
                vec3 V = normalize(uCameraPos - vWorldPos);

                // Hemisphere ambient lighting for realistic sky/ground bounce
                float hemi = N.y * 0.5 + 0.5;
                vec3 ambient = mix(uAmbientBottom, uAmbientTop, hemi);

                // Wrapped diffuse sun + subtle rim light
                float ndl = max(dot(N, L), 0.0);
                float rim = pow(1.0 - max(dot(N, V), 0.0), 3.0) * 0.22;
                vec3 diffuse = uSunColor * ndl;

                // Blinn-Phong metallic/wet specular
                vec3 H = normalize(L + V);
                float specPow = mix(12.0, 56.0, vSpec);
                float spec = pow(max(dot(N, H), 0.0), specPow) * vSpec * 0.85;

                vec3 baseCol = vColor.rgb * uTintColor.rgb;
                vec3 lit = baseCol * (ambient + diffuse) + uSunColor * (spec + rim * 0.4);

                // Exponential atmospheric battlefield fog
                float dist = length(uCameraPos - vWorldPos);
                float fogFactor = 1.0 - exp(-pow(dist * uFogDensity, 1.45));
                fogFactor = clamp(fogFactor, 0.0, 1.0);

                vec3 finalRgb = mix(lit, uFogColor, fogFactor);
                fragColor = vec4(finalRgb, vColor.a * uTintColor.a);
            }
        """

        fun compileShader(type: Int, src: String): Int {
            val id = GLES30.glCreateShader(type)
            GLES30.glShaderSource(id, src)
            GLES30.glCompileShader(id)
            return id
        }
    }
}
