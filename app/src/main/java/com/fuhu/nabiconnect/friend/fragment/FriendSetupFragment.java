package com.fuhu.nabiconnect.friend.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.FriendActivity;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.utils.Utils;

import java.util.ArrayList;

public class FriendSetupFragment extends Fragment {

	public static final String TAG = "FriendSetupFragment";

	public static final int NEXT_BUTTON_ID = 100;
	IButtonClickListener m_Callback;

	private FriendActivity m_Activity;
	private RelativeLayout m_NextButton;
	private EditText m_UserNameText;
	private TextView m_Setup1Text;
	private TextView m_CreateNameText;
	private TextView m_WarningText;
	private ArrayList<IButtonClickListener> m_ButtonListeners;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.friend_setup_view, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		m_Activity = (FriendActivity) getActivity();
		try {
			m_Callback = (IButtonClickListener) m_Activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(m_Activity.toString() + " must implement IButtonClickListener");
		}

		m_NextButton = (RelativeLayout) getView().findViewById(R.id.friend_setup_next_btn);
		NSAUtil.setTypeface(m_Activity, (TextView) getView().findViewById(R.id.tv_next),
				getString(R.string.roboto_medium));
		m_UserNameText = (EditText) getView().findViewById(R.id.friend_setup_user_name);
		m_Setup1Text = (TextView) getView().findViewById(R.id.friend_setup_step1_text);
		m_CreateNameText = (TextView) getView().findViewById(R.id.friend_setup_create_name_text);
		m_WarningText = (TextView) getView().findViewById(R.id.friend_setup_create_name_warning_text);

		m_NextButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				if (Utils.isTextEmpty(m_UserNameText)) {
					LOG.V(TAG, "Entered user name is empty");
					return;
				}
				String inputUserName = m_UserNameText.getText().toString();
				notifyButtonListeners(NEXT_BUTTON_ID, TAG, new Object[] { inputUserName });
			}
		});

		if (m_Activity.isParentMode()) {
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) m_Setup1Text.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.friend_setup_text_parent_margin_left);
			m_Setup1Text.requestLayout();

			param = (RelativeLayout.LayoutParams) m_CreateNameText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.friend_setup_text_parent_margin_left);
			m_CreateNameText.requestLayout();

			param = (RelativeLayout.LayoutParams) m_WarningText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.friend_setup_text_parent_margin_left);
			m_WarningText.requestLayout();

			param = (RelativeLayout.LayoutParams) m_UserNameText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.friend_setup_user_name_parent_margin_left);
			int width = m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_setup_user_name_parent_width);
			if (width > 0) {
				// NOTE: width in parent mode is only explicitly defined for
				// nabi junior (800x480).
				// not sure why it isn't defined for other devices, but just
				// skip setting this width for them.
				param.width = width;
			}
			m_UserNameText.requestLayout();

			// param = (RelativeLayout.LayoutParams)
			// m_NextButton.getLayoutParams();
			// param.rightMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_setup_next_button_parent_margin_right);
			// m_NextButton.requestLayout();
		} else {
			RelativeLayout.LayoutParams param = (RelativeLayout.LayoutParams) m_Setup1Text.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_setup_text_margin_left);
			m_Setup1Text.requestLayout();

			param = (RelativeLayout.LayoutParams) m_CreateNameText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_setup_text_margin_left);
			m_CreateNameText.requestLayout();

			param = (RelativeLayout.LayoutParams) m_WarningText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(R.dimen.friend_setup_text_margin_left);
			m_WarningText.requestLayout();

			param = (RelativeLayout.LayoutParams) m_UserNameText.getLayoutParams();
			param.leftMargin = m_Activity.getResources().getDimensionPixelSize(
					R.dimen.friend_setup_user_name_margin_left);
			m_UserNameText.requestLayout();

			// param = (RelativeLayout.LayoutParams)
			// m_NextButton.getLayoutParams();
			// param.rightMargin =
			// m_Activity.getResources().getDimensionPixelSize(
			// R.dimen.friend_setup_next_button_margin_right);
			// m_NextButton.requestLayout();
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
}
