package com.fuhu.nabiconnect.mail.effect;

import java.util.ArrayList;

public abstract class Effect {

	protected ArrayList<Effect> m_SubItems = null;
	
	abstract public void apply();
	public void cancel()
	{
		// default do nothing
	};
	abstract public int getEffectIconRes();
	abstract public ArrayList<Effect> getSubItems();
}
