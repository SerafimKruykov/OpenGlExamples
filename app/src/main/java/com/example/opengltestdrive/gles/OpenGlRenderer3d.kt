package com.example.opengltestdrive.gles

import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

// не работает))

class OpenGlRenderer3d : Renderer {

    companion object Shaders {
        val verticesShaderCode =
            """
            attribute vec4 a_Position;
            uniform mat4 u_Matrix;
            
            void main() {
                gl_Position = u_Matrix * a_Position;
                gl_PointSize = 5.0;
            }
            """.trimIndent()

        val fragmentShaderCode =
            """
            precision mediump float;
            uniform vec4 u_Color;

            void main() {
                gl_FragColor = u_Color;
            }
            """.trimIndent()
    }

    private val POSITION_COUNT = 4

    private var programId = 0

    private lateinit var vertexData: FloatBuffer
    private var uColorLocation = 0
    private var aPositionLocation = 0
    private var uMatrixLocation = 0

    private val mProjectionMatrix = FloatArray(16)

    init {
        prepareData()
    }

    override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        val vertexShaderId =
            ShaderUtils.createShader(GLES20.GL_VERTEX_SHADER, verticesShaderCode)
        val fragmentShaderId =
            ShaderUtils.createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        GLES20.glUseProgram(programId)
        bindData()
    }

    override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        bindMatrix()
    }

    private fun prepareData() {
        val x1 = -0.5f
        val y1 = -0.8f
        val x2 = 0.5f
        val y2 = -0.8f

        val vertices = floatArrayOf(
            x1, y1, -1.0f,
            x1, y1, -1.5f,
            x1, y1, -2.0f,
            x1, y1, -2.5f,
            x1, y1, -3.0f,
            x1, y1, -3.5f,

            x2, y2, -1.0f,
            x2, y2, -1.5f,
            x2, y2, -2.0f,
            x2, y2, -2.5f,
            x2, y2, -3.0f,
            x2, y2, -3.5f,
        )

        vertexData = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData.put(vertices)
    }

    private fun bindData() {
        // координаты
        aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        vertexData.position(0)
        GLES20.glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GLES20.GL_FLOAT,
            false, 0, vertexData
        )
        GLES20.glEnableVertexAttribArray(aPositionLocation)

        // цвет
        uColorLocation = GLES20.glGetUniformLocation(programId, "u_Color")

        // матрица
        uMatrixLocation = GLES20.glGetUniformLocation(programId, "u_Matrix")
    }

    private fun bindMatrix() {
        val left = -1.0f
        val right = 1.0f
        val bottom = -1.0f
        val top = 1.0f
        val near = 1.0f
        val far = 8.0f

        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mProjectionMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10?) {
        GLES20.glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        // зеленый треугольник
        GLES20.glUniform4f(uColorLocation, 0.0f, 1.0f, -1.0f, 1.0f);
        GLES20.glDrawArrays(GL_TRIANGLES, 0, 3);


        // синий треугольник
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, -1.0f, 1.0f);
        GLES20.glDrawArrays(GL_TRIANGLES, 3, 3);
    }
}
