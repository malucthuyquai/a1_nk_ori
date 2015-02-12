package com.fuhu.nabiconnect.friend.dialog;


import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class PopupDialog extends Dialog{

	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "PopupDialog";
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	
	/*======================================================================
	 * Constructors
	 *=======================================================================*/
	public PopupDialog(Context context) {
		this(context, R.style.Theme_Transparent);	
	}
	public PopupDialog(Context context, int themeTransparent) {
		super(context, themeTransparent);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.m_Context = context;
	}
	
	/*======================================================================
	 * Add listners for button
	 *=======================================================================*/
	public void notifyButtonListeners(int buttonID, String tag, Object[] args)
	{
		if(m_ButtonListeners != null)
			for(IButtonClickListener listener : m_ButtonListeners)
				listener.onButtonClicked(buttonID, tag, args);
	}
	
	/*======================================================================
	 * Notify button listeners
	 *=======================================================================*/
	public void addButtonListener(IButtonClickListener listener)
	{
		if(m_ButtonListeners == null)
			m_ButtonListeners = new ArrayList<IButtonClickListener>();
			
		m_ButtonListeners.add(listener);
	}	

}
