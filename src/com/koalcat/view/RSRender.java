package com.koalcat.view;

import com.koalcat.blurdemo.R;
import com.koalcat.blurdemo.ScriptC_blur;
import com.koalcat.blurdemo.ScriptField_ConvolveParams_s;

import android.content.Context;
import android.renderscript.Allocation;

public class RSRender extends Render {

	private ScriptC_blur mScript;
	private ScriptField_ConvolveParams_s cp;
	
	public RSRender(Context context) {
		super(context);
		mScript = new ScriptC_blur(rs, context.getResources(), R.raw.blur);
		cp = new ScriptField_ConvolveParams_s(rs, 1);
		mScript.bind_cp(cp);
	}
	
	@Override
	public void blur(float radius, Allocation tmpIn, Allocation tmpOut) {
		//mScript.set_radius(radius);
		mScript.forEach_root(tmpIn, tmpOut);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		mScript.destroy();
	}
}
