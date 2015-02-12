package com.fuhu.nabiconnect.mail.widget;


import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fuhu.data.MailData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;

import java.util.ArrayList;

public class MailContentWidget extends RelativeLayout{

	public static final String TAG = "MailContentWidget";
	
	private Context m_Context;

	private RelativeLayout m_BackgroundContainer;
	private ImageView m_MailContentImage;
	private String m_FullImageUrl;
	private String m_ThumbnailUrl;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private ProgressBar m_ProgressBar;
	private Bitmap m_ContentBitmap;
	private LoadAvatarBitmapTask m_ThumbnailLoadingTask; 
	private LoadAvatarBitmapTask m_FullImageLoadingTask; 
	private MailData m_MailData;

	private LoadAvatarBitmapTask.IOnBitmapLoaded m_BitmapCallback = new LoadAvatarBitmapTask.IOnBitmapLoaded(){

		@Override
		public void onBitmapLoaded(Bitmap bitmap) {
			if(bitmap != null)
			{
				m_ContentBitmap = bitmap;
				m_MailContentImage.setImageBitmap(m_ContentBitmap);
				m_ProgressBar.setVisibility(View.INVISIBLE);
			}
			else
			{
				LOG.W(TAG,"Load mail content failed");
				
				// load full image
				m_FullImageLoadingTask = new LoadAvatarBitmapTask();
				Utils.executeAsyncTask(m_FullImageLoadingTask, new LoadAvatarBitmapTask.IOnBitmapLoaded(){

					@Override
					public void onBitmapLoaded(Bitmap bitmap) {
						
						if(bitmap != null)
						{
							m_ContentBitmap = bitmap;
							m_MailContentImage.setImageBitmap(m_ContentBitmap);
							m_ProgressBar.setVisibility(View.INVISIBLE);
						}
						
					}}, m_FullImageUrl);
				
			}
			
		}
		
	};

	public MailContentWidget(Context context, MailData mailData) {
		super(context, null);

		this.m_Context = context;
		this.m_MailData = mailData;
		this.m_FullImageUrl = mailData.fileUrl;
		this.m_ThumbnailUrl = mailData.thumbnailUrl;
		LOG.V(TAG, "mailData.thumbnailUrl is "+mailData.thumbnailUrl);
		if(m_ThumbnailUrl == null || m_ThumbnailUrl.isEmpty())
			m_ThumbnailUrl = m_FullImageUrl;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.mail_content_widget, this);
		
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.mail_content_background);
		m_MailContentImage = (ImageView)m_BackgroundContainer.findViewById(R.id.mail_content_image);
		m_ProgressBar = (ProgressBar)m_BackgroundContainer.findViewById(R.id.marker_progress);
		
		//m_Name.setTypeface(Typeface.createFromAsset(m_Context.getAssets(), "fonts/GothamRnd-Bold.otf"));
		
		m_MailContentImage.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(m_ContentBitmap != null)
					notifyButtonListeners(0, TAG, new Object[]{m_ContentBitmap, m_FullImageUrl});
			}
		});
	
		m_ThumbnailLoadingTask = new LoadAvatarBitmapTask();
		Utils.executeAsyncTask(m_ThumbnailLoadingTask, m_BitmapCallback, m_ThumbnailUrl);
	}

	public void cancelLoadingContent()
	{
		if(m_ThumbnailLoadingTask != null)
			m_ThumbnailLoadingTask.cancel(true);
		
		if(m_FullImageLoadingTask != null)
			m_FullImageLoadingTask.cancel(true);
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
