package com.fuhu.nabiconnect.friend.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;

public class AddFriendDialog extends PopupDialog {

	public interface IFriendCodeDeletedListener {
		public void onFriendCodeDeleted();
	}

	/*
	 * ======================================================================
	 * Constant Fields
	 * =======================================================================
	 */
	public static final String TAG = "AddFriendDialog";
	private static final int MSG_REFRESH_INDICATOR = 10001;
	private static final int INDICATOR_REFRESH_DURATION = 1000;

	public static final int CANCEL_BUTTON_ID = 100;
	public static final int YES_BUTTON_ID = 101;
	// sid added
	public static final int X_BUTTON_ID = 102;

	// When you modify this value, remember to modify the maxLength of editText
	// in the layout file!!!
	private static final int FRIEND_CODE_LENGTH = 7;

	/*
	 * ======================================================================
	 * Fields
	 * =======================================================================
	 */
	private Context m_Context;
	private Button m_YesButton;
	private Button m_CancelButton;
	private TextView m_InvalidText;

	private EditText m_FriendCodeEditText;
	private RelativeLayout m_CodeTextContainer;
	private TextView m_FCode_1;
	private TextView m_FCode_2;
	private TextView m_FCode_3;
	private TextView m_FCode_4;
	private TextView m_FCode_5;
	private TextView m_FCode_6;
	private TextView m_FCode_7;
	/*
	 * private TextView m_FCode_8; private TextView m_FCode_9; private TextView
	 * m_FCode_10;
	 */
	private ArrayList<TextView> m_FriendCodeTextList = new ArrayList<TextView>();

	private ArrayList<IButtonClickListener> m_ButtonListeners;

	private boolean isForAcceptRequest = false;

	private Handler m_Handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// LOG.V(TAG,"Receive msg "+msg.what);

