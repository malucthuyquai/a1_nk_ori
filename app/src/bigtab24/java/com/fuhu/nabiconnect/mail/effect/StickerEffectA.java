package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffectA extends IStickerEffect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_sticker_a;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        return null;
    }

    @Override
    public ArrayList<Integer> getStickerResId() {

        if (m_StickerList == null) {
            m_StickerList = new ArrayList<Integer>();
//			m_StickerList.add(R.drawable.sticker_a_01);
//			m_StickerList.add(R.drawable.sticker_a_02);
//			m_StickerList.add(R.drawable.sticker_a_03);
//			m_StickerList.add(R.drawable.sticker_a_04);
//			m_StickerList.add(R.drawable.sticker_a_05);
//			m_StickerList.add(R.drawable.sticker_a_06);
//			m_StickerList.add(R.drawable.sticker_a_07);
//			m_StickerList.add(R.drawable.sticker_a_08);
//			m_StickerList.add(R.drawable.sticker_a_09);
//			m_StickerList.add(R.drawable.sticker_a_10);
//			m_StickerList.add(R.drawable.sticker_a_11);
//			m_StickerList.add(R.drawable.sticker_a_12);
//			m_StickerList.add(R.drawable.sticker_a_13);


        }


        return m_StickerList;
    }

}
