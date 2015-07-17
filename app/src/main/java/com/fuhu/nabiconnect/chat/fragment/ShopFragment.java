/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fuhu.nabiconnect.chat.fragment;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.chat.IOnChatMessageReceivedListener;
import com.fuhu.nabiconnect.chat.widget.ShopBarButtonWidget;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.tracking.TrackingFragment;

import java.util.ArrayList;

public class ShopFragment extends TrackingFragment implements IOnChatMessageReceivedListener, IButtonClickListener{

	public static final String TAG = "ShopFragment";

	public interface IItemLoadedListner {
		public void onItemLoaded(ArrayList<TableRow> list);
	}

	
	//private ListView m_IconListView;
	private ChatActivity m_Activity;
	private ShopBarButtonWidget m_ShopTopButton;
	private ShopBarButtonWidget m_ShopFreeButton;
	private ShopBarButtonWidget m_ShopNewButton;
	//ArrayList<StickerCategory> m_SelectedCategories;
	private SharedPreferences m_Preference;
	private int m_CurrentCategory;
	private TableLayout m_ItemListTable;
	private IItemLoadedListner m_ItemLoadedListener = new IItemLoadedListner(){

		@Override
		public void onItemLoaded(ArrayList<TableRow> list) {
			LOG.V(TAG, "onItemLoaded() - start");
			m_ItemListTable.removeAllViews();
			
			for(TableRow row : list)
				m_ItemListTable.addView(row);
			
			//m_ItemListTable.invalidate();
			
			
			updateBarItem();
			
			//m_Activity.getDialogManager().dismissDialog();
			
			LOG.V(TAG, "onItemLoaded() - end");
		}
		
	};

