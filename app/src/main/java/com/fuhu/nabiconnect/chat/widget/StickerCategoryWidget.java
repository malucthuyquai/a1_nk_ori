package com.fuhu.nabiconnect.chat.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.chat.stickers.StickerCategory;

import java.util.ArrayList;

public class StickerCategoryWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "StickerCategoryWidget";
	private Context m_Context;
	private StickerCategory m_StickerCategory;
	private ImageView m_CategoryIcon;
	private ImageView m_Divider;
	private ArrayList<IButtonClickListener> m_ButtonListeners;

	private RelativeLayout m_BackgroundContainer;
	
	public static final int BUTTON_ID = 100;
	
	public StickerCategoryWidget(Context context, StickerCategory category) {
		super(context, null);

		this.m_Context = context;
		this.m_StickerCategory = category;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.chat_sticker_category_widget, this);
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.sticker_category_background);
		m_CategoryIcon = (ImageView)m_BackgroundContainer.findViewById(R.id.category_icon);
		m_Divider = (ImageView)m_BackgroundContainer.findViewById(R.id.category_icon_divider);

		if(category != null)
			m_CategoryIcon.setImageResource(category.getCoverResId());
		else
		{
			// add plus icon
			m_CategoryIcon.setImageResource(R.drawable.chat_plus);
			m_Divider.setVisibility(View.INVISIBLE);
		}
		
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
						notifyButtonListeners(BUTTON_ID, TAG, new Object[]{m_StickerCategory});
						return true;
				}			
				return false;
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
