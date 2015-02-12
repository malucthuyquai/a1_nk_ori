package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectF extends IStickerEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_sticker_f;
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
			m_StickerList.add(R.drawable.sticker_f_01);
			m_StickerList.add(R.drawable.sticker_f_02);
			m_StickerList.add(R.drawable.sticker_f_03);
			m_StickerList.add(R.drawable.sticker_f_04);
			m_StickerList.add(R.drawable.sticker_f_05);
			m_StickerList.add(R.drawable.sticker_f_06);
			m_StickerList.add(R.drawable.sticker_f_07);
			m_StickerList.add(R.drawable.sticker_f_08);
			m_StickerList.add(R.drawable.sticker_f_09);
			m_StickerList.add(R.drawable.sticker_f_10);
			m_StickerList.add(R.drawable.sticker_f_11);
			m_StickerList.add(R.drawable.sticker_f_12);
			m_StickerList.add(R.drawable.sticker_f_13);
			m_StickerList.add(R.drawable.sticker_f_14);
			m_StickerList.add(R.drawable.sticker_f_15);
			m_StickerList.add(R.drawable.sticker_f_16);
			m_StickerList.add(R.drawable.sticker_f_17);
			m_StickerList.add(R.drawable.sticker_f_18);
		}
	
		return m_StickerList;
	}
}
