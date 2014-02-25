package com.koalcat.view;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.util.Log;

public class GLRender {
	
	private static final String TAG = "GLRender";
	
	private EGL10 mEgl;
	private EGLDisplay mEglDisplay;
	private EGLContext mEglContext;
	private EGLSurface mEglPBSurface;
	private GL10 mGL;
	
	private IntBuffer PixelBuffer;
	
	public GLRender(int width, int height) {
		
		int[] version = new int[2];   
		EGLConfig[] configs = new EGLConfig[1];  
		int[] num_config = new int[1];  

		int[] configSpec ={  
				EGL10.EGL_SURFACE_TYPE, EGL10.EGL_PBUFFER_BIT,   
				EGL10.EGL_RED_SIZE, 8,  
				EGL10.EGL_GREEN_SIZE, 8,  
				EGL10.EGL_BLUE_SIZE, 8,  
				EGL10.EGL_ALPHA_SIZE, 8,  
				EGL10.EGL_NONE   
		};  

		int attribListPbuffer[] = {  
				EGL10.EGL_WIDTH, width,  
				EGL10.EGL_HEIGHT, height,  
				EGL10.EGL_NONE  
		};
		
		mEgl = (EGL10)EGLContext.getEGL();
		mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);
	
		mEgl.eglInitialize(mEglDisplay, version);
	
		mEgl.eglChooseConfig(mEglDisplay, configSpec, configs, 1, num_config);
	
		EGLConfig mEglConfig = configs[0];

		mEglContext = mEgl.eglCreateContext(mEglDisplay, mEglConfig, EGL10.EGL_NO_CONTEXT, null);
		if (mEglContext == EGL10.EGL_NO_CONTEXT) {
			Log.d(TAG, "EGL_NO_CONTEXT");
		}

		mEglPBSurface = mEgl.eglCreatePbufferSurface(mEglDisplay, mEglConfig, attribListPbuffer);
		if (mEglPBSurface == EGL10.EGL_NO_SURFACE) {
			int errorcode = mEgl.eglGetError();
			switch(errorcode) {
			case EGL10.EGL_BAD_DISPLAY:
				Log.d(TAG, "EGL_BAD_DISPLAY");
				break;
			case EGL10.EGL_NOT_INITIALIZED:
				Log.d(TAG, "EGL_NOT_INITIALIZED");
				break;
			case EGL10.EGL_BAD_CONFIG:
				Log.d(TAG, "EGL_BAD_CONFIG");
				break;
			case EGL10.EGL_BAD_ATTRIBUTE:
				Log.d(TAG, "EGL_BAD_ATTRIBUTE");
				break;
			case EGL10.EGL_BAD_ALLOC:
				Log.d(TAG, "EGL_BAD_ALLOC");
				break;
			case EGL10.EGL_BAD_MATCH:
				Log.d(TAG, "EGL_BAD_MATCH");
				break;
			}				
		}
	
	
		if (!mEgl.eglMakeCurrent(mEglDisplay, mEglPBSurface, mEglPBSurface, mEglContext)) {
			Log.d(TAG, "bind failed code:" + mEgl.eglGetError());
		}
	
		mGL = (GL10) mEglContext.getGL();
		PixelBuffer = IntBuffer.allocate(width * height);
	}
	
	private void process(Bitmap bitmap, boolean fast) {
		final GL10 gl = mGL;
		
		gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
		gl.glClearDepthf(1.0f);
		gl.glDepthFunc(GL10.GL_LEQUAL);
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		gl.glEnable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_LEQUAL);
		
		float[] color = new float[16];
		FloatBuffer colorBuffer;
		float[] texVertex = new float[12];
		FloatBuffer vertexBuffer;
		
		ByteBuffer texByteBuffer = ByteBuffer.allocateDirect(texVertex.length * 4);
		texByteBuffer.order(ByteOrder.nativeOrder());
		vertexBuffer = texByteBuffer.asFloatBuffer();
		vertexBuffer.put(texVertex);
		vertexBuffer.position(0);
		
		ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(color.length * 4);
		colorByteBuffer.order(ByteOrder.nativeOrder());
		colorBuffer = colorByteBuffer.asFloatBuffer();
		colorBuffer.put(color);
		colorBuffer.position(0);
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glPushMatrix();
		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		

		
		colorBuffer.clear();
		colorBuffer.put(1.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(1.0f);
		
		colorBuffer.put(1.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(1.0f);
		
		colorBuffer.put(1.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(1.0f);
		
		colorBuffer.put(1.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(0.0f);
		colorBuffer.put(1.0f);
		
		vertexBuffer.clear();
		vertexBuffer.put(0);
		vertexBuffer.put(0);
		vertexBuffer.put(0);
		vertexBuffer.put(5f);
		vertexBuffer.put(0);
		vertexBuffer.put(0);
		vertexBuffer.put(5f);
		vertexBuffer.put(5f);
		vertexBuffer.put(0);
		vertexBuffer.put(0);
		vertexBuffer.put(5f);
		vertexBuffer.put(0);
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		

		
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		
		gl.glDrawArrays(GL10.GL_LINE_LOOP, 0, 4);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glPopMatrix();
	}
	
	private Bitmap pullpixs(Bitmap bitmap) {
		final int width = bitmap.getWidth();
		final int height = bitmap.getHeight();
		PixelBuffer.position(0);
		mGL.glReadPixels(0, 0, width, height, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, PixelBuffer);
		PixelBuffer.position(0);
		bitmap.copyPixelsFromBuffer(PixelBuffer);

		return bitmap;
	}

	public Bitmap Blur(Bitmap bitmap, boolean fast) {
		process(bitmap, fast);
		return pullpixs(bitmap);
	}
}
