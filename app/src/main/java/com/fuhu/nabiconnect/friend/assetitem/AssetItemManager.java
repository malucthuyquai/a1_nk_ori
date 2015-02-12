package com.fuhu.nabiconnect.friend.assetitem;

import android.content.Context;
import android.util.Pair;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.friend.avatar.AvatarManager;
import com.fuhu.nabiconnect.friend.avatar.FriendBean;
import com.fuhu.nabiconnect.friend.widget.AssetItemWidget;
import com.fuhu.nabiconnect.friend.widget.AssetItemWidget.SelectionStatus;
import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

public class AssetItemManager implements IButtonClickListener{
	
	
	public static final String TAG = "AssetItemManager";
	
	public static final int CATEGORY_ID_AVATAR_ICON = 1;
	public static final int CATEGORY_ID_AVATAR_COLOR = 2;
	public static final int CATEGORY_ID_AVATAR_CLOTHES = 3;
	public static final int CATEGORY_ID_AVATAR_ACCESSORIES = 4;
	public static final int CATEGORY_ID_AVATAR_BACKGROUND = 5;
	
	public static final int SUBCATEGORY_ID_GLASSES = 0;
	public static final int SUBCATEGORY_ID_PIPE = 1;
	public static final int SUBCATEGORY_ID_HAT = 2;
	public static final int SUBCATEGORY_ID_HAIRBAND = 3;
	public static final int SUBCATEGORY_ID_NECKTIE = 4;
	public static final int SUBCATEGORY_ID_MOUSTACHE = 5;
	
	private Context m_Context;
	private int m_CurrentCharacterIndex = AvatarManager.ID_TYPE_BEAR;

	private Hashtable<Integer, ArrayList<AssetItemWidget>> m_ListTable = new Hashtable<Integer, ArrayList<AssetItemWidget>>();
	
	private ArrayList<AssetItemWidget> m_AvatarIconList = new ArrayList<AssetItemWidget>();
	private ArrayList<AssetItemWidget> m_AvatarColorList = new ArrayList<AssetItemWidget>();
	private ArrayList<AssetItemWidget> m_AvatarClothesList = new ArrayList<AssetItemWidget>();
	private ArrayList<AssetItemWidget> m_AvatarAccessoriesList = new ArrayList<AssetItemWidget>();
	private ArrayList<AssetItemWidget> m_AvatarBackgroundList = new ArrayList<AssetItemWidget>();
	
	public AssetItemManager(Context context)
	{
		this.m_Context = context;
		
		// create list table
		m_ListTable.put(CATEGORY_ID_AVATAR_ICON, m_AvatarIconList);
		m_ListTable.put(CATEGORY_ID_AVATAR_COLOR, m_AvatarColorList);
		m_ListTable.put(CATEGORY_ID_AVATAR_CLOTHES, m_AvatarClothesList);
		m_ListTable.put(CATEGORY_ID_AVATAR_ACCESSORIES, m_AvatarAccessoriesList);
		m_ListTable.put(CATEGORY_ID_AVATAR_BACKGROUND, m_AvatarBackgroundList);
		
		// create asset item list
		// TODO:load avatar data from server
		
		/*
		
		// avatar icon
		Enumeration<Integer> keys = AvatarManager.AvatarIconTable.keys();
		while (keys.hasMoreElements()) {
			Integer keyInt = keys.nextElement();
			Integer resId = AvatarManager.AvatarIconTable.get(keyInt);
			addAssetItem(keyInt, CATEGORY_ID_AVATAR_ICON,  resId, R.drawable.list_bg_hover, SelectionStatus.UnSelected);
		}
		
		// avatar color
		Enumeration<Pair<Integer, Integer>> pairKeys = AvatarManager.AvatarColorTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarColorTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_COLOR,  resId, R.drawable.list_bg_hover, SelectionStatus.UnSelected);
			}
			
		}
		
		// hat
		pairKeys = AvatarManager.AvatarHatTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarHatTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_CLOTHES, SUBCATEGORY_ID_HAT, resId, R.drawable.vvv, SelectionStatus.UnSelected);
			}
			
		}

		// glasses
		pairKeys = AvatarManager.AvatarGlassesTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarGlassesTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_ACCESSORIES, SUBCATEGORY_ID_GLASSES, resId, R.drawable.vvv, SelectionStatus.UnSelected);
			}
			
		}
		
		// neck tie
		pairKeys = AvatarManager.AvatarNeckTieTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarNeckTieTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_ACCESSORIES, SUBCATEGORY_ID_NECKTIE, resId, R.drawable.vvv, SelectionStatus.UnSelected);
			}
			
		}
		
		// hairband
		pairKeys = AvatarManager.AvatarHairBandTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarHairBandTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_ACCESSORIES, SUBCATEGORY_ID_HAIRBAND, resId, R.drawable.vvv, SelectionStatus.UnSelected);
			}
			
		}
		
		// moustache
		pairKeys = AvatarManager.AvatarMoustacheTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == AvatarManager.ID_TYPE_BEAR)
			{
				Integer resId = AvatarManager.AvatarMoustacheTable.get(keyPair);
				addAssetItem(keyPair.second, CATEGORY_ID_AVATAR_ACCESSORIES, SUBCATEGORY_ID_MOUSTACHE, resId, R.drawable.vvv, SelectionStatus.UnSelected);
			}
			
		}
		
		// background
		keys = AvatarManager.AvatarBackgroundTable.keys();
		while (keys.hasMoreElements()) {
			Integer keyInt = keys.nextElement();
			Integer colorResId = AvatarManager.AvatarBackgroundTable.get(keyInt);
			//LOG.V(TAG, "AvatarBackgroundTable : keyInt is "+keyInt);
			addAssetItem(keyInt, CATEGORY_ID_AVATAR_BACKGROUND, AssetItemWidget.NON_SUBCATEGORY_ID, 0, R.drawable.list_bg_hover, SelectionStatus.UnSelected, colorResId);
			
		}
		*/
	}
	
