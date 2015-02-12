package com.fuhu.nabiconnect.mail.effect;

import android.util.Pair;

import java.util.ArrayList;

public abstract class IMultipleWallPaperEffect extends Effect{

	public ArrayList<Pair<Integer, Integer>> m_WallPaperList;
	
	public abstract ArrayList<Pair<Integer, Integer>> getWallPaperResId();
}
