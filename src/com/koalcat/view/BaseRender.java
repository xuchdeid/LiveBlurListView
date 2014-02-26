package com.koalcat.view;

import android.graphics.Bitmap;

public interface BaseRender {
	public abstract void blur(float radius, Bitmap in, Bitmap out);
	public abstract void destroy() ;
}
