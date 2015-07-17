package com.fuhu.nabiconnect.friend.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.data.FriendData;
import com.fuhu.data.PendingFriendRequestData;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.friend.dialog.AddFriendDialog;
import com.fuhu.nabiconnect.friend.dialog.BlockedDialog;
import com.fuhu.nabiconnect.friend.dialog.FriendRequestSent;
import com.fuhu.nabiconnect.friend.dialog.PopupDialog;
import com.fuhu.nabiconnect.friend.widget.FriendItemWidget;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.nsa.fragment.FragmentNSA;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.ndnslibsoutstructs.pendingFriendRequests_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;

public class FriendMainFragment extends Tracking.TrackingInfoFragment {

	public static final String TAG = "FriendMainFragment";

	public static final int EDIT_AVATAR_BUTTON_ID = 100;
	private static final int MSG_REFRESH_LIST = 10001;
	private static final int LIST_REFRESH_TIME = 30 * 1000;
	private ListView m_FriendListView;
	private Hashtable<String, Bitmap> m_FriendAvatarTable = new Hashtable<String, Bitmap>();
	private ArrayList<FriendListItemProperty> m_FriendItemList = new ArrayList<FriendListItemProperty>();
	private ArrayList<FriendListItemProperty> mPendingFriendListBuffer = new ArrayList<FriendListItemProperty>();
	private ArrayList<FriendListItemProperty> mFriendListBuffer = new ArrayList<FriendListItemProperty>();
	private FriendWidgetAdapter m_FriendWidgetAdapter;
	private ArrayList<LoadAvatarBitmapTask> m_LoadAvatarTasks = new ArrayList<LoadAvatarBitmapTask>();

	private FriendActivity m_Activity;
	private RelativeLayout m_SelfInformationContainer;
	private AddFriendDialog m_AddFriendDialog;
	private Button m_AddFriendButton;
	private TextView m_FriendCodeText;
	private Button m_EditAvatarButton;
	private ArrayList<IButtonClickListener> m_ButtonListeners;
	private TextView m_KidNameText;
	private ImageView m_SelfAvatarIcon;
	private TextView m_FriendListTitleText;
	private TextView m_YourCodeDescription;
	private View m_HiddenButton;
	private boolean m_IsPreloading = true;
	private DatabaseAdapter m_DatabaseAdapter;
	private String m_InputFriendCode;

	private UserData m_UserData;
	private PendingFriendRequestData m_CurrentFriendRequest;
	private String m_DeletedFriendId;

	private Bitmap defaultAvatar;

	private FriendRequestSent m_FriendRequestSent;

