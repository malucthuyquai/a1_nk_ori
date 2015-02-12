package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class StickerEffect extends Effect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_icon_sticker;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        if (m_SubItems == null) {
            m_SubItems = new ArrayList<Effect>();
            //m_SubItems.add(new StickerEffectA());
            //m_SubItems.add(new StickerEffectB());
            //m_SubItems.add(new StickerEffectC());
            //m_SubItems.add(new StickerEffectD());
            m_SubItems.add(new StickerEffectE());
            m_SubItems.add(new StickerEffectF());
            m_SubItems.add(new StickerEffectG());
            m_SubItems.add(new StickerEffectH());
        }
        return m_SubItems;
    }
}
