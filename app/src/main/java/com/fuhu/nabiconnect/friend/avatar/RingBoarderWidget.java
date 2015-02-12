package com.fuhu.nabiconnect.friend.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;

public class RingBoarderWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "RingBoarderWidget";
	private Context m_Context;
	private ImageView m_AvatarIcon;

	public RingBoarderWidget(Context context,  AttributeSet attrs) {
		super(context, attrs);
	}
	
	public RingBoarderWidget(Context context) {
		super(context, null);
		
		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.friend_ring_boarder_widget, this);
		m_AvatarIcon = (ImageView)this.findViewById(R.id.input_image);
	}

	public void setTargetImage(Bitmap targetBitmap)
	{
		m_AvatarIcon.setImageBitmap(targetBitmap);
	}


	
}