			switch (msg.what) {
			case MSG_REFRESH_INDICATOR:
				refreshIndicator();
				m_Handler.sendEmptyMessageDelayed(MSG_REFRESH_INDICATOR, INDICATOR_REFRESH_DURATION);
				break;
			}
			super.handleMessage(msg);
		}
	};

	public AddFriendDialog(Context context) {
		super(context);
		this.m_Context = context;

		setContentView(R.layout.friend_add_friend_dialog);

		m_YesButton = (Button) this.findViewById(R.id.add_friend_ok_button);
		m_CancelButton = (Button) this.findViewById(R.id.add_friend_cancel_button);
		m_InvalidText = (TextView) this.findViewById(R.id.add_friend_invalid);

		m_CodeTextContainer = (RelativeLayout) this.findViewById(R.id.friend_code_text_container);

		m_FCode_1 = (TextView) this.findViewById(R.id.friend_code_1);
		m_FCode_2 = (TextView) this.findViewById(R.id.friend_code_2);
		m_FCode_3 = (TextView) this.findViewById(R.id.friend_code_3);
		m_FCode_4 = (TextView) this.findViewById(R.id.friend_code_4);
		m_FCode_5 = (TextView) this.findViewById(R.id.friend_code_5);
		m_FCode_6 = (TextView) this.findViewById(R.id.friend_code_6);
		m_FCode_7 = (TextView) this.findViewById(R.id.friend_code_7);
		/*
		 * m_FCode_8 = (TextView)this.findViewById(R.id.friend_code_8);
		 * m_FCode_9 = (TextView)this.findViewById(R.id.friend_code_9);
		 * m_FCode_10 = (TextView)this.findViewById(R.id.friend_code_10);
		 */
		m_FriendCodeEditText = (EditText) this.findViewById(R.id.add_friend_code_edit_text);

		m_FriendCodeTextList.add(m_FCode_1);
		m_FriendCodeTextList.add(m_FCode_2);
		m_FriendCodeTextList.add(m_FCode_3);
		m_FriendCodeTextList.add(m_FCode_4);
		m_FriendCodeTextList.add(m_FCode_5);
		m_FriendCodeTextList.add(m_FCode_6);
		m_FriendCodeTextList.add(m_FCode_7);
		/*
		 * m_FriendCodeTextList.add(m_FCode_8);
		 * m_FriendCodeTextList.add(m_FCode_9);
		 * m_FriendCodeTextList.add(m_FCode_10);
		 */
		// for (TextView view : m_FriendCodeTextList)
		// view.setTypeface(Typeface.createFromAsset(m_Context.getAssets(),
		// "fonts/GothamRnd-Book.otf"));
		// m_DescriptionText.setTypeface(Typeface.createFromAsset(m_Context.getAssets(),
		// "fonts/GothamRnd-Medium.otf"));
		// m_InvalidText.setTypeface(Typeface.createFromAsset(m_Context.getAssets(),
		// "fonts/GothamRnd-Medium.otf"));

		m_FriendCodeEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
			}

			@Override
			public void afterTextChanged(Editable arg0) {
				setIsInvalid(false);
				updateCodeText(arg0.toString());
			}
		});

		m_CodeTextContainer.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				m_FriendCodeEditText.requestFocus();

				// show keyboard
				InputMethodManager imm = (InputMethodManager) m_Context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
			}
		});

		if (m_YesButton != null)
			m_YesButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {

					commitFriendCode();
				}
			});
		if (m_CancelButton != null)
			m_CancelButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					notifyButtonListeners(CANCEL_BUTTON_ID, TAG, null);
				}
			});

		m_FriendCodeEditText.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					commitFriendCode();
				}

				return false;
			}
		});
		m_FriendCodeEditText.requestFocus();

		this.setOnShowListener(mOnShowListener);
	}

	public void setIsForAccept(boolean forAccept) {
		this.isForAcceptRequest = forAccept;
	}

	public boolean isForAccept() {
		return this.isForAcceptRequest;
	}

	private void commitFriendCode() {
		String friendCode = m_FriendCodeEditText.getText().toString();
		LOG.V(TAG, "commitFriendCode() - friendCode is " + friendCode);

		if (friendCode == null || friendCode.length() < FRIEND_CODE_LENGTH) {
			setIsInvalid(true);
		} else {
			notifyButtonListeners(YES_BUTTON_ID, TAG, new Object[] { friendCode });

			// hide keyboard
			InputMethodManager imm = (InputMethodManager) m_Context.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(0, 0);
			// imm.hideSoftInputFromWindow(m_FriendCodeEditText.getWindowToken(),0);
		}
	}

	private void updateCodeText(String code) {
		// LOG.V(TAG,"updateCodeText() - code is "+code);
		for (int i = 0; i < m_FriendCodeTextList.size(); i++) {
			TextView view = m_FriendCodeTextList.get(i);

			if (i < code.length()) {
				view.setText(String.valueOf(code.charAt(i)));
			} else {
				view.setText("");
			}

			/*
			 * if(i==9 && !view.getText().equals("")) {
			 * LOG.V(TAG,"updateCodeText() - hide the keyboard"); // hide the
			 * keyboard
			 * 
			 * // hide keyboard InputMethodManager imm = (InputMethodManager)
			 * m_Context.getSystemService(m_Context.INPUT_METHOD_SERVICE);
			 * imm.hideSoftInputFromWindow
			 * (m_FriendCodeEditText.getWindowToken(),0); }
			 */
		}

	}

	private void refreshIndicator() {
		for (int i = 0; i < m_FriendCodeTextList.size(); i++) {
			TextView view = m_FriendCodeTextList.get(i);
			String str = view.getText().toString();

			if (str.isEmpty()) {
				view.setText("|");
				break;
			} else if (str.equals("|")) {
				view.setText("");
				break;
			}
		}
	}

	public void setIsInvalid(boolean isInValid) {
		LOG.V(TAG, "setIsInvalid() - isInValid : " + isInValid);
		m_InvalidText.setText(R.string.friend_invalid_code);
		m_InvalidText.setVisibility(isInValid ? View.VISIBLE : View.INVISIBLE);
	}

	public void setRequestAlreadySent(boolean alreadySent){
		m_InvalidText.setText(R.string.friend_already_sent);
		m_InvalidText.setVisibility(alreadySent ? View.VISIBLE : View.INVISIBLE);
	}

	@Override
	public void dismiss() {
		super.dismiss();

		if (m_Handler != null)
			m_Handler.removeMessages(MSG_REFRESH_INDICATOR);
	}

	/*
	 * ======================================================================
	 * Add listners for button
	 * =======================================================================
	 */
	public void notifyButtonListeners(int buttonID, String tag, Object[] args) {
		if (m_ButtonListeners != null)
			for (IButtonClickListener listener : m_ButtonListeners)
				listener.onButtonClicked(buttonID, tag, args);
	}

	/*
	 * ======================================================================
	 * Notify button listeners
	 * =======================================================================
	 */
	public void addButtonListener(IButtonClickListener listener) {
		if (m_ButtonListeners == null)
			m_ButtonListeners = new ArrayList<IButtonClickListener>();

		m_ButtonListeners.add(listener);
	}

	/**
	 * clears friend code in current dialog
	 */
	public void clearFriendCode() {
		m_FriendCodeEditText.setText("");
		for (TextView tv : m_FriendCodeTextList) {
			tv.setText("");
		}
	}

	private DialogInterface.OnShowListener mOnShowListener = new DialogInterface.OnShowListener() {

		@Override
		public void onShow(DialogInterface dialog) {
			m_Handler.removeMessages(MSG_REFRESH_INDICATOR);
			m_Handler.sendEmptyMessage(MSG_REFRESH_INDICATOR);
		}
	};
}