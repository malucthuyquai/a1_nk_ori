package com.fuhu.nabiconnect.mail.dialog;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.widget.MailChooseContactWidget;
import com.fuhu.ndnslibsoutstructs.friends_outObj;

import java.util.ArrayList;

public class ChooseContactDialog extends PopupDialog {
	

	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "ChooseContactDialog";
	
	public static final int CANCEL_BUTTON_ID = 100;
	public static final int YES_BUTTON_ID = 101;
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private Button m_YesButton;
	private Button m_CancelButton;
	private TableLayout m_ContactTable;
	private ArrayList<MailChooseContactWidget> m_ContactWidgetList;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private friends_outObj m_FriendList;


	
	public ChooseContactDialog(Context context, friends_outObj friendList)
	{
		super(context);
		this.m_Context = context;
		this.m_FriendList = friendList;
				
		setContentView(R.layout.mail_choose_contact_dialog);

		m_YesButton = (Button)this.findViewById(R.id.choose_contact_ok_button);
		m_CancelButton = (Button)this.findViewById(R.id.choose_contact_cancel_button);
		m_ContactTable = (TableLayout)this.findViewById(R.id.choose_contact_table);

		
		if(m_YesButton != null)
			m_YesButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					ArrayList<FriendData> friendList = new ArrayList<FriendData>();
					for(MailChooseContactWidget widget : m_ContactWidgetList)
					{
						if(widget.getIsChoosen())
							friendList.add(widget.getFriendData());
					}
					cancelWidgetLoadingAvatar();
					notifyButtonListeners(YES_BUTTON_ID, TAG, new Object[]{friendList});
					
					// disable the button
					m_YesButton.setEnabled(false);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_choose_contact_send_message");
				}
			});
		if(m_CancelButton != null)
			m_CancelButton.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					cancelWidgetLoadingAvatar();
					notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_choose_contact_cancel");
				}
			});
		
		loadContact();

	}

	private void loadContact()
	{
		m_ContactWidgetList = new ArrayList<MailChooseContactWidget>();
		m_ContactTable.removeAllViews();
		
		TableRow tableRow = new TableRow(m_Context);	
		
		if(m_FriendList == null)
		{
			LOG.V(TAG,"loadContact() - m_FriendList is null");
			return;
		}
		
		for(FriendData fData : m_FriendList.getFriends())
		{
			MailChooseContactWidget widget = new MailChooseContactWidget(m_Context, fData);
			m_ContactWidgetList.add(widget);
			tableRow.addView(widget);		
		}
		m_ContactTable.addView(tableRow);
	}
	
	private void cancelWidgetLoadingAvatar()
	{
		if(m_ContactWidgetList != null)
			for(MailChooseContactWidget widget : m_ContactWidgetList)
				widget.cancelLoadingAvater();
	}
	
	
	
	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
		
		cancelWidgetLoadingAvatar();
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


