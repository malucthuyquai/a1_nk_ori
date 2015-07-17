package com.fuhu.nabiconnect.mail.effect;

import android.util.Pair;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class WallPaperSceneEffect extends IMultipleWallPaperEffect{

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon16;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}

	@Override
	public ArrayList<Pair<Integer, Integer>> getWallPaperResId() {
		if(m_WallPaperList == null)
		{
			m_WallPaperList = new ArrayList<Pair<Integer, Integer>>();
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene01, R.drawable.mail_wallpaper_thumb_scene01));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene02, R.drawable.mail_wallpaper_thumb_scene02));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene03, R.drawable.mail_wallpaper_thumb_scene03));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene04, R.drawable.mail_wallpaper_thumb_scene04));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene05, R.drawable.mail_wallpaper_thumb_scene05));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene06, R.drawable.mail_wallpaper_thumb_scene06));
			m_WallPaperList.add(new Pair(R.drawable.mail_wallpaper_scene07, R.drawable.mail_wallpaper_thumb_scene07));
		}
		
		return m_WallPaperList;
	}

    @Override
    public String getTrack() {
        return "change_background_scene";
    }
}
