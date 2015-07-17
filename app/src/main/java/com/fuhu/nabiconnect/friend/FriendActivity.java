package com.fuhu.nabiconnect.friend;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;

import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.event.Event;
import com.fuhu.nabiconnect.friend.avatar.FriendBean;
import com.fuhu.nabiconnect.friend.fragment.EditAvatarFragment;
import com.fuhu.nabiconnect.friend.fragment.FragmentFriendSetup;
import com.fuhu.nabiconnect.friend.fragment.FriendMainBarFragment;
import com.fuhu.nabiconnect.friend.fragment.FriendMainFragment;
import com.fuhu.nabiconnect.friend.fragment.FriendSetupFragment;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.ndnslibsoutstructs.uploadUserAvatarImage_outObj;

public class FriendActivity extends ApiBaseActivity implements IButtonClickListener, InterfaceFriend {

	public static final String TAG = "FriendActivity";
	private FriendMainFragment m_FriendMainFrag;
	private FriendMainBarFragment m_MainBarFrag;
	// private FriendSetupFragment m_SetupFrag;
	private FragmentFriendSetup m_SetupFrag;

	public static final int FRAGMENT_ID_FRIEND_LIST = 100;
	public static final int FRAGMENT_ID_EDIT_AVATAR = 101;
	public static final int FRAGMENT_ID_SETUP = 102;

	public Event onFriendRequestReceived;
	public Event onFriendRequestAcceptedReceived;
	public Event onFriendRequestRejectedOrDeletedReceived;
	public Event onBackKeyPressed;

	private String m_CreatedUserName;
	private boolean m_IsInSetup;
	private boolean m_IsMommyMode;
	private String m_LogonUserKey;

	private View mLeftBar;

    public FriendActivity() {
        super("nabiFriend");
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_activity_main);

		mLeftBar = findViewById(R.id.left_fragment_container);

