package com.fuhu.nabiconnect.mail.dialog;


import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;

public class MailContentDialog extends PopupDialog {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "MailContentDialog";
	public static final int BUTTON_ID = 100;
	
	
	
	/*======================================================================
	 * Fields
	 *=======================================================================*/
	private Context m_Context;
	private RelativeLayout m_DialogContainer;
	private ImageView m_ContentImage;
	private Button m_ReplyButton;
	private LoadAvatarBitmapTask m_LoadingTask; 
	final private float[] mMatrix = new float[9];
	final private RectF mRect = new RectF();
	
	public MailContentDialog(Context context, Bitmap bitmap, String fullImageUrl)
	{
		super(context);
		this.m_Context = context;
		setContentView(R.layout.mail_content_dialog);

		m_DialogContainer = (RelativeLayout)this.findViewById(R.id.mail_content_dialog_container);
		m_ContentImage = (ImageView)this.findViewById(R.id.mail_content_dialog_image);
		m_ReplyButton = (Button)this.findViewById(R.id.mail_content_dialog_replay_button);
		
		if(bitmap != null)
			m_ContentImage.setImageBitmap(bitmap);
		
		this.setOnShowListener(new DialogInterface.OnShowListener() {
			
			@Override
			public void onShow(DialogInterface dialog) {
				LOG.V(TAG, "onShow() ");
				refreshTouchAreaAndSetButtonMargin();
				
			}
		});
		
		// load full image
		m_LoadingTask = new LoadAvatarBitmapTask();
		Utils.executeAsyncTask(m_LoadingTask, new LoadAvatarBitmapTask.IOnBitmapLoaded(){

			@Override
			public void onBitmapLoaded(Bitmap bitmap) {
				
				if(bitmap != null)
				{
					LOG.V(TAG,"onBitmapLoaded() - full image loaded");
					m_ContentImage.setImageBitmap(bitmap);
					refreshTouchAreaAndSetButtonMargin();
				}
				else
				{
					LOG.E(TAG,"onBitmapLoaded() - full image bitmap is null");
				}
				
			}}, fullImageUrl);
		
		m_DialogContainer.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View view, MotionEvent mv) {
				if(mv.getX() < 0 || mv.getX() > view.getWidth() || mv.getY() < 0 || mv.getY() > view.getHeight())
				{
					return false;
				}			
				switch(mv.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						return true;
					case MotionEvent.ACTION_UP:
						dismiss();
						return true;
				}			
				return false;
			}
		});
		
		m_ContentImage.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				
				switch(event.getAction())
				{
					case MotionEvent.ACTION_DOWN:
						return true;
					case MotionEvent.ACTION_UP:
						
						if (mRect.contains(event.getX(), event.getY())) {
							LOG.V(TAG, "Touch inside the image");
						} else {
							dismiss();

                            //tracking
                            Tracking.pushTrack(v.getContext(), "dialog_mail_content_close");
						}
						
						//dismiss();
						return true;
				}			
				return false;
				
				
				
				
			}
		});
		
		m_ReplyButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				notifyButtonListeners(BUTTON_ID, TAG, null);

                //tracking
                Tracking.pushTrack(v.getContext(), "dialog_mail_content_reply_button");
			}
		});
		
	}

	private void refreshTouchAreaAndSetButtonMargin()
	{
		BitmapDrawable bd = null;
		Drawable d = m_ContentImage.getDrawable();
		if (d != null) {
			if (d instanceof TransitionDrawable) {
				TransitionDrawable td = (TransitionDrawable) d;
				bd = (BitmapDrawable) td.getDrawable(1);
			} else {
				bd = (BitmapDrawable) m_ContentImage.getDrawable();
			}
		}
		else
		{
			LOG.W(TAG, "refreshTouchArea() - m_ContentImage.getDrawable() is null");
		}
		// get image view center point
		int w2 = m_ContentImage.getWidth() / 2;
		int h2 = m_ContentImage.getHeight() / 2;
		
		LOG.W(TAG, "refreshTouchArea() - w2 is "+w2+" , h2 is "+h2);
		
		// get bitmap center point
		int iw2 = bd == null ? w2 : getImageWidth(bd) / 2;
		int ih2 = bd == null ? h2 : getImageHeight(bd) / 2;
		
		LOG.W(TAG, "refreshTouchArea() - iw2 is "+iw2+" , ih2 is "+ih2);
		mRect.set(w2 - iw2, h2 - ih2, w2 + iw2, h2 + ih2);
		
		if(m_ReplyButton != null)
		{
			int buttonWidth = m_ReplyButton.getWidth();
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m_ReplyButton.getLayoutParams();
			params.leftMargin = w2 + iw2 - buttonWidth/2;
			m_ReplyButton.requestLayout();
		}
	}
	
	private int getImageWidth(BitmapDrawable bd) {
		if (bd != null) {
			Bitmap bmp = bd.getBitmap();
			m_ContentImage.getImageMatrix().getValues(mMatrix);
			if (bmp != null) {
				return Math.round(bmp.getWidth() * mMatrix[Matrix.MSCALE_X]);
			}
		}
		return m_ContentImage.getWidth();
	}

	private int getImageHeight(BitmapDrawable bd) {
		if (bd != null) {
			Bitmap bmp = bd.getBitmap();
			m_ContentImage.getImageMatrix().getValues(mMatrix);
			if (bmp != null) {
				return Math.round(bmp.getHeight() * mMatrix[Matrix.MSCALE_Y]);
			}
		}
		return m_ContentImage.getHeight();
	}
	
	@Override
	public void dismiss() {
		super.dismiss();
		
		if(m_LoadingTask != null)
			m_LoadingTask.cancel(true);
	}

	
	

}


