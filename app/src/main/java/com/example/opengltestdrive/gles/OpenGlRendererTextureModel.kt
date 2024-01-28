package com.example.opengltestdrive.gles

import android.content.Context
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_BUFFER_BIT
import android.opengl.GLES20.GL_DEPTH_TEST
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_TEXTURE0
import android.opengl.GLES20.GL_TEXTURE_CUBE_MAP
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_UNSIGNED_BYTE
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glActiveTexture
import android.opengl.GLES20.glBindTexture
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawElements
import android.opengl.GLES20.glEnable
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glGetUniformLocation
import android.opengl.GLES20.glUniform1i
import android.opengl.GLES20.glUniformMatrix4fv
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import android.os.SystemClock
import com.example.opengltestdrive.R
import com.example.opengltestdrive.gles.ShaderUtils.createProgram
import com.example.opengltestdrive.gles.ShaderUtils.createShader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGlRendererTextureModel(private val context: Context) : Renderer {

    companion object Shaders {
        val verticesShaderCode =
            """
            attribute vec4 a_Position;
            uniform mat4 u_Matrix;
            varying vec3 v_Position;

            void main() {
                v_Position = a_Position.xyz;
                gl_Position = u_Matrix * a_Position;
            }
            """.trimIndent()

        val fragmentShaderCode =
            """
            precision mediump float;

            uniform samplerCube u_TextureUnit;
            varying vec3 v_Position;

            void main() {
                gl_FragColor = textureCube(u_TextureUnit, v_Position);
            }
            """.trimIndent()
    }


    private val POSITION_COUNT = 3

    private var vertexData: FloatBuffer? = null
    private var indexArray: ByteBuffer? = null

    private var aPositionLocation = 0
    private var uTextureUnitLocation = 0
    private var uMatrixLocation = 0

    private var programId = 0

    private val mProjectionMatrix = FloatArray(16)
    private val mViewMatrix = FloatArray(16)
    private val mModelMatrix = FloatArray(16)
    private val mMatrix = FloatArray(16)

    private var texture = 0

    override fun onSurfaceCreated(arg0: GL10?, arg1: EGLConfig?) {
        glClearColor(0f, 0f, 0f, 1f)
        glEnable(GL_DEPTH_TEST)
        createAndUseProgram()
        getLocations()
        prepareData()
        bindData()
        createViewMatrix()
        Matrix.setIdentityM(mModelMatrix, 0)
    }

    override fun onSurfaceChanged(arg0: GL10?, width: Int, height: Int) {
        glViewport(0, 0, width, height)
        createProjectionMatrix(width, height)
        bindMatrix()
    }

    private fun prepareData() {
        val vertices = floatArrayOf( // вершины куба
            -1f, 1f, 1f,  // верхняя левая ближняя
            1f, 1f, 1f,  // верхняя правая ближняя
            -1f, -1f, 1f,  // нижняя левая ближняя
            1f, -1f, 1f,  // нижняя правая ближняя
            -1f, 1f, -1f,  // верхняя левая дальняя
            1f, 1f, -1f,  // верхняя правая дальняя
            -1f, -1f, -1f,  // нижняя левая дальняя
            1f, -1f, -1f // нижняя правая дальняя
        )
        vertexData = ByteBuffer
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        vertexData?.put(vertices)
        indexArray = ByteBuffer.allocateDirect(36)
            .put(
                byteArrayOf( // грани куба
                    // ближняя
                    1, 3, 0,
                    0, 3, 2,
                    // дальняя
                    4, 6, 5,
                    5, 6, 7,
                    // левая
                    0, 2, 4,
                    4, 2, 6,
                    // правая
                    5, 7, 1,
                    1, 7, 3,
                    // верхняя
                    5, 1, 4,
                    4, 1, 0,
                    // нижняя
                    6, 2, 7,
                    7, 2, 3
                )
            )
        indexArray?.position(0)
        texture = TextureUtils.loadTextureCube(
            context, intArrayOf(
                R.drawable.box0, R.drawable.box1,
                R.drawable.box2, R.drawable.box3,
                R.drawable.box4, R.drawable.box5
            )
        )
    }

    private fun createAndUseProgram() {
        val vertexShaderId = createShader(GL_VERTEX_SHADER, verticesShaderCode)
        val fragmentShaderId = createShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        programId = createProgram(vertexShaderId, fragmentShaderId)
        glUseProgram(programId)
    }

    private fun getLocations() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        uTextureUnitLocation = glGetUniformLocation(programId, "u_TextureUnit")
        uMatrixLocation = glGetUniformLocation(programId, "u_Matrix")
    }

    private fun bindData() {
        // координаты вершин
        vertexData!!.position(0)
        glVertexAttribPointer(
            aPositionLocation, POSITION_COUNT, GL_FLOAT,
            false, 0, vertexData
        )
        glEnableVertexAttribArray(aPositionLocation)

        // помещаем текстуру в target CUBE_MAP юнита 0
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_CUBE_MAP, texture)

        // юнит текстуры
        glUniform1i(uTextureUnitLocation, 0)
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
        val eyeX = 0f
        val eyeY = 2f
        val eyeZ = 4f

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
        glUniformMatrix4fv(uMatrixLocation, 1, false, mMatrix, 0)
    }

    var TIME = 10000L

    override fun onDrawFrame(arg0: GL10?) {
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        Matrix.setIdentityM(mModelMatrix, 0)

        // вращение
        setModelMatrix()
        glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indexArray)
    }

    private fun setModelMatrix() {
        val angle = (SystemClock.uptimeMillis() % TIME).toFloat() / TIME * 360
        Matrix.rotateM(mModelMatrix, 0, angle, 0f, 1f, 0f)
        bindMatrix()
    }
}