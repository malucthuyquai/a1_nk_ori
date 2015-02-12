package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

public abstract class IPaintEffect extends Effect{
	
	Paint m_Paint = new Paint();

	public Paint getPaint()
	{
		m_Paint.setXfermode(null);
		m_Paint.setAlpha(0xFF);
		m_Paint.setStyle(Paint.Style.STROKE);
		m_Paint.setStrokeJoin(Paint.Join.ROUND);
		m_Paint.setStrokeCap(Paint.Cap.ROUND);
		
		return m_Paint;
	}
}
