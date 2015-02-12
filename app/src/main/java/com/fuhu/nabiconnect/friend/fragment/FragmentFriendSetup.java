package com.fuhu.nabiconnect.friend.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.InterfaceFriend;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;

/**
 * re-write
 */

public class FragmentFriendSetup extends Fragment {

	final private static String TAG = FragmentFriendSetup.class.getSimpleName();

	private InterfaceFriend mCallback;
	private EditText et_name;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.universal_fragment_friend_setup, parent, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		try {
			mCallback = (InterfaceFriend) getActivity();
		} catch (ClassCastException e) {
			e.printStackTrace();
			throw new RuntimeException("hosting activity should implement InterfaceFriend");
		}
		et_name = (EditText) getView().findViewById(R.id.et_name);
		getView().findViewById(R.id.rl_next).setOnClickListener(next_ocl);
		NSAUtil.setTypeface(getActivity(), (TextView) getView().findViewById(R.id.tv_next),
				getString(R.string.roboto_medium));
	}

	private View.OnClickListener next_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			String name = et_name.getText().toString();
			if (name.isEmpty()) {
				return;
			} else {
				mCallback.goToStepTwo(name);
			}
		}
	};
}
