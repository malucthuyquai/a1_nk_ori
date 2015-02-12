package com.fuhu.nabiconnect.friend.avatar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Pair;
import android.view.View.MeasureSpec;

import com.fuhu.nabiconnect.R;

import java.util.Hashtable;

public class AvatarManager {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "AvatarManager";
	
	public static final int ID_TYPE_BEAR = 0;
	public static final int ID_TYPE_CAT = 1;
	public static final int ID_TYPE_DOG = 2;
	public static final int ID_TYPE_FOX = 3;
	public static final int ID_TYPE_PENGUIN = 4;
	public static final int ID_TYPE_ROBOT_1 = 5;
	public static final int ID_TYPE_ROBOT_2 = 6;
	public static final int ID_TYPE_ROBOT_3 = 7;
	
	public static final int ID_COLOR_BROWN = 0;
	public static final int ID_COLOR_GREY = 1;
	public static final int ID_COLOR_ORANGE = 2;
	public static final int ID_COLOR_PINK = 3;
	public static final int ID_COLOR_RED = 4;
	public static final int ID_COLOR_TEAL = 5;
	public static final int ID_COLOR_YELLOW = 6;
	
	public static final int ID_GLASSES_1 = 0;
	public static final int ID_GLASSES_2 = 1;
	public static final int ID_GLASSES_3 = 2;
	public static final int ID_GLASSES_4 = 3;
	public static final int ID_GLASSES_5 = 4;
	public static final int ID_GLASSES_6 = 5;
	public static final int ID_GLASSES_7 = 6;
	public static final int ID_GLASSES_8 = 7;
	
	public static final int ID_HAT_1 = 0;
	public static final int ID_HAT_2 = 1;
	public static final int ID_HAT_3 = 2;
	public static final int ID_HAT_4 = 3;
	public static final int ID_HAT_5 = 4;
	public static final int ID_HAT_6 = 5;
	public static final int ID_HAT_7 = 6;
	public static final int ID_HAT_8 = 7;
	
	public static final int ID_NECKTIE_1 = 0;
	public static final int ID_NECKTIE_2 = 1;
	
	public static final int ID_HAIRBAND_1 = 0;
	public static final int ID_HAIRBAND_2 = 1;
	
	public static final int ID_MOUSTACHE_1 = 0;
	public static final int ID_MOUSTACHE_2 = 1;
	
	public static final int ID_BG_RED = 0;
	public static final int ID_BG_GREEN = 1;
	public static final int ID_BG_ORANGE = 2;
	public static final int ID_BG_YELLOW = 3;
	public static final int ID_BG_GRAY = 4;
	public static final int ID_BG_BROWN = 5;
	
	public static Hashtable<Integer, Integer> AvatarIconTable = new Hashtable<Integer, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarColorTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarHatTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarGlassesTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarNeckTieTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarHairBandTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Pair<Integer,Integer>, Integer> AvatarMoustacheTable = new Hashtable<Pair<Integer,Integer>, Integer>();
	public static Hashtable<Integer, Integer> AvatarBackgroundTable = new Hashtable<Integer, Integer>();
	
