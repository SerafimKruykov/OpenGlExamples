package com.example.opengltestdrive

import android.opengl.GLSurfaceView
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.opengltestdrive.gles.OpenGlRendererTextureModel

class MainActivity : ComponentActivity() {

    private lateinit var glSurfaceView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        glSurfaceView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(2)
            setRenderer(OpenGlRendererTextureModel(context))
        }
        setContentView(glSurfaceView)
    }

    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
}
