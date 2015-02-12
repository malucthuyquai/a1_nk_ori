package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectD extends IStickerEffect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_sticker_d;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        return null;
    }

    @Override
    public ArrayList<Integer> getStickerResId() {

        if (m_StickerList == null) {
            m_StickerList = new ArrayList<Integer>();
            m_StickerList.add(R.drawable.sticker_d_01);
            m_StickerList.add(R.drawable.sticker_d_02);
            m_StickerList.add(R.drawable.sticker_d_03);
            m_StickerList.add(R.drawable.sticker_d_04);
            m_StickerList.add(R.drawable.sticker_d_05);
            m_StickerList.add(R.drawable.sticker_d_06);
            m_StickerList.add(R.drawable.sticker_d_07);
            m_StickerList.add(R.drawable.sticker_d_08);
            m_StickerList.add(R.drawable.sticker_d_09);
            m_StickerList.add(R.drawable.sticker_d_10);
        }

        return m_StickerList;
    }
}
