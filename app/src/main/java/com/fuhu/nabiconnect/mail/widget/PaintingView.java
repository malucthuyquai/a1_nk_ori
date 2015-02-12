package com.fuhu.nabiconnect.mail.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaintingView extends View {

	private Paint 		mBitmapPaint;
	private Paint 		mPaint;
	private Bitmap 		mBitmap;
	private Bitmap mBitmap_sticker;
	Canvas 				mCanvas_pencil;
	Canvas 				mCanvas_sticker;
	private Path 		mPath;
	private boolean 	m_IsEraser;
	private float 		strokeWidth;
	private boolean		isCleanAll;
	private int			viewWidth;
	private int  		viewHight;
	private boolean     m_IsEditable;

	public PaintingView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mPaint = new Paint();
		mPath = new Path();
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		mPaint.setStrokeWidth(7);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		//Log.i("gary", "onSizeChanged !!");
		viewWidth = w;
		viewHight = h;
		mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas_pencil = new Canvas(mBitmap);
		
		mBitmap_sticker = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		mCanvas_sticker = new Canvas(mBitmap_sticker);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if(mBitmap != null && isCleanAll == false){
			canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);

			canvas.drawPath(mPath, mPaint);
			
			// show sticker in the canvas
			canvas.drawBitmap(mBitmap_sticker, 0, 0, mBitmapPaint);
		}
		
		// end drawing path
	}
	
	protected Bitmap getDrawingBitmap(){
		
		return mBitmap;
	}

	private float mX, mY;
	private static final float TOUCH_TOLERANCE = 4;

	private void touch_start(float x, float y) {
		mPath.reset();
		mPath.moveTo(x, y);
		mX = x;
		mY = y;
	}

	private void touch_move(float x, float y) {
		float dx = Math.abs(x - mX);
		float dy = Math.abs(y - mY);
		if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
			mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
			if (m_IsEraser == true) {
				mPath.lineTo(mX, mY);
				// commit the path to our offscreen
				mCanvas_pencil.drawPath(mPath, mPaint);
				mPath.reset();
				mPath.moveTo(mX, mY);
			}
			mX = x;
			mY = y;
		}
	}

	private void touch_up() {
		mCanvas_pencil.drawPoint(mX, mY, mPaint);
		if (m_IsEraser == false) {
			mPath.lineTo(mX, mY);
			// commit the path to our offscreen
			mCanvas_pencil.drawPath(mPath, mPaint);
		}

		// kill this so we don't double draw
		mPath.reset();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
//		if(DreamWorkDrawing.isColoringDrawing == true){
//			return true;
//		}
		
		float x = event.getX();
		float y = event.getY();
		
		if(!m_IsEditable){
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(isCleanAll == true){
				isCleanAll = false;
			}
			touch_start(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_MOVE:
			touch_move(x, y);
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			touch_up();
			invalidate();
			break;
		}
		return true;
	}

	/*
	public void setPencil() {
		m_IsEraser = false;
		mPaint.setXfermode(null);
		mPaint.setAlpha(0xFF);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeJoin(Paint.Join.ROUND);
		mPaint.setStrokeCap(Paint.Cap.ROUND);

		// for drawing path
		mPaint.setColor(0xff3b3232);
	}
*/
	/*
	public void setEraser() {
		m_IsEraser = true;
		mPaint.setXfermode(null);
		mPaint.setAlpha(0x00);
		mPaint.setColor(Color.TRANSPARENT);
		mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

	}
	*/
	
	public void setEditable(boolean isEditable){
		this.m_IsEditable = isEditable;
	}
	
	public void changeStrokeWidth(float w){
		mPaint.setStrokeWidth(w);
		strokeWidth = w;
	}
	
	public void setPaintColor(int color){
		mPaint.setColor(color);
	}
	
	public void cleanAllCanvas(){
		mBitmap = Bitmap.createBitmap(viewWidth, viewHight, Bitmap.Config.ARGB_8888);
		mCanvas_pencil = new Canvas(mBitmap);
		
		mBitmap_sticker = Bitmap.createBitmap(viewWidth, viewHight,
				Bitmap.Config.ARGB_8888);
		mCanvas_sticker = new Canvas(mBitmap_sticker);
		
		isCleanAll = true;
		mPath.reset();
		invalidate();
	}

	public void setCleanAll(boolean isCleanAll) {
		this.isCleanAll = isCleanAll;
	}

	public void setPaint(Paint paint)
	{
		m_IsEraser = false;
		this.mPaint = paint;
	}
	public void setEraser(Paint paint) {
		m_IsEraser = true;
		this.mPaint = paint;
	}
}
