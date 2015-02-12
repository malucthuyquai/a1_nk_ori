package com.fuhu.nabiconnect.utils.stickerwidget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;

import com.fuhu.nabiconnect.R;

public class StickerWidget extends ImageView {

	final private String TAG = StickerWidget.class.getSimpleName();

	/** min and max values of scale supported */
	private static float mMinScale = 0.5f;
	private static float mMaxScale = 1.0f;

	/** length of icons in pixels */
	private static float mButtonSide = 60;
	/** space between view's central line and icon edges in pixels */
	private static float mButtonSpacing = 24;

	/** thickness of dotted frame */
	private static float mBoarderWidth = 10;

	private Context mContext;
	private Paint mPaint = new Paint();
	private static float mDensity;

	/** indicates whether we're dragging or scaling/rotating */
	private boolean mDragging = true;

	/** related to dragging operation */
	private int mLastX, mLastY;

	/** related to scaling operation */
	private Matrix mMatrix = new Matrix();
	private float mInitDistance = 0;
	private float mLastScale = 1;
	private float mCurrentScale = 1;

	/** related to rotate operation */
	private float mLastDegree = 0;

	/** control buttons */
	private RectF mButtonCheck = new RectF();
	private RectF mButtonCross = new RectF();
	private RectF mSideIcons = new RectF();
	private Bitmap mIconCheck, mIconCross, mIconResize, mIconRotate;

	/** indicator whether to draw control icons */
	private boolean mShouldDrawFullBackground = true;
	private boolean mShouldDrawBackground = false;

	/** indicator whether sticker will respond to touch event */
	private boolean mIsEditable = true;
	// reports resizing gesture to us
	private ScaleGestureDetector mScaleGestureDetector;

	/** call back function supplied by activity / fragment */
	private StickerButtonListener mParentCallback;
	/** assigned from parent */
	private int mIndex = -1;

	private int mBitmapWidth = 0;
	private int mBitmapHeight = 0;

	public StickerWidget(Context context) {
		this(context, null, null);
	}

	public StickerWidget(Context context, StickerButtonListener listener) {
		this(context, null, listener);
	}

	/**
	 * 
	 * @param context
	 * @param resId
	 *            image resource id for the imageview
	 * @param listener
	 */
	public StickerWidget(Context context, int resId, StickerButtonListener listener) {
		this(context, BitmapFactory.decodeResource(context.getResources(), resId), listener);
	}

	public StickerWidget(Context context, Bitmap image, StickerButtonListener listener, boolean showFullMenu) {
		this(context, image, listener);
		mShouldDrawFullBackground = showFullMenu;
	}

	public StickerWidget(Context context, Bitmap image, StickerButtonListener listener) {
		super(context);
		this.mContext = context;

		TypedValue value = new TypedValue();
		mContext.getResources().getValue(R.dimen.sticker_widget_min_scale, value, true);
		mMinScale = value.getFloat();

		mContext.getResources().getValue(R.dimen.sticker_widget_max_scale, value, true);
		mMaxScale = value.getFloat();

		mContext.getResources().getValue(R.dimen.sticker_widget_button_side, value, true);
		mButtonSide = value.getFloat();

		mContext.getResources().getValue(R.dimen.sticker_widget_button_spacing, value, true);
		mButtonSpacing = value.getFloat();

		mContext.getResources().getValue(R.dimen.sticker_widget_boarder_width, value, true);
		mBoarderWidth = value.getFloat();

		mPaint.setColor(Color.WHITE);
		mPaint.setStyle(Style.STROKE);
		mScaleGestureDetector = new ScaleGestureDetector(context, mScaleGestureListener);
		mIconCheck = BitmapFactory.decodeResource(context.getResources(), R.drawable.stickerwidget_check);
		mIconCross = BitmapFactory.decodeResource(context.getResources(), R.drawable.stickerwidget_cross);
		mIconResize = BitmapFactory.decodeResource(context.getResources(), R.drawable.stickerwidget_resize);
		mIconRotate = BitmapFactory.decodeResource(context.getResources(), R.drawable.stickerwidget_rotate);
		this.setImageBitmap(image);
		mBitmapWidth = image.getWidth();
		mBitmapHeight = image.getHeight();
		mMatrix.postTranslate(mButtonSide, mButtonSide);
		this.setImageMatrix(mMatrix);
		float scale = 1f / 1.5f;
		this.setScaleX(scale);
		this.setScaleY(scale);
		mCurrentScale = scale;
		mLastScale = scale;
		mParentCallback = listener;
	}

