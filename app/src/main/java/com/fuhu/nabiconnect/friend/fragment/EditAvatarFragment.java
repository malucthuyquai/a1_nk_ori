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
package com.fuhu.nabiconnect.friend.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.Event;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.event.IEventListener;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.friend.assetitem.AssetItemManager;
import com.fuhu.nabiconnect.friend.avatar.AvatarManager;
import com.fuhu.nabiconnect.friend.avatar.FriendBean;
import com.fuhu.nabiconnect.friend.widget.AssetItemWidget;
import com.fuhu.nabiconnect.friend.widget.AssetItemWidget.SelectionStatus;
import com.fuhu.nabiconnect.friend.widget.AvatarBarButtonWidget;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.nabiconnect.utils.IntentList;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibsoutstructs.uploadUserAvatarImage_outObj;

import java.util.ArrayList;
import java.util.Enumeration;

public class EditAvatarFragment extends Fragment implements IButtonClickListener {

	public static final String TAG = "EditAvatarFragment";
	public static final int CANCEL_BUTTON_ID = 1001;
	public static final int CONFIRM_BUTTON_ID = 1002;
	public static final int ASK_MOM_REQUEST_CODE = 700;

	private FriendActivity m_Activity;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private Button m_CancelButton;
	private Button m_ConfirmButton;
	private RelativeLayout m_EditAvatarContainer;
	private RelativeLayout m_SetupDescriptionContainer;

	private AvatarBarButtonWidget m_AvatarIconButton;
	private AvatarBarButtonWidget m_AvatarColorButton;
	private AvatarBarButtonWidget m_AvatarHatButton;
	private AvatarBarButtonWidget m_AvatarAccessoriesButton;
	private AvatarBarButtonWidget m_AvatarBackgroundButton;
	private ArrayList<AvatarBarButtonWidget> m_AvatarButtonList = new ArrayList<AvatarBarButtonWidget>();

	private ArrayList<AssetItemWidget> m_CurrentItemList = new ArrayList<AssetItemWidget>();

	private AssetItemManager m_AssetItemManager;
	private TableLayout m_AssetItemTable;
	private FriendBean m_LocalFriendBean;
	private FriendBean m_LocalFriendBeanTempCopy;
	private RelativeLayout m_NameContainer;
	private TextView m_NameText;
	private EditText m_NameEditText;
	private View m_NameEditButton;
	private ImageView m_AvatarIcon;

	private boolean m_IsEditingName = false;
	private boolean m_IsPaused = false;
	private boolean m_IsInSetup;
	private DatabaseAdapter m_DatabaseAdapter;
	private Bitmap m_AvatarBitmap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.friend_edit_avatar_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		m_Activity = (FriendActivity) getActivity();
		m_DatabaseAdapter = m_Activity.getDatabaseAdapter();

		m_EditAvatarContainer = (RelativeLayout) getView().findViewById(R.id.edit_avatar_container);
		m_SetupDescriptionContainer = (RelativeLayout) getView().findViewById(R.id.friend_setup_step2_container);
		m_CancelButton = (Button) getView().findViewById(R.id.cancel_button);
		m_ConfirmButton = (Button) getView().findViewById(R.id.confirm_button);
		m_AssetItemTable = (TableLayout) getView().findViewById(R.id.assets_item_table);
		m_AvatarIcon = (ImageView) getView().findViewById(R.id.edit_avatar_icon);

		m_AvatarIconButton = (AvatarBarButtonWidget) getView().findViewById(R.id.avatar_icon_button);
		m_AvatarColorButton = (AvatarBarButtonWidget) getView().findViewById(R.id.avatar_color_button);
		m_AvatarHatButton = (AvatarBarButtonWidget) getView().findViewById(R.id.avatar_hat_button);
		m_AvatarAccessoriesButton = (AvatarBarButtonWidget) getView().findViewById(R.id.avatar_accessories_button);
		m_AvatarBackgroundButton = (AvatarBarButtonWidget) getView().findViewById(R.id.avatar_background_button);
		m_NameContainer = (RelativeLayout) getView().findViewById(R.id.name_container);
		m_NameText = (TextView) getView().findViewById(R.id.name_text);
		m_NameEditText = (EditText) getView().findViewById(R.id.name_edittext);
		m_NameEditButton = (View) getView().findViewById(R.id.name_edit_button);

