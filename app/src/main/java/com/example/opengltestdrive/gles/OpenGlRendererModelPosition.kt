package com.example.opengltestdrive.gles

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import android.os.SystemClock
import com.example.opengltestdrive.gles.ShaderUtils.createProgram
import com.example.opengltestdrive.gles.ShaderUtils.createShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

const val POSITION_COUNT = 3
private const val TIME = 10000L

class OpenGlRendererModelPosition : GLSurfaceView.Renderer {

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

    private var vertexData: FloatBuffer? = null
    private var uColorLocation = 0
    private var uMatrixLocation = 0
    private var programId = 0
    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)


    override fun onSurfaceCreated(arg0: GL10, arg1: EGLConfig) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        val vertexShaderId = createShader(GLES20.GL_VERTEX_SHADER, verticesShaderCode)
        val fragmentShaderId =
            createShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        programId = createProgram(vertexShaderId, fragmentShaderId)
        GLES20.glUseProgram(programId)
        createViewMatrix()
        prepareData()
        bindData()
    }

    override fun onSurfaceChanged(arg0: GL10, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
        bindMatrix()
    }

    private fun prepareData() {
        val vertices = floatArrayOf(
            // треугольник
            -1f, -0.5f, 0.5f,
            1f, -0.5f, 0.5f,
            0f, 0.5f, 0.5f,
            // ось X
            -3f, 0f, 0f,
            3f, 0f, 0f,
            // ось Y
            0f, -3f, 0f,
            0f, 3f, 0f,
            // ось Z
            0f, 0f, -3f,
            0f, 0f, 3f
        )
        vertexData = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData?.put(vertices)
    }

    private fun bindData() {
        // примитивы
        val aPositionLocation = GLES20.glGetAttribLocation(programId, "a_Position")
        vertexData!!.position(0)
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

    private fun createProjectionMatrix(width: Int, height: Int) {
        var ratio = 1f
        var left = -1f
        var right = 1f
        var bottom = -1f
        var top = 1f
        val near = 2f
        val far = 12f
        if (width > height) {
            ratio = width.toFloat() / height
            left *= ratio
            right *= ratio
        } else {
            ratio = height.toFloat() / width
            bottom *= ratio
            top *= ratio
        }
        Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far)
    }

    private fun createViewMatrix() {
        // точка положения камеры
        val eyeX = 2f
        val eyeY = 2f
        val eyeZ = 3f

        // точка направления камеры
        val centerX = 0f
        val centerY = 0f
        val centerZ = 0f

        // up-вектор
        val upX = 0f
        val upY = 1f
        val upZ = 0f
        Matrix.setLookAtM(
            mViewMatrix,
            0,
            eyeX,
            eyeY,
            eyeZ,
            centerX,
            centerY,
            centerZ,
            upX,
            upY,
            upZ
        )
    }

    private fun bindMatrix() {
        Matrix.multiplyMM(mMatrix, 0, mViewMatrix, 0, mModelMatrix, 0)
        Matrix.multiplyMM(mMatrix, 0, mProjectionMatrix, 0, mMatrix, 0)
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    override fun onDrawFrame(arg0: GL10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // оси
        drawAxes()

        // треугольник
        drawTriangle()
    }

    private fun drawAxes() {
        Matrix.setIdentityM(mModelMatrix, 0)
        bindMatrix()
        GLES20.glLineWidth(3f)
        GLES20.glUniform4f(uColorLocation, 1.0f, 0.0f, 0.0f, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 3, 2)
        GLES20.glUniform4f(uColorLocation, 0.0f, 0.0f, 1.0f, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 5, 2)
        GLES20.glUniform4f(uColorLocation, 1.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_LINES, 7, 2)
    }

    private fun drawTriangle() {
        Matrix.setIdentityM(mModelMatrix, 0)
        setModelMatrix()
        bindMatrix()
        GLES20.glUniform4f(uColorLocation, 0.0f, 1.0f, 0.0f, 1.0f)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3)
    }

    private fun setModelMatrix() {
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 0f, 1f);
        Matrix.translateM(mModelMatrix, 0, 2f, 0f, 0f);
    }

}
