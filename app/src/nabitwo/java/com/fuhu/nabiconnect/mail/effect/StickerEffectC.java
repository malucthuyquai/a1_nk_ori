package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

import java.util.ArrayList;

public class StickerEffectC extends IStickerEffect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_sticker_c;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        return null;
    }

    @Override
    public ArrayList<Integer> getStickerResId() {

        if (m_StickerList == null) {
            m_StickerList = new ArrayList<Integer>();
//			m_StickerList.add(R.drawable.sticker_c_01);
//			m_StickerList.add(R.drawable.sticker_c_02);
//			m_StickerList.add(R.drawable.sticker_c_03);
//			m_StickerList.add(R.drawable.sticker_c_04);
//			m_StickerList.add(R.drawable.sticker_c_05);
//			m_StickerList.add(R.drawable.sticker_c_06);
//			m_StickerList.add(R.drawable.sticker_c_07);
//			m_StickerList.add(R.drawable.sticker_c_08);
//			m_StickerList.add(R.drawable.sticker_c_09);
//			m_StickerList.add(R.drawable.sticker_c_10);
//			m_StickerList.add(R.drawable.sticker_c_11);
//			m_StickerList.add(R.drawable.sticker_c_12);
//			m_StickerList.add(R.drawable.sticker_c_13);
//			m_StickerList.add(R.drawable.sticker_c_14);
        }

        return m_StickerList;
    }

    @Override
    public String getTrack() {
        return Tracking.TRACK_PHOTO_STICKER_EFFECT_C;
    }
}
