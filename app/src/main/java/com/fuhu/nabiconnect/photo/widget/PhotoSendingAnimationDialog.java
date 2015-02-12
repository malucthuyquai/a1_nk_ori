package com.fuhu.nabiconnect.photo.widget;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;

import java.util.Timer;
import java.util.TimerTask;

public class PhotoSendingAnimationDialog extends PopupDialog {
	
	public static final String TAG = "CreateUserNameDialog";
	private Context m_Context;
	private TextView m_photo_animation_dialog_words;
	private ImageView m_photo_animaiton_zone;
	
	private String waitingMessage_1 ;
	private String waitingMessage_2 ;
	private String waitingMessage_3 ;
	private String waitingMessage_4 ;
	
	private AnimationDrawable frameAnimation;
	
	public static final int Cancel_test_BUTTON_ID = 101;	
	
	boolean flagstrat = true;
	private Timer timer;
	
	private Button m_tester;

	public PhotoSendingAnimationDialog(Context context)
	{
		super(context);
		this.m_Context = context;
				
		setContentView(R.layout.photo_sending_dialog_animation);
		waitingMessage_1 = context.getResources().getString(R.string.SendingMessage_1);
		waitingMessage_2 = context.getResources().getString(R.string.SendingMessage_2);
		waitingMessage_3 = context.getResources().getString(R.string.SendingMessage_3);
		waitingMessage_4 = context.getResources().getString(R.string.SendingMessage_4);
		m_photo_animation_dialog_words = (TextView)this.findViewById(R.id.photo_sending_textview);
		m_photo_animaiton_zone = (ImageView)this.findViewById(R.id.photo_sending_animation_zone);	
		
		m_photo_animaiton_zone = (ImageView) findViewById(R.id.photo_sending_animation_zone);
		m_photo_animaiton_zone.setBackgroundResource(R.drawable.sending_animation);
		
		Typeface tf = Typeface.createFromAsset(m_Context.getAssets(),"fonts/MyriadPro-Regular.otf");
		m_photo_animation_dialog_words.setTypeface(tf,Typeface.NORMAL);
		
		frameAnimation = (AnimationDrawable) m_photo_animaiton_zone.getBackground();
		frameAnimation.start();	
		timer = new Timer();
		flagstrat = true;
		timer.schedule(tTask, 0, 550);
	}
	
	public void setTextviewTextColor(int textColor){
		m_photo_animation_dialog_words.setTextColor(textColor);
	}
	
	public void sentSuccess(String DisplayAfterSentSuccess){
		frameAnimation.stop();
		flagstrat = false;
		m_photo_animaiton_zone.setBackgroundResource(R.drawable.sending_animation21);
		m_photo_animation_dialog_words.setText(DisplayAfterSentSuccess);
	}
	
	public TimerTask tTask = new TimerTask() {
		int counter = 4;
		public void run() {
			if(flagstrat){
				Message message = new Message();
				message.what = counter % 4;
				counter++;
				handler.sendMessage(message);
			}
		}
	};

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				m_photo_animation_dialog_words.setText(waitingMessage_1);
				break;
			case 1:
				m_photo_animation_dialog_words.setText(waitingMessage_2);
				break;
			case 2:
				m_photo_animation_dialog_words.setText(waitingMessage_3);
				break;
			case 3:
				m_photo_animation_dialog_words.setText(waitingMessage_4);
				break;
			default:
			}
		}
	};
}
