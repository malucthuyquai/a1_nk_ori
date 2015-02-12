package com.fuhu.nabiconnect.friend.avatar;

import com.fuhu.nabiconnect.log.LOG;

import java.util.ArrayList;

public class FriendBean {
	
	public static final String TAG = "FriendBean";
	
	public static final String KEY_FRIEND_CODE = "friendCode";
	public static final String KEY_ACCOUNT_ID = "accountId";
	public static final String KEY_ACCOUNT_TYPE = "accountType";
	public static final String KEY_NAME = "name";
	public static final String KEY_CHARACTER_TYPE = "characterTypeIndex";
	public static final String KEY_CHARACTER_COLOR = "characterColorIndex";
	public static final String KEY_CHARACTER_CLOTHING = "characterClothingIndex";
	public static final String KEY_CHARACTER_GLASSES = "characterGlassesIndex";
	//public static final String KEY_CHARACTER_PIPE = "characterPipeIndex";
	public static final String KEY_CHARACTER_NECKTIE = "characterNeckTieIndex";
	public static final String KEY_CHARACTER_HAIRBAND = "characterHairBandIndex";
	public static final String KEY_CHARACTER_MOUSTACHE = "characterMoustacheIndex";
	public static final String KEY_CHARACTER_BACKGROUND_COLOR = "characterBackgroundColorIndex";
	public static final String KEY_STATUS = "status";
	
	String friendCode;
	String accountId;
	String accountType;
	String name;
	int characterTypeIndex;
	int characterColorIndex;
	int characterClothingIndex;
	int characterGlassesIndex;
	//int characterPipeIndex;
	int characterNeckTieIndex;
	int characterHairBandIndex;
	int characterMoustacheIndex;
	int characterBackgroundColorIndex;
	int status;
	
	public FriendBean()
	{}
	
	public FriendBean(FriendBean bean)
	{
		this.friendCode = bean.friendCode;
		this.accountId = bean.accountId;
		this.accountType = bean.accountType;
		this.name = bean.name;
		this.characterTypeIndex = bean.characterTypeIndex;
		this.characterColorIndex = bean.characterColorIndex;
		this.characterClothingIndex = bean.characterClothingIndex;
		this.characterGlassesIndex = bean.characterGlassesIndex;
		//this.characterPipeIndex = bean.characterPipeIndex;
		this.characterNeckTieIndex = bean.characterNeckTieIndex;
		this.characterHairBandIndex = bean.characterHairBandIndex;
		this.characterMoustacheIndex = bean.characterMoustacheIndex;
		this.characterBackgroundColorIndex = bean.characterBackgroundColorIndex;
		this.status = bean.status;	
	}
	
	public String getFriendCode() {
		return friendCode;
	}
	public void setFriendCode(String friendCode) {
		this.friendCode = friendCode;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getAccountType() {
		return accountType;
	}
	public void setAccountType(String accountType) {
		this.accountType = accountType;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getCharacterTypeIndex() {
		return characterTypeIndex;
	}
	public void setCharacterTypeIndex(int characterTypeIndex) {
		this.characterTypeIndex = characterTypeIndex;
	}
	public int getCharacterColorIndex() {
		return characterColorIndex;
	}
	public void setCharacterColorIndex(int characterColorIndex) {
		this.characterColorIndex = characterColorIndex;
	}
	public int getCharacterClothingIndex() {
		return characterClothingIndex;
	}
	public void setCharacterClothingIndex(int characterClothingIndex) {
		this.characterClothingIndex = characterClothingIndex;
	}
	public int getCharacterGlassesIndex() {
		return characterGlassesIndex;
	}
	public void setCharacterGlassesIndex(int characterGlassesIndex) {
		this.characterGlassesIndex = characterGlassesIndex;
	}
	/*
	public int getCharacterPipeIndex() {
		return characterPipeIndex;
	}
	public void setCharacterPipeIndex(int characterPipeIndex) {
		this.characterPipeIndex = characterPipeIndex;
	}
	*/
	
	public int getCharacterMoustacheIndex() {
		return characterMoustacheIndex;
	}
	public int getCharacterNeckTieIndex() {
		return characterNeckTieIndex;
	}

	public void setCharacterNeckTieIndex(int characterNeckTieIndex) {
		this.characterNeckTieIndex = characterNeckTieIndex;
	}

	public int getCharacterHairBandIndex() {
		return characterHairBandIndex;
	}

	public void setCharacterHairBandIndex(int characterHairBandIndex) {
		this.characterHairBandIndex = characterHairBandIndex;
	}

	public void setCharacterMoustacheIndex(int characterMoustacheIndex) {
		this.characterMoustacheIndex = characterMoustacheIndex;
	}
	public int getCharacterBackgroundColorIndex() {
		return characterBackgroundColorIndex;
	}
	public void setCharacterBackgroundColorIndex(int characterBackgroundColorIndex) {
		this.characterBackgroundColorIndex = characterBackgroundColorIndex;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public void printInfo()
	{
		LOG.V(TAG, "friendCode : "+friendCode);
		LOG.V(TAG, "accountId : "+accountId);
		LOG.V(TAG, "accountType : "+accountType);
		LOG.V(TAG, "name : "+name);
		LOG.V(TAG, "characterTypeIndex : "+characterTypeIndex);
		LOG.V(TAG, "characterColorIndex : "+characterColorIndex);
		LOG.V(TAG, "characterClothingIndex : "+characterClothingIndex);
		LOG.V(TAG, "characterGlassesIndex : "+characterGlassesIndex);
		//LOG.V(TAG, "characterPipeIndex : "+characterPipeIndex);	
		LOG.V(TAG, "characterNeckTieIndex : "+characterNeckTieIndex);	
		LOG.V(TAG, "characterHairBandIndex : "+characterHairBandIndex);	
		LOG.V(TAG, "characterMoustacheIndex : "+characterMoustacheIndex);
		LOG.V(TAG, "characterBackgroundColorIndex : "+characterBackgroundColorIndex);
		LOG.V(TAG, "status : "+status);
		
	}
	
	public static ArrayList<Long> getAccssoriesList(FriendBean bean)
	{
		if(bean == null)
			return null;
		
		ArrayList<Long> accessories = new ArrayList<Long>();
		accessories.add((long)bean.getCharacterGlassesIndex());
		accessories.add((long)bean.getCharacterHairBandIndex());
		accessories.add((long)bean.getCharacterMoustacheIndex());
		accessories.add((long)bean.getCharacterNeckTieIndex());
		
		return accessories;
	}
	
}
