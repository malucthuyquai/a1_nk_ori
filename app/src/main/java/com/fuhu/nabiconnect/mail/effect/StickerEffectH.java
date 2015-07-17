package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectH extends IStickerEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_sticker_h;
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
			m_StickerList.add(R.drawable.sticker_h_01);
			m_StickerList.add(R.drawable.sticker_h_02);
			m_StickerList.add(R.drawable.sticker_h_03);
			m_StickerList.add(R.drawable.sticker_h_04);
			m_StickerList.add(R.drawable.sticker_h_05);
			m_StickerList.add(R.drawable.sticker_h_06);
			m_StickerList.add(R.drawable.sticker_h_07);
			m_StickerList.add(R.drawable.sticker_h_08);
			m_StickerList.add(R.drawable.sticker_h_09);
			m_StickerList.add(R.drawable.sticker_h_10);
			m_StickerList.add(R.drawable.sticker_h_11);
			m_StickerList.add(R.drawable.sticker_h_12);
			m_StickerList.add(R.drawable.sticker_h_13);
			m_StickerList.add(R.drawable.sticker_h_14);
			m_StickerList.add(R.drawable.sticker_h_15);
			m_StickerList.add(R.drawable.sticker_h_16);
		}
	
		return m_StickerList;
	}

    @Override
    public String getTrack() {
        return "sticker_h";
    }
}