	public ArrayList<AssetItemWidget> getAssetItemList(int categoryId)
	{
		return m_ListTable.get(categoryId);
	}
	
	private void addAssetItem(int itemId, int categoryId, int iconResId, int coverResId, SelectionStatus status)
	{
		addAssetItem( itemId,  categoryId,  AssetItemWidget.NON_SUBCATEGORY_ID,  iconResId,  coverResId,  status);	
	}
	
	private void addAssetItem(int itemId, int categoryId, int subCategoryId, int iconResId, int coverResId, SelectionStatus status)
	{
		addAssetItem( itemId,  categoryId, subCategoryId,  iconResId,  coverResId,  status , 0);	
	}
	
	private void addAssetItem(int itemId, int categoryId, int subCategoryId, int iconResId, int coverResId, SelectionStatus status, int backgroundRes)
	{
		AssetItemWidget widget = new AssetItemWidget(m_Context, itemId,  categoryId,  subCategoryId,  iconResId, coverResId, status, backgroundRes);
		widget.addButtonListener(this);
		
		
		//LOG.V(TAG, "AvatarBackgroundTable : itemId is "+itemId+" , categoryId is "+categoryId);
		ArrayList<AssetItemWidget> list = m_ListTable.get(categoryId);
		
		//LOG.V(TAG, "AvatarBackgroundTable : list is "+list);
		//if (!list.contains(widget))
		if (!hasItemInList(list, widget))
			list.add(widget);	
		
	}
	
	public void onCharacterTypeChanged(int updatedIndex)
	{
		if(m_CurrentCharacterIndex == updatedIndex)
			return;
		
		// avatar color
		Enumeration<Pair<Integer, Integer>> pairKeys = AvatarManager.AvatarColorTable.keys();
		while (pairKeys.hasMoreElements()) {
			Pair<Integer, Integer> keyPair = pairKeys.nextElement();
			if(keyPair.first == updatedIndex)
			{
				Integer resId = AvatarManager.AvatarColorTable.get(keyPair);
				
				// update the image of color page
				for(AssetItemWidget widget : m_AvatarColorList)
				{
					if(widget.getItemId() == keyPair.second)
						widget.setIconResId(resId);
				}
			}
			
		}
		
		
	}
	
	private boolean hasItemInList(ArrayList<AssetItemWidget> list ,AssetItemWidget item)
	{
		boolean result = false;
		
		for(AssetItemWidget itemInlist : list)
		{
			if(itemInlist.getItemId() == item.getItemId() && itemInlist.getSubCategoryId() == item.getSubCategoryId())
			{
				result = true;
				break;
			}
		}
		
		return result;
	}

