package com.fuhu.nabiconnect.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class ConnectItemWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "ConnectItemWidget";
	private Context m_Context;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private ImageView m_MainImage;
	private TextView m_ItemText;
	private ImageView m_SubImage;
	private RelativeLayout m_UnreadIndicatorContainer;
	private TextView m_UnreadText;
	private Button m_ItemButton;
	
	public static final int BUTTON_ID = 100;
	
	public ConnectItemWidget(Context context) {
		super(context, null);
	}
	public ConnectItemWidget(Context context, AttributeSet attrs) {
		super(context, attrs);

		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.connect_item_widget, this);
		
		m_MainImage = (ImageView)this.findViewById(R.id.connect_item_main_image);
		m_ItemText = (TextView)this.findViewById(R.id.connect_item_text);
		m_SubImage = (ImageView)this.findViewById(R.id.connect_item_sub_image);
		m_UnreadIndicatorContainer = (RelativeLayout)this.findViewById(R.id.mail_inbox_unread_indicator);
		m_UnreadText = (TextView)this.findViewById(R.id.mail_unread_number);
		m_ItemButton = (Button)this.findViewById(R.id.connect_item_button);
		
		m_ItemButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				notifyButtonListeners(BUTTON_ID, TAG, null);
			}
		});
	}
	
	public void setInfomation(int mainImageResId, String itemString, int subImageResId)
	{
		m_MainImage.setImageResource(mainImageResId);
		m_ItemText.setText(itemString);
		m_SubImage.setImageResource(subImageResId);
	}
	
	public void setUnreadCount(int count)
	{
		if(count > 0)
		{
			m_UnreadIndicatorContainer.setVisibility(View.VISIBLE);
			if(count < 100)
				m_UnreadText.setText(String.valueOf(count));
			else
				m_UnreadText.setText("99+");
		}
		else
		{
			m_UnreadIndicatorContainer.setVisibility(View.INVISIBLE);
		}
		
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
			
		if(!m_ButtonListeners.contains(listener))
			m_ButtonListeners.add(listener);
	}
	
	
}
