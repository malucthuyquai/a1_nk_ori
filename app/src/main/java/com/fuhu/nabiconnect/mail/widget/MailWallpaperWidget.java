package com.fuhu.nabiconnect.mail.widget;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

import java.util.ArrayList;

public class MailWallpaperWidget extends RelativeLayout{

	public static final String TAG = "MailWallpaperWidget";
	
	public static final int BUTTON_ID_IMAGE = 100;
	public static final int BUTTON_ID_COLOR = 101;
	
	private Context m_Context;

	private RelativeLayout m_BackgroundContainer;
	private View m_TopSticker;
	private View m_BottomSticker;
	private int m_TopImageResId;
	private int m_BottomImageResId;
	private int m_TopLargeImageResId;
	private int m_BottomLargeImageResId;
	private int m_TopColorId;
	private int m_BottomColorId;
	
	private ImageView m_VerticalDivider;
	private ArrayList<IButtonClickListener> m_ButtonListeners;



	public MailWallpaperWidget(Context context) {
		super(context, null);

		this.m_Context = context;
		
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.mail_wallpaper_widget, this);
		
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.mail_wallpaper_widget_background);
		m_TopSticker = (View)m_BackgroundContainer.findViewById(R.id.mail_wallpaper_top_image);
		m_BottomSticker = (View)m_BackgroundContainer.findViewById(R.id.mail_wallpaper_bottom_image);
		
		m_VerticalDivider = (ImageView)m_BackgroundContainer.findViewById(R.id.mail_wallpaper_vertical_divider);
		
			
			

	}
	
	public void setTopImage(int imageRes, int thumbImageRes, int colorId)
	{
		this.m_TopImageResId = thumbImageRes;
		this.m_TopLargeImageResId = imageRes;
		this.m_TopColorId = colorId;
		if(m_TopImageResId > 0)
		{
			m_TopSticker.setBackgroundResource(m_TopImageResId);
			m_TopSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(BUTTON_ID_IMAGE, TAG, new Object[]{m_TopLargeImageResId});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_background_" + m_TopLargeImageResId);
				}
			});
		}
		if(m_TopColorId > 0)
		{
			m_TopSticker.setBackgroundColor(m_Context.getResources().getColor(m_TopColorId));
			m_TopSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(BUTTON_ID_COLOR, TAG, new Object[]{m_TopColorId});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_background_" + m_BottomColorId);
				}
			});
		}
	}
	
	public void setBottomImage(int imageRes, int thumbImageRes, int colorId)
	{
		this.m_BottomImageResId = thumbImageRes;
		this.m_BottomLargeImageResId = imageRes;
		this.m_BottomColorId = colorId;
		if(m_BottomImageResId > 0)
		{
			m_BottomSticker.setBackgroundResource(m_BottomImageResId);		
			m_BottomSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(BUTTON_ID_IMAGE, TAG, new Object[]{m_BottomLargeImageResId});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_background_#" + m_TopLargeImageResId);
				}
			});
		}
		if(m_BottomColorId > 0)
		{
			m_BottomSticker.setBackgroundColor(m_Context.getResources().getColor(m_BottomColorId));
			m_BottomSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					notifyButtonListeners(BUTTON_ID_COLOR, TAG, new Object[]{m_TopColorId});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_background_#" + m_BottomColorId);
				}
			});
		}
	}

	public void hideVerticalDivider()
	{
		m_VerticalDivider.setVisibility(View.INVISIBLE);
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
