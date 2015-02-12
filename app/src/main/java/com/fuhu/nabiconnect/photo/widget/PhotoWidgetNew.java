package com.fuhu.nabiconnect.photo.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity;
import com.fuhu.nabiconnect.photo.PhotoActivity.PhotoWidgetListener;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager;
import com.fuhu.nabiconnect.photo.object.ImageDownLoadManager.OnTaskCompleted;

import java.util.ArrayList;

public class PhotoWidgetNew extends RelativeLayout {

	public static final String TAG = "PhotoNewWidget";
	
	PhotoActivity pAct = null;
	private PhotoWidgetListener pwListener; 
	LayoutInflater layoutInflater;
	private View rootView;
	private ImageView PhotoView;
	private ImageView UserImgaeView;
	private TextView UserName;
	private TextView PostTime;
	private ImageButton X_Button;
		
	private String UserImageUrl = null;
	private String PhotoImageUrl = null;
	private String PhotoBigImageUrl = null;
	private String PhotoId = null;
	private String SharetoUserId = null;
	private String FromUserId = null;
	private String UserId = null;
	
	private int mIndex = -1;
			
	private ArrayList<String> PhotoIdArray = new ArrayList<String>();
	private ArrayList<Drawable> PhotoDrawableArray = new ArrayList<Drawable>();
			
	public PhotoWidgetNew(PhotoActivity mAct, String userImageUrl,
			String photoImageUrl, String PhotoBigImageUrl, String userName,
			String photoId, String userid, String postTime, String fromuserid) {
		super(mAct);
		InitUI(mAct);

		this.pAct = mAct;

		if (userName != null)
			UserName.setText(userName);
		if (postTime != null)
			PostTime.setText(postTime);
		if (userImageUrl.equals("")) 
			userImageUrl = null;
		if (photoImageUrl.equals("")) 
			photoImageUrl = null;
		if (PhotoBigImageUrl.equals("")) 
			PhotoBigImageUrl = null;

		this.PhotoId = photoId;
		this.PhotoImageUrl = photoImageUrl;
		this.PhotoBigImageUrl = PhotoBigImageUrl;
		this.UserImageUrl = userImageUrl;
		this.UserId = userid;
		this.FromUserId = fromuserid;
	
	}
	
	public PhotoWidgetNew(PhotoActivity pAct, Drawable userImage,
			String photoImageUrl, String PhotoBigImageUrl, String userName,
			String photoId, String userid, String postTime, String sharetouserid) {
		super(pAct);
		InitUI(pAct);
		this.pAct = pAct;

		if (userImage != null)
			UserImgaeView.setImageDrawable(userImage);

		if (userName != null)
			UserName.setText(userName);
		if (postTime != null)
			PostTime.setText(postTime);

		this.PhotoId = photoId;
		this.PhotoImageUrl = photoImageUrl;
		this.PhotoBigImageUrl = PhotoBigImageUrl;
		this.SharetoUserId = sharetouserid;
		this.UserId = userid;

	}
		
	private void InitUI(Context mCtx){
		
		layoutInflater = (LayoutInflater)mCtx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rootView = layoutInflater.inflate(R.layout.photo_photowedgit, this);
		
		PhotoView = (ImageView) rootView.findViewById(R.id.Photo);
		UserImgaeView = (ImageView) rootView.findViewById(R.id.Profile_Photo);	
		UserName =  (TextView) rootView.findViewById(R.id.Profile_Name);		
		PostTime = (TextView) rootView.findViewById(R.id.Post_Time);
		X_Button =(ImageButton) rootView.findViewById(R.id.X_Button);	
		
		PhotoView.setOnClickListener(clickListener);
		//X_Button.setOnClickListener(clickListener);
	
	}
	
	public void SetPhotoWidgetListener(PhotoWidgetListener listener){
		this.pwListener = listener;
	}
	
	public void SetPhotoIdArrayList(ArrayList<String> arraylist){
		this.PhotoIdArray  = arraylist ;
	}
	
	public ArrayList<String> GetPhotoIdArrayList(){
		return this.PhotoIdArray;
	}
	
	public void SetAvatar(Drawable avatar){
		UserImgaeView.setImageDrawable(avatar);
	}
	
	public void SetPhotoImage(Drawable photo){
		PhotoView.setImageDrawable(photo);
	}
			
	public void SetX_ButtonId(int id){		
		X_Button.setId(id);
	}
	
	public int GetX_ButtonId(){		
		return X_Button.getId();
	}
	
	public void SetX_ButtonBackgroundResource(int resId){		
		X_Button.setBackgroundResource(resId);
	}
	
	public void SetIndex(int index) {
		this.mIndex = index;
	}
	
	public int GetIndex(){
		return this.mIndex;
	}
	
	public String GetFromId(){
		return FromUserId;
	}

	public String GetPhotoId(){
		
		if(PhotoId != null)
			return PhotoId;
		else 
			return null;
	}	
	
	public void ExecutePhoto(ImageDownLoadManager ImageDM){
		ImageDM.LoadImageFromServerForPhoto(PhotoImageUrl, PhotoId, UserId, PhotoBigImageUrl, PhotoView, null);
	}	

	public void ExecuteAvatar(ImageDownLoadManager ImageDM, OnTaskCompleted listener){
		LOG.I(TAG, "ExecuteAvatar");
		ImageDM.LoadImageFromServerForAvatar(UserImageUrl, FromUserId, UserId, null, UserImgaeView, listener);
	}
		
	public String GetShareToUserId(){		
		if(SharetoUserId != null)
			return SharetoUserId;
		else 
			return null;
	} 
	
	public void SetX_ButtonClickListener(View.OnClickListener clickListener){
		X_Button.setOnClickListener(clickListener);
	}
	
	public void SetPhotoViewClickListener(View.OnClickListener clickListener){
		PhotoView.setOnClickListener(clickListener);
	}
	
	
	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {

			switch (v.getId()) {

			case R.id.X_Button:

				v.setBackgroundResource(R.drawable.photo_x_button_select);

				break;
			case R.id.Photo:
				LOG.W(TAG, "click photo ");

				if (PhotoView.getDrawable() != null) {
					pwListener.onClickPhoto(GetX_ButtonId());
				}

				break;

			}

		}
	};
	
}
