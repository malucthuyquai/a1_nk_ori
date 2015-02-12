package com.fuhu.nabiconnect.friend.avatar;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.R;

public class AvatarWidget extends RelativeLayout{
	
	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "AvatarWidget";
	
	private Context m_Context;
	private FriendBean m_FriendBean;
	private RelativeLayout m_Background;
	private ImageView m_AvatarIcon;
	private ImageView m_AvatarHat;
	private ImageView m_AvatarGlasses;
	private ImageView m_AvatarMoustache;
	private ImageView m_AvatarNeckTie;
	private ImageView m_AvatarHairBand;

	public AvatarWidget(Context context,  AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AvatarWidget(Context context) {
		super(context, null);

		this.m_Context = context;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.friend_avatar_widget, this);
		
		m_Background = (RelativeLayout)this.findViewById(R.id.avatar_icon_background);
		m_AvatarIcon = (ImageView)this.findViewById(R.id.avatar_icon);
		m_AvatarHat = (ImageView)this.findViewById(R.id.avatar_icon_hat);
		m_AvatarGlasses = (ImageView)this.findViewById(R.id.avatar_icon_glasses);
		m_AvatarMoustache = (ImageView)this.findViewById(R.id.avatar_icon_moustache);
		m_AvatarNeckTie = (ImageView)this.findViewById(R.id.avatar_icon_necktie);
		m_AvatarHairBand = (ImageView)this.findViewById(R.id.avatar_icon_hairband);

	
	}

	public void setFriendBean(FriendBean bean)
	{
		
		if(bean == null)
			return;
		
		this.m_FriendBean = bean;
		
		bean.printInfo();
		
		int iconTypeIndex = bean.getCharacterTypeIndex();
		// update UI
		Integer iconResId = AvatarManager.AvatarColorTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterColorIndex()));
		Integer hatResId = AvatarManager.AvatarHatTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterClothingIndex()));
		
		Integer glassesResId = AvatarManager.AvatarGlassesTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterGlassesIndex()));
		Integer moustacheResId = AvatarManager.AvatarMoustacheTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterMoustacheIndex()));
		Integer neckTieResId = AvatarManager.AvatarNeckTieTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterNeckTieIndex()));
		Integer hairBandResId = AvatarManager.AvatarHairBandTable.get(new Pair<Integer, Integer>(iconTypeIndex , bean.getCharacterHairBandIndex()));
		Integer bgColorResId = AvatarManager.AvatarBackgroundTable.get(bean.getCharacterBackgroundColorIndex());
		
		// always shown
		m_AvatarIcon.setImageResource(getIndexValue(iconResId));
		m_Background.setBackgroundColor(m_Context.getResources().getColor(getIndexValue(bgColorResId)));
		
		// accsessroies
		setImageContent(m_AvatarHat, getIndexValue(hatResId));
		setImageContent(m_AvatarGlasses, getIndexValue(glassesResId));
		setImageContent(m_AvatarMoustache, getIndexValue(moustacheResId));
		setImageContent(m_AvatarNeckTie, getIndexValue(neckTieResId));
		setImageContent(m_AvatarHairBand, getIndexValue(hairBandResId));
		
	}
	
	private void setImageContent(ImageView view, int resId)
	{
		if(resId > 0)
			view.setImageResource(resId);
		else
			view.setImageDrawable(null);
	}
	
	private int getIndexValue(Integer integer)
	{
		return integer == null ? -1 : integer.intValue();
	}
	
}
