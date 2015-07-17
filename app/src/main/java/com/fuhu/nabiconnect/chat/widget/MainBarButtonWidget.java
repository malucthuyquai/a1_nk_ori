package com.fuhu.nabiconnect.chat.widget;

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

public class MainBarButtonWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "MainBarButtonWidget";
	private Context m_Context;
	private RelativeLayout m_BackgroundContainer;
	private ImageView m_ButtonImage;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private int m_BackgroundId;
	private int m_IconReleasedId;
	private int m_IconPressedId;
	
	public static final int BUTTON_ID = 100;
	
	public MainBarButtonWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.chat_mainbar_button_widget, this);
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.button_container);
		m_ButtonImage = (ImageView)m_BackgroundContainer.findViewById(R.id.button_image);


		
		this.setOnTouchListener(new View.OnTouchListener() {
			
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
						notifyButtonListeners(BUTTON_ID, TAG, null);
						return true;
				}			
				return false;
			}
		});
	}
	
	public void setInformation(int backgroundResId, int iconReleasedResId, int iconSelectedResId)
	{	
		m_BackgroundId = backgroundResId;
		m_IconReleasedId = iconReleasedResId;
		m_IconPressedId = iconSelectedResId;
	}
	
	public void setSelected(boolean isSelected)
	{
		if(isSelected)
		{
			m_BackgroundContainer.setBackgroundResource(m_BackgroundId);
			m_ButtonImage.setImageResource(m_IconPressedId);
		}
		else
		{
			m_BackgroundContainer.setBackgroundDrawable(null);
			m_ButtonImage.setImageResource(m_IconReleasedId);
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
			
		m_ButtonListeners.add(listener);
	}
	
	
}
