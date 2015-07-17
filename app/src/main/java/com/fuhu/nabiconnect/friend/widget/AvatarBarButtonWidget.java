package com.fuhu.nabiconnect.friend.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class AvatarBarButtonWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "AvatarBarButtonWidget";
	
	public static final int BACKGROUND_SELECTED = R.drawable.friend_hover_blue;
	
	private Context m_Context;
	private RelativeLayout m_BackgroundContainer;
	private ImageView m_ItemIcon;
	private int m_Type;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	
	public static final int BUTTON_ID = 100;
	
	public AvatarBarButtonWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.friend_avatar_bar_button_widget, this);
		
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.edit_avatar_bar_button_background);
		m_ItemIcon = (ImageView)m_BackgroundContainer.findViewById(R.id.edit_avatar_bar_icon);

		
		m_BackgroundContainer.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent mv) {
				if(mv.getX() < 0 || mv.getX() > view.getWidth() || mv.getY() < 0 || mv.getY() > view.getHeight())
				{
					//m_Background.setBackgroundResource(m_BackgroundId);
					return false;
				}			
				switch(mv.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						//m_Background.setBackgroundResource(m_BackgroundPressedId);
						return true;
					case MotionEvent.ACTION_UP:
						//m_Background.setBackgroundResource(m_BackgroundId);
						notifyButtonListeners(BUTTON_ID, TAG, new Object[]{m_Type});
						return true;
				}

				return false;
			}
		});
		
	}
	
	public void setInformation(int typeId, int typeIconResId)
	{	
		m_Type = typeId;
		m_ItemIcon.setImageResource(typeIconResId);
	}
	
	public void setSelected(boolean isSelected)
	{
		if(isSelected)
		{
			m_BackgroundContainer.setBackgroundResource(BACKGROUND_SELECTED);
		}
		else
		{
			m_BackgroundContainer.setBackgroundDrawable(null);
		}
	}
	
	public void onButtonSelected(int seledtedId)
	{
		setSelected(seledtedId == this.m_Type);
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
