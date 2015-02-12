package com.fuhu.nabiconnect.utils.stickerwidget;

public interface StickerButtonListener {
	/**
	 * Called when an ACTION_UP event is received on the check button.
	 * Parent should use this to hide control around the widget
	 * @param sw
	 */
	public void onClick(StickerWidget sw);

	/**
	 * Called when receiving ACTION_DOWN touch event. Passes back the identifier of
	 * this StickerWidget assigned by its parent
	 * 
	 * @param id
	 */
	public void onGainFocus(int index);
}
