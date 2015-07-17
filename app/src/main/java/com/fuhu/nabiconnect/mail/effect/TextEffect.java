package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class TextEffect extends Effect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_text;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		if(m_SubItems == null)
		{
			m_SubItems = new  ArrayList<Effect>();
			m_SubItems.add(new TextEffectRed());
			m_SubItems.add(new TextEffectOrange());
			m_SubItems.add(new TextEffectYellow());		
			m_SubItems.add(new TextEffectGreen());
			m_SubItems.add(new TextEffectBlue());
			m_SubItems.add(new TextEffectBlueDark());	
			m_SubItems.add(new TextEffectPurple());	
		}
		return m_SubItems;
	}

    @Override
    public String getTrack() {
        return "text_tool";
    }
}
