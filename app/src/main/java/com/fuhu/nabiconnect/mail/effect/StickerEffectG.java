package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectG extends IStickerEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_sticker_g;
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
			m_StickerList.add(R.drawable.sticker_g_01);
			m_StickerList.add(R.drawable.sticker_g_02);
			m_StickerList.add(R.drawable.sticker_g_03);
			m_StickerList.add(R.drawable.sticker_g_04);
			m_StickerList.add(R.drawable.sticker_g_05);
			m_StickerList.add(R.drawable.sticker_g_06);
			m_StickerList.add(R.drawable.sticker_g_07);
			m_StickerList.add(R.drawable.sticker_g_08);
			m_StickerList.add(R.drawable.sticker_g_09);
			m_StickerList.add(R.drawable.sticker_g_10);
		}
	
		return m_StickerList;
	}

    @Override
    public String getTrack() {
        return "sticker_g";
    }
}
