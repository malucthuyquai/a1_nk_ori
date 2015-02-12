package com.fuhu.nabiconnect.chat.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;

public class ShopBarButtonWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "ShopBarButtonWidget";
	
	
	public static final int BACKGROUND_SELECTED = R.drawable.chat_top_bar_three1;
	
	public static final int SELECTED_STYLE = R.style.chat_primary_l;
	public static final int RELEASED_STYLE = R.style.chat_primary_l_light_orange;
	private Context m_Context;
	private RelativeLayout m_BackgroundContainer;
	private TextView m_ItemText;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	
	public static final int BUTTON_ID = 100;
	
	public ShopBarButtonWidget(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		
		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.chat_shop_bar_widget, this);
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.shop_bar_background);
		m_ItemText = (TextView)m_BackgroundContainer.findViewById(R.id.shop_bar_text);
		//m_ItemText.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Bold.otf"));

		
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
						notifyButtonListeners(BUTTON_ID, TAG, null);
						return true;
				}			
				return false;
			}
		});
	}
	
	public void setInformation(String description)
	{	
		m_ItemText.setText(description);
	}
	
	public void setSelected(boolean isSelected)
	{
		if(isSelected)
		{
			m_BackgroundContainer.setBackgroundResource(BACKGROUND_SELECTED);
			m_ItemText.setTextAppearance(m_Context, SELECTED_STYLE);
		}
		else
		{
			m_BackgroundContainer.setBackgroundDrawable(null);
			m_ItemText.setTextAppearance(m_Context, RELEASED_STYLE);
		}
		//m_ItemText.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Bold.otf"));
		m_ItemText.setVisibility(View.VISIBLE);
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
