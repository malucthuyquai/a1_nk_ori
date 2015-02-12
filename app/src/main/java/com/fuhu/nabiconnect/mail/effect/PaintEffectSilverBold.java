package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PaintEffectSilverBold extends IPaintBoldEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon57;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public Paint getPaint() {
		super.getPaint();
		
		// for drawing path
		m_Paint.setColor(0xffffffff);
		
		return m_Paint;
	}
}