	static
	{
		// avatar
		AvatarIconTable.put(ID_TYPE_BEAR, R.drawable.bear_brown);
		AvatarIconTable.put(ID_TYPE_CAT, R.drawable.cat_brown);
		AvatarIconTable.put(ID_TYPE_DOG, R.drawable.dog_brown);
		AvatarIconTable.put(ID_TYPE_FOX, R.drawable.fox_brown);
		AvatarIconTable.put(ID_TYPE_PENGUIN, R.drawable.penguin_brown);
		AvatarIconTable.put(ID_TYPE_ROBOT_1, R.drawable.robot1_brown);
		AvatarIconTable.put(ID_TYPE_ROBOT_2, R.drawable.robot2_brown);
		AvatarIconTable.put(ID_TYPE_ROBOT_3, R.drawable.robot3_brown);
		
		// background color
		AvatarBackgroundTable.put(ID_BG_RED, R.color.avatar_bg_red);
		AvatarBackgroundTable.put(ID_BG_GREEN, R.color.avatar_bg_green);
		AvatarBackgroundTable.put(ID_BG_ORANGE, R.color.avatar_bg_orange);
		AvatarBackgroundTable.put(ID_BG_YELLOW, R.color.avatar_bg_yellow);
		AvatarBackgroundTable.put(ID_BG_GRAY, R.color.avatar_bg_gray);
		AvatarBackgroundTable.put(ID_BG_BROWN, R.color.avatar_bg_brown);
		
		
		// moustache
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_MOUSTACHE_1), R.drawable.bear_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_MOUSTACHE_2), R.drawable.bear_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_MOUSTACHE_1), R.drawable.cat_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_MOUSTACHE_2), R.drawable.cat_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_MOUSTACHE_1), R.drawable.dog_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_MOUSTACHE_2), R.drawable.dog_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_MOUSTACHE_1), R.drawable.fox_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_MOUSTACHE_2), R.drawable.fox_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_MOUSTACHE_1), R.drawable.penguin_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_MOUSTACHE_2), R.drawable.penguin_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_MOUSTACHE_1), R.drawable.robot1_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_MOUSTACHE_2), R.drawable.robot1_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_MOUSTACHE_1), R.drawable.robot2_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_MOUSTACHE_2), R.drawable.robot2_a14);
		
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_MOUSTACHE_1), R.drawable.robot3_a13);
		AvatarMoustacheTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_MOUSTACHE_2), R.drawable.robot3_a14);
		
		// hair band
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAIRBAND_1), R.drawable.bear_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAIRBAND_2), R.drawable.bear_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAIRBAND_1), R.drawable.cat_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAIRBAND_2), R.drawable.cat_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAIRBAND_1), R.drawable.dog_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAIRBAND_2), R.drawable.dog_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAIRBAND_1), R.drawable.fox_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAIRBAND_2), R.drawable.fox_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAIRBAND_1), R.drawable.penguin_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAIRBAND_2), R.drawable.penguin_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAIRBAND_1), R.drawable.robot1_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAIRBAND_2), R.drawable.robot1_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAIRBAND_1), R.drawable.robot2_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAIRBAND_2), R.drawable.robot2_a12);
		
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAIRBAND_1), R.drawable.robot3_a6);
		AvatarHairBandTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAIRBAND_2), R.drawable.robot3_a12);
		
		// neck tie
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_NECKTIE_1), R.drawable.bear_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_NECKTIE_2), R.drawable.bear_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_NECKTIE_1), R.drawable.cat_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_NECKTIE_2), R.drawable.cat_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_NECKTIE_1), R.drawable.dog_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_NECKTIE_2), R.drawable.dog_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_NECKTIE_1), R.drawable.fox_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_NECKTIE_2), R.drawable.fox_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_NECKTIE_1), R.drawable.penguin_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_NECKTIE_2), R.drawable.penguin_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_NECKTIE_1), R.drawable.robot1_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_NECKTIE_2), R.drawable.robot1_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_NECKTIE_1), R.drawable.robot2_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_NECKTIE_2), R.drawable.robot2_a11);
		
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_NECKTIE_1), R.drawable.robot3_a5);
		AvatarNeckTieTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_NECKTIE_2), R.drawable.robot3_a11);
		
		// glasses
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_1), R.drawable.bear_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_2), R.drawable.bear_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_3), R.drawable.bear_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_4), R.drawable.bear_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_5), R.drawable.bear_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_6), R.drawable.bear_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_7), R.drawable.bear_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_GLASSES_8), R.drawable.bear_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_1), R.drawable.cat_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_2), R.drawable.cat_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_3), R.drawable.cat_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_4), R.drawable.cat_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_5), R.drawable.cat_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_6), R.drawable.cat_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_7), R.drawable.cat_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_GLASSES_8), R.drawable.cat_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_1), R.drawable.dog_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_2), R.drawable.dog_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_3), R.drawable.dog_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_4), R.drawable.dog_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_5), R.drawable.dog_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_6), R.drawable.dog_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_7), R.drawable.dog_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_GLASSES_8), R.drawable.dog_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_1), R.drawable.fox_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_2), R.drawable.fox_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_3), R.drawable.fox_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_4), R.drawable.fox_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_5), R.drawable.fox_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_6), R.drawable.fox_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_7), R.drawable.fox_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_GLASSES_8), R.drawable.fox_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_1), R.drawable.penguin_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_2), R.drawable.penguin_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_3), R.drawable.penguin_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_4), R.drawable.penguin_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_5), R.drawable.penguin_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_6), R.drawable.penguin_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_7), R.drawable.penguin_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_GLASSES_8), R.drawable.penguin_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_1), R.drawable.robot1_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_2), R.drawable.robot1_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_3), R.drawable.robot1_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_4), R.drawable.robot1_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_5), R.drawable.robot1_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_6), R.drawable.robot1_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_7), R.drawable.robot1_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_GLASSES_8), R.drawable.robot1_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_1), R.drawable.robot2_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_2), R.drawable.robot2_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_3), R.drawable.robot2_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_4), R.drawable.robot2_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_5), R.drawable.robot2_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_6), R.drawable.robot2_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_7), R.drawable.robot2_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_GLASSES_8), R.drawable.robot2_a10);
		
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_1), R.drawable.robot3_a1);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_2), R.drawable.robot3_a2);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_3), R.drawable.robot3_a3);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_4), R.drawable.robot3_a4);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_5), R.drawable.robot3_a7);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_6), R.drawable.robot3_a8);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_7), R.drawable.robot3_a9);
		AvatarGlassesTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_GLASSES_8), R.drawable.robot3_a10);
		
		// hat
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_1), R.drawable.bear_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_2), R.drawable.bear_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_3), R.drawable.bear_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_4), R.drawable.bear_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_5), R.drawable.bear_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_6), R.drawable.bear_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_7), R.drawable.bear_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_HAT_8), R.drawable.bear_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_1), R.drawable.cat_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_2), R.drawable.cat_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_3), R.drawable.cat_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_4), R.drawable.cat_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_5), R.drawable.cat_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_6), R.drawable.cat_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_7), R.drawable.cat_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_HAT_8), R.drawable.cat_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_1), R.drawable.dog_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_2), R.drawable.dog_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_3), R.drawable.dog_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_4), R.drawable.dog_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_5), R.drawable.dog_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_6), R.drawable.dog_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_7), R.drawable.dog_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_HAT_8), R.drawable.dog_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_1), R.drawable.fox_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_2), R.drawable.fox_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_3), R.drawable.fox_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_4), R.drawable.fox_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_5), R.drawable.fox_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_6), R.drawable.fox_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_7), R.drawable.fox_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_HAT_8), R.drawable.fox_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_1), R.drawable.penguin_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_2), R.drawable.penguin_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_3), R.drawable.penguin_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_4), R.drawable.penguin_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_5), R.drawable.penguin_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_6), R.drawable.penguin_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_7), R.drawable.penguin_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_HAT_8), R.drawable.penguin_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_1), R.drawable.robot1_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_2), R.drawable.robot1_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_3), R.drawable.robot1_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_4), R.drawable.robot1_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_5), R.drawable.robot1_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_6), R.drawable.robot1_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_7), R.drawable.robot1_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_HAT_8), R.drawable.robot1_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_1), R.drawable.robot2_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_2), R.drawable.robot2_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_3), R.drawable.robot2_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_4), R.drawable.robot2_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_5), R.drawable.robot2_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_6), R.drawable.robot2_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_7), R.drawable.robot2_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_HAT_8), R.drawable.robot2_h8);
		
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_1), R.drawable.robot3_h1);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_2), R.drawable.robot3_h2);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_3), R.drawable.robot3_h3);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_4), R.drawable.robot3_h4);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_5), R.drawable.robot3_h5);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_6), R.drawable.robot3_h6);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_7), R.drawable.robot3_h7);
		AvatarHatTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_HAT_8), R.drawable.robot3_h8);
		
		
		
		
		// color
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_BROWN), R.drawable.bear_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_GREY), R.drawable.bear_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_ORANGE), R.drawable.bear_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_PINK), R.drawable.bear_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_RED), R.drawable.bear_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_TEAL), R.drawable.bear_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_BEAR,ID_COLOR_YELLOW), R.drawable.bear_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_BROWN), R.drawable.cat_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_GREY), R.drawable.cat_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_ORANGE), R.drawable.cat_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_PINK), R.drawable.cat_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_RED), R.drawable.cat_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_TEAL), R.drawable.cat_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_CAT,ID_COLOR_YELLOW), R.drawable.cat_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_BROWN), R.drawable.dog_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_GREY), R.drawable.dog_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_ORANGE), R.drawable.dog_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_PINK), R.drawable.dog_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_RED), R.drawable.dog_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_TEAL), R.drawable.dog_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_DOG,ID_COLOR_YELLOW), R.drawable.dog_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_BROWN), R.drawable.fox_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_GREY), R.drawable.fox_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_ORANGE), R.drawable.fox_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_PINK), R.drawable.fox_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_RED), R.drawable.fox_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_TEAL), R.drawable.fox_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_FOX,ID_COLOR_YELLOW), R.drawable.fox_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_BROWN), R.drawable.penguin_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_GREY), R.drawable.penguin_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_ORANGE), R.drawable.penguin_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_PINK), R.drawable.penguin_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_RED), R.drawable.penguin_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_TEAL), R.drawable.penguin_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_PENGUIN,ID_COLOR_YELLOW), R.drawable.penguin_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_BROWN), R.drawable.robot1_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_GREY), R.drawable.robot1_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_ORANGE), R.drawable.robot1_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_PINK), R.drawable.robot1_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_RED), R.drawable.robot1_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_TEAL), R.drawable.robot1_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_1,ID_COLOR_YELLOW), R.drawable.robot1_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_BROWN), R.drawable.robot2_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_GREY), R.drawable.robot2_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_ORANGE), R.drawable.robot2_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_PINK), R.drawable.robot2_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_RED), R.drawable.robot2_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_TEAL), R.drawable.robot2_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_2,ID_COLOR_YELLOW), R.drawable.robot2_yellow);
		
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_BROWN), R.drawable.robot3_brown);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_GREY), R.drawable.robot3_grey);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_ORANGE), R.drawable.robot3_orange);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_PINK), R.drawable.robot3_pink);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_RED), R.drawable.robot3_red);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_TEAL), R.drawable.robot3_teal);
		AvatarColorTable.put(new Pair<Integer, Integer>(ID_TYPE_ROBOT_3,ID_COLOR_YELLOW), R.drawable.robot3_yellow);
		
	}
	
	public static Bitmap getAvatarIconBitmap(Context context, FriendBean bean)
	{		
		AvatarWidget m_AvatarWidget = new AvatarWidget(context);
		m_AvatarWidget.setFriendBean(bean);
		m_AvatarWidget.setDrawingCacheEnabled(true);
		
		// 1. Create layout bitmap		
		// Create a new bitmap and a new canvas using that bitmap
		//int containerSize = context.getResources().getDimensionPixelSize(R.dimen.avatar_icon_container_size);
		int containerSize = 500;
		Bitmap bmp = Bitmap.createBitmap(containerSize, containerSize, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bmp);
		
		
		// Supply measurements
		m_AvatarWidget.measure(MeasureSpec.makeMeasureSpec(canvas.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(canvas.getHeight(), MeasureSpec.EXACTLY));
		 
		// Apply the measures so the layout would resize before drawing.
		m_AvatarWidget.layout(0, 0, m_AvatarWidget.getMeasuredWidth(), m_AvatarWidget.getMeasuredHeight());
		
		// create layout bitmap
		canvas.drawBitmap(m_AvatarWidget.getDrawingCache(), 0, 0, new Paint());
		
		// 2. Crop image to circle
		
		Bitmap circleImage = GraphicsUtil.getCircleBitmap(bmp);
		
		// 3. Create ring boarder image
		RingBoarderWidget m_RingWidget = new RingBoarderWidget(context);
		m_RingWidget.setTargetImage(circleImage);
		
		m_RingWidget.setDrawingCacheEnabled(true);
		Bitmap bmp2 = Bitmap.createBitmap(508, 508, Bitmap.Config.ARGB_8888);
		Canvas canvas2 = new Canvas(bmp2);
		
		
		// Supply measurements
		m_RingWidget.measure(MeasureSpec.makeMeasureSpec(canvas2.getWidth(), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(canvas2.getHeight(), MeasureSpec.EXACTLY));
		 
		// Apply the measures so the layout would resize before drawing.
		m_RingWidget.layout(0, 0, m_RingWidget.getMeasuredWidth(), m_RingWidget.getMeasuredHeight());
		
		// create layout bitmap
		canvas2.drawBitmap(m_RingWidget.getDrawingCache(), 0, 0, new Paint());
		
		// recycle bitmap
		bmp.recycle();
		circleImage.recycle();
		
		return bmp2;
	}
}
