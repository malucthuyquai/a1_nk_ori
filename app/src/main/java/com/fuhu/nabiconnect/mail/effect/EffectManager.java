package com.fuhu.nabiconnect.mail.effect;

import android.content.Context;

import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;
public class EffectManager {

	public static final String TAG = "EffectManager";
	
	public interface IEffectManagerHolder
	{
		public EffectManager getEffectManager();
	}
	
	private static ArrayList<Effect> m_AllEffects;
	private Effect m_CurrentEffect;
	private Context m_Context;
	
	private  ArrayList<IEffectUpdatedListener> m_EffectUpdatedListeners;
	
	static {
		
		if(m_AllEffects == null)
		{
			m_AllEffects = new ArrayList<Effect>();
			
			m_AllEffects.add(new PaintEffect());
			m_AllEffects.add(new TextEffect());
			m_AllEffects.add(new EraserEffect());
			m_AllEffects.add(new StickerEffect());
			m_AllEffects.add(new CameraEffect());
			m_AllEffects.add(new WallPaperEffect());
		}	
		
	}
	
	public EffectManager(Context context)
	{
		m_Context = context;
	}
	
	
	public ArrayList<Effect> getAllEffects()
	{
		return m_AllEffects;
	}
	
	public void clearEffect()
	{
		applyEffect(new NoneEffect());
	}
	
	public void applyEffect(Effect effect)
	{
		
		if(m_CurrentEffect == effect)
		{
			LOG.V(TAG,"applyEffect() - apply the same effect");
			//return;
		}
		
		// cancel current effect
		if(m_CurrentEffect != null)
			m_CurrentEffect.cancel();
		
		Effect oldEffect = m_CurrentEffect;
		
		// apply effect
		effect.apply();
		
		// update current effect
		m_CurrentEffect = effect;
		
		// notify listeners
		notifyEffectUpdatedListeners(m_CurrentEffect, oldEffect);
	}
	
	public Effect getCurrentEffect()
	{
		return m_CurrentEffect;
	}
	
	/*======================================================================
	 * Add listners for button
	 *=======================================================================*/
	public void notifyEffectUpdatedListeners(Effect newEffect, Effect oldEffect)	
	{
		if(m_EffectUpdatedListeners != null)
			for(IEffectUpdatedListener listener : m_EffectUpdatedListeners)
				listener.onEffectUpdated(newEffect, oldEffect);
	}
	
	/*======================================================================
	 * Notify button listeners
	 *=======================================================================*/
	public void addEffectUpdatedListener(IEffectUpdatedListener listener)
	{
		if(m_EffectUpdatedListeners == null)
			m_EffectUpdatedListeners = new ArrayList<IEffectUpdatedListener>();
			
		if(!m_EffectUpdatedListeners.contains(listener))
			m_EffectUpdatedListeners.add(listener);
	}
}
