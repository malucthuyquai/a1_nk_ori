package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PaintEffectBrownBold extends IPaintBoldEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon51;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public Paint getPaint() {
		super.getPaint();
		
		// for drawing path
		m_Paint.setColor(0xff7a2b1f);
		
		return m_Paint;
	}

    @Override
    public String getTrack() {
        return "crayon_tool_brown_bold";
    }
}