	public void checkSelectedStatusByFriendBean(AssetItemWidget item, FriendBean bean)
	{
		LOG.V(TAG,"checkSelectedStatusByFriendBean() - start().");
		
		if(item == null || bean == null)
		{
			LOG.W(TAG,"checkSelectedStatusByFriendBean() - failed.");
			return;
		}
		int indexInBean = 0;
		
		if(item.hasSubCategory())
		{
			//LOG.W(TAG,"item.hasSubCategory item.getSubCategoryId() is "+item.getSubCategoryId());
			
			switch(item.getSubCategoryId())
			{
				case AssetItemManager.SUBCATEGORY_ID_HAT:
					indexInBean = bean.getCharacterClothingIndex();
					break;
				case AssetItemManager.SUBCATEGORY_ID_GLASSES:
					indexInBean = bean.getCharacterGlassesIndex();
					break;
				case AssetItemManager.SUBCATEGORY_ID_HAIRBAND:
					indexInBean = bean.getCharacterHairBandIndex();
					break;
				case AssetItemManager.SUBCATEGORY_ID_MOUSTACHE:
					indexInBean = bean.getCharacterMoustacheIndex();
					break;
				case AssetItemManager.SUBCATEGORY_ID_NECKTIE:
					indexInBean = bean.getCharacterNeckTieIndex();
					break;
			}
		}
		else
		{
			
			
			//LOG.V(TAG,"item.getCategoryId() is "+item.getCategoryId());
			switch(item.getCategoryId())
			{
				case CATEGORY_ID_AVATAR_ICON:
					indexInBean = bean.getCharacterTypeIndex();
					break;
				case CATEGORY_ID_AVATAR_COLOR:
					indexInBean = bean.getCharacterColorIndex();
					break;
				case CATEGORY_ID_AVATAR_CLOTHES:
					//indexInBean = bean.getCharacterClothingIndex();
					LOG.W(TAG,"checkSelectedStatusByFriendBean() - it's impossible to be here : CATEGORY_ID_AVATAR_CLOTHES");
					break;
				case CATEGORY_ID_AVATAR_ACCESSORIES:
					// it's impossible to be here
					LOG.W(TAG,"checkSelectedStatusByFriendBean() - it's impossible to be here : CATEGORY_ID_AVATAR_ACCESSORIES");
					break;
				case CATEGORY_ID_AVATAR_BACKGROUND:
					indexInBean = bean.getCharacterBackgroundColorIndex();
					break;
			}		
		}
		
		//LOG.V(TAG,"indexInBean is "+indexInBean+" , item.getItemId() is "+item.getItemId());
		
		if(item.getItemId() == indexInBean)
		{
			item.setSelectStatus(SelectionStatus.Selected);
		}
		else
		{
			item.setSelectStatus(SelectionStatus.UnSelected);
		}
		
		LOG.V(TAG,"checkSelectedStatusByFriendBean() - end");

	}
	
	@Override
	public void onButtonClicked(int buttonId, String viewName, Object[] args) {
			
		if(viewName.equals(AssetItemWidget.TAG))
		{
			LOG.V(TAG,"onButtonClicked() - start().");
			
			AssetItemWidget widget = (AssetItemWidget)args[0];
			ArrayList<AssetItemWidget> list = m_ListTable.get(widget.getCategoryId());
			
			//if(!widget.isCancelable())
			//{
			if(widget.hasSubCategory())
			{
				for(AssetItemWidget itemInlist : list)
				{
					if(itemInlist.getSubCategoryId() == widget.getSubCategoryId())
					{
						if(itemInlist.getItemId() != widget.getItemId())
						{
							itemInlist.setSelectStatus(SelectionStatus.UnSelected);
						}
					}
				}
			}
			else
			{
				for(AssetItemWidget itemInlist : list)
				{
					if(itemInlist.getCategoryId() == widget.getCategoryId())
					{
						if(itemInlist.getItemId() != widget.getItemId())
						{
							itemInlist.setSelectStatus(SelectionStatus.UnSelected);
						}
					}
				}
			}		
			//}
			
			LOG.V(TAG,"onButtonClicked() - end().");
			
		}
		
	}
}
