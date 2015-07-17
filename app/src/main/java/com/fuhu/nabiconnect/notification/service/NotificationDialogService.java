package com.fuhu.nabiconnect.notification.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.account.AccountParser;
import com.fuhu.account.data.AccessToken.OSGToken;
import com.fuhu.account.data.Kid;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.ApiHelperSocial;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.notification.bean.NotificationBean;

import org.json.JSONException;
import org.json.JSONObject;

public class NotificationDialogService extends IntentService {

	private final static String TAG = "NotificationDialogService";	
	public static final String PREF_KEY_ENABLED = "notificationDialogEnabled";	
	public static final String KEY_MESSAGE = "message";
	private static final int MESSAGE_LOGIN = 1001;
	private NotificationBean m_NotificationBean;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message m) {
			if (m.what != MESSAGE_LOGIN) {
				return;
			}
			switch (m.arg1) {
			case ApiHelperSocial.LOGIN:
				JSONObject jsonObj = (JSONObject)m.obj;
				
				String currentFriendCode = "";
				try {
					currentFriendCode = jsonObj.getString("friendCode");
				} catch (JSONException e) {
					LOG.E(TAG, "handleMessage() - failed to parse friendCode.",e);
				}
				
				LOG.V(TAG, "handleMessage() - currentFriendCode is "+currentFriendCode);
				
				if(m_NotificationBean == null)
				{
					LOG.E(TAG, "handleMessage() - m_NotificationBean is null.");
					return;
				}
				
				if(!currentFriendCode.isEmpty())
				{
					if(currentFriendCode.equals(m_NotificationBean.getFriendCode()))
					{
						// update the userKey & kidId in notification bean 
						boolean isInNabiMode = AccountParser.isInnabiMode();
						try {				
							OSGToken m_OsgToken;
							Kid currentKid = AccountParser.getAccount(NotificationDialogService.this).getCurrentKid();
							m_OsgToken = AccountParser.getAccount(NotificationDialogService.this).getAccessToken().getOSGToken();
							m_NotificationBean.setUserKey(m_OsgToken.getOSGUserKey());
							if(isInNabiMode)
								m_NotificationBean.setKidId(String.valueOf(currentKid.getKidId()));
						} catch (Throwable tr) {
							LOG.E(TAG, "handleMessage() - Failed to update notification bean userKey & kidId", tr);
						}
						
						// notify nabi message center
						NabiNotificationManager.notifyNabiMessageCenter(NotificationDialogService.this, m_NotificationBean);
									
						LOG.V(TAG, "handleMessage() - friend code matched, show dialog.");
						mHandler.post(new DisplayDialog(NotificationDialogService.this, m_NotificationBean, isInNabiMode));
					}
					else
					{
						LOG.V(TAG, "handleMessage() - friend code is not matched.");
						
						// check if there is cache data to send
						SharedPreferences preference = NotificationDialogService.this.getSharedPreferences(ApiBaseActivity.PREF_NAME_OF_MESSAGE_CENTER_CACHE, Context.MODE_PRIVATE);
						String cacheString = preference.getString(m_NotificationBean.getFriendCode(), "");
						LOG.V(TAG, "handleMessage() - cacheString is "+cacheString);			
						if(!cacheString.isEmpty())
						{
							// parse the cache string
							String[] result = cacheString.split(":");
							if(result != null && result.length == 2)
							{
								String cacheUserKey = result[0];
								String cacheKidId = result[1];
								
								LOG.V(TAG, "handleMessage() - cache data for FRIEND_CODE "+m_NotificationBean.getFriendCode()+" , cacheUserKey is "+cacheUserKey+" , cacheKidId is "+cacheKidId);
								
								m_NotificationBean.setUserKey(cacheUserKey);
								m_NotificationBean.setKidId(cacheKidId);
								
								NabiNotificationManager.notifyNabiMessageCenter(NotificationDialogService.this, m_NotificationBean);
								
							}
						}
						else
						{
							LOG.V(TAG, "handleMessage() - there is no cache for FRIEND_CODE "+m_NotificationBean.getFriendCode());
						}
					}
				}
				
				break;
			case ApiHelperSocial.ERROR:
				LOG.W(TAG, "handleMessage() - failed to login");
				break;
			}
		}
	};
	private ApiHelperSocial m_Helper;
	
	public NotificationDialogService() {
		super(TAG);
		
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Show dialog
		LOG.V(TAG,"onHandleIntent() - start");
		
		boolean isEnabled = true;
		
		SharedPreferences perference = getSharedPreferences(NabiNotificationManager.PREF_NAME, Context.MODE_PRIVATE);
		perference.getBoolean(PREF_KEY_ENABLED, true);
		
		if(!isEnabled)
		{
			LOG.V(TAG,"onHandleIntent() - NotificationDialog has been disabled.");
			return;
		}
		
		// check account
		if (AccountParser.getAccount(this) == null) {
			LOG.W(TAG, "onHandleIntent() - there is no account.");
			return;
		}
		
		// get needed information
		boolean isInNabiMode = AccountParser.isInnabiMode();
		LOG.V(TAG,"onHandleIntent() - isInNabiMode is "+isInNabiMode);
		Kid currentKid = AccountParser.getAccount(this).getCurrentKid();
		OSGToken m_OsgToken;
		try {
			m_OsgToken = AccountParser.getAccount(this).getAccessToken().getOSGToken();
		} catch (Throwable tr) {
			LOG.E(TAG, "onHandleIntent() - Failed to get osg token", tr);
			return;
		}
		String currentUserKey = m_OsgToken.getOSGUserKey();
		
		Bundle extras = intent.getExtras();
		if(extras != null)
		{
			String dataString = (String)extras.get(KEY_MESSAGE);
			LOG.V(TAG, "onHandleIntent() - dataString is "+dataString); 
			
			m_NotificationBean = new NotificationBean();
			try {
				JSONObject obj = new JSONObject(dataString);
				
				if (!obj.isNull(NotificationBean.KEY_CONTENT))
					m_NotificationBean.setContent(obj.getString(NotificationBean.KEY_CONTENT));
				
				if (!obj.isNull(NotificationBean.KEY_FRIEND_CODE))
					m_NotificationBean.setFriendCode(obj.getString(NotificationBean.KEY_FRIEND_CODE));
				
				if (!obj.isNull(NotificationBean.KEY_KID_ID))
					m_NotificationBean.setKidId(obj.getString(NotificationBean.KEY_KID_ID));
				
				if (!obj.isNull(NotificationBean.KEY_PACKAGE_NAME))
					m_NotificationBean.setPackageName(obj.getString(NotificationBean.KEY_PACKAGE_NAME));
				
				if (!obj.isNull(NotificationBean.KEY_TITLE))
					m_NotificationBean.setTitle(obj.getString(NotificationBean.KEY_TITLE));
				
				if (!obj.isNull(NotificationBean.KEY_USER_KEY))
					m_NotificationBean.setUserKey(obj.getString(NotificationBean.KEY_USER_KEY));
				
				if (!obj.isNull(NotificationBean.KEY_APPLICATION_NAME))
					m_NotificationBean.setApplicationName(obj.getString(NotificationBean.KEY_APPLICATION_NAME));
				
			} catch (Throwable tr) {
				LOG.E(TAG, "onHandleIntent() - failed to parse dataString.",tr);
				return;
			}
			
			// check if needs to show the dialog
			if(!m_NotificationBean.getUserKey().isEmpty())
			{
				// notify nabi message center
				NabiNotificationManager.notifyNabiMessageCenter(this, m_NotificationBean);
				
				// compare the userkey
				if(m_NotificationBean.getUserKey().equals(currentUserKey))
				{
					if(isInNabiMode)
					{
						if(currentKid == null)
						{
							LOG.V(TAG, "onHandleIntent() - current kid is null.");
							return;
						}
						
						// compare the kid id
						String currentKidIdString = String.valueOf(currentKid.getKidId());
						if(m_NotificationBean.getKidId().equals(currentKidIdString))
						{
							LOG.V(TAG, "onHandleIntent() - kid id matched, show dialog.");
							mHandler.post(new DisplayDialog(this, m_NotificationBean, isInNabiMode));
						}
						else
						{
							LOG.V(TAG, "onHandleIntent() - kid account is different, won't show dialog.");
							return;
						}
					}
					else
					{
						long beanKidIdValue;
						try{						
							beanKidIdValue = Long.parseLong(m_NotificationBean.getKidId());
						} catch (Throwable th)
						{
							LOG.W(TAG, "onHandleIntent() - failed to parse kid Id.");
							return;
						}
						
						// check kidId, if kid == -1, it's a parent's nabiFriend account
						if(beanKidIdValue > 0)
						{
							LOG.V(TAG, "onHandleIntent() - it's a kid account, won't show dialog in mommy mode.");
							return;
						}
						else
						{
							LOG.V(TAG, "onHandleIntent() - userKey id matched, show dialog.");					
							mHandler.post(new DisplayDialog(this, m_NotificationBean, isInNabiMode));
						}					
					}			
				}
				else
				{
					LOG.V(TAG, "onHandleIntent() - different acount, won't show dialog");
					return;
				}
			
			}
			else if(!m_NotificationBean.getFriendCode().isEmpty())
			{
				// login to nabiFriend system to get the friend code
				m_Helper = ApiHelperSocial.getInstance(this, mHandler);
				
				if(isInNabiMode)
				{
					if(currentKid == null)
					{
						LOG.V(TAG, "onHandleIntent() - current kid is null.");
						return;
					}
					
					m_Helper.login(MESSAGE_LOGIN, currentKid.getKidId());
				}
				else
				{
					m_Helper.login(MESSAGE_LOGIN, 0);
				}
			}
			else
			{
				LOG.W(TAG, "onHandleIntent() - illegal NotificationBean.");
				return;
			}	
		}
		else
			LOG.V(TAG,"onHandleIntent() - extras is null");
			
		LOG.V(TAG,"onHandleIntent() - end");
		
	}
	
	public class DisplayDialog implements Runnable {
		private final static int DIALOG_APPEAR_TIME = 2500;
		private final static int ANIMATION_PERIOD = 500;
	    private final Context mContext;
	    private NotificationBean mBean;
	    private boolean mIsInNabimode;

	    private WindowManager windowManager;
		private View view;
		private RelativeLayout m_BackgroundContainer;
		private ImageView m_IconImage;
		private TextView m_TitleText;
		private TextView m_ContentText;		
		private Intent mActivityIntent;
	    
	    public DisplayDialog(Context mContext, NotificationBean bean, boolean isInNabimode){
	        this.mContext = mContext;
	        mBean = bean;
	        mIsInNabimode = isInNabimode;
	        mActivityIntent = null;
	    }

	    public void run(){
	    	
	    	
	    	windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

			LayoutInflater inflater = LayoutInflater.from(mContext);
			view = inflater.inflate(R.layout.nabi_notification_dialog, null);

			m_BackgroundContainer = (RelativeLayout)view.findViewById(R.id.dialog_background_container);
			m_IconImage = (ImageView)view.findViewById(R.id.dialog_app_icon);
			m_TitleText = (TextView)view.findViewById(R.id.dialog_title);
			m_ContentText = (TextView)view.findViewById(R.id.dialog_content);
			
			// check the application name to decide the icon & intent
			int iconResId = 0;
			
			if(NabiNotificationManager.APPLICATION_NAME_CHAT.equals(mBean.getApplicationName()))
			{
				iconResId = R.drawable.notification_chat_icon;
				mActivityIntent = new Intent(ApiBaseActivity.INTENT_CHAT_ACTIVITY);			
			}
			else if(NabiNotificationManager.APPLICATION_NAME_FRIEND.equals(mBean.getApplicationName()))
			{
				iconResId = R.drawable.notification_friend_icon;
				mActivityIntent = new Intent(ApiBaseActivity.INTENT_FRIEND_ACTIVITY);			
			}
			else if(NabiNotificationManager.APPLICATION_NAME_MAIL.equals(mBean.getApplicationName()))
			{
				iconResId = R.drawable.notification_mail_icon;
				mActivityIntent = new Intent(ApiBaseActivity.INTENT_MAIL_ACTIVITY);			
			}
			else if(NabiNotificationManager.APPLICATION_NAME_PHOTO.equals(mBean.getApplicationName()))
			{
				iconResId = R.drawable.notification_photo_icon;
				mActivityIntent = new Intent(ApiBaseActivity.INTENT_PHOTO_ACTIVITY);			
			}
			else
			{
				LOG.E(TAG, "run() - mBean.getApplicationName() is "+mBean.getApplicationName()+" , which is un-defined.");
				iconResId = R.drawable.ic_launcher;
			}
				
			if(mActivityIntent != null)
			{
				mActivityIntent.putExtra(ApiBaseActivity.KEY_IS_MOMMY_MODE, !mIsInNabimode);
				mActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			}
			
			m_IconImage.setImageResource(iconResId);
			
			//m_IconImage.setImageDrawable(NotificationUtils.getAppIconDrawableByPackageName(mContext, mBean.getPackageName()));
			m_TitleText.setText(mBean.getTitle());
			m_ContentText.setText(mBean.getContent());
			m_BackgroundContainer.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					if(mActivityIntent != null)
						mContext.startActivity(mActivityIntent);

                    //tracking (just a tag for searching)
				}
			});
			
			final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
					WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
					WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
					PixelFormat.TRANSLUCENT);

			params.gravity = Gravity.CENTER_HORIZONTAL|Gravity.TOP;
			//params.y = mContext.getResources().getDimensionPixelSize(R.dimen.notification_dialog_margin_top);
			windowManager.addView(view, params);
			
			
			int dialogHeight = mContext.getResources().getDimensionPixelSize(R.dimen.notification_dialog_height);
			final TranslateAnimation slideIn = new TranslateAnimation(0, 0, -dialogHeight, 0);
			slideIn.setDuration(ANIMATION_PERIOD);
			slideIn.setFillAfter(true);
			final TranslateAnimation slideOut = new TranslateAnimation(0, 0, 0, -dialogHeight);
			slideOut.setDuration(ANIMATION_PERIOD);
			slideOut.setFillAfter(true);
			slideOut.setAnimationListener(new Animation.AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation animation) {}
				
				@Override
				public void onAnimationRepeat(Animation animation) {}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					LOG.V(TAG, "onAnimationEnd() - slideOut animation end");
					if (view != null)
						windowManager.removeView(view);	
				}
			});
			
			view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
				
				@Override
				public void onViewDetachedFromWindow(View v) {}
		
				@Override
				public void onViewAttachedToWindow(View v) {
					// sliding-in animation
					m_BackgroundContainer.startAnimation(slideIn);				
				}
			});
	    	
			// hide the dialog after DIALOG_APPEAR_TIME
			new CountDownTimer(DIALOG_APPEAR_TIME,100) {
				
				@Override
				public void onTick(long millisUntilFinished) {
					//LOG.V(TAG, "onTick() - millisUntilFinished : "+millisUntilFinished);
				}
				
				@Override
				public void onFinish() {
					LOG.V(TAG, "onFinish() - showing time is up.");
					m_BackgroundContainer.startAnimation(slideOut);
				}
			}.start();
	    }
	}

}