    public ShopFragment() {
        super("shop_station");
    }

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		m_Activity = (ChatActivity)getActivity();
	}



	@Override
	public void onPause() {
		super.onPause();
	}



	@Override
	public void onResume() {
		super.onResume();
		
		LOG.V(TAG, "onResume() - start");
		
		//m_IconListView = (ListView)m_Activity.findViewById(R.id.shop_items_listview);
		m_ShopTopButton = (ShopBarButtonWidget)m_Activity.findViewById(R.id.shop_top_button);
		m_ShopFreeButton = (ShopBarButtonWidget)m_Activity.findViewById(R.id.shop_free_button);
		m_ShopNewButton = (ShopBarButtonWidget)m_Activity.findViewById(R.id.shop_new_button);
		m_ItemListTable = (TableLayout)m_Activity.findViewById(R.id.shop_items_table);
		
		m_Preference = m_Activity.getShopPreference();
			
		m_ShopTopButton.addButtonListener(new IButtonClickListener() {
			
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				//updateItemTable(StickerCategory.SHOP_TYPE_TOP);
				//setAdapter(StickerCategory.SHOP_TYPE_TOP);
				
				//m_CurrentCategory = StickerCategory.SHOP_TYPE_TOP;
				
				loadItemTable(m_CurrentCategory);
				//updateBarItem();

                //tracking
                Tracking.pushTrack(getActivity(), "top");
			}
		});
		
		m_ShopFreeButton.addButtonListener(new IButtonClickListener() {
			
			@Override
			public void onButtonClicked(int buttonId, String viewName, Object[] args) {
				//updateItemTable(StickerCategory.SHOP_TYPE_FREE);
				//setAdapter(StickerCategory.SHOP_TYPE_FREE);
				
				//m_CurrentCategory = StickerCategory.SHOP_TYPE_FREE;
				
				loadItemTable(m_CurrentCategory);
				//updateBarItem();

                //tracking
                Tracking.pushTrack(getActivity(), "free");
			}
		});

		m_ShopNewButton.addButtonListener(new IButtonClickListener() {

			@Override
			public void onButtonClicked(int buttonId, String viewName,
					Object[] args) {
				//updateItemTable(StickerCategory.SHOP_TYPE_NEW);
				//setAdapter(StickerCategory.SHOP_TYPE_NEW);
				
				//m_CurrentCategory = StickerCategory.SHOP_TYPE_NEW;
				
				
				loadItemTable(m_CurrentCategory);
				//updateBarItem();

                //tracking
                Tracking.pushTrack(getActivity(), "new");
            }
		});

		//m_CurrentCategory = StickerCategory.SHOP_TYPE_TOP;

		loadItemTable(m_CurrentCategory);
		
		/*
		m_Activity.getDialogManager().showDialog();
		UpdateItemTableTask task = new UpdateItemTableTask();
		task.execute(StickerCategory.SHOP_TYPE_TOP, m_ItemLoadedListener);
		*/
		//updateItemTable(StickerCategory.SHOP_TYPE_TOP);
		
		
		
		
		
		
		//setAdapter(StickerCategory.SHOP_TYPE_TOP);
		
		
		LOG.V(TAG, "onResume() - end");
		
	}

	private void loadItemTable(int category)
	{
		//m_Activity.getDialogManager().showDialog();
		UpdateItemTableTask task = new UpdateItemTableTask();
		task.execute(category, m_ItemLoadedListener);
	}
	
	private void updateItemTable(int shopType)
	{
		LOG.V(TAG, "updateItemTable() - start");
		/*
		
		m_ItemListTable.removeAllViews();
		
		
		
		m_SelectedCategories = StickerManager.getShopCategory(shopType);
		
		for(int i=0;i<m_SelectedCategories.size();i++)
		{
		
			TableRow tableRow = new TableRow(m_Activity);
			
			ShopItemWidget widget = new ShopItemWidget(m_Activity, null);
			StickerCategory category = m_SelectedCategories.get(i);
			
			// check if is downloaded
			boolean isDownloaded = m_Preference.getBoolean(category.getId(), false);
			if(isDownloaded)
				category.setPurchaseStatus(StickerCategory.PURCHASE_STATUS_DOWNLOADED);

			LOG.V(TAG, "updateItemTable() - setOnShopItemClickListener() - start");
			widget.setOnShopItemClickListener(this);
			LOG.V(TAG, "updateItemTable() - setOnShopItemClickListener() - end");
			
			LOG.V(TAG, "updateItemTable() - setCategory() - start");
			widget.setCategory(category);
			LOG.V(TAG, "updateItemTable() - setCategory() - end");
			
			
			
			//tableRow.setPadding(7, 0, 0, 0);
			
			LOG.V(TAG, "updateItemTable() - tableRow.addView(widget); - start");
			tableRow.addView(widget);
			LOG.V(TAG, "updateItemTable() - tableRow.addView(widget); - end");
			
			LOG.V(TAG, "updateItemTable() - m_ItemListTable.addView(tableRow);; - start");
			m_ItemListTable.addView(tableRow);
			LOG.V(TAG, "updateItemTable() - m_ItemListTable.addView(tableRow);; - end");
			
		}
		*/
		
		LOG.V(TAG, "updateItemTable() - end");
	}
	
	private void updateBarItem()
	{
		LOG.V(TAG, "updateBarItem() - start");
		
		m_ShopTopButton.setInformation("TOP");
		m_ShopFreeButton.setInformation("FREE");
		m_ShopNewButton.setInformation("NEW");
		
		/*
		switch(m_CurrentCategory)
		{
			case StickerCategory.SHOP_TYPE_TOP:
				m_ShopTopButton.setSelected(true);
				m_ShopFreeButton.setSelected(false);
				m_ShopNewButton.setSelected(false);
				break;
			case StickerCategory.SHOP_TYPE_FREE:
				m_ShopTopButton.setSelected(false);
				m_ShopFreeButton.setSelected(true);
				m_ShopNewButton.setSelected(false);
				break;
			case StickerCategory.SHOP_TYPE_NEW:
				m_ShopTopButton.setSelected(false);
				m_ShopFreeButton.setSelected(false);
				m_ShopNewButton.setSelected(true);
				break;
			
		}
		*/
		LOG.V(TAG, "updateBarItem() - end");
	}
	/*
	private void setAdapter(int shopType)
	{
		LOG.V(TAG, "setAdapter() - start");
		
		m_SelectedCategories = StickerManager.getShopCategory(shopType);
		ShopItemAdapter adapter = new ShopItemAdapter(m_Activity, m_SelectedCategories, this);
		m_IconListView.setAdapter(adapter);
		
		LOG.V(TAG, "setAdapter() - end");
	}
*/

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, 
        Bundle savedInstanceState) {

        return inflater.inflate(R.layout.chat_shop_view, container, false);
    }



	@Override
	public void OnChatMessageReceived() {
		// TODO Auto-generated method stub
		
	}

	public class UpdateItemTableTask extends AsyncTask<Object, Object, Object> {	
		
		int type;
		ArrayList<TableRow> rowList; 
		IItemLoadedListner listener;
		@Override
		protected ArrayList<TableRow> doInBackground(Object... arg0) {	
			
			rowList = new ArrayList<TableRow>(); 
			
			type = (Integer)arg0[0];
			listener = (IItemLoadedListner)arg0[1];
			//updateItemTable(type);
			/*
			m_SelectedCategories = StickerManager.getShopCategory(type);
			
			for(int i=0;i<m_SelectedCategories.size();i++)
			{
			
				TableRow tableRow = new TableRow(m_Activity);
				
				ShopItemWidget widget = new ShopItemWidget(m_Activity, null);
				StickerCategory category = m_SelectedCategories.get(i);
				
				// check if is downloaded
				boolean isDownloaded = m_Preference.getBoolean(category.getId(), false);
				if(isDownloaded)
					category.setPurchaseStatus(StickerCategory.PURCHASE_STATUS_DOWNLOADED);

				LOG.V(TAG, "updateItemTable() - setOnShopItemClickListener() - start");
				widget.setOnShopItemClickListener(ShopFragment.this);
				LOG.V(TAG, "updateItemTable() - setOnShopItemClickListener() - end");
				
				LOG.V(TAG, "updateItemTable() - setCategory() - start");
				if(m_CurrentCategory == StickerCategory.SHOP_TYPE_NEW)
					widget.setCategory(category, false);
				else
					widget.setCategory(category);
				LOG.V(TAG, "updateItemTable() - setCategory() - end");
				
				
				
				//tableRow.setPadding(7, 0, 0, 0);
				
				LOG.V(TAG, "updateItemTable() - tableRow.addView(widget); - start");
				tableRow.addView(widget);
				LOG.V(TAG, "updateItemTable() - tableRow.addView(widget); - end");
				
				rowList.add(tableRow);
			}
			
			*/
			
			
			return rowList;
		}
		
		@Override
		protected void onPostExecute(Object result) {		
			super.onPostExecute(result);	
			
			listener.onItemLoaded(rowList);
		}
	}
	
	/*======================================================================
	 * Custom listview adapter
	 *=======================================================================*/
	/*
	private class ShopItemAdapter extends BaseAdapter{

		private Context m_Context;
		private ArrayList<StickerCategory> m_CategoryList;
		private IButtonClickListener m_Listener;

		
		public ShopItemAdapter(Context context, ArrayList<StickerCategory> categoryList, IButtonClickListener listener)
		{
			m_Context = context;
			m_CategoryList = categoryList;
			m_Listener = listener;
		}
		
		@Override
		public int getCount() {
			if(m_CategoryList != null)
				return m_CategoryList.size();
			else
				return 0;
		}

		@Override
		public Object getItem(int index) {
			if(m_CategoryList != null)
				return m_CategoryList.get(index);
			else
				return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LOG.V(TAG, "getView() - start");
			
			if (convertView == null) {
				LOG.V(TAG, "getView() - inflate() - start");
				LayoutInflater inflater = LayoutInflater.from(m_Context);
				convertView = inflater.inflate(R.layout.listitem, null);
				LOG.V(TAG, "getView() - inflate() - end");
			}
			ShopItemWidget widget = (ShopItemWidget)convertView.findViewById(R.id.widget);
			
			StickerCategory category = m_CategoryList.get(position);
			
			// check if is downloaded
			boolean isDownloaded = m_Preference.getBoolean(category.getId(), false);
			if(isDownloaded)
				category.setPurchaseStatus(StickerCategory.PURCHASE_STATUS_DOWNLOADED);

			LOG.V(TAG, "getView() - setOnShopItemClickListener() - start");
			widget.setOnShopItemClickListener(m_Listener);
			LOG.V(TAG, "getView() - setOnShopItemClickListener() - end");
			
			LOG.V(TAG, "getView() - setCategory() - start");
			widget.setCategory(category);
			LOG.V(TAG, "getView() - setCategory() - end");
			
			LOG.V(TAG, "getView() - end");

			return convertView;
		}
		
	}
*/
	@Override
	public void onButtonClicked(int buttonId, String viewName, Object[] args) {
		LOG.V(TAG,"onButtonClicked() - start");
		/*
		if(viewName.equals(ShopItemButtonWidget.TAG))
		{
			StickerCategory selectedCategory = null;
			if(args != null)
				selectedCategory = (StickerCategory)args[0];
			LOG.V(TAG, "categoryId is "+selectedCategory.getId());
			
			// write to preference
			SharedPreferences.Editor editor = m_Preference.edit();
	        editor.putBoolean(selectedCategory.getId(), true);
	        editor.apply();
			
		}
		*/
		LOG.V(TAG,"onButtonClicked() - end");
	}
	
}