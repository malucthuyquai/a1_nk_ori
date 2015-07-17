package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectE extends IStickerEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_sticker_e;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}
	
	@Override
	public ArrayList<Integer> getStickerResId() {
		
		if(m_StickerList == null)
		{
			m_StickerList = new ArrayList<Integer>();
			m_StickerList.add(R.drawable.sticker_e_01);
			m_StickerList.add(R.drawable.sticker_e_02);
			m_StickerList.add(R.drawable.sticker_e_03);
			m_StickerList.add(R.drawable.sticker_e_04);
			m_StickerList.add(R.drawable.sticker_e_05);
			m_StickerList.add(R.drawable.sticker_e_06);
			m_StickerList.add(R.drawable.sticker_e_07);
			m_StickerList.add(R.drawable.sticker_e_08);
		}
	
		return m_StickerList;
	}

    @Override
    public String getTrack() {
        return "sticker_e";
    }
}
