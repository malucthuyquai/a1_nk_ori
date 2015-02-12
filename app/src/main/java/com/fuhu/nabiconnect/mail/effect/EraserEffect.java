package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class EraserEffect extends Effect{
	
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
		if(m_SubItems == null)
		{
			m_SubItems = new  ArrayList<Effect>();
			m_SubItems.add(new EraserEffectAll());
			m_SubItems.add(new EraserEffectBold());
			m_SubItems.add(new EraserEffectThin());		
		}
		return m_SubItems;
	}
}
