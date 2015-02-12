package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

public abstract class IPaintThinEffect extends IPaintEffect{
	
	@Override
	public Paint getPaint()
	{
		super.getPaint();
		m_Paint.setStrokeWidth(10);
		
		return m_Paint;
	}
}
