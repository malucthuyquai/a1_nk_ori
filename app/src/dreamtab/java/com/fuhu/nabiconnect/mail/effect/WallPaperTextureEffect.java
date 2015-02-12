package com.fuhu.nabiconnect.mail.effect;

import android.util.Pair;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class WallPaperTextureEffect extends IMultipleWallPaperEffect {

    @Override
    public void apply() {
        // TODO Auto-generated method stub

    }

    @Override
    public int getEffectIconRes() {
        return R.drawable.mail_icon17;
    }

    @Override
    public ArrayList<Effect> getSubItems() {
        return null;
    }

    @Override
    public ArrayList<Pair<Integer, Integer>> getWallPaperResId() {
        if (m_WallPaperList == null) {
            m_WallPaperList = new ArrayList<Pair<Integer, Integer>>();
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture01, R.drawable.mail_wallpaper_thumb_texture01));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture02, R.drawable.mail_wallpaper_thumb_texture02));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture03, R.drawable.mail_wallpaper_thumb_texture03));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture04, R.drawable.mail_wallpaper_thumb_texture04));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture05, R.drawable.mail_wallpaper_thumb_texture05));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture06, R.drawable.mail_wallpaper_thumb_texture06));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture07, R.drawable.mail_wallpaper_thumb_texture07));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture08, R.drawable.mail_wallpaper_thumb_texture08));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture09, R.drawable.mail_wallpaper_thumb_texture09));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture10, R.drawable.mail_wallpaper_thumb_texture10));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture11, R.drawable.mail_wallpaper_thumb_texture11));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture12, R.drawable.mail_wallpaper_thumb_texture12));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture13, R.drawable.mail_wallpaper_thumb_texture13));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture14, R.drawable.mail_wallpaper_thumb_texture14));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture15, R.drawable.mail_wallpaper_thumb_texture15));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture16, R.drawable.mail_wallpaper_thumb_texture16));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture17, R.drawable.mail_wallpaper_thumb_texture17));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture18, R.drawable.mail_wallpaper_thumb_texture18));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture19, R.drawable.mail_wallpaper_thumb_texture19));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture20, R.drawable.mail_wallpaper_thumb_texture20));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture21, R.drawable.mail_wallpaper_thumb_texture21));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture22, R.drawable.mail_wallpaper_thumb_texture22));
            m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_texture23, R.drawable.mail_wallpaper_thumb_texture23));

        }

        return m_WallPaperList;
    }
}
