package com.fuhu.nabiconnect.chat.stickers;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;
import java.util.Hashtable;

public class StickerManager {

    /*======================================================================
     * Constant Fields
     *=======================================================================*/
    public static final String TAG = "StickerManager";
    public static Hashtable<String, Integer> stickerIconTable = new Hashtable<String, Integer>();
    public static final String STICKER_PREFIX = "#::#";


    public static ArrayList<Sticker> A_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> B_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> C_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> D_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> E_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> F_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> G_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> H_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> I_stickers = new ArrayList<Sticker>();
    public static ArrayList<Sticker> J_stickers = new ArrayList<Sticker>();


    public static StickerCategory A_category;
    public static StickerCategory B_category;
    public static StickerCategory C_category;
    public static StickerCategory D_category;
    public static StickerCategory E_category;
    public static StickerCategory F_category;
    public static StickerCategory G_category;
    public static StickerCategory H_category;
    public static StickerCategory I_category;
    public static StickerCategory J_category;


    public static ArrayList<StickerCategory> totalCategories = new ArrayList<StickerCategory>();

    static {
        LOG.V(TAG, "static initialize - start()");

        A_stickers.add(new Sticker(1, R.drawable.chat_sticker_a_01));
        A_stickers.add(new Sticker(2, R.drawable.chat_sticker_a_02));


        B_stickers.add(new Sticker(1, R.drawable.chat_sticker_b_01));
        B_stickers.add(new Sticker(2, R.drawable.chat_sticker_b_02));
        B_stickers.add(new Sticker(3, R.drawable.chat_sticker_b_03));
        B_stickers.add(new Sticker(4, R.drawable.chat_sticker_b_04));
        B_stickers.add(new Sticker(5, R.drawable.chat_sticker_b_05));
        B_stickers.add(new Sticker(6, R.drawable.chat_sticker_b_06));
        B_stickers.add(new Sticker(7, R.drawable.chat_sticker_b_07));
        B_stickers.add(new Sticker(8, R.drawable.chat_sticker_b_08));
        B_stickers.add(new Sticker(9, R.drawable.chat_sticker_b_09));
        B_stickers.add(new Sticker(10, R.drawable.chat_sticker_b_10));
        B_stickers.add(new Sticker(11, R.drawable.chat_sticker_b_11));
        B_stickers.add(new Sticker(12, R.drawable.chat_sticker_b_12));
        B_stickers.add(new Sticker(13, R.drawable.chat_sticker_b_13));
        B_stickers.add(new Sticker(14, R.drawable.chat_sticker_b_14));
        B_stickers.add(new Sticker(15, R.drawable.chat_sticker_b_15));
        B_stickers.add(new Sticker(16, R.drawable.chat_sticker_b_16));
        B_stickers.add(new Sticker(17, R.drawable.chat_sticker_b_17));
        B_stickers.add(new Sticker(18, R.drawable.chat_sticker_b_18));
        B_stickers.add(new Sticker(19, R.drawable.chat_sticker_b_19));
        B_stickers.add(new Sticker(20, R.drawable.chat_sticker_b_20));

        C_stickers.add(new Sticker(1, R.drawable.chat_sticker_c_01));
        C_stickers.add(new Sticker(2, R.drawable.chat_sticker_c_02));
        C_stickers.add(new Sticker(3, R.drawable.chat_sticker_c_03));

        D_stickers.add(new Sticker(1, R.drawable.chat_sticker_d_01));
        D_stickers.add(new Sticker(2, R.drawable.chat_sticker_d_02));
        D_stickers.add(new Sticker(3, R.drawable.chat_sticker_d_03));
        D_stickers.add(new Sticker(4, R.drawable.chat_sticker_d_04));
        D_stickers.add(new Sticker(5, R.drawable.chat_sticker_d_05));
        D_stickers.add(new Sticker(6, R.drawable.chat_sticker_d_06));
        D_stickers.add(new Sticker(7, R.drawable.chat_sticker_d_07));
        D_stickers.add(new Sticker(8, R.drawable.chat_sticker_d_08));

        E_stickers.add(new Sticker(1, R.drawable.chat_sticker_e_01));
        E_stickers.add(new Sticker(2, R.drawable.chat_sticker_e_02));
        E_stickers.add(new Sticker(3, R.drawable.chat_sticker_e_03));
        E_stickers.add(new Sticker(4, R.drawable.chat_sticker_e_04));
        E_stickers.add(new Sticker(5, R.drawable.chat_sticker_e_05));
        E_stickers.add(new Sticker(6, R.drawable.chat_sticker_e_06));
        E_stickers.add(new Sticker(7, R.drawable.chat_sticker_e_07));
        E_stickers.add(new Sticker(8, R.drawable.chat_sticker_e_08));
        E_stickers.add(new Sticker(9, R.drawable.chat_sticker_e_09));
        E_stickers.add(new Sticker(10, R.drawable.chat_sticker_e_10));
        E_stickers.add(new Sticker(11, R.drawable.chat_sticker_e_11));
        E_stickers.add(new Sticker(12, R.drawable.chat_sticker_e_12));
        E_stickers.add(new Sticker(13, R.drawable.chat_sticker_e_13));
        E_stickers.add(new Sticker(14, R.drawable.chat_sticker_e_14));
        E_stickers.add(new Sticker(15, R.drawable.chat_sticker_e_15));
        E_stickers.add(new Sticker(16, R.drawable.chat_sticker_e_16));
        E_stickers.add(new Sticker(17, R.drawable.chat_sticker_e_17));
        E_stickers.add(new Sticker(18, R.drawable.chat_sticker_e_18));
        E_stickers.add(new Sticker(19, R.drawable.chat_sticker_e_19));
        E_stickers.add(new Sticker(20, R.drawable.chat_sticker_e_20));
        E_stickers.add(new Sticker(21, R.drawable.chat_sticker_e_21));
        E_stickers.add(new Sticker(22, R.drawable.chat_sticker_e_22));

        F_stickers.add(new Sticker(1, R.drawable.chat_sticker_f_01));
        F_stickers.add(new Sticker(2, R.drawable.chat_sticker_f_02));
        F_stickers.add(new Sticker(3, R.drawable.chat_sticker_f_03));
        F_stickers.add(new Sticker(4, R.drawable.chat_sticker_f_04));
        F_stickers.add(new Sticker(5, R.drawable.chat_sticker_f_05));
        F_stickers.add(new Sticker(6, R.drawable.chat_sticker_f_06));
        F_stickers.add(new Sticker(7, R.drawable.chat_sticker_f_07));
        //F_stickers.add(new Sticker(8,R.drawable.chat_sticker_f_08));
        F_stickers.add(new Sticker(9, R.drawable.chat_sticker_f_09));
        F_stickers.add(new Sticker(10, R.drawable.chat_sticker_f_10));
        F_stickers.add(new Sticker(11, R.drawable.chat_sticker_f_11));
        F_stickers.add(new Sticker(12, R.drawable.chat_sticker_f_12));
        F_stickers.add(new Sticker(13, R.drawable.chat_sticker_f_13));
        F_stickers.add(new Sticker(14, R.drawable.chat_sticker_f_14));
        F_stickers.add(new Sticker(15, R.drawable.chat_sticker_f_15));
        F_stickers.add(new Sticker(16, R.drawable.chat_sticker_f_16));
        F_stickers.add(new Sticker(17, R.drawable.chat_sticker_f_17));
        //F_stickers.add(new Sticker(18,R.drawable.chat_sticker_f_18));
        F_stickers.add(new Sticker(19, R.drawable.chat_sticker_f_19));
        F_stickers.add(new Sticker(20, R.drawable.chat_sticker_f_20));

        G_stickers.add(new Sticker(1, R.drawable.chat_sticker_g_01));
        G_stickers.add(new Sticker(2, R.drawable.chat_sticker_g_02));
        G_stickers.add(new Sticker(3, R.drawable.chat_sticker_g_03));
        G_stickers.add(new Sticker(4, R.drawable.chat_sticker_g_04));
        G_stickers.add(new Sticker(5, R.drawable.chat_sticker_g_05));

        H_stickers.add(new Sticker(1, R.drawable.chat_sticker_h_01));
        H_stickers.add(new Sticker(2, R.drawable.chat_sticker_h_02));
        H_stickers.add(new Sticker(3, R.drawable.chat_sticker_h_03));
        H_stickers.add(new Sticker(4, R.drawable.chat_sticker_h_04));
        H_stickers.add(new Sticker(5, R.drawable.chat_sticker_h_05));
        H_stickers.add(new Sticker(6, R.drawable.chat_sticker_h_06));
        H_stickers.add(new Sticker(7, R.drawable.chat_sticker_h_07));
        H_stickers.add(new Sticker(8, R.drawable.chat_sticker_h_08));
        H_stickers.add(new Sticker(9, R.drawable.chat_sticker_h_09));
        H_stickers.add(new Sticker(10, R.drawable.chat_sticker_h_10));
        H_stickers.add(new Sticker(11, R.drawable.chat_sticker_h_11));
        H_stickers.add(new Sticker(12, R.drawable.chat_sticker_h_12));
        H_stickers.add(new Sticker(13, R.drawable.chat_sticker_h_13));
        H_stickers.add(new Sticker(14, R.drawable.chat_sticker_h_14));
        H_stickers.add(new Sticker(15, R.drawable.chat_sticker_h_15));
        H_stickers.add(new Sticker(16, R.drawable.chat_sticker_h_16));
        H_stickers.add(new Sticker(17, R.drawable.chat_sticker_h_17));
        H_stickers.add(new Sticker(18, R.drawable.chat_sticker_h_18));
        H_stickers.add(new Sticker(19, R.drawable.chat_sticker_h_19));
        H_stickers.add(new Sticker(20, R.drawable.chat_sticker_h_20));

        I_stickers.add(new Sticker(1, R.drawable.chat_sticker_i_01));
        I_stickers.add(new Sticker(2, R.drawable.chat_sticker_i_02));
        I_stickers.add(new Sticker(3, R.drawable.chat_sticker_i_03));
        I_stickers.add(new Sticker(4, R.drawable.chat_sticker_i_04));
        I_stickers.add(new Sticker(5, R.drawable.chat_sticker_i_05));
        I_stickers.add(new Sticker(6, R.drawable.chat_sticker_i_06));
        I_stickers.add(new Sticker(7, R.drawable.chat_sticker_i_07));
        I_stickers.add(new Sticker(8, R.drawable.chat_sticker_i_08));
        I_stickers.add(new Sticker(9, R.drawable.chat_sticker_i_09));
        I_stickers.add(new Sticker(10, R.drawable.chat_sticker_i_10));
        I_stickers.add(new Sticker(11, R.drawable.chat_sticker_i_11));
        I_stickers.add(new Sticker(12, R.drawable.chat_sticker_i_12));
        I_stickers.add(new Sticker(13, R.drawable.chat_sticker_i_13));
        I_stickers.add(new Sticker(14, R.drawable.chat_sticker_i_14));
        I_stickers.add(new Sticker(15, R.drawable.chat_sticker_i_15));
        I_stickers.add(new Sticker(16, R.drawable.chat_sticker_i_16));
        I_stickers.add(new Sticker(17, R.drawable.chat_sticker_i_17));
        I_stickers.add(new Sticker(18, R.drawable.chat_sticker_i_18));
        I_stickers.add(new Sticker(19, R.drawable.chat_sticker_i_19));
        I_stickers.add(new Sticker(20, R.drawable.chat_sticker_i_20));
        I_stickers.add(new Sticker(21, R.drawable.chat_sticker_i_21));
        I_stickers.add(new Sticker(22, R.drawable.chat_sticker_i_22));
        I_stickers.add(new Sticker(23, R.drawable.chat_sticker_i_23));
        I_stickers.add(new Sticker(24, R.drawable.chat_sticker_i_24));
        I_stickers.add(new Sticker(25, R.drawable.chat_sticker_i_25));
        I_stickers.add(new Sticker(26, R.drawable.chat_sticker_i_26));
        I_stickers.add(new Sticker(27, R.drawable.chat_sticker_i_27));
        I_stickers.add(new Sticker(28, R.drawable.chat_sticker_i_28));
        I_stickers.add(new Sticker(29, R.drawable.chat_sticker_i_29));
        I_stickers.add(new Sticker(30, R.drawable.chat_sticker_i_30));
        I_stickers.add(new Sticker(31, R.drawable.chat_sticker_i_31));
        I_stickers.add(new Sticker(32, R.drawable.chat_sticker_i_32));
        I_stickers.add(new Sticker(33, R.drawable.chat_sticker_i_33));
        I_stickers.add(new Sticker(34, R.drawable.chat_sticker_i_34));
        I_stickers.add(new Sticker(35, R.drawable.chat_sticker_i_35));
        I_stickers.add(new Sticker(36, R.drawable.chat_sticker_i_36));
        I_stickers.add(new Sticker(37, R.drawable.chat_sticker_i_37));
        I_stickers.add(new Sticker(38, R.drawable.chat_sticker_i_38));
        I_stickers.add(new Sticker(39, R.drawable.chat_sticker_i_39));
        I_stickers.add(new Sticker(40, R.drawable.chat_sticker_i_40));
        I_stickers.add(new Sticker(41, R.drawable.chat_sticker_i_41));
        I_stickers.add(new Sticker(42, R.drawable.chat_sticker_i_42));
        I_stickers.add(new Sticker(43, R.drawable.chat_sticker_i_43));
        I_stickers.add(new Sticker(44, R.drawable.chat_sticker_i_44));
        I_stickers.add(new Sticker(45, R.drawable.chat_sticker_i_45));
        I_stickers.add(new Sticker(46, R.drawable.chat_sticker_i_46));
        I_stickers.add(new Sticker(47, R.drawable.chat_sticker_i_47));
        I_stickers.add(new Sticker(48, R.drawable.chat_sticker_i_48));
        I_stickers.add(new Sticker(49, R.drawable.chat_sticker_i_49));
        I_stickers.add(new Sticker(50, R.drawable.chat_sticker_i_50));
        I_stickers.add(new Sticker(51, R.drawable.chat_sticker_i_51));
        I_stickers.add(new Sticker(52, R.drawable.chat_sticker_i_52));
        I_stickers.add(new Sticker(53, R.drawable.chat_sticker_i_53));
        I_stickers.add(new Sticker(54, R.drawable.chat_sticker_i_54));
        I_stickers.add(new Sticker(55, R.drawable.chat_sticker_i_55));
        I_stickers.add(new Sticker(56, R.drawable.chat_sticker_i_56));
        I_stickers.add(new Sticker(57, R.drawable.chat_sticker_i_57));
        I_stickers.add(new Sticker(58, R.drawable.chat_sticker_i_58));
        I_stickers.add(new Sticker(59, R.drawable.chat_sticker_i_59));
        I_stickers.add(new Sticker(60, R.drawable.chat_sticker_i_60));
        I_stickers.add(new Sticker(61, R.drawable.chat_sticker_i_61));
        I_stickers.add(new Sticker(62, R.drawable.chat_sticker_i_62));
        I_stickers.add(new Sticker(63, R.drawable.chat_sticker_i_63));
        I_stickers.add(new Sticker(64, R.drawable.chat_sticker_i_64));
        I_stickers.add(new Sticker(65, R.drawable.chat_sticker_i_65));

        J_stickers.add(new Sticker(1, R.drawable.chat_sticker_j_01));
        J_stickers.add(new Sticker(2, R.drawable.chat_sticker_j_02));
        J_stickers.add(new Sticker(3, R.drawable.chat_sticker_j_03));
        J_stickers.add(new Sticker(4, R.drawable.chat_sticker_j_04));
        J_stickers.add(new Sticker(5, R.drawable.chat_sticker_j_05));
        J_stickers.add(new Sticker(6, R.drawable.chat_sticker_j_06));
        J_stickers.add(new Sticker(7, R.drawable.chat_sticker_j_07));
        J_stickers.add(new Sticker(8, R.drawable.chat_sticker_j_08));
        J_stickers.add(new Sticker(9, R.drawable.chat_sticker_j_09));
        J_stickers.add(new Sticker(10, R.drawable.chat_sticker_j_10));
        J_stickers.add(new Sticker(11, R.drawable.chat_sticker_j_11));
        J_stickers.add(new Sticker(12, R.drawable.chat_sticker_j_12));
        J_stickers.add(new Sticker(13, R.drawable.chat_sticker_j_13));
        J_stickers.add(new Sticker(14, R.drawable.chat_sticker_j_14));
        J_stickers.add(new Sticker(15, R.drawable.chat_sticker_j_15));
        J_stickers.add(new Sticker(16, R.drawable.chat_sticker_j_16));
        J_stickers.add(new Sticker(17, R.drawable.chat_sticker_j_17));
        J_stickers.add(new Sticker(18, R.drawable.chat_sticker_j_18));


        // FREE
        A_category = new StickerCategory("Dragons", "A", A_stickers, R.drawable.chat_sticker_a_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 10.0, StickerCategory.SHOP_TYPE_FREE, 0);
        B_category = new StickerCategory("Fox", "B", B_stickers, R.drawable.chat_sticker_b_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 10.0, StickerCategory.SHOP_TYPE_FREE, 0);
        C_category = new StickerCategory("KFP", "C", C_stickers, R.drawable.chat_sticker_c_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 20.0, StickerCategory.SHOP_TYPE_FREE, 0);
        D_category = new StickerCategory("Madagascar", "D", D_stickers, R.drawable.chat_sticker_d_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 50.0, StickerCategory.SHOP_TYPE_FREE, 0);
        E_category = new StickerCategory("nabi", "E", E_stickers, R.drawable.chat_sticker_e_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 10.0, StickerCategory.SHOP_TYPE_FREE, 0);
        F_category = new StickerCategory("RedBird", "F", F_stickers, R.drawable.chat_sticker_f_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 10.0, StickerCategory.SHOP_TYPE_FREE, 0);
        G_category = new StickerCategory("Shrek", "G", G_stickers, R.drawable.chat_sticker_g_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 20.0, StickerCategory.SHOP_TYPE_FREE, 0);
        H_category = new StickerCategory("Tong", "H", H_stickers, R.drawable.chat_sticker_h_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 50.0, StickerCategory.SHOP_TYPE_FREE, 0);
        I_category = new StickerCategory("Emoji", "I", I_stickers, R.drawable.chat_sticker_i_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 50.0, StickerCategory.SHOP_TYPE_FREE, 0);
        J_category = new StickerCategory("Dreamworks Characters", "J", J_stickers, R.drawable.chat_sticker_j_01, StickerCategory.PURCHASE_STATUS_DOWNLOADED, 50.0, StickerCategory.SHOP_TYPE_FREE, 0);


        totalCategories.add(I_category);
        totalCategories.add(H_category);
        totalCategories.add(B_category);
        totalCategories.add(F_category);
        totalCategories.add(E_category);
        totalCategories.add(J_category);

		/*
        totalCategories.add(A_category);
		
		totalCategories.add(C_category);
		totalCategories.add(D_category);
		
		
		totalCategories.add(G_category);
		*/


        // create sticker hash table
        for (StickerCategory category : totalCategories) {
            ArrayList<Sticker> stickerList = category.getStickerList();
            for (Sticker sticker : stickerList) {
                stickerIconTable.put(createPrefixString(category.getId(), String.valueOf(sticker.getId())), sticker.getResId());
            }
        }

        // remove dreamwork stickers
        totalCategories.remove(J_category);

        LOG.V(TAG, "static initialize - end()");
    }

    public static ArrayList<StickerCategory> getShopCategory(int shopType) {
        ArrayList<StickerCategory> categories = new ArrayList<StickerCategory>();
        for (StickerCategory item : totalCategories) {
            if (item.getShopType() == shopType)
                categories.add(item);
        }

        return categories;
    }

    public static int getSticker(String code) {
        //LOG.V(TAG,"getSticker() - code is "+code);

		/*
        Enumeration<String> keys = stickerIconTable.keys();
		while (keys.hasMoreElements()) {
			String keyStr = String.valueOf(keys.nextElement());
			int resId = stickerIconTable.get(keyStr);

			LOG.V(TAG, keyStr + " : "+resId);

		}

	*/

        if (stickerIconTable.containsKey(code))
            return stickerIconTable.get(code);

        return -1;
    }

    public static String createPrefixString(String categoryId, String stickerId) {
        return STICKER_PREFIX + categoryId + "_" + stickerId;
    }
}
