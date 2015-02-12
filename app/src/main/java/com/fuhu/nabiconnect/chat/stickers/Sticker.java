package com.fuhu.nabiconnect.chat.stickers;

public class Sticker {

	private int id;
	private int resId;
	
	public Sticker(int id, int resId)
	{
		this.id = id;
		this.resId = resId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getResId() {
		return resId;
	}

	public void setResId(int resId) {
		this.resId = resId;
	}
	
	
}
