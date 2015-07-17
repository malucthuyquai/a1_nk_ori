package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PaintEffectGrayThin extends IPaintThinEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon56;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public Paint getPaint() {
		super.getPaint();
		
		// for drawing path
		m_Paint.setColor(0xff8b8b8b);
		
		return m_Paint;
	}

    @Override
    public String getTrack() {
        return "gray_thin";
    }
}
