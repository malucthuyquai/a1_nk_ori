package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

import java.util.ArrayList;

public class StickerEffectB extends IStickerEffect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_sticker_b;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        return null;
    }

    @Override
    public ArrayList<Integer> getStickerResId() {

        if (m_StickerList == null) {
            m_StickerList = new ArrayList<Integer>();
//			m_StickerList.add(R.drawable.sticker_b_01);
//			m_StickerList.add(R.drawable.sticker_b_02);
//			m_StickerList.add(R.drawable.sticker_b_03);
//			m_StickerList.add(R.drawable.sticker_b_04);
//			m_StickerList.add(R.drawable.sticker_b_05);
//			m_StickerList.add(R.drawable.sticker_b_06);
//			m_StickerList.add(R.drawable.sticker_b_07);
//			m_StickerList.add(R.drawable.sticker_b_08);
//			m_StickerList.add(R.drawable.sticker_b_09);
//			m_StickerList.add(R.drawable.sticker_b_10);
        }

        return m_StickerList;
    }

    @Override
    public String getTrack() {
        return Tracking.TRACK_PHOTO_STICKER_EFFECT_B;
    }
}
