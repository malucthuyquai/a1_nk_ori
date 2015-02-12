package com.fuhu.nabiconnect.utils;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;

import java.util.ArrayList;

public class TwoButtonsDialog extends PopupDialog  {
	/*======================================================================
	 *nabiMode_PopUps_Welcome
	 *=======================================================================*/
	public static final String TAG = "TwoButtonsDialog";

	public static final int CLOSE_BUTTON_ID = 100;
	public static final int OK_BUTTON_ID = 101;	
	public static final int X_BUTTON_ID = 102;	


	private Context m_Context;
	private Button m_AcceptButton;
	private Button m_DeclineButton;
	private Button m_XButton;
	private ArrayList<IButtonClickListener> m_ButtonListeners;

	private TextView m_DialogTitle;
	private TextView m_DialogDescription;
	private ImageView m_DialogIcon;

	public TwoButtonsDialog(Context context)
	{
		super(context);
		this.m_Context = context;
				
		setContentView(R.layout.mail_create_user_name_dialog);

		m_AcceptButton = (Button)this.findViewById(R.id.friend_dialog_submit_icon);
		m_DeclineButton = (Button)this.findViewById(R.id.friend_dialog_cancel_icon);
		m_XButton = (Button)this.findViewById(R.id.friend_dialog_x_icon);
		m_DialogTitle = (TextView)this.findViewById(R.id.friend_dialog_title);
		m_DialogDescription = (TextView)this.findViewById(R.id.friend_dialog_context);
		m_DialogIcon = (ImageView)this.findViewById(R.id.connect_mail_main_icon);
		
		//set font
		Typeface tf = Typeface.createFromAsset(m_Context.getAssets(),"fonts/Roboto-Medium.ttf");
		m_DialogTitle.setTypeface(tf,Typeface.NORMAL);
		m_AcceptButton.setTypeface(tf,Typeface.NORMAL);
		m_DeclineButton.setTypeface(tf,Typeface.NORMAL);
		
		
		if(m_AcceptButton != null)
			m_AcceptButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(OK_BUTTON_ID, TAG, null);
				}
			});
		if(m_DeclineButton != null)
			m_DeclineButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(CLOSE_BUTTON_ID, TAG, null);
				}
			});
		
		if(m_XButton != null)
			m_XButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(X_BUTTON_ID, TAG, null);
				}
			});
	}
	
	public void setDialogInfo(String title, String description, int iconResId, String accept, String decline)
	{
		m_DialogTitle.setText(title);
		m_DialogDescription.setText(description);
		m_DialogIcon.setImageResource(iconResId);
		m_AcceptButton.setText(accept);
		m_DeclineButton.setText(decline);		
	}
	
	public void setIconMarginTop(int dimen)
	{
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams)m_DialogIcon.getLayoutParams();
		param.topMargin = dimen;
		m_DialogIcon.requestLayout();
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
