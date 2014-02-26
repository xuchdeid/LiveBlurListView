package com.koalcat.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.ScriptIntrinsicBlur;

public class ScriptIntrinsicBlurRender extends RSRender {

	private ScriptIntrinsicBlur theIntrinsic;
	
	@SuppressLint("NewApi")
	public ScriptIntrinsicBlurRender(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
	}

	@SuppressLint("NewApi")
	@Override
	public void blur(float radius, Allocation tmpIn, Allocation tmpOut) {
		// TODO Auto-generated method stub
		theIntrinsic.setRadius(radius);
		theIntrinsic.setInput(tmpIn);
		theIntrinsic.forEach(tmpOut);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		theIntrinsic.destroy();
	}
}
