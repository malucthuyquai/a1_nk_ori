package com.fuhu.nabiconnect.chat.stickers;

import java.util.ArrayList;

public class StickerCategory {

	/*======================================================================
	 * Constant Fields
	 *=======================================================================*/
	public static final String TAG = "StickerManager";
	
	public static final int PURCHASE_STATUS_ON_SALE = 1;
	public static final int PURCHASE_STATUS_DOWNLOADED = 2;
	public static final int PURCHASE_STATUS_FREE = 3;
	
	public static final int SHOP_TYPE_TOP = 100;
	public static final int SHOP_TYPE_FREE = 101;
	public static final int SHOP_TYPE_NEW = 102;
	
	
	private String name;
	private String id;
	private ArrayList<Sticker> stickerList;
	private int coverResId;
	private int purchaseStatus;
	private double price;
	private int shopType;
	private int stickerBarResId;
	
	public StickerCategory(String name, String id, ArrayList<Sticker> list, int coverResId, int purchaseStatus, double price, int shopType, int stickerBarResId)
	{
		this.name = name;
		this.id = id;
		this.stickerList = list;
		this.coverResId = coverResId;
		this.purchaseStatus = purchaseStatus;
		this.price = price;
		this.shopType = shopType;
		this.stickerBarResId = stickerBarResId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ArrayList<Sticker> getStickerList() {
		return stickerList;
	}

	public void setStickerList(ArrayList<Sticker> stickerList) {
		this.stickerList = stickerList;
	}

	public int getCoverResId() {
		return coverResId;
	}

	public void setCoverResId(int coverResId) {
		this.coverResId = coverResId;
	}

	public int getPurchaseStatus() {
		return purchaseStatus;
	}

	public void setPurchaseStatus(int purchaseStatus) {
		this.purchaseStatus = purchaseStatus;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getShopType() {
		return shopType;
	}

	public void setShopType(int shopType) {
		this.shopType = shopType;
	}

	public int getStickerBarResId() {
		return stickerBarResId;
	}

	public void setStickerBarResId(int stickerBarResId) {
		this.stickerBarResId = stickerBarResId;
	}
	
	
	
}
