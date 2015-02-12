package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

public abstract class IPaintBoldEffect extends IPaintEffect{
	
	@Override
	public Paint getPaint()
	{
		super.getPaint();
		m_Paint.setStrokeWidth(20);
		
		return m_Paint;
	}
}
