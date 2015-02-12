package com.fuhu.nabiconnect.mail.effect;

import android.graphics.Color;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class TextEffectBlueDark extends Effect implements ITextEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon34;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public int getTextColor() {
		// TODO Auto-generated method stub
		return Color.parseColor("#0B7DB9");
	}
	
	@Override
	public int getSelectedID() {
		// TODO Auto-generated method stub
		return 5;
	}
}
