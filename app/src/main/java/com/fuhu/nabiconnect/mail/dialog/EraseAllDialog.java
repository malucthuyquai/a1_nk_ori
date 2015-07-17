package com.fuhu.nabiconnect.mail.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;

import java.util.ArrayList;

public class EraseAllDialog extends PopupDialog {
	

	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "EraseAllDialog";
	
	public static final int CANCEL_BUTTON_ID = 100;
	public static final int YES_BUTTON_ID = 101;
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private Button m_YesButton;
	private Button m_CancelButton;
	private ArrayList<IButtonClickListener> m_ButtonListeners;


	
	public EraseAllDialog(Context context)
	{
		super(context);
		this.m_Context = context;
				
		setContentView(R.layout.mail_erase_all_dialog);

		m_YesButton = (Button)this.findViewById(R.id.erase_all_ok_button);
		m_CancelButton = (Button)this.findViewById(R.id.erase_all_cancel_button);

		
		if(m_YesButton != null)
			m_YesButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(YES_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_erase_all_erase_all");
				}
			});
		if(m_CancelButton != null)
			m_CancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_erase_all_close");
				}
			});


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


