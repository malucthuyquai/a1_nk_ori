package com.fuhu.nabiconnect.chat.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.chat.stickers.Sticker;

import java.util.ArrayList;

public class ChatStickerWidget extends RelativeLayout {

    /*======================================================================
     * Constant Fields
     *=======================================================================*/
    public static final String TAG = "StickerWidget";
    private Context m_Context;
    private Sticker m_Sticker;
    private ImageView m_StickerIcon;
    private ArrayList<IButtonClickListener> m_ButtonListeners;

    private RelativeLayout m_BackgroundContainer;

    public static final int BUTTON_ID = 100;

    public ChatStickerWidget(Context context, Sticker sticker) {
        super(context, null);

        this.m_Context = context;
        this.m_Sticker = sticker;

        // inflate layout
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.chat_sticker_widget, this);

        m_BackgroundContainer = (RelativeLayout) this.findViewById(R.id.sticker_background);
        m_StickerIcon = (ImageView) m_BackgroundContainer.findViewById(R.id.sticker_icon);

        m_StickerIcon.setImageResource(sticker.getResId());

        this.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View view, MotionEvent mv) {
                if (mv.getX() < 0 || mv.getX() > view.getWidth() || mv.getY() < 0 || mv.getY() > view.getHeight()) {
                    //m_Background.setBackgroundResource(m_BackgroundId);
                    return false;
                }
                switch (mv.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //m_Background.setBackgroundResource(m_BackgroundPressedId);
                        return true;
                    case MotionEvent.ACTION_UP:
                        //m_Background.setBackgroundResource(m_BackgroundId);
                        notifyButtonListeners(BUTTON_ID, TAG, new Object[]{m_Sticker});
                        return true;
                }
                return false;
            }
        });

    }

    /*======================================================================
     * Add listners for button
     *=======================================================================*/
    public void notifyButtonListeners(int buttonID, String tag, Object[] args) {
        if (m_ButtonListeners != null)
            for (IButtonClickListener listener : m_ButtonListeners)
                listener.onButtonClicked(buttonID, tag, args);
    }

    /*======================================================================
     * Notify button listeners
     *=======================================================================*/
    public void addButtonListener(IButtonClickListener listener) {
        if (m_ButtonListeners == null)
            m_ButtonListeners = new ArrayList<IButtonClickListener>();

        m_ButtonListeners.add(listener);
    }


}