		// get intent extra
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			m_IsMommyMode = extras.getBoolean(KEY_IS_MOMMY_MODE, false);
			m_LogonUserKey = extras.getString(KEY_LOGON_USER_KEY);
		}

		// create fragments
		m_FriendMainFrag = new FriendMainFragment();
		// m_EditAvatarFrag = new EditAvatarFragment();
		m_MainBarFrag = new FriendMainBarFragment();
		m_SetupFrag = new FragmentFriendSetup();

		// add listener
		m_FriendMainFrag.addButtonListener(this);
		// m_EditAvatarFrag.addButtonListener(this);
		// m_SetupFrag.addButtonListener(this);

		// check login function
		if (m_IsMommyMode) {
			if (m_LogonUserKey != null) {
				LOG.V(TAG, "onCreate() - there is logon userkey. Get data from cache.");
				UserData logonUser = getDatabaseAdapter().getUserData(m_LogonUserKey);

				if (logonUser != null) {
					LOG.V(TAG, "onCreate() - use the cache as the logon data.");
					loginUserSuccess(logonUser);
				} else {
					LOG.V(TAG, "onCreate() - there is no cache in database.");
					loginAccountNoKid();
				}
			} else {
				LOG.V(TAG, "onCreate() - there is no logon userkey in extra.");
				loginAccountNoKid();
			}
		} else {
			// kid mode, hide left bar
			mLeftBar.setVisibility(View.GONE);
			loginAccount();
		}

		onFriendRequestReceived = new Event(this);
		onFriendRequestAcceptedReceived = new Event(this);
		onFriendRequestRejectedOrDeletedReceived = new Event(this);
		onBackKeyPressed = new Event(this);

	}

	@Override
	public void onButtonClicked(int buttonId, String viewName, Object[] args) {
		if (viewName.equals(FriendMainFragment.TAG)) {
			switchFragment(FRAGMENT_ID_EDIT_AVATAR);
		} else if (viewName.equals(EditAvatarFragment.TAG)) {
			switch (buttonId) {
			case EditAvatarFragment.CANCEL_BUTTON_ID:

				if (m_IsInSetup)
					switchFragment(FRAGMENT_ID_SETUP);
				else
					switchFragment(FRAGMENT_ID_FRIEND_LIST);

				break;
			case EditAvatarFragment.CONFIRM_BUTTON_ID:

				FriendBean updatedBean = (FriendBean) args[0];
				uploadUserAvatarImage_outObj imageObj = (uploadUserAvatarImage_outObj) args[1];

				// need to update current user data
				this.getCurrentUserData().userName = updatedBean.getName();
				this.getCurrentUserData().Character = updatedBean.getCharacterTypeIndex();
				this.getCurrentUserData().CharacterColor = updatedBean.getCharacterColorIndex();
				this.getCurrentUserData().CharacterClothing = updatedBean.getCharacterClothingIndex();
				this.getCurrentUserData().CharacterAccessories = FriendBean.getAccssoriesList(updatedBean);
				this.getCurrentUserData().CharacterBackground = updatedBean.getCharacterBackgroundColorIndex();
				this.getCurrentUserData().avatarURL = imageObj.getAvatarImageUrl();

				switchFragment(FRAGMENT_ID_FRIEND_LIST);
				break;
			}

		} else if (viewName.equals(FriendSetupFragment.TAG)) {
			m_CreatedUserName = (String) args[0];
			LOG.V(TAG, "created user name is " + m_CreatedUserName);
			switchFragment(FRAGMENT_ID_EDIT_AVATAR);
		}

	}

	private void switchFragment(int fragmentId) {
		FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
		Fragment newFragment = null;
		switch (fragmentId) {
		case FRAGMENT_ID_FRIEND_LIST:
			newFragment = m_FriendMainFrag;
			break;
		case FRAGMENT_ID_EDIT_AVATAR:
			// newFragment = m_EditAvatarFrag;
			EditAvatarFragment fragment = new EditAvatarFragment();
			fragment.addButtonListener(this);
			newFragment = fragment;
			break;
		case FRAGMENT_ID_SETUP:
			newFragment = m_SetupFrag;
			break;
		}
		transaction.replace(R.id.right_fragment_container, newFragment);
		transaction.commit();
	}

	public boolean isInSetup() {
		return this.m_IsInSetup;
	}

	public boolean isParentMode() {
		return this.m_IsMommyMode;
	}

	public String getCreatedUserName() {
		return this.m_CreatedUserName;
	}

	/*
	 * @Override public boolean handleMessage(android.os.Message msg) {
	 * 
	 * super.handleMessage(msg);
	 * LOG.V(TAG,"FriendActivity receive msg : "+msg.what); Object[] objs;
	 * switch (msg.what) { case MSG_FRIEND_REQUEST_RECEIVED:
	 * LOG.V(TAG,"MSG_FRIEND_REQUEST_RECEIVED");
	 * onFriendRequestReceived.raise((Object[])msg.obj); break; case
	 * MSG_FRIEND_REJECT_DELETE_RECEIVED:
	 * LOG.V(TAG,"MSG_FRIEND_REJECT_DELETE_RECEIVED");
	 * onFriendRequestRejectedOrDeletedReceived.raise((Object[])msg.obj); break;
	 * case MSG_FRIEND_REQUEST_ACCEPT_RECEIVED:
	 * LOG.V(TAG,"MSG_FRIEND_REQUEST_ACCEPT_RECEIVED");
	 * onFriendRequestAcceptedReceived.raise((Object[])msg.obj); break; } return
	 * false; }
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		// Log.v(TAG,"onKeyUp() - event is "+event.getKeyCode());

		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_BACK:
			onBackKeyPressed.raise();
			if (onBackKeyPressed.getHandled()) {
				LOG.V(TAG, "onBackKeyPressed is handled");
				return true;
			}
			break;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void loginUserSuccess(UserData data) {
		super.loginUserSuccess(data);
		m_IsInSetup = false;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.left_fragment_container, m_MainBarFrag, "leftfragment");
		ft.replace(R.id.right_fragment_container, m_FriendMainFrag, "rightfragment");
		ft.commit();
	}

	@Override
	public void loginUserFailure(String data) {
		super.loginUserFailure(data);

		this.showGeneralWarningDialog();
		finish();

	}

	@Override
	public void accountNeedsCreate(String data) {
		super.accountNeedsCreate(data);
		m_IsInSetup = true;
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction ft = fm.beginTransaction();
		ft.replace(R.id.left_fragment_container, m_MainBarFrag, "leftfragment");
		ft.replace(R.id.right_fragment_container, m_SetupFrag, "rightfragment");
		ft.commit();
	}

	@Override
	public void createAccountSuccess(UserData data) {
		super.createAccountSuccess(data);
		m_IsInSetup = false;
	}

	/**
	 * friend interface
	 */

	@Override
	public void goToStepTwo(String userName) {
		m_CreatedUserName = userName;
		LOG.V(TAG, "created user name is " + m_CreatedUserName);
		switchFragment(FRAGMENT_ID_EDIT_AVATAR);
	}
}
