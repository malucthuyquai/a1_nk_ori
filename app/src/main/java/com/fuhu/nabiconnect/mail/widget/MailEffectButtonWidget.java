package com.fuhu.nabiconnect.mail.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.effect.Effect;
import com.fuhu.nabiconnect.mail.effect.EffectManager;
import com.fuhu.nabiconnect.mail.effect.EffectManager.IEffectManagerHolder;

import java.util.ArrayList;

public class MailEffectButtonWidget extends RelativeLayout {

    /*======================================================================
     * Constant Fields
     *=======================================================================*/
    public static final String TAG = "MailEffectButtonWidget";
    private Context m_Context;
    private RelativeLayout m_BackgroundContainer;
    private ImageView m_ButtonImage;
    private ImageView m_ButtonImageRing;
    private ArrayList<IButtonClickListener> m_ButtonListeners;
    private EffectManager m_EffectManager;
    private Effect m_Effect;

    public static final int BUTTON_ID = 100;

    //public MailEffectButtonWidget(Context context, AttributeSet attrs)
    public MailEffectButtonWidget(Context context, Effect effect) {
        super(context);

        this.m_Context = context;
        this.m_Effect = effect;

        if (context instanceof IEffectManagerHolder) {
            m_EffectManager = ((IEffectManagerHolder) context).getEffectManager();
            /*
			m_EffectManager.addEffectUpdatedListener(new IEffectUpdatedListener() {
				
				@Override
				public void onEffectUpdated(Effect effect) {
					if(effect == m_Effect)
						m_ButtonImageRing.setVisibility(View.VISIBLE);
					else
						m_ButtonImageRing.setVisibility(View.INVISIBLE);
					
				}
			});
			*/
        } else {
            LOG.E(TAG, "MailEffectButtonWidget - cannot get effect manager");
        }

        // inflate layout
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.mail_effect_button_widget, this);

        m_BackgroundContainer = (RelativeLayout) this.findViewById(R.id.effect_button_container);
        m_ButtonImage = (ImageView) m_BackgroundContainer.findViewById(R.id.effect_button_image);
        m_ButtonImageRing = (ImageView) m_BackgroundContainer.findViewById(R.id.effect_button_image_ring);


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
                        m_EffectManager.applyEffect(m_Effect);
                        //notifyButtonListeners(BUTTON_ID, TAG, null);
                        return true;
                }
                return false;
            }
        });

        m_ButtonImage.setImageResource(effect.getEffectIconRes());
    }

    public void setSelected(boolean selected) {
        if (selected)
            m_ButtonImageRing.setVisibility(View.VISIBLE);
        else
            m_ButtonImageRing.setVisibility(View.INVISIBLE);
    }

    public Effect getEffect() {
        return m_Effect;
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
