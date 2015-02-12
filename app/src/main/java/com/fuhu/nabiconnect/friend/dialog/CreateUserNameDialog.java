package com.fuhu.nabiconnect.friend.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;



public class CreateUserNameDialog extends PopupDialog {
/*======================================================================
 *nabiMode_PopUps_Welcome
 *=======================================================================*/
public static final String TAG = "CreateUserNameDialog";

public static final int CLOSE_BUTTON_ID = 100;
public static final int CREATE_BUTTON_ID = 101;	
public static final int X_BUTTON_ID = 102;	


private Context m_Context;
private Button m_RightButton;
private Button m_LeftButton;
private Button m_XButton;
private ArrayList<IButtonClickListener> m_ButtonListeners;

private TextView m_friend_dialog_title;
private TextView m_friend_dialog_context;
private ImageView m_connect_mail_main_icon;

public CreateUserNameDialog(Context context)
{
	super(context);
	this.m_Context = context;
			
	setContentView(R.layout.mail_create_user_name_dialog);

	m_RightButton = (Button)this.findViewById(R.id.friend_dialog_submit_icon);
	m_LeftButton = (Button)this.findViewById(R.id.friend_dialog_cancel_icon);
	m_XButton = (Button)this.findViewById(R.id.friend_dialog_x_icon);
	
	m_friend_dialog_title = (TextView)this.findViewById(R.id.friend_dialog_title);
	m_friend_dialog_context = (TextView)this.findViewById(R.id.friend_dialog_context);
	m_connect_mail_main_icon = (ImageView)this.findViewById(R.id.connect_mail_main_icon);
	
	//set font
	Typeface tf = Typeface.createFromAsset(m_Context.getAssets(),"fonts/Roboto-Medium.ttf");
	m_friend_dialog_title.setTypeface(tf,Typeface.NORMAL);
	m_RightButton.setTypeface(tf,Typeface.NORMAL);
	m_LeftButton.setTypeface(tf,Typeface.NORMAL);
	
	if(m_RightButton != null)
		m_RightButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				notifyButtonListeners(CREATE_BUTTON_ID, TAG, null);
			}
		});
	if(m_LeftButton != null)
		m_LeftButton.setOnClickListener(new View.OnClickListener() {
			
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
