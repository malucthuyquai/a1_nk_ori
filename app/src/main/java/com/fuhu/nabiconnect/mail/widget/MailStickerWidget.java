package com.fuhu.nabiconnect.mail.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;

import java.util.ArrayList;

public class MailStickerWidget extends RelativeLayout{

	public static final String TAG = "MailStickerWidget";
	
	private Context m_Context;

	private RelativeLayout m_BackgroundContainer;
	private ImageView m_TopSticker;
	private ImageView m_BottomSticker;
	private int m_TopImageResId;
	private int m_BottomImageResId;
	private ImageView m_VerticalDivider;
	private int m_ImageWidth;
	private int m_ImageHeight;
	private ArrayList<IButtonClickListener> m_ButtonListeners;


	public MailStickerWidget(Context context) {
		this(context, RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
	}

	public MailStickerWidget(Context context, int imageWidth, int imageHeight) {
		super(context, null);

		this.m_Context = context;
		this.m_ImageWidth = imageWidth;
		this.m_ImageHeight = imageHeight;
		
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.mail_sticker_widget, this);
		
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.mail_sticker_widget_background);
		m_TopSticker = (ImageView)m_BackgroundContainer.findViewById(R.id.mail_sticker_top_image);
		m_BottomSticker = (ImageView)m_BackgroundContainer.findViewById(R.id.mail_sticker_bottom_image);
		
		m_VerticalDivider = (ImageView)m_BackgroundContainer.findViewById(R.id.mail_sticker_vertical_divider);
		
			
			

	}
	
	public void setTopImage(int imageRes)
	{
		this.m_TopImageResId = imageRes;
		if(m_TopImageResId != 0)
		{
			m_TopSticker.setImageResource(m_TopImageResId);
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m_TopSticker.getLayoutParams();		
			if(m_ImageWidth > 0)
				params.width = m_ImageWidth;
			if(m_ImageHeight > 0)
				params.height = m_ImageHeight;
			m_TopSticker.requestLayout();
			
			
			m_TopSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bitmap stickerBitmap = ((BitmapDrawable)m_TopSticker.getDrawable()).getBitmap();
					notifyButtonListeners(0, TAG, new Object[]{stickerBitmap});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_sticker_" + m_TopImageResId);
				}
			});
		}
	}
	
	public void setBottomImage(int imageRes)
	{
		this.m_BottomImageResId = imageRes;
		if(m_BottomImageResId != 0)
		{
			m_BottomSticker.setImageResource(m_BottomImageResId);
			
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m_BottomSticker.getLayoutParams();		
			if(m_ImageWidth > 0)
				params.width = m_ImageWidth;
			if(m_ImageHeight > 0)
				params.height = m_ImageHeight;
			m_BottomSticker.requestLayout();
			
			m_BottomSticker.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Bitmap stickerBitmap = ((BitmapDrawable)m_BottomSticker.getDrawable()).getBitmap();
					notifyButtonListeners(0, TAG, new Object[]{stickerBitmap});

                    //tracking
                    Tracking.pushTrack(v.getContext(), "select_sticker_" + m_TopImageResId);
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
