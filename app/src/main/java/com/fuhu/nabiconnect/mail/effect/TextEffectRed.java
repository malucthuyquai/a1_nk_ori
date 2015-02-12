package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Color;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class TextEffectRed extends Effect implements ITextEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon29;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public int getTextColor() {
		// TODO Auto-generated method stub
		return Color.parseColor("#E91C2F");
	}
	
	@Override
	public int getSelectedID() {
		// TODO Auto-generated method stub
		return 0;
	}
}
