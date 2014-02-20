package com.koalcat.view;
/**
 * @author xuchdeid@gmail.com
 *  __________________________     \_/
   |                          |   /._.\
   |  Android!Android!         > U|   |U
   |                xuchdeid  |   |___|
   |__________________________|    U U
 * */
import com.koalcat.blurdemo.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class LiveBlurListView extends ListView {
	
	/**
	 * Information about how tall the blur on bottom of the view wants to be.
	 */
	private int blur_bottom_height = 0;
	
	/**
	 * Information about how tall the blur on top of the view wants to be.
	 */
	private int blur_top_height = 0;
	
	/**
	 * Paint used for draw blur bitmap.
	 */
	private Paint paint;
	
	/**
	 * Rect for blur on bottom
	 */
	private Rect mRectBlurForBottom;
	
	/**
	 * Rect for blur on top
	 */
	private Rect mRectBlurForTop;
	
	/**
	 * Rect for clip Canvas.
	 */
	private Rect mRect;
	
	/*
	 * Bitmap for blur on bottom.
	 */
	private Bitmap mCanvasBitmapforBottom;
	
	/**
	 * Canvas for blur on bottom.
	 */
	private Canvas mCanvasforBottom;
	
	/*
	 * Bitmap for blur on top.
	 */
	private Bitmap mCanvasBitmapforTop;
	
	/**
	 * Canvas for blur on top.
	 */
	private Canvas mCanvasforTop;
	
	/**
	 * Blur bitmap
	 */
	private Blur mBlur;
	
	/**
	 * switch 
	 */
	private boolean enableBlur = true;
	
	public LiveBlurListView(Context context) {
		super(context);
	}
	
	public LiveBlurListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setup(context, attrs, 0);
	}

	public LiveBlurListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setup(context, attrs, defStyle);
	}
	
	private void setup(Context context, AttributeSet attrs, int defStyle) {
		TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.BlurView, defStyle, 0);
		blur_bottom_height = a.getDimensionPixelSize(R.styleable.BlurView_blur_bottom_height, 0);
		blur_top_height = a.getDimensionPixelSize(R.styleable.BlurView_blur_top_height, 0);
		enableBlur = a.getBoolean(R.styleable.BlurView_blur_enable, false);
		a.recycle();

		if (enableBlur) {
			//mBlur = new Blur(context);
		}
		
		if (blur_bottom_height > 0) {
			View footer = new View(getContext());
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, blur_bottom_height);
			footer.setLayoutParams(params);
			addFooterView(footer, null, false);
		}
		
		if (blur_top_height > 0) {
			View header = new View(getContext());
			LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, blur_top_height);
			header.setLayoutParams(params);
			addHeaderView(header, null, false);
		}
		
		setLayerType(View.LAYER_TYPE_HARDWARE, null);
	}

	@Override
	public void draw(Canvas canvas) {
		if (enableBlur) {
			
			final Rect rect = mRect;
			final Paint mPaint = paint;
			
			final Rect blurbottom = mRectBlurForBottom;
			final Bitmap canvasBitmapbottom = mCanvasBitmapforBottom;
			final Canvas mTempCanvasbottom = mCanvasforBottom;
			if (blur_bottom_height > 0) { 
				mTempCanvasbottom.save();
				mTempCanvasbottom.translate(0, -blurbottom.top);
				super.draw(mTempCanvasbottom);
				mTempCanvasbottom.restore();
			}
			
			final Rect blurtop = mRectBlurForTop;
			final Bitmap canvasBitmaptop = mCanvasBitmapforTop;
			final Canvas mTempCanvastop = mCanvasforTop;
			if (blur_top_height > 0) {
				mTempCanvastop.save();
				super.draw(mTempCanvastop);
				mTempCanvastop.restore();
			}

			canvas.save();
			canvas.clipRect(rect);
			super.draw(canvas);
			canvas.restore();
			
			if (blur_bottom_height > 0) {
				Bitmap mBitmap = mBlur.blur(canvasBitmapbottom, true);
				canvas.drawBitmap(mBitmap, null, blurbottom, mPaint);
				//canvas.drawBitmap(canvasBitmapbottom, null, blurbottom, mPaint);
				
				mPaint.setColor(0x18888888);
				canvas.drawLine(blurbottom.left, blurbottom.top, blurbottom.right, blurbottom.top, mPaint);
				mPaint.setColor(Color.BLACK);

				mTempCanvasbottom.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			}
			if (blur_top_height > 0) { 
				Bitmap mBitmap = mBlur.blur(canvasBitmaptop, true);
				canvas.drawBitmap(mBitmap, null, blurtop, mPaint);
				//canvas.drawBitmap(canvasBitmaptop, null, blurtop, mPaint);
				
				mPaint.setColor(0x18888888);
				canvas.drawLine(blurtop.left, blurtop.bottom, blurtop.right, blurtop.bottom, mPaint);
				mPaint.setColor(Color.BLACK);

				mTempCanvastop.drawColor(Color.TRANSPARENT, Mode.CLEAR);
			}
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
			
			if (paint == null) {
				paint = new Paint();
				paint.setAntiAlias(true);
			}
			
			if (mRectBlurForTop == null) mRectBlurForTop = new Rect();
			mRectBlurForTop.left = 0;
			mRectBlurForTop.right = getWidth();
			mRectBlurForTop.top = 0;
			mRectBlurForTop.bottom = blur_top_height;
			
			if (mRectBlurForBottom == null) mRectBlurForBottom = new Rect();
			mRectBlurForBottom.left = 0;
			mRectBlurForBottom.right = getWidth();
			mRectBlurForBottom.bottom = getHeight();
			mRectBlurForBottom.top = mRectBlurForBottom.bottom - blur_bottom_height;
			
			if (mRect == null) mRect = new Rect();
			mRect.left = 0;
			mRect.right = getWidth();
			mRect.bottom = mRectBlurForBottom.top;
			mRect.top = mRectBlurForTop.bottom;
			
			recycle(false);
			
			if (blur_bottom_height > 0) {
				mCanvasBitmapforBottom = Bitmap.createBitmap(mRectBlurForBottom.right - mRectBlurForBottom.left,
						blur_bottom_height, Config.ARGB_8888);
				mCanvasforBottom = new Canvas(mCanvasBitmapforBottom);
			}
			
			if (blur_top_height > 0) {
				mCanvasBitmapforTop = Bitmap.createBitmap(mRectBlurForTop.right - mRectBlurForTop.left,
						blur_top_height, Config.ARGB_8888);
				mCanvasforTop = new Canvas(mCanvasBitmapforTop);
			}
			
			if (mBlur == null) {
				mBlur = new Blur(getContext());
			}
		}
	}
	
	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		recycle(true);
	}

	private void recycle(boolean all) {

		if (mCanvasBitmapforBottom != null && !mCanvasBitmapforBottom.isRecycled()) {
			mCanvasBitmapforBottom.recycle();
		}
		
		if (mCanvasBitmapforTop != null && !mCanvasBitmapforTop.isRecycled()) {
			mCanvasBitmapforTop.recycle();
		}
		
		if (all) {
			if (mBlur != null) {
				mBlur.Destroy();
			}
		}
	}
}
