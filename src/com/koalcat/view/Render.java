package com.koalcat.view;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.RenderScript;

public abstract class Render {
	protected RenderScript rs;
	public Render(Context context) {
		rs = RenderScript.create(context);
	}
	public abstract void blur(float radius, Allocation tmpIn, Allocation tmpOut);
	public void destroy() {
		if (rs != null) rs.destroy();
	}
}
