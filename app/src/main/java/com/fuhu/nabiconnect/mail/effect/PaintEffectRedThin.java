package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PaintEffectRedThin extends IPaintThinEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_red_thin;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public Paint getPaint() {
		super.getPaint();

		// for drawing path
		m_Paint.setColor(0xffe91c2f);
		
		return m_Paint;
	}

    @Override
    public String getTrack() {
        return "crayon_tool_red_thin";
    }
}
