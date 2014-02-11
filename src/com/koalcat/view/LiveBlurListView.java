package com.koalcat.view;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LiveBlurListView extends ListView {
	
	private int BLUR_HEIGHT = 65;
	
	private Paint paint;
	private Rect mRectBlur, mRect;
	private Bitmap mBitmap, mCanvasBitmap;
	private Canvas mCanvas;
	private Blur mBlur;
	private boolean enableBlur = true;
	
	public LiveBlurListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public LiveBlurListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		if (enableBlur) {
			mBlur = new Blur(context);
		}
		
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		BLUR_HEIGHT = (int) ((BLUR_HEIGHT * (float)dm.densityDpi / 160.0f));
		
		View footer = new View(context);
		LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BLUR_HEIGHT);
		footer.setLayoutParams(params);
		addFooterView(footer, null, false);
		
		this.setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	public LiveBlurListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/*@Override
	**protected void dispatchDraw(Canvas canvas) {
	*/
	@Override
	public void draw(Canvas canvas) {
		if (enableBlur) {

			final Rect blur = mRectBlur;
			final Rect rect = mRect;
			final Paint mPaint = paint;
			final Bitmap canvasBitmap = mCanvasBitmap;
			final Canvas mTempCanvas = mCanvas;
	        
			mTempCanvas.save();
			//mTempCanvas.clipRect(blur);
			mTempCanvas.translate(0, -blur.top);
			super.draw(mTempCanvas);
			mTempCanvas.restore();
	        

			//mBitmap = Bitmap.createBitmap(canvasBitmap, 0, 0, blur.right - blur.left, blur.top);
			//canvas.drawBitmap(mBitmap, null, rect, mPaint);
			canvas.save();
			canvas.clipRect(rect);
			super.draw(canvas);
			canvas.restore();
			
			//mBitmap = Bitmap.createBitmap(canvasBitmap, blur.left, blur.top,
			//		blur.right - blur.left, blur.bottom - blur.top);
			mBitmap = mBlur.blur(canvasBitmap);
			canvas.drawBitmap(mBitmap, null, blur, mPaint);

			mTempCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			//mTempCanvas.clipRect(rect);
			
		} else {
			super.draw(canvas);
		}
	}
	
	@Override
	public void onFinishInflate() {
		super.onFinishInflate();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
	}
	
	public void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (w != oldw || h != oldh) {
			init();
		}
	}

	private void init() {
		if (enableBlur) {
			paint = new Paint();
			//paint.setAntiAlias(true);
			//paint.setColor(0xffbb0000);
			//paint.setStyle(Paint.Style.STROKE);
			mRectBlur = new Rect();
			mRectBlur.left = 0;
			mRectBlur.right = getWidth();
			mRectBlur.bottom = getHeight();
			mRectBlur.top = mRectBlur.bottom - BLUR_HEIGHT;
			
			mRect = new Rect();
			mRect.left = 0;
			mRect.right = getWidth();
			mRect.bottom = mRectBlur.top;
			mRect.top = 0;
			
			if (mCanvasBitmap != null && !mCanvasBitmap.isRecycled()) {
				mCanvasBitmap.recycle();
			}
			mCanvasBitmap = Bitmap.createBitmap(mRectBlur.right, BLUR_HEIGHT, Config.ARGB_8888);
			mCanvas = new Canvas(mCanvasBitmap);
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		recycle();
	}

	private void recycle() {
		if (mBitmap != null && !mBitmap.isRecycled()) {
			mBitmap.recycle();
		}
		if (mCanvasBitmap != null && !mCanvasBitmap.isRecycled()) {
			mCanvasBitmap.recycle();
		}
		if (mBlur != null) {
			mBlur.Destroy();
		}
	}
}