		// adjust layout for parent / kid mode
		if (m_Activity.isParentMode()) {
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) m_EditAvatarContainer.getLayoutParams();
			param.width = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.edit_avatar_container_parent_mode_width);
			m_EditAvatarContainer.requestLayout();
			// param = (RelativeLayout.LayoutParams)
			// m_ConfirmButton.getLayoutParams();
			// param.rightMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.edit_avatar_circle_button_parent_mode_margin_side);
			// m_ConfirmButton.requestLayout();
			//
			// param = (RelativeLayout.LayoutParams)
			// m_CancelButton.getLayoutParams();
			// param.leftMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.edit_avatar_circle_button_parent_mode_margin_side);
			// m_CancelButton.requestLayout();
		} else {
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) m_EditAvatarContainer.getLayoutParams();
			param.width = m_Activity.getResources().getDimensionPixelSize(R.dimen.edit_avatar_container_width);
			m_EditAvatarContainer.requestLayout();
			// param = (RelativeLayout.LayoutParams)
			// m_ConfirmButton.getLayoutParams();
			// param.rightMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.edit_avatar_circle_button_margin_side);
			// m_ConfirmButton.requestLayout();
			//
			// param = (RelativeLayout.LayoutParams)
			// m_CancelButton.getLayoutParams();
			// param.leftMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.edit_avatar_circle_button_margin_side);
			// m_CancelButton.requestLayout();
		}

		m_AvatarIconButton.setInformation(AssetItemManager.CATEGORY_ID_AVATAR_ICON, R.drawable.friend_white_icon1);
		m_AvatarColorButton.setInformation(AssetItemManager.CATEGORY_ID_AVATAR_COLOR, R.drawable.friend_white_icon2);
		m_AvatarHatButton.setInformation(AssetItemManager.CATEGORY_ID_AVATAR_CLOTHES, R.drawable.friend_white_icon3);
		m_AvatarAccessoriesButton.setInformation(AssetItemManager.CATEGORY_ID_AVATAR_ACCESSORIES,
				R.drawable.friend_white_icon4);
		m_AvatarBackgroundButton.setInformation(AssetItemManager.CATEGORY_ID_AVATAR_BACKGROUND,
				R.drawable.friend_white_icon5);

		m_AvatarButtonList.clear();
		m_AvatarButtonList.add(m_AvatarIconButton);
		m_AvatarButtonList.add(m_AvatarColorButton);
		m_AvatarButtonList.add(m_AvatarHatButton);
		m_AvatarButtonList.add(m_AvatarAccessoriesButton);
		m_AvatarButtonList.add(m_AvatarBackgroundButton);

		for (AvatarBarButtonWidget widget : m_AvatarButtonList)
			widget.addButtonListener(m_AvatarButtonSelectedListener);

		m_CancelButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);
			}
		});

		m_ConfirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!m_Activity.getNetworkManager().checkWifiProcess()) {
					return;
				}

				if (m_IsInSetup) {
					if (m_Activity.isParentMode()) {
						m_Activity.createAccountNoKid(m_Activity.getCreatedUserName(), m_LocalFriendBean);
					} else {
						m_Activity.createAccount(m_Activity.getCreatedUserName(), m_LocalFriendBean);
					}
				} else {
					// sync the username from edit text
					m_NameText.setText(m_NameEditText.getText().toString());
					// update user name
					String updateName = m_NameText.getText().toString();
					if (updateName.isEmpty()) {
						LOG.W(TAG, "The user name is empty");
						return;
					}
					LOG.V(TAG, "updateName is " + updateName);
					// reset the temp bean
					m_LocalFriendBeanTempCopy = null;
					// check if user name is changed
					if (updateName.equals(m_LocalFriendBean.getName()) || m_Activity.isParentMode()) {
						// update user data with avatar
						m_LocalFriendBean.setName(updateName);
						updateUserData();
					} else {
						LOG.V(TAG, "user name has been changed");
						Intent intent = new Intent(IntentList.INTENT_ASK_MOM);
						intent.putExtra("dialogContent", getResources().getString(R.string.friend_ask_mom_edit_name));
						intent.putExtra("signIn", "");
						intent.putExtra("fullscreen", true);
						startActivityForResult(intent, ASK_MOM_REQUEST_CODE);
					}
				}
			}
		});

		m_NameEditButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				updateEditingName();
			}
		});
		m_NameEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					updateEditingName();
				}
				return false;
			}
		});

		m_Activity.onBackKeyPressed.addEventListener(new IEventListener() {

			@Override
			public void onEvent(Event event, Object[] objs) {
				if (!m_IsPaused) {
					LOG.V(TAG, "on back key pressed");
					event.setHandled();
					notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);
				}
			}
		});

		m_AssetItemManager = new AssetItemManager(m_Activity);

		// check if is in setup
		m_IsInSetup = m_Activity.isInSetup();
		if (m_IsInSetup) {
			m_NameContainer.setVisibility(View.INVISIBLE);
			m_SetupDescriptionContainer.setVisibility(View.VISIBLE);

			// setup default avatar
			FriendBean m_TempBean = new FriendBean();
			m_TempBean.setName(m_Activity.getCreatedUserName());
			m_TempBean.setCharacterTypeIndex(0);
			m_TempBean.setCharacterColorIndex(0);
			m_TempBean.setCharacterClothingIndex(-1);
			m_TempBean.setCharacterGlassesIndex(-1);
			m_TempBean.setCharacterHairBandIndex(-1);
			m_TempBean.setCharacterNeckTieIndex(-1);
			m_TempBean.setCharacterMoustacheIndex(-1);
			m_TempBean.setCharacterBackgroundColorIndex(4);
			m_LocalFriendBean = new FriendBean(m_TempBean);
		} else {
			m_SetupDescriptionContainer.setVisibility(View.INVISIBLE);
			UserData userData = m_Activity.getCurrentUserData();
			m_LocalFriendBean = new FriendBean();
			m_LocalFriendBean.setName(userData.userName);
			m_LocalFriendBean.setCharacterTypeIndex((int) userData.Character);
			m_LocalFriendBean.setCharacterColorIndex((int) userData.CharacterColor);
			m_LocalFriendBean.setCharacterClothingIndex((int) userData.CharacterClothing);

			ArrayList<Long> accessories = userData.CharacterAccessories;
			try {
				m_LocalFriendBean.setCharacterGlassesIndex(accessories.get(0).intValue());
				m_LocalFriendBean.setCharacterHairBandIndex(accessories.get(1).intValue());
				m_LocalFriendBean.setCharacterMoustacheIndex(accessories.get(2).intValue());
				m_LocalFriendBean.setCharacterNeckTieIndex(accessories.get(3).intValue());
			} catch (Throwable tr) {
				LOG.E(TAG, "Failed to load accessories", tr);
			}
			m_LocalFriendBean.setCharacterBackgroundColorIndex((int) userData.CharacterBackground);
		}

		m_NameText.setText(m_LocalFriendBean.getName());
		m_NameEditText.setText(m_LocalFriendBean.getName());

		// initial state
		updateItemTable(AssetItemManager.CATEGORY_ID_AVATAR_ICON);
		onAvatarButtonSelected(AssetItemManager.CATEGORY_ID_AVATAR_ICON);
		updateAvatarIcon(m_LocalFriendBean);
	}

	@Override
	public void onResume() {
		super.onResume();
		addApiEventListener();
		m_IsPaused = false;
	}

	@Override
	public void onPause() {
		removeApiEventListener();
		m_IsPaused = true;
		super.onPause();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		LOG.V(TAG, "onActivityResult() - requestCode is " + requestCode + " , resultCode is " + resultCode);
		if (requestCode == ASK_MOM_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				String updateName = m_NameText.getText().toString();
				LOG.V(TAG, "onActivityResult() - updateName is " + updateName);
				m_LocalFriendBean.setName(updateName);
				m_LocalFriendBeanTempCopy = new FriendBean(m_LocalFriendBean);
				updateUserData();
			} else {
				LOG.V(TAG, "onActivityResult() - not allowed to modify name");
			}
		}
	}

	private IButtonClickListener m_AvatarButtonSelectedListener = new IButtonClickListener() {

		@Override
		public void onButtonClicked(int buttonId, String viewName, Object[] args) {

			int categoryId = (Integer) args[0];
			updateItemTable(categoryId);
			onAvatarButtonSelected(categoryId);
		}
	};

	private IApiEventListener m_CreateAccountListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "create account successfully");
				String avatarPath = Utils.saveToInternalSorage(m_Activity,
						((BitmapDrawable) m_AvatarIcon.getDrawable()).getBitmap());
				LOG.V(TAG, "avatarPath is " + avatarPath);
				m_Activity.uploadUserAvatar(m_Activity.getCurrentUserData().userKey, avatarPath);
			} else {
				LOG.W(TAG, "failed to create account");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private IApiEventListener m_UpdateUserInfoWithAvatarListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_UpdateUserInfoWithAvatarListener successfully");
				// update database
				m_DatabaseAdapter.saveMyAvatar(m_Activity.getCurrentUserData().userKey,
						((BitmapDrawable) m_AvatarIcon.getDrawable()).getBitmap());

				FriendBean infoBean;
				if (m_LocalFriendBeanTempCopy != null) {
					infoBean = m_LocalFriendBeanTempCopy;
				} else {
					infoBean = m_LocalFriendBean;
				}

				uploadUserAvatarImage_outObj imageObj = (uploadUserAvatarImage_outObj) obj;
				notifyButtonListeners(CONFIRM_BUTTON_ID, TAG, new Object[] { infoBean, imageObj });
			} else {
				LOG.W(TAG, "failed to m_UpdateUserInfoWithAvatarListener");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private IApiEventListener m_UploadAvatarListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.W(TAG, "m_UploadAvatarListener successfully");

				// update database
				m_DatabaseAdapter.saveMyAvatar(m_Activity.getCurrentUserData().userKey,
						((BitmapDrawable) m_AvatarIcon.getDrawable()).getBitmap());

				uploadUserAvatarImage_outObj imageObj = (uploadUserAvatarImage_outObj) obj;
				notifyButtonListeners(CONFIRM_BUTTON_ID, TAG, new Object[] { m_LocalFriendBean, imageObj });
			} else {
				LOG.W(TAG, "m_UploadAvatarListener");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private void addApiEventListener() {
		m_Activity.onCreateAccount.addEventListener(m_CreateAccountListener);
		m_Activity.onUploadAvatar.addEventListener(m_UploadAvatarListener);
		m_Activity.onUpdateUserInfoWithAvatar.addEventListener(m_UpdateUserInfoWithAvatarListener);
	}

	private void removeApiEventListener() {
		m_Activity.onCreateAccount.removeEventListener(m_CreateAccountListener);
		m_Activity.onUploadAvatar.removeEventListener(m_UploadAvatarListener);
		m_Activity.onUpdateUserInfoWithAvatar.removeEventListener(m_UpdateUserInfoWithAvatarListener);
	}

	private void updateUserData() {
		String avatarPath = Utils.saveToInternalSorage(m_Activity,
				((BitmapDrawable) m_AvatarIcon.getDrawable()).getBitmap());
		LOG.V(TAG, "avatarPath is " + avatarPath);
		m_Activity.updateUserInfoWithAvatar(m_Activity.getCurrentUserData().userKey, m_LocalFriendBean, avatarPath);
	}

	private void updateEditingName() {
		if (!m_IsEditingName) {
			m_NameText.setVisibility(View.INVISIBLE);
			m_NameEditText.setVisibility(View.VISIBLE);
			m_NameEditText.requestFocus();

			m_NameEditButton.setBackgroundResource(R.drawable.friend_pen_hover);
			m_NameContainer.setBackgroundResource(R.drawable.friend_headphoto_up_bg);

			// show keyboard
			InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(m_NameEditText, InputMethodManager.SHOW_IMPLICIT);
			m_IsEditingName = true;
			m_NameEditText.setSelection(m_NameEditText.getText().length());
		} else {
			// hide keyboard
			InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(m_NameEditText.getWindowToken(), 0);
			m_NameText.setText(m_NameEditText.getText().toString());

			m_NameText.setVisibility(View.VISIBLE);
			m_NameEditText.setVisibility(View.INVISIBLE);

			m_NameEditButton.setBackgroundResource(R.drawable.friend_pen);
			m_NameContainer.setBackgroundDrawable(null);

			m_IsEditingName = false;
		}
	}

	private void updateAvatarIcon(FriendBean bean) {

		if (m_AvatarBitmap != null) {
			m_AvatarBitmap.recycle();
			System.gc();
		}
		m_AvatarBitmap = AvatarManager.getAvatarIconBitmap(m_Activity, bean);
		if (m_AvatarBitmap != null) {
			m_AvatarIcon.setImageBitmap(m_AvatarBitmap);
		}
	}

	private void onAvatarButtonSelected(int index) {
		for (AvatarBarButtonWidget widget : m_AvatarButtonList) {
			widget.onButtonSelected(index);
		}
	}

	private void updateItemTable(int categoryId) {
		m_AssetItemTable.removeAllViews();
		TableRow tableRow = new TableRow(m_Activity);
		// generate widget list
		m_CurrentItemList = new ArrayList<AssetItemWidget>();

		Enumeration<Integer> keys;
		Enumeration<Pair<Integer, Integer>> pairKeys;
		switch (categoryId) {
		case AssetItemManager.CATEGORY_ID_AVATAR_ICON:
			keys = AvatarManager.AvatarIconTable.keys();
			while (keys.hasMoreElements()) {
				Integer keyInt = keys.nextElement();
				Integer resId = AvatarManager.AvatarIconTable.get(keyInt);
				AssetItemWidget widget = new AssetItemWidget(m_Activity, keyInt, categoryId, resId,
						R.drawable.friend_list_bg_hover, SelectionStatus.UnSelected);
				m_CurrentItemList.add(widget);
			}
			break;
		case AssetItemManager.CATEGORY_ID_AVATAR_COLOR:
			pairKeys = AvatarManager.AvatarColorTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarColorTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId, resId,
							R.drawable.friend_list_bg_hover, SelectionStatus.UnSelected);
					m_CurrentItemList.add(widget);
				}
			}
			break;
		case AssetItemManager.CATEGORY_ID_AVATAR_CLOTHES:
			pairKeys = AvatarManager.AvatarHatTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarHatTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId,
							AssetItemManager.SUBCATEGORY_ID_HAT, resId, R.drawable.friend_vvv,
							SelectionStatus.UnSelected, 0);
					m_CurrentItemList.add(widget);
				}
			}
			break;
		case AssetItemManager.CATEGORY_ID_AVATAR_ACCESSORIES:
			// glasses
			pairKeys = AvatarManager.AvatarGlassesTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarGlassesTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId,
							AssetItemManager.SUBCATEGORY_ID_GLASSES, resId, R.drawable.friend_vvv,
							SelectionStatus.UnSelected, 0);
					m_CurrentItemList.add(widget);
				}
			}

			// hair band
			pairKeys = AvatarManager.AvatarHairBandTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarHairBandTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId,
							AssetItemManager.SUBCATEGORY_ID_HAIRBAND, resId, R.drawable.friend_vvv,
							SelectionStatus.UnSelected, 0);
					m_CurrentItemList.add(widget);
				}
			}

			// neck tie
			pairKeys = AvatarManager.AvatarNeckTieTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarNeckTieTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId,
							AssetItemManager.SUBCATEGORY_ID_NECKTIE, resId, R.drawable.friend_vvv,
							SelectionStatus.UnSelected, 0);
					m_CurrentItemList.add(widget);
				}
			}

			// moustache
			pairKeys = AvatarManager.AvatarMoustacheTable.keys();
			while (pairKeys.hasMoreElements()) {
				Pair<Integer, Integer> keyPair = pairKeys.nextElement();
				if (keyPair.first == m_LocalFriendBean.getCharacterTypeIndex()) {
					Integer resId = AvatarManager.AvatarMoustacheTable.get(keyPair);
					AssetItemWidget widget = new AssetItemWidget(m_Activity, keyPair.second, categoryId,
							AssetItemManager.SUBCATEGORY_ID_MOUSTACHE, resId, R.drawable.friend_vvv,
							SelectionStatus.UnSelected, 0);
					m_CurrentItemList.add(widget);
				}
			}

			break;

		case AssetItemManager.CATEGORY_ID_AVATAR_BACKGROUND:
			keys = AvatarManager.AvatarBackgroundTable.keys();
			while (keys.hasMoreElements()) {
				Integer keyInt = keys.nextElement();
				Integer colorResId = AvatarManager.AvatarBackgroundTable.get(keyInt);
				AssetItemWidget widget = new AssetItemWidget(m_Activity, keyInt, categoryId,
						AssetItemWidget.NON_SUBCATEGORY_ID, 0, R.drawable.friend_list_bg_hover,
						SelectionStatus.UnSelected, colorResId);
				m_CurrentItemList.add(widget);
			}
			break;
		}

		// add view into table
		for (int i = 0; i < m_CurrentItemList.size(); i++) {
			AssetItemWidget widget = m_CurrentItemList.get(i);
			if (widget.getParent() != null)
				((ViewGroup) widget.getParent()).removeAllViews();
			widget.addButtonListener(this);

			int paddingLeft = m_Activity.getResources().getDimensionPixelSize(R.dimen.asset_item_row_padding_left);
			int paddingTop = m_Activity.getResources().getDimensionPixelSize(R.dimen.asset_item_row_padding_top);
			tableRow.setPadding(paddingLeft, paddingTop, 0, paddingTop);

			// check the selected status
			m_AssetItemManager.checkSelectedStatusByFriendBean(widget, m_LocalFriendBean);

			if (i % 2 == 0) {
				tableRow = new TableRow(m_Activity);
				tableRow.setOrientation(TableRow.HORIZONTAL);
				m_AssetItemTable.addView(tableRow);
			}
			tableRow.addView(widget);
		}
	}

	/*
	 * ======================================================================
	 * Add listners for button
	 * =======================================================================
	 */
	public void notifyButtonListeners(int buttonID, String tag, Object[] args) {
		if (m_ButtonListeners != null) {
			for (IButtonClickListener listener : m_ButtonListeners) {
				listener.onButtonClicked(buttonID, tag, args);
			}
		}
	}

	/*
	 * ======================================================================
	 * Notify button listeners
	 * =======================================================================
	 */
	public void addButtonListener(IButtonClickListener listener) {
		if (m_ButtonListeners == null) {
			m_ButtonListeners = new ArrayList<IButtonClickListener>();
		}
		m_ButtonListeners.add(listener);
	}

	@Override
	public void onButtonClicked(int buttonId, String viewName, Object[] args) {

		if (viewName.equals(AssetItemWidget.TAG)) {
			AssetItemWidget widget = (AssetItemWidget) args[0];
			LOG.V(TAG, "widget has been clicked : " + widget.getItemId());

			// update local friend bean
			int updatedItemId = widget.getItemId();

			if (widget.getSelectStatus() == SelectionStatus.UnSelected && widget.isCancelable()) {
				LOG.V(TAG, "onCancelable and unSelected, set index as -1");
				updatedItemId = -1;
			}

			switch (widget.getCategoryId()) {
			case AssetItemManager.CATEGORY_ID_AVATAR_ICON:
				m_LocalFriendBean.setCharacterTypeIndex(updatedItemId);
				break;
			case AssetItemManager.CATEGORY_ID_AVATAR_COLOR:
				m_LocalFriendBean.setCharacterColorIndex(updatedItemId);
				break;
			case AssetItemManager.CATEGORY_ID_AVATAR_CLOTHES:
				m_LocalFriendBean.setCharacterClothingIndex(updatedItemId);
				break;
			case AssetItemManager.CATEGORY_ID_AVATAR_ACCESSORIES: {

				switch (widget.getSubCategoryId()) {
				case AssetItemManager.SUBCATEGORY_ID_GLASSES:
					m_LocalFriendBean.setCharacterGlassesIndex(updatedItemId);
					break;
				case AssetItemManager.SUBCATEGORY_ID_HAIRBAND:
					m_LocalFriendBean.setCharacterHairBandIndex(updatedItemId);
					break;
				case AssetItemManager.SUBCATEGORY_ID_MOUSTACHE:
					m_LocalFriendBean.setCharacterMoustacheIndex(updatedItemId);
					break;
				case AssetItemManager.SUBCATEGORY_ID_NECKTIE:
					m_LocalFriendBean.setCharacterNeckTieIndex(updatedItemId);
					break;
				}
			}
				break;
			case AssetItemManager.CATEGORY_ID_AVATAR_BACKGROUND:
				m_LocalFriendBean.setCharacterBackgroundColorIndex(updatedItemId);
				break;
			}

			updateAvatarIcon(m_LocalFriendBean);

			// check the selected status
			for (AssetItemWidget innerWidget : m_CurrentItemList)
				m_AssetItemManager.checkSelectedStatusByFriendBean(innerWidget, m_LocalFriendBean);
		}
	}
}