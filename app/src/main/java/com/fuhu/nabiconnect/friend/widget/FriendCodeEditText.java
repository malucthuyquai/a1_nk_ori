package com.fuhu.nabiconnect.friend.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputConnectionWrapper;
import android.widget.EditText;

import com.fuhu.nabiconnect.friend.dialog.AddFriendDialog.IFriendCodeDeletedListener;
import com.fuhu.nabiconnect.log.LOG;

public class FriendCodeEditText extends EditText {
	
	public static final String TAG = "FriendCodeEditText";
	private IFriendCodeDeletedListener m_Callback;
	
	public FriendCodeEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FriendCodeEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FriendCodeEditText(Context context) {
		super(context);
	}
	
	public void setCallback(IFriendCodeDeletedListener callback)
	{
		this.m_Callback = callback;
	}
	
	@Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        return new FriendCodeInputConnection(super.onCreateInputConnection(outAttrs),
                true);
    }

    private class FriendCodeInputConnection extends InputConnectionWrapper {

        public FriendCodeInputConnection(InputConnection target, boolean mutable) {
            super(target, mutable);
        }

        @Override
        public boolean sendKeyEvent(KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN
                    && event.getKeyCode() == KeyEvent.KEYCODE_DEL) {
                LOG.V(TAG,"Delete key has been pressed");
                
                
                //LOG.V(TAG, "current text is "+this.gett);
                if(m_Callback != null)
                	m_Callback.onFriendCodeDeleted();
                
            }
            return super.sendKeyEvent(event);
        }
        
        @Override
        public boolean deleteSurroundingText(int beforeLength, int afterLength) {       
            // magic: in latest Android, deleteSurroundingText(1, 0) will be called for backspace
            if (beforeLength == 1 && afterLength == 0) {
                // backspace
                return sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                    && sendKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL));
            }

            return super.deleteSurroundingText(beforeLength, afterLength);
        }

    }
}
