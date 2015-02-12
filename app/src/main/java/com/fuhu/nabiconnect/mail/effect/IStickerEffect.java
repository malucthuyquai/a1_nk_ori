package com.fuhu.nabiconnect.mail.effect;

import java.util.ArrayList;

public abstract class IStickerEffect extends Effect{
	
	public ArrayList<Integer> m_StickerList;
	
	public abstract ArrayList<Integer> getStickerResId();
}
