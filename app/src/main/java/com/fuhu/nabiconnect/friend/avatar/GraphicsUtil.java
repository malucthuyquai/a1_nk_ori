package com.fuhu.nabiconnect.friend.avatar;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * GraphicsUtil an utility class which convert the image in circular shape
 * Author : Mukesh Yadav
 */
public class GraphicsUtil {

	/*
	 * Draw image in circular shape Note: change the pixel size if you want
	 * image small or large
	 */
	public static Bitmap getCircleBitmap(Bitmap bitmap) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int color = 0xffff0000;
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawOval(rectF, paint);

		paint.setColor(Color.BLUE);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth((float) 4);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		return output;
	}

	/**
	 * This will crop supplied bitmap to make a circular image with a white
	 * outer ring
	 * 
	 * @param bitmap
	 * @param padding
	 *            padding around original bitmap to ignore
	 * @return
	 */
	public static Bitmap getCircleBitmap(Bitmap bitmap, int targetWidth, int strokeWidth, int padding) {
		// 1--init target drawing area
		Bitmap output = Bitmap.createBitmap(targetWidth, targetWidth, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		final Paint paint = new Paint();
		final Rect rect = new Rect(1, 1, output.getWidth() - 1, output.getHeight() - 1);
		final RectF rectF = new RectF(rect);
		// 2--defines drawing area of the original bitmap
		final Rect originalRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

		// 3--set bound for circle
		final Rect circleRect = new Rect(padding, padding, targetWidth - padding, targetWidth - padding);
		final RectF circleRectF = new RectF(circleRect);

		// 4--init paint
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setFilterBitmap(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(Color.RED);

		// 5--draws a solid circle in the middle
		canvas.drawOval(rectF, paint);
		// 6--use original bitmap image to invert solid circle
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, originalRect, rect, paint);

		// 6--reset paint for drawing outer ring
		paint.setStyle(Paint.Style.STROKE);
		paint.setXfermode(null);
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(strokeWidth);
		// 7--draws ring
		canvas.drawOval(circleRectF, paint);
		return output;
	}
}
