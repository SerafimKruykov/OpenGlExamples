package com.example.opengltestdrive.gles

import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLES20.GL_FLOAT
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_TRIANGLES
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.opengl.GLES20.glClear
import android.opengl.GLES20.glClearColor
import android.opengl.GLES20.glDrawArrays
import android.opengl.GLES20.glEnableVertexAttribArray
import android.opengl.GLES20.glGetAttribLocation
import android.opengl.GLES20.glLineWidth
import android.opengl.GLES20.glUseProgram
import android.opengl.GLES20.glVertexAttribPointer
import android.opengl.GLES20.glViewport
import android.opengl.GLSurfaceView.Renderer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10


class OpenGlRenderer : Renderer {

    companion object Shaders {
        val verticesShaderCode =
            """
                attribute vec4 a_Position;
                attribute vec4 a_Color;
                varying vec4 v_Color;

                void main() {
                    gl_Position = a_Position;
                    gl_PointSize = 0.69;
                    v_Color = a_Color;
                }
            """.trimIndent()

        val fragmentShaderCode =
            """
                precision mediump float;
                varying vec4 v_Color;

                void main() {
                    gl_FragColor = v_Color;
                }
            """.trimIndent()
    }

    private lateinit var vertexData: FloatBuffer
    private var programId = 0
    private var aColorLocation = 0
    private var aPositionLocation = 0

    init {
        prepareData()
    }

    private fun prepareData() {
        // создаем массив из координат вершин
        val vertices =
            floatArrayOf(
                -0.5f, -0.2f, 1.0f, 0.0f, 0.0f,
                0.0f, 0.2f, 0.0f, 1.0f, 0.0f,
                0.5f, -0.2f, 0.0f, 0.0f, 1.0f,
            )
        // создаем байт-буфер для хранения координат
        vertexData = ByteBuffer
            // говорим сколько нам нужно выделить памяти для хранения
            .allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
        // сохраняем координаты в буфере
        vertexData.put(vertices)
    }

    // передаем данные в шейдеры
    private fun bindData() {
        aPositionLocation = glGetAttribLocation(programId, "a_Position")
        vertexData.position(0)
        glVertexAttribPointer(aPositionLocation, 2, GL_FLOAT, false, 20, vertexData)
        glEnableVertexAttribArray(aPositionLocation)


        aColorLocation = glGetAttribLocation(programId, "a_Color")
        vertexData.position(2)
        glVertexAttribPointer(aColorLocation, 3, GL_FLOAT, false, 20, vertexData)
        glEnableVertexAttribArray(aColorLocation)


//        // получаем положение attribute переменной
//        aPositionLocation = glGetAttribLocation(programId, "a_Position")
//        // говорим с какого места читать координаты (с начала)
//        vertexData.position(0)
//        glVertexAttribPointer(
//            // передаем указатель на переменную в шейдере,
//            aPositionLocation,
//            // говорим сколько элементов массива брать для заполнения атрибута (по 2 - x,y)
//            2,
//            // у нас float значения
//            GL_FLOAT,
//            false,
//            0,
//            // ссылка на буфер с координатами
//            vertexData
//        )
//        // "включаем" атрибут
//        glEnableVertexAttribArray(aPositionLocation)
    }

    override fun onSurfaceCreated(p0: GL10?, p1: EGLConfig?) {
        // дефолтный RGBA после полной очистки
        glClearColor(0f, 0f, 0f, 1f)
        // создаем вершинный шейдер
        val vertexShaderId =
            ShaderUtils.createShader(GL_VERTEX_SHADER, verticesShaderCode)
        // создаем фрагментный шейдер
        val fragmentShaderId =
            ShaderUtils.createShader(GL_FRAGMENT_SHADER, fragmentShaderCode)
        // обьеденяем шейдеры в программу (0 - программа не создалась)
        programId = ShaderUtils.createProgram(vertexShaderId, fragmentShaderId)
        // сообщаем системе что программу надо использовать для построения изображения
        glUseProgram(programId)

        bindData()
    }

    override fun onSurfaceChanged(p0: GL10?, width: Int, height: Int) {
        // пересчитывам область отрисовки при смене конфишгурации
        glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(p0: GL10?) {
        // метод очистки
        glClear(GL_COLOR_BUFFER_BIT)
        glLineWidth(5f)
        glDrawArrays(GL_TRIANGLES, 0, 3)
    }
}