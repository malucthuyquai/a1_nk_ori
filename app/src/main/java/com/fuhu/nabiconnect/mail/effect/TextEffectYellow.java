package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Color;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class TextEffectYellow extends Effect implements ITextEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon31;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public int getTextColor() {
		// TODO Auto-generated method stub
		return Color.parseColor("#F9EB56");
	}
	
	@Override
	public int getSelectedID() {
		// TODO Auto-generated method stub
		return 2;
	}

    @Override
    public String getTrack() {
        return "text_tool_yellow";
    }
}
