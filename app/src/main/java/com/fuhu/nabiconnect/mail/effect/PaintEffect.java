package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PaintEffect extends Effect{

	//private ArrayList<Effect> m_SubItems;
	
	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_red_bold;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		if(m_SubItems == null)
		{
			m_SubItems = new  ArrayList<Effect>();
			m_SubItems.add(new PaintEffectRedBold());
			m_SubItems.add(new PaintEffectRedThin());
			m_SubItems.add(new PaintEffectOrangeBold());
			m_SubItems.add(new PaintEffectOrangeThin());
			
			m_SubItems.add(new PaintEffectYellowBold());
			m_SubItems.add(new PaintEffectYellowThin());
			m_SubItems.add(new PaintEffectGreenBold());
			m_SubItems.add(new PaintEffectGreenThin());
			
			m_SubItems.add(new PaintEffectDarkGreenBold());
			m_SubItems.add(new PaintEffectDarkGreenThin());
			m_SubItems.add(new PaintEffectBlueBold());
			m_SubItems.add(new PaintEffectBlueThin());
			
			m_SubItems.add(new PaintEffectDarkBlueBold());
			m_SubItems.add(new PaintEffectDarkBlueThin());
			m_SubItems.add(new PaintEffectPurpleBold());
			m_SubItems.add(new PaintEffectPurpleThin());
			
			m_SubItems.add(new PaintEffectBrownBold());
			m_SubItems.add(new PaintEffectBrownThin());
			m_SubItems.add(new PaintEffectBlackBold());
			m_SubItems.add(new PaintEffectBlackThin());
			
			m_SubItems.add(new PaintEffectGrayBold());
			m_SubItems.add(new PaintEffectGrayThin());
			m_SubItems.add(new PaintEffectSilverBold());
			m_SubItems.add(new PaintEffectSilverThin());
			
			
		}
		return m_SubItems;
	}
}
