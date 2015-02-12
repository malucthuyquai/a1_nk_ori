package com.fuhu.nabiconnect.friend.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.assetitem.AssetItemManager;
import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;

public class AssetItemWidget extends RelativeLayout{



	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "AssetItemWidget";
	public static final int BUTTON_ID = 100;
	public static final int NON_SUBCATEGORY_ID = -1;
	
	public enum SelectionStatus{Selected, UnSelected};
	
	private Context m_Context;
	
	private int categoryId;
	private int subCategoryId;
	private SelectionStatus selectStatus;
	private int iconResId;
	private int coverResId;
	private int itemId;
	private boolean hasSubCategory;
	private boolean isCancelable;
	private int backgroundColorResId;
	
	private RelativeLayout m_BackgroundContainer;
	private ImageView m_ItemImage;
	private ImageView m_CoverImage;
	private RelativeLayout m_TouchReceiver;
	private RelativeLayout m_InnerBackground;
	
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	
	public AssetItemWidget(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public AssetItemWidget(Context context, int itemId, int categoryId, int iconResId, int coverResId, SelectionStatus status)
	{
		this( context,  itemId,  categoryId,  NON_SUBCATEGORY_ID,  iconResId,  coverResId,  status , 0);
	}
	
	public AssetItemWidget(Context context, int itemId, int categoryId, int subCategoryId, int iconResId, int coverResId, SelectionStatus status, int backgroundColorResId)
	{
		super(context, null);
		
		this.m_Context = context;
		this.itemId = itemId;
		this.categoryId = categoryId;
		this.subCategoryId = subCategoryId;
		this.iconResId = iconResId;
		this.coverResId = coverResId;
		this.selectStatus = status;
		this.hasSubCategory = (subCategoryId != NON_SUBCATEGORY_ID);
		this.isCancelable = (categoryId == AssetItemManager.CATEGORY_ID_AVATAR_ACCESSORIES || categoryId == AssetItemManager.CATEGORY_ID_AVATAR_CLOTHES);
		this.backgroundColorResId = backgroundColorResId;
		
		// inflate layout
		LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.friend_asset_item_widget, this);
		
		m_BackgroundContainer = (RelativeLayout)this.findViewById(R.id.asset_item_background);
		m_ItemImage = (ImageView)m_BackgroundContainer.findViewById(R.id.item_image);
		m_CoverImage = (ImageView)m_BackgroundContainer.findViewById(R.id.cover_image);
		m_TouchReceiver = (RelativeLayout)m_BackgroundContainer.findViewById(R.id.touch_receiver);
		m_InnerBackground = (RelativeLayout)m_BackgroundContainer.findViewById(R.id.asset_inner_background);
		
		if(iconResId != 0)
			m_ItemImage.setImageResource(iconResId);
		m_CoverImage.setImageResource(coverResId);
		
		if(backgroundColorResId != 0)
		{
			m_InnerBackground.setBackgroundColor(m_Context.getResources().getColor(backgroundColorResId));
		}
		
		m_TouchReceiver.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				switchSelectedStatus();
				notifyButtonListeners(BUTTON_ID, TAG, new Object[]{AssetItemWidget.this});
			}
		});
		
		// initialize state
		onSelectedStatusChanged();
	}
	
	private void switchSelectedStatus()
	{
		if(selectStatus == SelectionStatus.Selected)
		{
			if(isCancelable)
			{
				selectStatus = SelectionStatus.UnSelected;
				onSelectedStatusChanged();
			}
			else
				LOG.V(TAG,"This cannot be canceled");
		}
		else if(selectStatus == SelectionStatus.UnSelected)
		{
			selectStatus = SelectionStatus.Selected;
			onSelectedStatusChanged();
		}
		
		
			
	}
	
	public boolean hasSubCategory() {
		return hasSubCategory;
	}


	private void onSelectedStatusChanged()
	{
		// update UI
		switch(selectStatus)
		{
			case Selected:
				m_CoverImage.setVisibility(View.VISIBLE);
				break;
			case UnSelected:
				m_CoverImage.setVisibility(View.INVISIBLE);
				break;
		}
	}
	
	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getSubCategoryId() {
		return subCategoryId;
	}

	public void setSubCategoryId(int subCategoryId) {
		this.subCategoryId = subCategoryId;
	}

	public SelectionStatus getSelectStatus() {
		return selectStatus;
	}

	public void setSelectStatus(SelectionStatus selectStatus) {
		if(this.selectStatus != selectStatus)
		{
			this.selectStatus = selectStatus;
			onSelectedStatusChanged();
		}
	}

	public int getIconResId() {
		return iconResId;
	}

	public void setIconResId(int iconResId) {
		
		if(iconResId == this.iconResId)
			return;
		
		this.iconResId = iconResId;
		this.m_ItemImage.setImageResource(iconResId);
	}

	public int getItemId() {
		return itemId;
	}

	public void setItemId(int itemId) {
		this.itemId = itemId;
	}	

	public int getCoverResId() {
		return coverResId;
	}

	public void setCoverResId(int coverResId) {
		this.coverResId = coverResId;
	}
	
	public boolean isCancelable(){
		return isCancelable;
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
