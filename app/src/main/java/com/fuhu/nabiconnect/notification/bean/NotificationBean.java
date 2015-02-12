package com.fuhu.nabiconnect.notification.bean;

public class NotificationBean {

	
	public static final String KEY_PACKAGE_NAME = "packageName";
	public static final String KEY_USER_KEY = "userKey";
	public static final String KEY_KID_ID = "kidId";
	public static final String KEY_TITLE = "title";
	public static final String KEY_FRIEND_CODE = "friendCode";
	public static final String KEY_CONTENT = "content";
	public static final String KEY_APPLICATION_NAME = "application";
	
	private String packageName;
	private String userKey;
	private String kidId;
	private String title;
	private String friendCode;
	private String content;
	private String applicationName;
	
	public NotificationBean() {
		packageName = "";
		userKey= "";
		kidId= "";
		title= "";
		friendCode= "";
		content= "";
		applicationName = "";
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}

	public String getKidId() {
		return kidId;
	}

	public void setKidId(String kidId) {
		this.kidId = kidId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFriendCode() {
		return friendCode;
	}

	public void setFriendCode(String friendCode) {
		this.friendCode = friendCode;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
}
