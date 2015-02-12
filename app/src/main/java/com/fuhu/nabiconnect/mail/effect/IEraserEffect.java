package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public abstract class IEraserEffect extends Effect{
	
	Paint m_Paint = new Paint();

	public Paint getPaint()
	{
		m_Paint.setXfermode(null);
		m_Paint.setAlpha(0x00);
		m_Paint.setColor(Color.TRANSPARENT);
		m_Paint.setStyle(Paint.Style.STROKE);
		m_Paint.setStrokeJoin(Paint.Join.ROUND);
		m_Paint.setStrokeCap(Paint.Cap.ROUND);
		m_Paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		
		return m_Paint;
	}
}
