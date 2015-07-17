package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Paint;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class EraserEffectBold extends IEraserEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_eraser;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public Paint getPaint()
	{
		super.getPaint();
		
		m_Paint.setStrokeWidth(100);
		
		return m_Paint;
	}

    @Override
    public String getTrack() {
        return "eraser_tool_eraser_bold";
    }
}