	@Override
	protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		if (gainFocus) {
			mShouldDrawBackground = true;
		} else {
			mShouldDrawBackground = mShouldDrawFullBackground = false;
		}
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
	}

	/*
	 * public methods
	 */
	/**
	 * signals to hide control buttons when drawing
	 */
	public void hideControl() {
		mShouldDrawFullBackground = false;
		mShouldDrawBackground = false;
		invalidate();
	}

	/**
	 * toggle edit mode of this StickerWidget
	 * 
	 * @param isEditable
	 */
	public void setEditable(boolean isEditable) {
		mIsEditable = isEditable;
	}

	/**
	 * registers callback function
	 * 
	 * @param listener
	 *            the object whose onClick method will be called
	 */
	public void setCrossButtonListener(StickerButtonListener listener) {
		mParentCallback = listener;
	}

	/**
	 * sets private int mIndex.
	 * 
	 * @param id
	 */
	public void setIndex(int index) {
		this.mIndex = index;
	}

	public void setMinScale(float minScale) {
		mMinScale = minScale;
	}

	public void setMaxScale(float maxScale) {
		mMaxScale = maxScale;
	}

	/**
	 * 
	 * @return private int mIndex
	 */
	public int getIndex() {
		return mIndex;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		LayoutParams params = getLayoutParams();
		if (params != null) {
			// params.width = LayoutParams.WRAP_CONTENT;
			// params.height = LayoutParams.WRAP_CONTENT;
			params.width = mBitmapWidth + (int) (2 * mButtonSide);
			params.height = mBitmapHeight + (int) (2 * mButtonSide);
			this.setLayoutParams(params);
		}
		mDensity = mContext.getResources().getDisplayMetrics().density;
		mPaint.setStrokeWidth(mBoarderWidth * mDensity);
		this.setScaleType(ScaleType.MATRIX);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!mIsEditable) {
			return false;
		}
		switch (event.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:
			mParentCallback.onGainFocus(mIndex);
			mShouldDrawBackground = true;
			mLastX = (int) event.getRawX();
			mLastY = (int) event.getRawY();
			invalidate();
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			mLastDegree = getRotationDegree(event);
			mDragging = false;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mDragging) {
				drag(event);
			} else {
				float currentDegree = getRotationDegree(event);
				float delta_rotate = currentDegree - mLastDegree;
				rotate(delta_rotate);
				mLastDegree = currentDegree;
				// resize will be handled by ScaleGestureDetector
			}
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mLastScale = mCurrentScale;
			mDragging = true;
			// this essentially skips the next drag(event)
			mLastX = Integer.MAX_VALUE;
			mLastY = Integer.MAX_VALUE;
			break;
		case MotionEvent.ACTION_UP:
			if (mShouldDrawFullBackground && mButtonCheck.contains(event.getX(), event.getY())) {
				mShouldDrawFullBackground = mShouldDrawBackground = false;
				invalidate();
			} else if (mButtonCross.contains(event.getX(), event.getY())) {
				if (mParentCallback == null) {
					;
				} else {
					mParentCallback.onClick(this);
				}
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			invalidate();
			break;
		default:
			break;
		}
		return mScaleGestureDetector.onTouchEvent(event);
	}

	private int mLeft = -1, mTop = -1, mRight = -1, mBottom = -1;

	@Override
	public void layout(int l, int t, int r, int b) {
		if (mLeft == -1 && mTop == -1 && mRight == -1 && mBottom == -1) {
			// when we do not have our desired position
			super.layout(l, t, r, b);
		} else {
			super.layout(mLeft, mTop, mRight, mBottom);
		}
	}

	/**
	 * the order of the steps must be: 1. canvas save 2. draw control & frame 3.
	 * restore
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// draws image resource
		super.onDraw(canvas);
		if (mShouldDrawBackground || mShouldDrawFullBackground) {
			canvas.save();
			canvas.scale(1 / mCurrentScale, 1 / mCurrentScale);
			/* */
			drawFrame(canvas);
			if (mShouldDrawFullBackground) {
				drawControl(canvas);
			} else {
				drawCenterCrossButton(canvas);
			}
			drawSideIcons(canvas);
			/* */
			canvas.restore();
		}
	}

	private void drag(MotionEvent event) {
		if (mLastX == Integer.MAX_VALUE && mLastY == Integer.MAX_VALUE) {
			mLastX = (int) event.getRawX();
			mLastY = (int) event.getRawY();
			return;
		}
		int dx = (int) event.getRawX() - mLastX;
		int dy = (int) event.getRawY() - mLastY;
		mLeft = getLeft() + dx;
		mBottom = getBottom() + dy;
		mRight = getRight() + dx;
		mTop = getTop() + dy;

		// if (l < 0) {
		// l = 0;
		// r = l + getWidth();
		// }

		// if (t < 0) {
		// t = 0;
		// b = t + getHeight();
		// }

		// if (r > )) {
		// r = screenWidth;
		// l = r - screenWidth;
		// }

		// if (b > screenHeight) {
		// b = screenHeight;
		// t = b - screenHeight;
		// }

		layout(mLeft, mBottom, mRight, mTop);
		mLastX = (int) event.getRawX();
		mLastY = (int) event.getRawY();
		postInvalidate();
	}

	/**
	 * 
	 * @param degree
	 *            the degree to rotate
	 */
	private void rotate(float degree) {
		// rotation degree is additive, need not reset
		mMatrix.postRotate(degree, getWidth() / 2, getHeight() / 2);
		this.setImageMatrix(mMatrix);
	}

	private void resize(float scale) {
		if (scale > mMaxScale) {
			scale = mMaxScale;
		} else if (scale < mMinScale) {
			scale = mMinScale;
		}
		mCurrentScale = scale;
		setScaleX(mCurrentScale);
		setScaleY(mCurrentScale);
	}

	/*
	 * Helper math functions
	 */

	/**
	 * 
	 * @param event
	 *            current motion event
	 * @return degree between two pointers
	 */
	private float getRotationDegree(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double radians = Math.atan2(delta_y, delta_x);
		return (float) Math.toDegrees(radians);
	}

	private ScaleGestureDetector.SimpleOnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {

		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mInitDistance = mScaleGestureDetector.getCurrentSpan();
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float scale = mScaleGestureDetector.getCurrentSpan() / mInitDistance * mLastScale;
			resize(scale);
			return true;
		}
	};

	/**
	 * void drawing function
	 */

	/**
	 * draws control buttons
	 * 
	 * @param canvas
	 */
	private void drawControl(Canvas canvas) {
		float w = this.getWidth() * mCurrentScale;
		float h = this.getHeight() * mCurrentScale;
		float top = h - mButtonSide * mDensity;
		float bottom = top + mButtonSide * mDensity;

		// ======
		// draws check button
		float left = w / 2 + mButtonSpacing * mDensity;
		float right = left + mButtonSide * mDensity;
		mButtonCheck.set(left, top, right, bottom);
		canvas.drawBitmap(mIconCheck, null, mButtonCheck, null);

		// draws cross button
		left = w / 2 - (mButtonSide + mButtonSpacing) * mDensity;
		right = left + mButtonSide * mDensity;
		mButtonCross.set(left, top, right, bottom);
		canvas.drawBitmap(mIconCross, null, mButtonCross, null);
		// ======

		refreshRectTouchArea(mButtonCheck);
		refreshRectTouchArea(mButtonCross);
	}

	/**
	 * draws cross button at the bottom center
	 * 
	 * @param canvas
	 */
	private void drawCenterCrossButton(Canvas canvas) {
		float w = this.getWidth() * mCurrentScale;
		float h = this.getHeight() * mCurrentScale;
		float left = w / 2 - mButtonSide / 2 * mDensity;
		float top = h - mButtonSide * mDensity;
		float right = left + mButtonSide * mDensity;
		float bottom = top + mButtonSide * mDensity;
		mButtonCross.set(left, top, right, bottom);
		canvas.drawBitmap(mIconCross, null, mButtonCross, null);
		refreshRectTouchArea(mButtonCross);
	}

	/**
	 * draw side icons. resize icon is on the left
	 * 
	 * @param canvas
	 */
	private void drawSideIcons(Canvas canvas) {
		float w = this.getWidth() * mCurrentScale;
		float h = this.getHeight() * mCurrentScale;

		float top = h / 2 - mButtonSide / 2 * mDensity;
		float bottom = top + mButtonSide * mDensity;

		// left side resize icon
		// left = 0;
		float right = mButtonSide * mDensity;

		mSideIcons.set(0, top, right, bottom);
		canvas.drawBitmap(mIconResize, null, mSideIcons, null);

		// right side rotate icon
		// right = width
		float left = w - mButtonSide * mDensity;
		mSideIcons.set(left, top, (int) w, bottom);
		canvas.drawBitmap(mIconRotate, null, mSideIcons, null);
	}

	private void refreshRectTouchArea(RectF rect) {
		rect.set(rect.left / mCurrentScale, rect.top / mCurrentScale, rect.right / mCurrentScale, rect.bottom
				/ mCurrentScale);
	}

	private RectF frame = new RectF();
	private final static float radius = 25f;

	DashPathEffect mDashPathEffect = new DashPathEffect(new float[] { 50, 20 }, (float) 1.0);

	private void drawFrame(Canvas canvas) {
		float w = getWidth() * mCurrentScale;
		float h = getHeight() * mCurrentScale;
		float r = radius * mDensity;
		frame.set(0 + mButtonSide * mDensity / 2, 0 + mButtonSide / 2, w - mButtonSide * mDensity / 2, h - mButtonSide
				* mDensity / 2);
		mPaint.setPathEffect(mDashPathEffect);
		canvas.drawRoundRect(frame, r, r, mPaint);
	}
}