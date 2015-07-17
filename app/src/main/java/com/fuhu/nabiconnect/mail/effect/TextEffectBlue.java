package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Color;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class TextEffectBlue extends Effect implements ITextEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon33;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public int getTextColor() {
		// TODO Auto-generated method stub
		return Color.parseColor("#2CCDE8");
	}
	
	@Override
	public int getSelectedID() {
		// TODO Auto-generated method stub
		return 4;
	}

    @Override
    public String getTrack() {
        return "text_tool_blue";
    }
}
