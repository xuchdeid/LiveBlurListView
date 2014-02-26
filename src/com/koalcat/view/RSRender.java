package com.koalcat.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

public abstract class RSRender implements BaseRender {
	protected RenderScript rs;
	
	public RSRender(Context context) {
		rs = RenderScript.create(context);
	}
	
	@Override
	public void blur(float radius, Bitmap in, Bitmap out) {
		Allocation tmpIn = Allocation.createFromBitmap(rs, in);
		Allocation tmpOut = Allocation.createFromBitmap(rs, out);
		blur(radius, tmpIn, tmpOut);
		tmpOut.copyTo(out);
		tmpIn.destroy();
		tmpOut.destroy();
	}
	
	public abstract void blur(float radius, Allocation tmpIn, Allocation tmpOut);
	
	@Override
	public void destroy() {
		if (rs != null) rs.destroy();
	}
}
