package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class WallPaperEffect extends Effect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_wallpaper;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		if(m_SubItems == null)
		{
			m_SubItems = new  ArrayList<Effect>();
			m_SubItems.add(new WallPaperEffectDefault());
			m_SubItems.add(new WallPaperColorEffect());
			m_SubItems.add(new WallPaperSceneEffect());		
			m_SubItems.add(new WallPaperTextureEffect());
		}
		return m_SubItems;
	}
}
