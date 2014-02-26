package com.koalcat.view;

import android.graphics.Bitmap;

public class JNIRender implements BaseRender {
	
	static {
        System.loadLibrary("blurjni");
    }
	
	private static native void Blur(Bitmap in, Bitmap out, int r);

	@Override
	public void blur(float radius, Bitmap in, Bitmap out) {
		// TODO Auto-generated method stub
		Blur(in, out, (int)radius);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

}
