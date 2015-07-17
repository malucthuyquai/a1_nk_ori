package com.fuhu.nabiconnect.mail.effect;

import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class WallPaperEffectDefault extends ISingleWallPaperEffect {

	@Override
	public void apply() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int getEffectIconRes() {
		return R.drawable.mail_icon_eraser_all;
	}

	@Override
	public ArrayList<Effect> getSubItems() {
		return null;
	}

	@Override
	public int getWallPaperResId() {
		return R.drawable.mail_wallpaper_default;
	}

    @Override
    public String getTrack() {
        return "change_background_remove_background";
    }
}