    public FriendMainFragment() {
        super(FriendMainFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return "landing_page";
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.friend_main_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		m_Activity = (FriendActivity) getActivity();
		m_UserData = m_Activity.getCurrentUserData();

		defaultAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.chat_avatar_default);

		m_SelfInformationContainer = (RelativeLayout) getView().findViewById(R.id.self_information_container);
		// adjust layout for parent / kid mode
		RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) m_SelfInformationContainer.getLayoutParams();
		if (m_Activity.isParentMode()) {
			param.width = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.self_information_container_parent_mode_width);
		} else {
			param.width = m_Activity.getResources().getDimensionPixelSize(R.dimen.self_information_container_width);
		}
		m_SelfInformationContainer.requestLayout();

		m_AddFriendButton = (Button) getView().findViewById(R.id.add_friend_button);
		m_FriendListTitleText = (TextView) getView().findViewById(R.id.friend_list_title_text);

		m_FriendListView = (ListView) getView().findViewById(R.id.friend_items_list_view);
		m_FriendWidgetAdapter = new FriendWidgetAdapter(m_Activity, 0, m_FriendItemList);
		m_FriendListView.setAdapter(m_FriendWidgetAdapter);

		m_FriendCodeText = (TextView) getView().findViewById(R.id.friend_code_text);
		m_EditAvatarButton = (Button) getView().findViewById(R.id.edit_avatar_button);
		m_KidNameText = (TextView) getView().findViewById(R.id.kid_name_text);
		m_SelfAvatarIcon = (ImageView) getView().findViewById(R.id.self_avatar_icon);

		m_YourCodeDescription = (TextView) getView().findViewById(R.id.your_code_description);
		m_HiddenButton = (View) getView().findViewById(R.id.hidden_button);

		m_AddFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
				if (m_AddFriendDialog != null && m_AddFriendDialog.isShowing()) {
					LOG.V(TAG, "m_AddFriendDialog is already shown");
					return;
				}

				m_AddFriendDialog = new AddFriendDialog(m_Activity);
				m_AddFriendDialog.addButtonListener(m_OnButtonClickListener);
				m_AddFriendDialog.show();

                //tracking
                Tracking.pushTrack(v.getContext(), "add_friend");
			}
		});

		m_EditAvatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
			public void onClick(View v) {
				notifyButtonListeners(EDIT_AVATAR_BUTTON_ID, TAG, null);

                //tracking
                Tracking.pushTrack(v.getContext(), "edit_character");
			}
		});

		// get database adapter
		m_DatabaseAdapter = m_Activity.getDatabaseAdapter();
	}

	@Override
	public void onResume() {
		super.onResume();
		LOG.V(TAG, "onResume() - start");

		// add api event listener
		addApiEventListener();

		// update Kid info
		updateKidInformation();

		// get friend list from cache
		ArrayList<FriendData> friendCache = m_DatabaseAdapter.getFriendList(m_UserData.userKey);
		friends_outObj tempObj = new friends_outObj();
		tempObj.addFriendData(friendCache);
		updateFriendWidgetList(tempObj);

		if (!m_Activity.isFinishing()) {
			m_Activity.getPendingFriendList(m_UserData.userKey, false);
		}
		LOG.V(TAG, "onResume() - end");
	}

	@Override
	public void onPause() {
		super.onPause();
		closeDialog(m_AddFriendDialog);

		for (LoadAvatarBitmapTask task : m_LoadAvatarTasks) {
			task.cancel(true);
		}
		m_LoadAvatarTasks.clear();

		// m_FriendItemList.clear();
		// m_FriendWidgetAdapter = null;

		// remove api event listener
		removeApiEventListener();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		m_FriendAvatarTable.clear();
	}

	private IApiEventListener m_GetFriendEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				friends_outObj data = (friends_outObj) obj;

				if (data == null) {
					LOG.V(TAG, "m_GetFriendEventListener - data is null");
					return;
				}

				m_DatabaseAdapter.updateFriendList(data);

				// get cache from database
				ArrayList<FriendData> friendCache = m_DatabaseAdapter.getFriendList(m_UserData.userKey);
				friends_outObj tempObj = new friends_outObj();
				tempObj.addFriendData(friendCache);
				updateFriendWidgetList(tempObj);
			} else {
				LOG.V(TAG, "m_GetFriendEventListener - failed to get friend list");
				m_Activity.showGeneralWarningDialog();
			}
			m_Handler.sendEmptyMessageDelayed(MSG_REFRESH_LIST, LIST_REFRESH_TIME);
		}
	};

	private IApiEventListener m_GetFriendRequestEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				pendingFriendRequests_outObj data = (pendingFriendRequests_outObj) obj;
				if (data == null) {
					LOG.V(TAG, "m_GetFriendRequestEventListener - data is null");
					return;
				}

				// update friend list
				updateFriendRequestWidgetList(data);
			} else {
				LOG.V(TAG, "m_GetFriendRequestEventListener - failed to get friend request list");
				m_Activity.showGeneralWarningDialog();
			}
			// get friend list
			m_Activity.getFriendList(m_Activity.getCurrentUserData().userKey, false);
		}
	};

	private IApiEventListener m_AcceptFriendRequestEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_AcceptFriendRequestEventListener - success");
				closeDialog(m_AddFriendDialog);
				// get friend request list
				m_Activity.getPendingFriendList(m_Activity.getCurrentUserData().userKey);
			} else {
				try {
					JSONObject jobj = new JSONObject(obj.toString());
					if (jobj.getString("status").equals("8085")) {
						// on block list
						closeDialog(m_AddFriendDialog);
						new BlockedDialog(getActivity()).show();
					} else if (jobj.getString("status").equals("8043")) {
						// already sent
						m_AddFriendDialog.setRequestAlreadySent(true);
					} else {
						LOG.V(TAG, "m_AcceptFriendRequestEventListener - failed to accept friend");
						m_AddFriendDialog.setIsInvalid(true);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					LOG.V(TAG, "m_AcceptFriendRequestEventListener - failed to accept friend");
					m_AddFriendDialog.setIsInvalid(true);
				}
			}
		}
	};
	private IApiEventListener m_DenyFriendRequestEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_DenyFriendRequestEventListener - success");
				m_Activity.getPendingFriendList(m_Activity.getCurrentUserData().userKey);
			} else {
				LOG.V(TAG, "m_DenyFriendRequestEventListener - failed to accept friend");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};
	private IApiEventListener m_MakeFriendEventListener = new IApiEventListener() {
		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_MakeFriendEventListener - success");
				// dismiss dialog
				closeDialog(m_AddFriendDialog);
				// notify GCM server
				NabiNotificationManager notificationManager = m_Activity.getNabiNotificationManager();
				notificationManager.notifyServerByFriendCode(m_InputFriendCode,
						m_Activity.getCurrentUserData().userName,
						m_Activity.getString(R.string.notification_friend_description),
						NabiNotificationManager.APPLICATION_NAME_FRIEND, new GCMSenderEventCallback() {

							@Override
							public void onSendMessageSuccess() {
							}

							@Override
							public void onMessgaeSendingError(int errorCode) {
							}
						});

				m_FriendRequestSent = new FriendRequestSent(m_Activity);
				m_FriendRequestSent.setCancelable(false);
				m_FriendRequestSent.addButtonListener(new IButtonClickListener() {
					@Override
					public void onButtonClicked(int buttonId, String viewName, Object[] args) {
						// ChooseContactDialog @ erase's folder
						if (buttonId == FriendRequestSent.OK_BUTTON_ID) {
						}
						m_FriendRequestSent.dismiss();
					}
				});
				m_FriendRequestSent.show();
			} else {
				try {
					JSONObject jobj = new JSONObject(obj.toString());
					if (jobj.getString("status").equals("8085")) {
						// on block list
						closeDialog(m_AddFriendDialog);
						new BlockedDialog(getActivity()).show();
					} else if (jobj.getString("status").equals("8043")) {
						// already sent
						m_AddFriendDialog.setRequestAlreadySent(true);
					} else {
						LOG.V(TAG, "m_MakeFriendEventListener - failed to make friend");
						m_AddFriendDialog.setIsInvalid(true);
					}
				} catch (JSONException e) {
					e.printStackTrace();
					LOG.V(TAG, "m_MakeFriendEventListener - failed to make friend");
					m_AddFriendDialog.setIsInvalid(true);
				}
			}
		}
	};

	private IApiEventListener m_RemoveFriendEventListener = new IApiEventListener() {

		@Override
		public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
			if (isSuccess) {
				LOG.V(TAG, "m_RemoveFriendEventListener - success");

				// update database
				LOG.V(TAG, "m_RemoveFriendEventListener -  m_DeletedFriendId is " + m_DeletedFriendId);
				if (m_Activity.isParentMode()) {
					// there is no block function in parent mode, just delete
					// this friend
					m_DatabaseAdapter.deleteFriend(m_Activity.getCurrentUserData().userKey, m_DeletedFriendId);
				} else {
					m_DatabaseAdapter.blockFriend(m_Activity.getCurrentUserData().userKey, m_DeletedFriendId);
				}
				// get friend list
				m_Activity.getFriendList(m_Activity.getCurrentUserData().userKey);
			} else {
				LOG.V(TAG, "m_RemoveFriendEventListener - failed to remove friend");
				m_Activity.showGeneralWarningDialog();
			}
		}
	};

	private IButtonClickListener m_OnButtonClickListener = new IButtonClickListener() {

		@Override
		public void onButtonClicked(int buttonId, String viewName, Object[] args) {

			if (viewName.equals(AddFriendDialog.TAG)) {
				switch (buttonId) {
				case AddFriendDialog.YES_BUTTON_ID:
					if (!m_Activity.getNetworkManager().checkWifiProcess())
						return;

					String inputFriendCode = (String) args[0];

					// store input friend code
					m_InputFriendCode = inputFriendCode;

					if (m_AddFriendDialog.isForAccept()) {
						// accept friend request
						m_Activity.acceptFriend(m_Activity.getCurrentUserData().userKey, m_CurrentFriendRequest.userID,
								inputFriendCode);
					} else {
						// send friend request
						m_Activity.makeFriend(m_Activity.getCurrentUserData().userKey, inputFriendCode);
					}

					break;
				case AddFriendDialog.CANCEL_BUTTON_ID:
					// LOG.V(TAG, "m_AddFriendDialog CANCEL");
					closeDialog(m_AddFriendDialog);
					break;
				}
			} else if (viewName.equals(FriendItemWidget.TAG)) {
				// PersonInfo info = (PersonInfo)args[0];
				if (!m_Activity.getNetworkManager().checkWifiProcess())
					return;

				switch (buttonId) {
				case FriendItemWidget.ACCEPT_BUTTON_ID:
					// LOG.V(TAG,
					// "ACCEPT request from "+info.getBean().getAccountId());

					m_CurrentFriendRequest = (PendingFriendRequestData) args[0];
					LOG.V(TAG, "m_CurrentFriendRequest is " + m_CurrentFriendRequest);

					// acceptFriendRequest(info);
					if (m_AddFriendDialog != null && m_AddFriendDialog.isShowing()) {
						LOG.V(TAG, "m_AddFriendDialog is already shown");
						return;
					}

					m_AddFriendDialog = new AddFriendDialog(m_Activity);
					m_AddFriendDialog.setIsForAccept(true);
					m_AddFriendDialog.addButtonListener(m_OnButtonClickListener);
					m_AddFriendDialog.show();

					break;
				case FriendItemWidget.REJECT_BUTTON_ID:
					// LOG.V(TAG,
					// "REJECT request from "+info.getBean().getAccountId());
					m_CurrentFriendRequest = (PendingFriendRequestData) args[0];
					LOG.V(TAG, "m_CurrentFriendRequest is " + m_CurrentFriendRequest);

					m_Activity.denyFriend(m_Activity.getCurrentUserData().userKey, m_CurrentFriendRequest.userID);
					// rejectFriendRequest(info);
					break;
				case FriendItemWidget.DELETE_BUTTON_ID:
					// LOG.V(TAG,
					// "DELETE request from "+info.getBean().getAccountId());
					FriendData friendData = (FriendData) args[0];
					m_Activity.removeFriend(m_Activity.getCurrentUserData().userKey, friendData.userID);
					// deleteFriend(info);
					break;
				}
			}
		}
	};

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			LOG.V(TAG, "Receive msg " + msg.what);
			switch (msg.what) {
			case MSG_REFRESH_LIST:
				m_Handler.removeMessages(MSG_REFRESH_LIST);
				// get friend request list
				if (m_Activity != null) {
					if (m_Activity.getCurrentUserData() != null)
						m_Activity.getPendingFriendList(m_Activity.getCurrentUserData().userKey, false);
				}
				break;
			}
		}
	};

	private void closeDialog(PopupDialog dialog) {

		if (dialog == null) {
			LOG.W(TAG, "closeDialog() - dialog is null.");
			return;
		}

		if (!dialog.isShowing()) {
			LOG.V(TAG, "dialog is hidden.");
			return;
		} else
			dialog.dismiss();
	}

	private void updateFriendWidgetList(friends_outObj updatedList) {
		mFriendListBuffer.clear();
		ArrayList<FriendData> list = new ArrayList<FriendData>();
		list.addAll(updatedList.getFriends());
		Collections.sort(list, mComparator);
		for (FriendData fd : list) {
			mFriendListBuffer.add(new FriendListItemProperty(fd, null));
		}
		m_FriendWidgetAdapter.clear();
		m_FriendItemList.addAll(mPendingFriendListBuffer);
		m_FriendItemList.addAll(mFriendListBuffer);
		mFriendListBuffer.clear();
		m_FriendWidgetAdapter.notifyDataSetChanged();
	}

	// private void addFriendDataIntoList(friends_outObj updatedList) {
	// // remove old friend data
	// ArrayList<FriendListItemProperty> removeItems = new
	// ArrayList<FriendListItemProperty>();
	// for (FriendListItemProperty innerProperty : m_FriendItemList) {
	// if (innerProperty.friendData != null)
	// removeItems.add(innerProperty);
	// }
	// m_FriendItemList.removeAll(removeItems);
	//
	// Collections.sort(updatedList.getFriends(), mComparator);
	//
	// // add new friend data
	// for (final FriendData fData : updatedList.getFriends()) {
	// FriendListItemProperty property = new FriendListItemProperty(fData,
	// null);
	// m_FriendItemList.add(property);
	//
	// // update avatar hashtable
	// Bitmap bitmap =
	// m_Activity.getDatabaseAdapter().getFriendAvatar(m_Activity.getCurrentUserData().userKey,
	// fData.userID,
	// m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_item_avatar_size));
	//
	// if (bitmap != null)
	// m_FriendAvatarTable.put(fData.userID, bitmap);
	//
	// // load picture from server
	// LoadAvatarBitmapTask bitmapTask = new LoadAvatarBitmapTask();
	// m_LoadAvatarTasks.add(bitmapTask);
	// Utils.executeAsyncTask(bitmapTask, new
	// LoadAvatarBitmapTask.IOnBitmapLoaded() {
	//
	// @Override
	// public void onBitmapLoaded(Bitmap bitmap) {
	//
	// if (bitmap == null) {
	// LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
	// return;
	// }
	//
	// // save bitmap
	// m_Activity.getDatabaseAdapter().saveAvatarAsync(m_Activity.getCurrentUserData().userKey,
	// fData.userID, bitmap);
	//
	// // update hashtable
	// m_FriendAvatarTable.put(fData.userID, bitmap);
	//
	// // notify data changed
	// if (m_FriendWidgetAdapter != null) {
	// m_FriendWidgetAdapter.notifyDataSetChanged();
	// }
	//
	// }
	// }, fData.AvatarUrl);
	// }
	// }

	// private void addFriendRequestIntoList(pendingFriendRequests_outObj
	// updatedList) {
	// ================== old code ======================
	// remove old friend request data
	// ArrayList<FriendListItemProperty> removeItems = new
	// ArrayList<FriendListItemProperty>();
	// for (FriendListItemProperty innerProperty : m_FriendItemList) {
	// if (innerProperty.getFriendRequest() != null)
	// removeItems.add(innerProperty);
	// }
	// m_FriendItemList.removeAll(removeItems);
	// // add new friend data
	// for (int i = updatedList.getPendingFriendRequests().size() - 1; i >=
	// 0; i--) {
	// final PendingFriendRequestData fData =
	// updatedList.getPendingFriendRequests().get(i);
	// FriendListItemProperty property = new FriendListItemProperty();
	// property.setFriendRequest(fData);
	// m_FriendItemList.add(0, property);
	// // load picture from server
	// LoadAvatarBitmapTask bitmapTask = new LoadAvatarBitmapTask();
	// m_LoadAvatarTasks.add(bitmapTask);
	// Utils.executeAsyncTask(bitmapTask, new
	// LoadAvatarBitmapTask.IOnBitmapLoaded() {
	// @Override
	// public void onBitmapLoaded(Bitmap bitmap) {
	// if (bitmap == null) {
	// LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
	// return;
	// }
	// // update hashtable
	// m_FriendAvatarTable.put(fData.userID, bitmap);
	// // notify data changed
	// if (m_FriendWidgetAdapter != null)
	// m_FriendWidgetAdapter.notifyDataSetChanged();
	// }
	// }, fData.AvatarUrl);
	// }
	// }

	private void updateFriendRequestWidgetList(pendingFriendRequests_outObj updatedList) {
		mPendingFriendListBuffer.clear();
		for (PendingFriendRequestData pfrd : updatedList.getPendingFriendRequests()) {
			mPendingFriendListBuffer.add(new FriendListItemProperty(null, pfrd));
		}
	}

	private void updateKidInformation() {
		if (m_UserData != null) {
			m_KidNameText.setText(m_UserData.userName);
			m_FriendCodeText.setText(m_UserData.friendCode);

			// load avatar from db
			final Bitmap selfBitmap = m_DatabaseAdapter.getMyAvatar(m_UserData.userKey, m_Activity.getResources()
					.getDimensionPixelSize(R.dimen.self_avatar_icon_size));
			if (selfBitmap != null) {
				m_SelfAvatarIcon.setImageBitmap(selfBitmap);
			} else {
				LOG.V(TAG, "updateKidInformation() - bitmap from db is null");
			}

			// load from server
			LoadAvatarBitmapTask bitmapTask = new LoadAvatarBitmapTask();
			m_LoadAvatarTasks.add(bitmapTask);
			Utils.executeAsyncTask(bitmapTask, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

				@Override
				public void onBitmapLoaded(Bitmap bitmap) {

					if (bitmap == null) {
						LOG.E(TAG, "onBitmapLoaded() - bitmap from server is null");
						return;
					}

					// update database
					m_DatabaseAdapter.saveAvatarAsync(m_UserData.userKey, m_UserData.userKey, bitmap);

					// update icon
					BitmapDrawable from = new BitmapDrawable(getResources(), selfBitmap == null ? defaultAvatar
							: selfBitmap);
					BitmapDrawable to = new BitmapDrawable(getResources(), bitmap);
					Drawable[] drawables = new Drawable[2];
					drawables[0] = from;
					drawables[1] = to;
					TransitionDrawable td = new TransitionDrawable(drawables);
					td.setCrossFadeEnabled(true);
					if (selfBitmap != null) {
						td.startTransition(0);
					} else {
						td.startTransition(300);
					}
					m_SelfAvatarIcon.setImageDrawable(td);

				}
			}, m_UserData.avatarURL);
		}
	}

	private void addApiEventListener() {
		// LOG.V(TAG, "addApiEventListener() - start");
		m_Activity.onGetFriendList.addEventListener(m_GetFriendEventListener);
		m_Activity.onGetFriendRequestList.addEventListener(m_GetFriendRequestEventListener);
		m_Activity.onAcceptFriendRequest.addEventListener(m_AcceptFriendRequestEventListener);
		m_Activity.onDenyFriendRequest.addEventListener(m_DenyFriendRequestEventListener);
		m_Activity.onRemoveFriend.addEventListener(m_RemoveFriendEventListener);
		m_Activity.onMakeFriend.addEventListener(m_MakeFriendEventListener);
		// LOG.V(TAG, "addApiEventListener() - end");
	}

	private void removeApiEventListener() {
		// LOG.V(TAG, "removeApiEventListener() - start");
		m_Activity.onGetFriendList.removeEventListener(m_GetFriendEventListener);
		m_Activity.onGetFriendRequestList.removeEventListener(m_GetFriendRequestEventListener);
		m_Activity.onAcceptFriendRequest.removeEventListener(m_AcceptFriendRequestEventListener);
		m_Activity.onDenyFriendRequest.removeEventListener(m_DenyFriendRequestEventListener);
		m_Activity.onRemoveFriend.removeEventListener(m_RemoveFriendEventListener);
		m_Activity.onMakeFriend.removeEventListener(m_MakeFriendEventListener);
		// LOG.V(TAG, "removeApiEventListener() - end");
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

	private class FriendWidgetAdapter extends ArrayAdapter<FriendListItemProperty> {

		private LayoutInflater inflater;
		private FriendActivity activity;
		private AQuery aq;

		public FriendWidgetAdapter(Context context, int resource, List<FriendListItemProperty> objects) {
			super(context, resource, objects);
			inflater = LayoutInflater.from(context);
			if (context instanceof FriendActivity) {
				activity = (FriendActivity) context;
			}
			aq = new AQuery(context);
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			FriendItemWidgetHolder holder;
			if (convertView == null) {
				holder = new FriendItemWidgetHolder();
				convertView = inflater.inflate(R.layout.friend_item_widget, parent, false);

				holder.m_BackgroundContainer = (RelativeLayout) convertView.findViewById(R.id.friend_item_background);
				holder.m_AvatarImageView = (ImageView) convertView.findViewById(R.id.friend_avatar);
				holder.m_FriendRequestContainer = (RelativeLayout) convertView
						.findViewById(R.id.friend_request_container);
				holder.m_FriendRequestName = (TextView) convertView.findViewById(R.id.friend_request_name);
				holder.m_FriendName = (TextView) convertView.findViewById(R.id.friend_name);
				holder.m_FriendRequestDescription = (TextView) convertView.findViewById(R.id.be_friend_string);
				holder.m_AcceptButton = (Button) convertView.findViewById(R.id.accept_button);
				holder.m_RejectButton = (Button) convertView.findViewById(R.id.reject_button);
				holder.m_DeleteFriendContainer = (RelativeLayout) convertView
						.findViewById(R.id.delete_friend_container);
				holder.m_DeleteFriendIcon = (ImageView) convertView.findViewById(R.id.delete_friend_icon);

				convertView.setTag(holder);
			} else {
				holder = (FriendItemWidgetHolder) convertView.getTag();
			}

			aq.recycle(convertView);

			// if (activity != null) {
			// AbsListView.LayoutParams params = (AbsListView.LayoutParams)
			// holder.m_BackgroundContainer
			// .getLayoutParams();
			// if (activity.isParentMode()) {
			// params.width = activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_item_background_parent_mode_width);
			// holder.m_FriendName.setWidth(m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_item_friend_name_parent_mode_width));
			// holder.m_FriendRequestName.setWidth(m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_item_request_name_parent_mode_width));
			// } else {
			// params.width =
			// activity.getResources().getDimensionPixelSize(R.dimen.friend_item_background_width);
			// holder.m_FriendName.setWidth(m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_item_friend_name_nabi_mode_width));
			// holder.m_FriendRequestName.setWidth(m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_item_request_name_nabi_mode_width));
			// }
			// holder.m_BackgroundContainer.requestLayout();
			// }

			final FriendData friendData = getItem(position).friendData;
			final PendingFriendRequestData friendRequest = getItem(position).friendRequest;
			final FriendListItemProperty.DeleteStatus deleteStatus = getItem(position).deleteStatus;

			if (friendData != null) {
				holder.m_BackgroundContainer.setBackgroundColor(m_Activity.getResources().getColor(
						R.color.friend_list_item_color));
				holder.m_FriendRequestContainer.setVisibility(View.INVISIBLE);
				holder.m_FriendName.setVisibility(View.VISIBLE);
				holder.m_AcceptButton.setVisibility(View.INVISIBLE);
				holder.m_RejectButton.setVisibility(View.INVISIBLE);

				if (friendData.relationship == FriendData.FRIEND)
					holder.m_DeleteFriendContainer.setVisibility(View.VISIBLE);
				else
					holder.m_DeleteFriendContainer.setVisibility(View.INVISIBLE);

				holder.m_FriendName.setText(friendData.userName);
				holder.m_FriendRequestName.setText("");

				// load avatar
				final String avatarUrl = friendData.AvatarUrl.replace("https", "http");
				aq.id(holder.m_AvatarImageView).tag(AQuery.TAG_1, avatarUrl);

                final String urlForCache = avatarUrl.substring(0,avatarUrl.indexOf("?"));
                /*
				if (BitmapAjaxCallback.getMemoryCached(friendData.userID, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
                 */
                if (BitmapAjaxCallback.getMemoryCached(urlForCache, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
                    /*
					holder.m_AvatarImageView.setImageBitmap(BitmapAjaxCallback.getMemoryCached(friendData.userID,
							FragmentNSA.AVATAR_TARGET_WIDTH));
                     */

                    holder.m_AvatarImageView.setImageBitmap(BitmapAjaxCallback.getMemoryCached(urlForCache,
                            FragmentNSA.AVATAR_TARGET_WIDTH));
				} else {
					// fetch newest from network
					final Bitmap cache = m_DatabaseAdapter.getFriendAvatar(m_UserData.userKey, friendData.userID,
							FragmentNSA.AVATAR_TARGET_WIDTH);
					final boolean hasCache = cache != null;
					BitmapAjaxCallback callback = new BitmapAjaxCallback() {
						@Override
						protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
							if (bmp != null) {
								// save once download is finished
								m_DatabaseAdapter.saveAvatarAsync(m_UserData.userKey, friendData.userID, bmp);
                                /*
								this.memPut(friendData.userID, bmp);
                                 */
                                this.memPut(urlForCache, bmp);
								if (imageView.getTag(AQuery.TAG_1).equals(avatarUrl)) {
									if (hasCache) {
										imageView.setImageBitmap(bmp);
									} else {
										Drawable[] drawables = new Drawable[2];
										drawables[0] = new BitmapDrawable(getResources(), defaultAvatar);
										drawables[1] = new BitmapDrawable(getResources(), bmp);
										TransitionDrawable td = new TransitionDrawable(drawables);
										td.setCrossFadeEnabled(true);
										td.startTransition(300);
										imageView.setImageDrawable(td);
									}
								}
							} else {
								// fallback
								if (hasCache) {
									imageView.setImageBitmap(cache);
								} else {
									imageView.setImageBitmap(defaultAvatar);
								}
							}
						}
					};
					callback.memCache(false);
					callback.fileCache(false);
					callback.targetWidth(FragmentNSA.AVATAR_TARGET_WIDTH);
					callback.url(avatarUrl);
					callback.imageView(aq.getImageView());
					callback.preset(hasCache ? cache : defaultAvatar);
					aq.image(callback);
				}

			} else if (friendRequest != null) {
				holder.m_BackgroundContainer.setBackgroundColor(m_Activity.getResources().getColor(
						R.color.friend_request_list_item_color));
				holder.m_FriendRequestContainer.setVisibility(View.VISIBLE);
				holder.m_FriendName.setVisibility(View.INVISIBLE);
				holder.m_AcceptButton.setVisibility(View.VISIBLE);
				holder.m_RejectButton.setVisibility(View.VISIBLE);

				holder.m_DeleteFriendContainer.setVisibility(View.INVISIBLE);

				holder.m_FriendName.setText("");
				holder.m_FriendRequestName.setText(friendRequest.userName);

				// load avatar
				final String avatarUrl = friendRequest.AvatarUrl.replace("https", "http");
				aq.id(holder.m_AvatarImageView).tag(AQuery.TAG_1, avatarUrl);
				if (BitmapAjaxCallback.getMemoryCached(friendRequest.userID, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
					holder.m_AvatarImageView.setImageBitmap(BitmapAjaxCallback.getMemoryCached(friendRequest.userID,
							FragmentNSA.AVATAR_TARGET_WIDTH));
				} else {
					// fetch newest from network
					final Bitmap cache = m_DatabaseAdapter.getFriendAvatar(m_UserData.userKey, friendRequest.userID,
							FragmentNSA.AVATAR_TARGET_WIDTH);
					final boolean hasCache = cache != null;
					BitmapAjaxCallback callback = new BitmapAjaxCallback() {
						@Override
						protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
							if (bmp != null) {
								// save once download is finished
								m_DatabaseAdapter.saveAvatarAsync(m_UserData.userKey, friendRequest.userID, bmp);
								this.memPut(friendRequest.userID, bmp);
								if (imageView.getTag(AQuery.TAG_1).equals(avatarUrl)) {
									if (hasCache) {
										imageView.setImageBitmap(bmp);
									} else {
										Drawable[] drawables = new Drawable[2];
										drawables[0] = new BitmapDrawable(getResources(), defaultAvatar);
										drawables[1] = new BitmapDrawable(getResources(), bmp);
										TransitionDrawable td = new TransitionDrawable(drawables);
										td.setCrossFadeEnabled(true);
										td.startTransition(300);
										imageView.setImageDrawable(td);
									}
								}
							} else {
								// fallback
								if (hasCache) {
									imageView.setImageBitmap(cache);
								} else {
									imageView.setImageBitmap(defaultAvatar);
								}
							}
						}
					};
					callback.memCache(false);
					callback.fileCache(false);
					callback.targetWidth(FragmentNSA.AVATAR_TARGET_WIDTH);
					callback.url(avatarUrl);
					callback.imageView(aq.getImageView());
					callback.preset(hasCache ? cache : defaultAvatar);
					aq.image(callback);
				}
			}

			switch (deleteStatus) {
			case NORMAL:
				holder.m_AvatarImageView.setAlpha(255);
				holder.m_FriendName.setTextColor(Color.WHITE);
				holder.m_DeleteFriendIcon.setBackgroundResource(R.drawable.friend_x_hover);
				holder.m_DeleteFriendContainer.setBackgroundColor(Color.TRANSPARENT);
				break;
			case DELETING:
				holder.m_AvatarImageView.setAlpha(127);
				holder.m_FriendName.setTextColor(Color.argb(127, 255, 255, 255));
				holder.m_DeleteFriendIcon.setBackgroundResource(R.drawable.friend_x_normal);
				holder.m_DeleteFriendContainer.setBackgroundColor(m_Activity.getResources().getColor(
						R.color.friend_delete_container_blue));
				break;
			}

			holder.m_AcceptButton.setOnClickListener(new View.OnClickListener() {
                @Override
				public void onClick(View v) {

					if (!m_Activity.getNetworkManager().checkWifiProcess())
						return;

					m_CurrentFriendRequest = friendRequest;

					if (m_AddFriendDialog != null && m_AddFriendDialog.isShowing()) {
						LOG.V(TAG, "m_AddFriendDialog is already shown");
						return;
					}

					m_AddFriendDialog = new AddFriendDialog(m_Activity);
					m_AddFriendDialog.setIsForAccept(true);
					m_AddFriendDialog.addButtonListener(m_OnButtonClickListener);
					m_AddFriendDialog.show();

                    //tracking
                    Tracking.pushTrack(v.getContext(), "accept_friend_request_#" + m_CurrentFriendRequest.userName);
				}
			});
			holder.m_RejectButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					if (!m_Activity.getNetworkManager().checkWifiProcess())
						return;

					m_CurrentFriendRequest = friendRequest;
					LOG.V(TAG, "m_CurrentFriendRequest is " + m_CurrentFriendRequest);

					m_Activity.denyFriend(m_Activity.getCurrentUserData().userKey, m_CurrentFriendRequest.userID);

                    //tracking
                    Tracking.pushTrack(v.getContext(), "decline_friend_request_#" + m_CurrentFriendRequest.userName);
				}
			});

			holder.m_DeleteFriendContainer.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					switch (deleteStatus) {
					case NORMAL:
						getItem(position).deleteStatus = FriendListItemProperty.DeleteStatus.DELETING;
						notifyDataSetChanged();

                        //tracking
                        Tracking.pushTrack(v.getContext(), "remove_friend_select_#" + friendData.userName);
						break;
					case DELETING:

						if (!m_Activity.getNetworkManager().checkWifiProcess())
							return;

						m_DeletedFriendId = friendData.userID;
						m_Activity.removeFriend(m_Activity.getCurrentUserData().userKey, friendData.userID);


                        //tracking
                        Tracking.pushTrack(v.getContext(), "remove_friend_#" + friendData.userName);

						break;
					}
				}
			});
			return convertView;
		}
	}

	static class FriendItemWidgetHolder {
		private RelativeLayout m_BackgroundContainer;
		private ImageView m_AvatarImageView;
		private RelativeLayout m_FriendRequestContainer;
		private TextView m_FriendRequestName;
		private TextView m_FriendRequestDescription;
		private TextView m_FriendName;
		private Button m_AcceptButton;
		private Button m_RejectButton;
		private RelativeLayout m_DeleteFriendContainer;
		private ImageView m_DeleteFriendIcon;
	}

	private static class FriendListItemProperty {

		public enum DeleteStatus {
			NORMAL, DELETING
		};

		public FriendData friendData;
		public PendingFriendRequestData friendRequest;
		public DeleteStatus deleteStatus = DeleteStatus.NORMAL;

		public FriendListItemProperty(FriendData friendData, PendingFriendRequestData friendRequest) {
			this.friendData = friendData;
			this.friendRequest = friendRequest;
		}
	}

	// sort friend data in alphabetical order
	private Comparator<FriendData> mComparator = new Comparator<FriendData>() {
		@Override
		public int compare(FriendData fd1, FriendData fd2) {
			return fd1.userName.compareToIgnoreCase(fd2.userName);
		}
	};
}
