package com.fuhu.nabiconnect.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.fuhu.data.FriendData;
import com.fuhu.data.InboxesData;
import com.fuhu.data.MailData;
import com.fuhu.data.OutboxesData;
import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.data.SharedPhotoData;
import com.fuhu.data.SharedPhotoToUserData;
import com.fuhu.data.UserData;
import com.fuhu.data.conversationData;
import com.fuhu.data.messageData;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.ndnslibsoutstructs.chatHistory_outObj;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.ndnslibsoutstructs.getInboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getMail_outObj;
import com.fuhu.ndnslibsoutstructs.getOutboxes_outObj;
import com.fuhu.ndnslibsoutstructs.getOutgoingMail_outObj;
import com.fuhu.ndnslibsoutstructs.getReceivedPhotos_outObj;
import com.fuhu.ndnslibsoutstructs.getSharedAndReceivedPhotos_outObj;
import com.fuhu.ndnslibsoutstructs.getSharedPhotos_outObj;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 
 * Usage:<br>
 * In Activity or Fragment where a context reference is available, obtain a
 * DatabaseAdapter instance by calling the static method:<br>
 * 
 * <b>public static DatabaseAdapter getInstance(Context context)</b><br>
 * 
 * Then call all public methods using the returned instance.
 * 
 */
public class DatabaseAdapter {

	final private static String TAG = DatabaseAdapter.class.getSimpleName();
	final Context mContext;

	/** database file name on device storage */
	final static String DATABASE_NAME = "connect.db";

	/** increment this number every time there is a change in db schema */
	final static int DATABASE_VERSION = 6;

	/**
	 * helper class to handle database creation and version upgrade (e.g. schema
	 * change)
	 */
	private DatabaseHelper mDatabaseHelper;

	/** internal reference of actual database object */
	private SQLiteDatabase db;

	/** internal reference of database adapter */
	private static volatile DatabaseAdapter mAdapter;

	/**
	 * do not use this constructor. use DatabaseAdapter.getInstance(Context
	 * context) instead
	 */
	public DatabaseAdapter(Context context) {
		this.mContext = context;
		mDatabaseHelper = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public static DatabaseAdapter getInstance(Context context) {
		DatabaseAdapter adapter = mAdapter;
		if (adapter == null) {
			synchronized (DatabaseAdapter.class) {
				adapter = mAdapter;
				if (adapter == null) {
					mAdapter = adapter = new DatabaseAdapter(context);
				}
			}
		}
		return adapter;
	}

	public DatabaseAdapter open() throws SQLException {
		db = mDatabaseHelper.getWritableDatabase();
		LOG.D(TAG, "database ready");
		return this;
	}

	public void close() {
		mDatabaseHelper.close();
	}

	/**********************************
	 *********** CONSTANTS ************
	 **********************************/

	/**********************************
	 ********** COLUMN NAMES **********
	 **********************************/

	/**
	 * USER_INFO
	 */
	final private static String TABLE_USER_INFO = "USER_INFO";
	final private static String KEY_USER_KEY = "USER_KEY";
	final private static String KEY_FRIEND_CODE = "FRIEND_CODE";
	final private static String KEY_SESSION_KEY = "SESSION_KEY";
	final private static String KEY_FIRST_NAME = "FIRST_NAME";
	final private static String KEY_LAST_NAME = "LAST_NAME";
	final private static String KEY_USER_NAME = "USER_NAME";
	final private static String KEY_OSG_KEY = "OSG_KEY";
	final private static String KEY_KID_ID = "KID_ID";
	final private static String KEY_AVATAR_CACHE = "AVATAR_CACHE";
	final private static String KEY_CHARACTER = "CHARACTER";
	final private static String KEY_CHARACTER_COLOR = "CHARACTER_COLOR";
	final private static String KEY_CLOTHING = "CLOTHING";
	final private static String KEY_BACKGROUND = "BACKGROUND";
	final private static String KEY_ACCESSORY = "ACCESSORY";

	/**
	 * FRIEND
	 */
	final private static String TABLE_FRIEND = "FRIEND";
	// final private static String KEY_USER_KEY;
	final private static String KEY_FRIEND_KEY = "FRIEND_KEY";
	final private static String KEY_FRIEND_USERNAME = "FRIEND_USERNAME";
	// final private static String KEY_AVATAR_CACHE;
	final private static String KEY_RELATIONSHIP = "RELATIONSHIP";
	final private static String KEY_BLOCKED = "BLOCKED";
	final private static String KEY_OSG_KID_ID = "OSG_KID_ID";
	final private static String KEY_OSG_USER_KEY = "OSG_USER_KEY";

	/**
	 * CONVERSATION
	 */
	final private static String TABLE_CONVERSATION = "CONVERSATION";
	// final privatestatic String USER_KEY;
	final private static String KEY_CONVERSATION_ID = "CONVERSATION_ID";
	final private static String KEY_ACTORS = "ACTORS";
	final private static String KEY_UNREAD_COUNT = "UNREAD_COUNT";
	final private static String KEY_LAST_READ_MESSAGE = "LAST_READ_MESSAGE";
	final private static String KEY_LAST_READ_TIMESTAMP = "LAST_READ_TIMESTAMP";

	/**
	 * CHAT MESSAGE
	 */
	final private static String TABLE_CHAT_MESSAGE = "CHAT_MESSAGE";
	// final private static String KEY_CONVERSATION_ID;
	final private static String KEY_MESSAGE_ID = "MESSAGE_ID";
	// final private static String KEY_USER_KEY;
	final private static String KEY_SENDER_KEY = "SENDER_KEY";
	final private static String KEY_TIMESTAMP = "TIMESTAMP";
	final private static String KEY_MESSAGE_BODY = "MESSAGE_BODY";

	/**
	 * MAIL
	 */
	final private static String TABLE_MAILBOX = "MAILBOX";
	// final private static String KEY_USER_KEY;
	final private static String KEY_BOX_ID = "BOX_ID";
	// final private static String KEY_FRIEND_KEY;
	final private static String KEY_TYPE = "TYPE";
	// final private static String KEY_FRIEND_USERNAME;
	// final private static String KEY_UNREAD_COUNT;
	// final private static String KEY_AVATAR_CACHE;
	final private static String KEY_LAST_RECEIVED_TIMESTAMP = "LAST_RECEIVED_TIMESTAMP";

	/**
	 * MAIL MESSAGE
	 */
	final private static String TABLE_MAIL_MESSAGE = "MAIL_MESSAGE";
	final private static String KEY_MAIL_ID = "MAIL_ID";
	// final private static String KEY_BOX_ID;
	// final private static String KEY_TYPE;
	final private static String KEY_FILE_URL = "FILE_URL";
	final private static String KEY_FILE_NAME = "FILE_NAME";
	// final private static String KEY_TIMESTAMP;

	/**
	 * PHOTO
	 */
	final private static String TABLE_PHOTO = "PHOTO";
	// final private static String KEY_USER_KEY;
	final private static String KEY_PHOTO_ID = "PHOTO_ID";
	final private static String KEY_PHOTO_TITLE = "PHOTO_TITLE";
	final private static String KEY_PHOTO_URL = "PHOTO_URL";
	final private static String KEY_SENDER_NAME = "SENDER_NAME";
	// final private static String KEY_SENDER_KEY;
	final private static String KEY_RECEIVER = "RECEIVER";
	// final private static String KEY_TIMESTAMP;
	// final private static String KEY_AVATAR_CACHE;
	final private static String KEY_THUMBNAIL_CACHE = "THUMBNAIL_CACHE";

	/************************************
	 ********** OTHER CONSTANT **********
	 ************************************/
	final private static String FOLDER_FRIEND = "friend";
	final private static String FOLDER_PHOTO = "photo";
	final private static String FOLDER_MAIL = "mail";
	// final private static String FOLDER_THUMBNAIL = "thumbnail";
	// final private static String FOLDER_USER_INFO = "userinfo";
	final private static String PNG = ".png";
	final private static String INBOX = "0";
	final private static String RECEIVED = "0";
	final private static String OUTBOX = "1";
	final private static String SENT = "1";
	final private static String FAKE_PHOTO_ID = "LOREMIPSUMDOLORSITAMETCONSECTETURADIPISICINGELITSEDDOEIUSMODTEMPORINCIDIDUNTUTLABOREETDOLOREMAGNAALIQUA";
	final private static String UNBLOCKED = "0";
	final private static String BLOCKED = "1";

	/***********************************
	 ********** BEGIN QUERIES **********
	 ***********************************/

	/*************
	 * USER DATA *
	 *************/

	/**
	 * replaces a row in USER_INFO table with
	 * 
	 * @param data
	 * @return true if insert ok
	 */
	public boolean createUser(UserData data) {
		try {
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, data.userKey);
			values.put(KEY_FRIEND_CODE, data.friendCode);
			values.put(KEY_SESSION_KEY, data.sessionKey);
			values.put(KEY_FIRST_NAME, data.firstName);
			values.put(KEY_LAST_NAME, data.lastName);
			values.put(KEY_USER_NAME, data.userName);
			values.put(KEY_OSG_KEY, data.osgKey);
			values.put(KEY_KID_ID, data.kidID);
			values.put(KEY_AVATAR_CACHE, data.avatarURL);
			values.put(KEY_CHARACTER, data.Character);
			values.put(KEY_CHARACTER_COLOR, data.CharacterColor);
			values.put(KEY_CLOTHING, data.CharacterClothing);
			values.put(KEY_BACKGROUND, data.CharacterBackground);
			values.put(KEY_ACCESSORY, data.getAccessoryString());
			return db.replace(TABLE_USER_INFO, null, values) != -1;
		} catch (NullPointerException e) {
			LOG.E(TAG, "NPE");
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param userKey
	 * @return UserData with matching key, or new UserData() if not found.
	 *         avatarURL will be relative local file path after data/data
	 */
	public UserData getUserData(String userKey) {
		UserData data = new UserData();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_USER_INFO, null, KEY_USER_KEY + " = ?", new String[] { userKey }, null, null, null);
			if (c != null && c.moveToFirst()) {
				data.userKey = c.getString(c.getColumnIndex(KEY_USER_KEY));
				data.friendCode = c.getString(c.getColumnIndex(KEY_FRIEND_CODE));
				data.sessionKey = c.getString(c.getColumnIndex(KEY_SESSION_KEY));
				data.firstName = c.getString(c.getColumnIndex(KEY_FIRST_NAME));
				data.lastName = c.getString(c.getColumnIndex(KEY_LAST_NAME));
				data.userName = c.getString(c.getColumnIndex(KEY_USER_NAME));
				data.osgKey = c.getString(c.getColumnIndex(KEY_OSG_KEY));
				data.kidID = c.getString(c.getColumnIndex(KEY_KID_ID));
				data.avatarURL = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
				data.Character = c.getLong(c.getColumnIndex(KEY_CHARACTER));
				data.CharacterColor = c.getLong(c.getColumnIndex(KEY_CHARACTER_COLOR));
				data.CharacterClothing = c.getLong(c.getColumnIndex(KEY_CLOTHING));
				data.CharacterBackground = c.getLong(c.getColumnIndex(KEY_BACKGROUND));
				data.parseAccessoryString(c.getString(c.getColumnIndex(KEY_ACCESSORY)));
				c.close();
			}
		}
		return data;
	}

	public String getUserKey(long kidId) {
		return getUserKey(Long.toString(kidId));
	}

	/**
	 * 
	 * @param kidId
	 * @return UserKey of kid, or empty string if not found
	 */
	public String getUserKey(String kidId) {
		Cursor c;
		c = db.query(TABLE_USER_INFO, new String[] { KEY_USER_KEY }, KEY_KID_ID + " = ?", new String[] { kidId }, null,
				null, null);
		if (c != null && c.moveToFirst()) {
			String key = c.getString(0);
			c.close();
			return key;
		} else {
			return "";
		}

	}

	public boolean updateUserData(UserData data) {
		return createUser(data);
	}

	/**********
	 * FRIEND *
	 **********/

	/**
	 * 
	 * @param userKey
	 * @return total number of friends of user, including blocked and deleted
	 *         friends
	 */
	// private int getFriendCount(String userKey) {
	// Cursor c;
	// c = db.rawQuery("SELECT COUNT(*) FROM FRIEND WHERE USER_KEY = ?", new
	// String[] { userKey });
	// if (c != null && c.moveToFirst()) {
	// int i = c.getInt(0);
	// c.close();
	// return i;
	// }
	// return 0;
	// }

	/**
	 * 
	 * @param userKey
	 *            current user key (id)
	 * @return ArrayList of FriendData not including friends blocked by you,
	 *         FriendData.AvatarUrl will be local file path
	 */
	public ArrayList<FriendData> getFriendList(String userKey) {
		ArrayList<FriendData> friends = new ArrayList<FriendData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_FRIEND, null, KEY_USER_KEY + " = ? AND (" + KEY_BLOCKED + " IS NULL OR " + KEY_BLOCKED
					+ " = ?)", new String[] { userKey, UNBLOCKED }, null, null, KEY_FRIEND_KEY + " ASC");
			if (c != null && c.moveToFirst()) {
				FriendData fd;
				while (!c.isAfterLast()) {
					fd = new FriendData();
					fd.userID = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
					fd.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
					fd.AvatarUrl = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					fd.relationship = c.getInt(c.getColumnIndex(KEY_RELATIONSHIP));
					fd.blocked = false;
					fd.osgKidId = c.getLong(c.getColumnIndex(KEY_OSG_KID_ID));
					fd.osgUserKey = c.getString(c.getColumnIndex(KEY_OSG_USER_KEY));
					friends.add(fd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return friends;
	}

	/**
	 * 
	 * @param userKey
	 *            current user key (id)
	 * @return ArrayList of FriendData including blocked friends, sorted by
	 *         username FriendData.AvatarUrl will be local file path
	 */
	public ArrayList<FriendData> getNSAFriendList(String userKey) {
		ArrayList<FriendData> friends = new ArrayList<FriendData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_FRIEND, null, KEY_USER_KEY + " = ?", new String[] { userKey }, null, null,
					KEY_FRIEND_USERNAME + " COLLATE NOCASE ASC");
			if (c != null && c.moveToFirst()) {
				FriendData fd;
				while (!c.isAfterLast()) {
					fd = new FriendData();
					fd.userID = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
					fd.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
					fd.AvatarUrl = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					fd.relationship = c.getInt(c.getColumnIndex(KEY_RELATIONSHIP));
					String blocked = c.getString(c.getColumnIndex(KEY_BLOCKED));
					fd.blocked = blocked != null && blocked.equals(BLOCKED);
					fd.osgKidId = c.getLong(c.getColumnIndex(KEY_OSG_KID_ID));
					fd.osgUserKey = c.getString(c.getColumnIndex(KEY_OSG_USER_KEY));
					friends.add(fd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return friends;
	}

	/**
	 * 
	 * @param data
	 *            must contain userKey
	 * @return true if update ok
	 */
	public boolean updateFriendList(friends_outObj data) {
		if (data.mUserKey == null || data.mUserKey.isEmpty()) {
			LOG.D(TAG, "user key null");
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		ContentValues values = new ContentValues();
		try {
			for (FriendData fd : data.getFriends()) {
				values.put(KEY_USER_KEY, data.mUserKey);
				values.put(KEY_FRIEND_KEY, fd.userID);
				values.put(KEY_FRIEND_USERNAME, fd.userName);
				values.put(KEY_AVATAR_CACHE, fd.AvatarUrl);
				values.put(KEY_RELATIONSHIP, fd.relationship);
				// force unblocked because friend list now only contains normal
				// friends
				values.put(KEY_BLOCKED, UNBLOCKED);
				values.put(KEY_OSG_KID_ID, fd.osgKidId);
				values.put(KEY_OSG_USER_KEY, fd.osgUserKey);
				db.replace(TABLE_FRIEND, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * old implementation
	 */
	// public boolean updateFriendList(friends_outObj data) {
	// if (data.mUserKey == null || data.mUserKey.isEmpty()) {
	// LOG.D(TAG, "user key null");
	// return false;
	// }
	//
	// // check if there is local friend data
	// Cursor c;
	// // =====
	// HashSet<String> localFriendId = null;
	// // =====
	// c = db.query(TABLE_FRIEND, new String[] { KEY_FRIEND_KEY }, KEY_USER_KEY
	// + " = ?",
	// new String[] { data.mUserKey }, null, null, null);
	// int count = 0;
	// if (c != null) {
	// count = c.getCount();
	// // =====
	// LOG.D(TAG, "local friend count: " + count);
	// if (count > 0) {
	// localFriendId = new HashSet<String>();
	// if (c.moveToFirst()) {
	// while (!c.isAfterLast()) {
	// // just use hard coded index 0 since its the only column
	// // returned;
	// LOG.D(TAG, "adding: " + c.getString(0));
	// localFriendId.add(c.getString(0));
	// c.moveToNext();
	// }
	// }
	// }
	// // =====
	// c.close();
	// }
	//
	// boolean returnVal = false;
	// ArrayList<FriendData> friends = data.getFriends();
	// ContentValues values = new ContentValues();
	// if (count > 0) {
	// db.beginTransaction();
	// try {
	// for (FriendData fd : friends) {
	// values.put(KEY_FRIEND_USERNAME, fd.userName);
	// values.put(KEY_AVATAR_CACHE, fd.AvatarUrl);
	// values.put(KEY_RELATIONSHIP, fd.relationship);
	// values.put(KEY_BLOCKED, UNBLOCKED);
	// values.put(KEY_OSG_KID_ID, fd.osgKidId);
	// values.put(KEY_OSG_USER_KEY, fd.osgUserKey);
	// int i = db.update(TABLE_FRIEND, values, KEY_USER_KEY + " = ? AND " +
	// KEY_FRIEND_KEY + " = ?",
	// new String[] { data.mUserKey, fd.userID });
	// if (i == 0) {
	// // nothing updated because this is a new friend
	// values.put(KEY_USER_KEY, data.mUserKey);
	// values.put(KEY_FRIEND_KEY, fd.userID);
	// db.insert(TABLE_FRIEND, null, values);
	// }
	// // =====
	// boolean b = localFriendId.remove(fd.userID);
	// LOG.D(TAG, "removed " + fd.userID + " " + b);
	// // =====
	// values.clear();
	// }
	// // =====
	// /**
	// * clean up friend; see if we have been deleted
	// */
	// for (String s : localFriendId) {
	// Cursor c1 = db
	// .rawQuery(
	// "SELECT FRIEND_USERNAME FROM FRIEND WHERE USER_KEY = ? AND FRIEND_KEY = ? AND (BLOCKED IS NULL OR BLOCKED = ?)",
	// new String[] { data.mUserKey, s, UNBLOCKED });
	// if (c1 != null && c1.moveToFirst()) {
	// LOG.D(TAG, c1.getString(0) + " blocked you");
	// int i = db.delete(TABLE_FRIEND, KEY_USER_KEY + " = ? AND " +
	// KEY_FRIEND_KEY + " = ? AND "
	// + KEY_BLOCKED + " = ?", new String[] { data.mUserKey, s, UNBLOCKED });
	// LOG.D(TAG, "deleting " + c1.getString(0) + " result: " + i);
	// c1.close();
	// } else {
	// LOG.D(TAG, "c1 is null");
	// }
	// }
	// // =====
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// } else {
	// db.beginTransaction();
	// try {
	// for (FriendData fd : friends) {
	// values.put(KEY_USER_KEY, data.mUserKey);
	// values.put(KEY_FRIEND_KEY, fd.userID);
	// values.put(KEY_FRIEND_USERNAME, fd.userName);
	// values.put(KEY_AVATAR_CACHE, fd.AvatarUrl);
	// values.put(KEY_RELATIONSHIP, fd.relationship);
	// values.put(KEY_BLOCKED, UNBLOCKED);
	// values.put(KEY_OSG_KID_ID, fd.osgKidId);
	// values.put(KEY_OSG_USER_KEY, fd.osgUserKey);
	// db.insert(TABLE_FRIEND, null, values);
	// }
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// }
	// return returnVal;
	// }
	/**
	 * 
	 */

	/**
	 * This will clear local history, use this in NSA only
	 * 
	 * @param data
	 * @return
	 */
	public boolean updateNSAFriendList(friends_outObj data) {
		if (data.mUserKey == null || data.mUserKey.isEmpty()) {
			return false;
		}
		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove all local friends first
			clearFriendList(data.mUserKey);
			// insert returned friend data
			ArrayList<FriendData> friends = data.getFriends();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, data.mUserKey);
			for (FriendData fd : friends) {
				values.put(KEY_FRIEND_KEY, fd.userID);
				values.put(KEY_FRIEND_USERNAME, fd.userName);
				values.put(KEY_AVATAR_CACHE, fd.AvatarUrl);
				values.put(KEY_RELATIONSHIP, fd.relationship);
				values.put(KEY_BLOCKED, fd.blocked ? BLOCKED : UNBLOCKED);
				values.put(KEY_OSG_KID_ID, fd.osgKidId);
				values.put(KEY_OSG_USER_KEY, fd.osgUserKey);
				db.insert(TABLE_FRIEND, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * see if specified friend is blocked by you.
	 * 
	 * @param userKey
	 * @param friendKey
	 * @return
	 */
	public boolean isFriendBlocked(String userKey, String friendKey) {
		Cursor c;
		// NOTE: update below if adding more column in result set
		c = db.query(TABLE_FRIEND, new String[] { KEY_BLOCKED }, KEY_USER_KEY + " = ? AND " + KEY_FRIEND_KEY + " = ?",
				new String[] { userKey, friendKey }, null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				// this is the only column returned, just use 0
				String s = c.getString(0);
				c.close();
				return s != null && s.equals(BLOCKED);
			} else {
				c.close();
			}
		}
		return false;
	}

	/**
	 * will delete local file cache first before clearing db data
	 * 
	 * @param userKey
	 *            current user's key
	 * @param friendKey
	 *            key of friend to be deleted
	 * @return
	 */
	// NOTE: not sure if we should delete other data as well, i.e. chat, mail,
	// photo
	public boolean deleteFriend(String userKey, String friendKey) {
		File file = new File(getFriendAvatarUrl(userKey, friendKey));
		boolean deleteOK = true;
		if (file.canRead() && file.length() > 0) {
			deleteOK = file.delete();
		}
		if (deleteOK) {
			return db.delete(TABLE_FRIEND, KEY_USER_KEY + " = ? AND " + KEY_FRIEND_KEY + " = ?", new String[] {
					userKey, friendKey }) == 1;
		} else {
			return false;
		}
	}

	public boolean blockFriend(String userKey, String friendKey) {
		ContentValues values = new ContentValues();
		values.put(KEY_BLOCKED, BLOCKED);
		return db.update(TABLE_FRIEND, values, KEY_USER_KEY + " = ? AND " + KEY_FRIEND_KEY + " = ?", new String[] {
				userKey, friendKey }) == 1;
	}

	/****************
	 * CONVERSATION *
	 ****************/

	/**
	 * creates an entry in conversation table
	 * 
	 * @param userKey
	 * @param conversationId
	 * @param actors
	 * @return
	 */
	public boolean createConversation(String userKey, String conversationId, ArrayList<String> actors) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}
		ContentValues values = new ContentValues();
		values.put(KEY_USER_KEY, userKey);
		values.put(KEY_CONVERSATION_ID, conversationId);
		values.put(KEY_ACTORS, asJSONArrayString(actors));
		values.put(KEY_UNREAD_COUNT, 0);
		values.put(KEY_LAST_READ_MESSAGE, "");
		values.put(KEY_LAST_READ_TIMESTAMP, 0);
		return db.insert(TABLE_CONVERSATION, null, values) != -1;
	}

	public String getConversationId(String userKey, String contactKey) {
		Cursor c;
		c = db.query(TABLE_CONVERSATION, new String[] { KEY_ACTORS, KEY_CONVERSATION_ID }, KEY_USER_KEY + " = ?",
				new String[] { userKey }, null, null, null);
		if (c != null && c.moveToFirst()) {
			while (!c.isAfterLast()) {
				try {
					JSONArray jArray = new JSONArray(c.getString(0));
					if (jArray.length() == 2) {
						if ((jArray.getString(0).equals(userKey) && jArray.getString(1).equals(contactKey))
								|| (jArray.getString(1).equals(userKey) && jArray.getString(0).equals(contactKey))) {
							// match
							String id = c.getString(1);
							c.close();
							return id;
						}
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				c.moveToNext();
			}
		}
		return "";
	}

	/**
	 * 
	 * @param userKey
	 * @return list of conversation that user of userKey is in
	 */
	public ArrayList<conversationData> getConversationList(String userKey) {
		ArrayList<conversationData> conversations = new ArrayList<conversationData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_CONVERSATION, null, KEY_USER_KEY + " = ?", new String[] { userKey }, null, null,
					KEY_CONVERSATION_ID + " ASC");
			if (c != null && c.moveToFirst()) {
				conversationData cd;
				while (!c.isAfterLast()) {
					cd = new conversationData();
					cd.m_Actors = asStringArrayList(c.getString(c.getColumnIndex(KEY_ACTORS)));
					cd.m_ConversationId = c.getString(c.getColumnIndex(KEY_CONVERSATION_ID));
					try {
						cd.m_UnreadMessageCount = c.getInt(c.getColumnIndex(KEY_UNREAD_COUNT));
					} catch (NumberFormatException e) {
						cd.m_UnreadMessageCount = 0;
					}
					cd.m_LastReadMessage = c.getString(c.getColumnIndex(KEY_LAST_READ_MESSAGE));
					try {
						cd.m_LastReadTimestamp = c.getLong(c.getColumnIndex(KEY_LAST_READ_TIMESTAMP));
					} catch (NumberFormatException e) {
						cd.m_LastReadTimestamp = 0;
					}
					conversations.add(cd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return conversations;
	}

	/**
	 * 
	 * @param data
	 * @return true if update ok
	 */
	public boolean updateConversationList(chatPollMessage_outObj data) {
		if (data.mUserKey == null || data.mUserKey.isEmpty()) {
			return false;
		}
		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove all local conversations first
			clearConversationList(data.mUserKey);
			// insert returned conversation data
			ArrayList<conversationData> conversations = data.getConversations();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, data.mUserKey);
			for (conversationData cd : conversations) {
				values.put(KEY_CONVERSATION_ID, cd.m_ConversationId);
				values.put(KEY_ACTORS, asJSONArrayString(cd.m_Actors));
				values.put(KEY_UNREAD_COUNT, cd.m_UnreadMessageCount);
				values.put(KEY_LAST_READ_MESSAGE, cd.m_LastReadMessage);
				values.put(KEY_LAST_READ_TIMESTAMP, cd.m_LastReadTimestamp);
				db.insert(TABLE_CONVERSATION, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/****************
	 * CHAT MESSAGE *
	 ****************/

	/**
	 * 
	 * @param userKey
	 * @param conversationId
	 * @return list of chat message with latest one on top
	 */
	public ArrayList<messageData> getChatHistory(String userKey, String conversationId) {
		ArrayList<messageData> messages = new ArrayList<messageData>();
		if (userKey != null && !userKey.isEmpty() && conversationId != null && !conversationId.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_CHAT_MESSAGE, new String[] { KEY_MESSAGE_ID, KEY_SENDER_KEY, KEY_TIMESTAMP,
					KEY_MESSAGE_BODY }, KEY_USER_KEY + " = ? AND " + KEY_CONVERSATION_ID + " = ?", new String[] {
					userKey, conversationId }, null, null, KEY_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				messageData md;
				while (!c.isAfterLast()) {
					md = new messageData();
					md.m_MessageId = c.getString(c.getColumnIndex(KEY_MESSAGE_ID));
					md.m_SenderId = c.getString(c.getColumnIndex(KEY_SENDER_KEY));
					try {
						md.m_MessageTime = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						md.m_MessageTime = 0;
					}
					md.m_MessageContent = c.getString(c.getColumnIndex(KEY_MESSAGE_BODY));
					messages.add(md);
					c.moveToNext();
				}
				c.close();
			}
		}
		return messages;
	}

	/**
	 * @param data
	 *            must contain conversationId
	 * @return true if update ok
	 */
	public boolean updateChatHistory(String userKey, chatHistory_outObj data) {
		if (data.mConversationId == null || data.mConversationId.isEmpty()) {
			return false;
		}
		boolean returnVal = false;
		db.beginTransaction();
		try {
			// insert returned conversation data
			ArrayList<messageData> messages = data.getMessages();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, userKey);
			values.put(KEY_CONVERSATION_ID, data.mConversationId);
			for (messageData md : messages) {
				values.put(KEY_MESSAGE_ID, md.m_MessageId);
				values.put(KEY_SENDER_KEY, md.m_SenderId);
				values.put(KEY_TIMESTAMP, md.m_MessageTime);
				values.put(KEY_MESSAGE_BODY, md.m_MessageContent);
				db.replace(TABLE_CHAT_MESSAGE, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * deletes a single history message
	 * 
	 * @param userKey
	 *            current user key
	 * @param messageId
	 * 
	 * @return true if delete ok
	 */
	public boolean deleteChatMessage(String userKey, String messageId) {
		return db.delete(TABLE_CHAT_MESSAGE, KEY_USER_KEY + " = ? AND " + KEY_MESSAGE_ID + " = ?", new String[] {
				userKey, messageId }) == 1;
	}

	/********
	 * MAIL *
	 ********/

	/**
	 * 
	 * @param userKey
	 * @return list of inboxes of userKey. <br>
	 *         avatarURL will be relative local file path after data/data
	 */
	// public ArrayList<InboxesData> getInboxes(String userKey) {
	// ArrayList<InboxesData> inboxes = new ArrayList<InboxesData>();
	// if (userKey != null && !userKey.isEmpty()) {
	// Cursor c;
	// c = db.query(TABLE_MAILBOX, new String[] { KEY_BOX_ID, KEY_FRIEND_KEY,
	// KEY_FRIEND_USERNAME,
	// KEY_LAST_RECEIVED_TIMESTAMP, KEY_UNREAD_COUNT, KEY_AVATAR_CACHE },
	// KEY_USER_KEY + " = ? AND "
	// + KEY_TYPE + " = ? AND (" + KEY_BLOCKED + " IS NULL OR " + KEY_BLOCKED +
	// " = ?)", new String[] {
	// userKey, INBOX, UNBLOCKED }, null, null, KEY_BOX_ID + " ASC");
	// if (c != null && c.moveToFirst()) {
	// InboxesData id;
	// while (!c.isAfterLast()) {
	// id = new InboxesData();
	// id.inboxID = c.getString(c.getColumnIndex(KEY_BOX_ID));
	// id.userId = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
	// id.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
	// try {
	// id.lastTimeOfNewReceive =
	// c.getLong(c.getColumnIndex(KEY_LAST_RECEIVED_TIMESTAMP));
	// } catch (NumberFormatException e) {
	// id.lastTimeOfNewReceive = 0;
	// }
	// try {
	// id.newReceiveCount = c.getLong(c.getColumnIndex(KEY_UNREAD_COUNT));
	// } catch (NumberFormatException e) {
	// id.newReceiveCount = 0;
	// }
	// id.avatarURL = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
	// inboxes.add(id);
	// c.moveToNext();
	// }
	// c.close();
	// }
	// }
	// return inboxes;
	// }

	/**
	 * 
	 * @param userKey
	 * @return list of inboxes of userKey. <br>
	 *         avatarURL will be relative local file path after data/data
	 */
	public ArrayList<InboxesData> getNSAInboxes(String userKey) {
		ArrayList<InboxesData> inboxes = new ArrayList<InboxesData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_MAILBOX, new String[] { KEY_BOX_ID, KEY_FRIEND_KEY, KEY_FRIEND_USERNAME,
					KEY_LAST_RECEIVED_TIMESTAMP, KEY_UNREAD_COUNT, KEY_AVATAR_CACHE, KEY_BLOCKED }, KEY_USER_KEY
					+ " = ? AND " + KEY_TYPE + " = ?", new String[] { userKey, INBOX }, null, null,
					KEY_LAST_RECEIVED_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				InboxesData id;
				while (!c.isAfterLast()) {
					id = new InboxesData();
					id.inboxID = c.getString(c.getColumnIndex(KEY_BOX_ID));
					id.userId = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
					id.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
					String blocked = c.getString(c.getColumnIndex(KEY_BLOCKED));
					id.blocked = blocked != null && blocked.equals(BLOCKED);
					try {
						id.lastTimeOfNewReceive = c.getLong(c.getColumnIndex(KEY_LAST_RECEIVED_TIMESTAMP));
					} catch (NumberFormatException e) {
						id.lastTimeOfNewReceive = 0;
					}
					try {
						id.newReceiveCount = c.getLong(c.getColumnIndex(KEY_UNREAD_COUNT));
					} catch (NumberFormatException e) {
						id.newReceiveCount = 0;
					}
					id.avatarURL = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					inboxes.add(id);
					c.moveToNext();
				}
				c.close();
			}
		}
		return inboxes;
	}

	/**
	 * 
	 * @param data
	 *            must contain userkey
	 * @return true if update ok
	 */
	// public boolean updateInboxes(getInboxes_outObj data) {
	// if (data.mUserKey == null || data.mUserKey.isEmpty()) {
	// return false;
	// }
	//
	// boolean returnVal = false;
	// db.beginTransaction();
	// try {
	// // insert returned inbox data
	// ArrayList<InboxesData> inboxes = data.getInboxes();
	// ContentValues values = new ContentValues();
	// for (InboxesData id : inboxes) {
	// values.put(KEY_FRIEND_KEY, id.userId);
	// values.put(KEY_FRIEND_USERNAME, id.userName);
	// values.put(KEY_UNREAD_COUNT, id.newReceiveCount);
	// values.put(KEY_AVATAR_CACHE, id.avatarURL);
	// values.put(KEY_LAST_RECEIVED_TIMESTAMP, id.lastTimeOfNewReceive);
	// int i = db.update(TABLE_MAILBOX, values, KEY_USER_KEY + " = ? AND " +
	// KEY_TYPE + " = ? AND "
	// + KEY_BOX_ID + " = ?", new String[] { data.mUserKey, INBOX, id.inboxID
	// });
	// if (i == 0) {
	// // mailbox does not exist
	// values.put(KEY_USER_KEY, data.mUserKey);
	// values.put(KEY_TYPE, INBOX);
	// values.put(KEY_BOX_ID, id.inboxID);
	// db.insert(TABLE_MAILBOX, null, values);
	// }
	// values.clear();
	// }
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// return returnVal;
	// }

	/**
	 * This will clear local history, use this in NSA only
	 * 
	 * @param data
	 * @return
	 */
	public boolean updateNSAInboxes(getInboxes_outObj data) {
		if (data.mUserKey == null || data.mUserKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove local inbox data first
			clearInboxes(data.mUserKey);
			// insert returned inbox data
			ArrayList<InboxesData> inboxes = data.getInboxes();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, data.mUserKey);
			values.put(KEY_TYPE, INBOX);
			for (InboxesData id : inboxes) {
				values.put(KEY_BOX_ID, id.inboxID);
				values.put(KEY_FRIEND_KEY, id.userId);
				values.put(KEY_FRIEND_USERNAME, id.userName);
				values.put(KEY_UNREAD_COUNT, id.newReceiveCount);
				values.put(KEY_AVATAR_CACHE, id.avatarURL);
				values.put(KEY_LAST_RECEIVED_TIMESTAMP, id.lastTimeOfNewReceive);
				values.put(KEY_BLOCKED, id.blocked ? BLOCKED : UNBLOCKED);
				db.replace(TABLE_MAILBOX, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userKey
	 * @return list of outboxes of userKey. <br>
	 *         avatarURL will be relative local file path after data/data
	 */
	// public ArrayList<OutboxesData> getOutboxes(String userKey) {
	// ArrayList<OutboxesData> outboxes = new ArrayList<OutboxesData>();
	// if (userKey != null && !userKey.isEmpty()) {
	// Cursor c;
	// c = db.query(TABLE_MAILBOX, new String[] { KEY_BOX_ID, KEY_FRIEND_KEY,
	// KEY_FRIEND_USERNAME,
	// KEY_LAST_RECEIVED_TIMESTAMP, KEY_UNREAD_COUNT, KEY_AVATAR_CACHE,
	// KEY_BLOCKED }, KEY_USER_KEY
	// + " = ? AND " + KEY_TYPE + " = ? AND (" + KEY_BLOCKED + " IS NULL OR " +
	// KEY_BLOCKED + " = ?)",
	// new String[] { userKey, OUTBOX, UNBLOCKED }, null, null, KEY_BOX_ID +
	// " ASC");
	// if (c != null && c.moveToFirst()) {
	// OutboxesData od;
	// while (!c.isAfterLast()) {
	// od = new OutboxesData();
	// od.outboxID = c.getString(c.getColumnIndex(KEY_BOX_ID));
	// od.userId = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
	// od.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
	// try {
	// od.lastTimeOfNewReceive =
	// c.getLong(c.getColumnIndex(KEY_LAST_RECEIVED_TIMESTAMP));
	// } catch (NumberFormatException e) {
	// od.lastTimeOfNewReceive = 0;
	// }
	// od.avatarURL = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
	// outboxes.add(od);
	// c.moveToNext();
	// }
	// c.close();
	// }
	// }
	// return outboxes;
	// }

	public ArrayList<OutboxesData> getNSAOutboxes(String userKey) {
		ArrayList<OutboxesData> outboxes = new ArrayList<OutboxesData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_MAILBOX, new String[] { KEY_BOX_ID, KEY_FRIEND_KEY, KEY_FRIEND_USERNAME,
					KEY_LAST_RECEIVED_TIMESTAMP, KEY_UNREAD_COUNT, KEY_AVATAR_CACHE, KEY_BLOCKED }, KEY_USER_KEY
					+ " = ? AND " + KEY_TYPE + " = ?", new String[] { userKey, OUTBOX }, null, null,
					KEY_LAST_RECEIVED_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				OutboxesData od;
				while (!c.isAfterLast()) {
					od = new OutboxesData();
					od.outboxID = c.getString(c.getColumnIndex(KEY_BOX_ID));
					od.userId = c.getString(c.getColumnIndex(KEY_FRIEND_KEY));
					od.userName = c.getString(c.getColumnIndex(KEY_FRIEND_USERNAME));
					String blocked = c.getString(c.getColumnIndex(KEY_BLOCKED));
					od.blocked = blocked != null && blocked.equals(BLOCKED);
					try {
						od.lastTimeOfNewReceive = c.getLong(c.getColumnIndex(KEY_LAST_RECEIVED_TIMESTAMP));
					} catch (NumberFormatException e) {
						od.lastTimeOfNewReceive = 0;
					}
					od.avatarURL = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					outboxes.add(od);
					c.moveToNext();
				}
				c.close();
			}
		}
		return outboxes;
	}

	/**
	 * 
	 * @param data
	 *            must contain userkey
	 * @return true if update ok
	 */
	// public boolean updateOutboxes(getOutboxes_outObj data) {
	// if (data.mUserKey == null || data.mUserKey.isEmpty()) {
	// return false;
	// }
	//
	// boolean returnVal = false;
	// db.beginTransaction();
	// try {
	// ArrayList<OutboxesData> outboxes = data.getOutboxes();
	// ContentValues values = new ContentValues();
	// for (OutboxesData od : outboxes) {
	// values.put(KEY_FRIEND_KEY, od.userId);
	// values.put(KEY_FRIEND_USERNAME, od.userName);
	// values.put(KEY_UNREAD_COUNT, 0);
	// values.put(KEY_AVATAR_CACHE, od.avatarURL);
	// values.put(KEY_LAST_RECEIVED_TIMESTAMP, od.lastTimeOfNewReceive);
	// int i = db.update(TABLE_MAILBOX, values, KEY_USER_KEY + " = ? AND " +
	// KEY_TYPE + " = ? AND "
	// + KEY_BOX_ID + " = ?", new String[] { data.mUserKey, OUTBOX, od.outboxID
	// });
	// if (i == 0) {
	// // mailbox does not exist
	// values.put(KEY_USER_KEY, data.mUserKey);
	// values.put(KEY_TYPE, OUTBOX);
	// values.put(KEY_BOX_ID, od.outboxID);
	// db.insert(TABLE_MAILBOX, null, values);
	// }
	// values.clear();
	// }
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// return returnVal;
	// }

	/**
	 * This will clear local history, use this in NSA only
	 * 
	 * @param data
	 * @return
	 */
	public boolean updateNSAOutboxes(getOutboxes_outObj data) {
		if (data.mUserKey == null || data.mUserKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove local outbox data first
			clearOutboxes(data.mUserKey);
			// insert returned outbox data
			ArrayList<OutboxesData> outboxes = data.getOutboxes();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, data.mUserKey);
			values.put(KEY_TYPE, OUTBOX);
			for (OutboxesData od : outboxes) {
				values.put(KEY_BOX_ID, od.outboxID);
				values.put(KEY_FRIEND_KEY, od.userId);
				values.put(KEY_FRIEND_USERNAME, od.userName);
				values.put(KEY_UNREAD_COUNT, 0);
				values.put(KEY_AVATAR_CACHE, od.avatarURL);
				values.put(KEY_LAST_RECEIVED_TIMESTAMP, od.lastTimeOfNewReceive);
				values.put(KEY_BLOCKED, od.blocked ? BLOCKED : UNBLOCKED);
				db.replace(TABLE_MAILBOX, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userkey
	 * @param boxId
	 * @return list of received mail with latest one on top.
	 */
	public ArrayList<MailData> getReceivedMail(String userKey, String boxId) {
		ArrayList<MailData> receivedMail = new ArrayList<MailData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_MAIL_MESSAGE, new String[] { KEY_MAIL_ID, KEY_BOX_ID, KEY_FILE_URL, KEY_FILE_NAME,
					KEY_TIMESTAMP }, KEY_USER_KEY + " = ? AND " + KEY_TYPE + " = ? AND " + KEY_BOX_ID + " = ?",
					new String[] { userKey, RECEIVED, boxId }, null, null, KEY_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				MailData md;
				while (!c.isAfterLast()) {
					md = new MailData();
					md.fileUrl = c.getString(c.getColumnIndex(KEY_FILE_URL));
					md.userFileName = c.getString(c.getColumnIndex(KEY_FILE_NAME));
					try {
						md.mailId = c.getLong(c.getColumnIndex(KEY_MAIL_ID));
					} catch (NumberFormatException e) {
						md.mailId = 0;
					}
					try {
						md.timeRecieved = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						md.timeRecieved = 0;
					}
					receivedMail.add(md);
					c.moveToNext();
				}
				c.close();
			}
		}
		return receivedMail;
	}

	/**
	 * 
	 * @param userKey
	 * @param data
	 * @return true if update ok
	 */
	// public boolean updateReceivedMail(String userKey, getMail_outObj data) {
	// if (userKey == null || userKey.isEmpty()) {
	// return false;
	// }
	//
	// boolean returnVal = false;
	// db.beginTransaction();
	// try {
	// // DO NOT REMOVE LOCAL MAIL
	// clearReceivedMailFromBox(data.mBoxId);
	// // insert returned received mail data
	// ArrayList<MailData> mails = data.getMail();
	// ContentValues values = new ContentValues();
	// values.put(KEY_USER_KEY, userKey);
	// values.put(KEY_BOX_ID, data.mBoxId);
	// values.put(KEY_TYPE, RECEIVED);
	// for (MailData md : mails) {
	// values.put(KEY_MAIL_ID, md.mailId);
	// values.put(KEY_FILE_URL, md.fileUrl);
	// values.put(KEY_FILE_NAME, md.userFileName);
	// values.put(KEY_TIMESTAMP, md.timeRecieved);
	// db.replace(TABLE_MAIL_MESSAGE, null, values);
	// }
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// return returnVal;
	// }

	/**
	 * This will clear local history, use this in NSA only
	 * 
	 * @param userKey
	 * @param data
	 * @return
	 */
	public boolean updateNSAReceivedMail(String userKey, getMail_outObj data) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove local received mail
			clearReceivedMailFromBox(data.mBoxId);
			// insert returned received mail data
			ArrayList<MailData> mails = data.getMail();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, userKey);
			values.put(KEY_BOX_ID, data.mBoxId);
			values.put(KEY_TYPE, RECEIVED);
			for (MailData md : mails) {
				values.put(KEY_MAIL_ID, md.mailId);
				values.put(KEY_FILE_URL, md.fileUrl);
				values.put(KEY_FILE_NAME, md.userFileName);
				values.put(KEY_TIMESTAMP, md.timeRecieved);
				db.replace(TABLE_MAIL_MESSAGE, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userkey
	 * @param boxId
	 * @return list of received mail with latest one on top.
	 */
	public ArrayList<MailData> getSentMail(String userKey, String boxId) {
		ArrayList<MailData> receivedMail = new ArrayList<MailData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			c = db.query(TABLE_MAIL_MESSAGE, new String[] { KEY_MAIL_ID, KEY_BOX_ID, KEY_FILE_URL, KEY_FILE_NAME,
					KEY_TIMESTAMP }, KEY_USER_KEY + " = ? AND " + KEY_TYPE + " = ? AND " + KEY_BOX_ID + " = ?",
					new String[] { userKey, SENT, boxId }, null, null, KEY_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				MailData md;
				while (!c.isAfterLast()) {
					md = new MailData();
					md.fileUrl = c.getString(c.getColumnIndex(KEY_FILE_URL));
					md.userFileName = c.getString(c.getColumnIndex(KEY_FILE_NAME));
					try {
						md.mailId = c.getLong(c.getColumnIndex(KEY_MAIL_ID));
					} catch (NumberFormatException e) {
						md.mailId = 0;
					}
					try {
						md.timeRecieved = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						md.timeRecieved = 0;
					}
					receivedMail.add(md);
					c.moveToNext();
				}
				c.close();
			}
		}
		return receivedMail;
	}

	/**
	 * 
	 * @param userKey
	 * @param data
	 * @return true if update ok
	 */
	// public boolean updateSentMail(String userKey, getOutgoingMail_outObj
	// data) {
	// if (userKey == null || userKey.isEmpty()) {
	// return false;
	// }
	//
	// boolean returnVal = false;
	// db.beginTransaction();
	// try {
	// // DO NOT REMOVE LOCAL SENT MAIL
	// clearSentMailFromBox(data.mBoxId);
	// // insert returned sent mail data
	// ArrayList<MailData> mails = data.getMail();
	// ContentValues values = new ContentValues();
	// values.put(KEY_USER_KEY, userKey);
	// values.put(KEY_BOX_ID, data.mBoxId);
	// values.put(KEY_TYPE, SENT);
	// for (MailData md : mails) {
	// values.put(KEY_MAIL_ID, md.mailId);
	// values.put(KEY_FILE_URL, md.fileUrl);
	// values.put(KEY_FILE_NAME, md.userFileName);
	// values.put(KEY_TIMESTAMP, md.timeRecieved);
	// db.replace(TABLE_MAIL_MESSAGE, null, values);
	// }
	// db.setTransactionSuccessful();
	// returnVal = true;
	// } finally {
	// db.endTransaction();
	// }
	// return returnVal;
	// }

	/**
	 * This will clear local history, use this in NSA only
	 * 
	 * @param userKey
	 * @param data
	 * @return
	 */
	public boolean updateNSASentMail(String userKey, getOutgoingMail_outObj data) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			// remove local sent mail
			clearSentMailFromBox(data.mBoxId);
			// insert returned sent mail data
			ArrayList<MailData> mails = data.getMail();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, userKey);
			values.put(KEY_BOX_ID, data.mBoxId);
			values.put(KEY_TYPE, SENT);
			for (MailData md : mails) {
				values.put(KEY_MAIL_ID, md.mailId);
				values.put(KEY_FILE_URL, md.fileUrl);
				values.put(KEY_FILE_NAME, md.userFileName);
				values.put(KEY_TIMESTAMP, md.timeRecieved);
				db.replace(TABLE_MAIL_MESSAGE, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userKey
	 *            current user's key
	 * @param mailId
	 * @return
	 */
	public boolean deleteMail(String userKey, String mailId) {
		File file = new File(getMailThumbnailUrl(userKey, mailId));
		boolean deleteOK = true;
		if (file.canRead() && file.length() > 0) {
			deleteOK = file.delete();
		}
		if (deleteOK) {
			return db.delete(TABLE_MAIL_MESSAGE, KEY_USER_KEY + " = ? AND " + KEY_MAIL_ID + " = ?", new String[] {
					userKey, mailId }) == 1;
		} else {
			return false;
		}
	}

	/*********
	 * PHOTO *
	 *********/

	/**
	 * @param userKey
	 * @return list of photo sent out by current user with latest one on top
	 */
	public ArrayList<SharedPhotoData> getSharedPhoto(String userKey) {
		ArrayList<SharedPhotoData> sharedPhoto = new ArrayList<SharedPhotoData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			// NOTE: currently retrieving columns not returned to caller
			c = db.query(TABLE_PHOTO, new String[] { KEY_PHOTO_ID, KEY_PHOTO_TITLE, KEY_PHOTO_URL, KEY_SENDER_NAME,
					KEY_SENDER_KEY, KEY_RECEIVER, KEY_TIMESTAMP, KEY_AVATAR_CACHE, KEY_THUMBNAIL_CACHE }, KEY_USER_KEY
					+ " = ? AND " + KEY_SENDER_KEY + " = ?", new String[] { userKey, userKey }, KEY_PHOTO_ID, null,
					KEY_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				SharedPhotoData spd;
				while (!c.isAfterLast()) {
					spd = new SharedPhotoData();
					spd.id = c.getString(c.getColumnIndex(KEY_PHOTO_ID));
					spd.title = c.getString(c.getColumnIndex(KEY_PHOTO_TITLE));
					spd.url = c.getString(c.getColumnIndex(KEY_PHOTO_URL));
					spd.m_ToList = asPhotoReceiverArrayList(c.getString(c.getColumnIndex(KEY_RECEIVER)));
					try {
						spd.createdTime = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						spd.createdTime = 0;
					}
					sharedPhoto.add(spd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return sharedPhoto;
	}

	/**
	 * 
	 * @param userKey
	 * @param data
	 * @param clearCache
	 *            whether to delete all shared photos of this user before
	 *            updating
	 * @return
	 */
	public boolean updateSharedPhoto(String userKey, String userName, String avatarUrl, getSharedPhotos_outObj data,
			boolean clearCache) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			if (clearCache) {
				new SharedPhotoCleanTask(userKey, data).execute();
			}
			// insert returned photo data
			ArrayList<SharedPhotoData> sharedPhoto = data.getPhotos();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, userKey);
			values.put(KEY_SENDER_KEY, userKey);
			values.put(KEY_SENDER_NAME, userName);
			values.put(KEY_AVATAR_CACHE, avatarUrl);
			for (SharedPhotoData spd : sharedPhoto) {
				values.put(KEY_PHOTO_ID, spd.id);
				values.put(KEY_PHOTO_TITLE, spd.title);
				values.put(KEY_PHOTO_URL, spd.url);
				values.put(KEY_TIMESTAMP, spd.createdTime);
				values.put(KEY_RECEIVER, asSharedPhotoToUserDataJSONArrayString(spd.m_ToList));
				db.replace(TABLE_PHOTO, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userKey
	 * @return list of photo current user received with latest on top
	 */
	public ArrayList<ReceivedPhotoData> getReceivedPhoto(String userKey) {
		ArrayList<ReceivedPhotoData> receivedPhoto = new ArrayList<ReceivedPhotoData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			// NOTE: currently retrieving columns not returned to caller
			c = db.query(TABLE_PHOTO, new String[] { KEY_PHOTO_ID, KEY_PHOTO_TITLE, KEY_PHOTO_URL, KEY_SENDER_NAME,
					KEY_SENDER_KEY, KEY_RECEIVER, KEY_TIMESTAMP, KEY_AVATAR_CACHE, KEY_THUMBNAIL_CACHE }, KEY_USER_KEY
					+ " = ? AND " + KEY_SENDER_KEY + " <> ? AND (" + KEY_BLOCKED + " IS NULL OR " + KEY_BLOCKED
					+ " = ?)", new String[] { userKey, userKey, UNBLOCKED }, KEY_PHOTO_ID, null, KEY_TIMESTAMP
					+ " DESC");
			if (c != null && c.moveToFirst()) {
				ReceivedPhotoData rpd;
				while (!c.isAfterLast()) {
					rpd = new ReceivedPhotoData();
					rpd.id = c.getString(c.getColumnIndex(KEY_PHOTO_ID));
					rpd.title = c.getString(c.getColumnIndex(KEY_PHOTO_TITLE));
					rpd.url = c.getString(c.getColumnIndex(KEY_PHOTO_URL));
					rpd.fromName = c.getString(c.getColumnIndex(KEY_SENDER_NAME));
					rpd.fromAvatarUrl = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					try {
						rpd.fromId = c.getLong(c.getColumnIndex(KEY_SENDER_KEY));
					} catch (NumberFormatException e) {
						rpd.fromId = 0;
					}
					try {
						rpd.createdTime = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						rpd.createdTime = 0;
					}
					receivedPhoto.add(rpd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return receivedPhoto;
	}

	/**
	 * 
	 * @param userKey
	 * @param data
	 * @param clearCache
	 *            whether to delete all shared photos of this user before
	 *            updating
	 * @return
	 */
	public boolean updateReceivedPhoto(String userKey, getReceivedPhotos_outObj data, boolean clearCache) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}

		boolean returnVal = false;
		db.beginTransaction();
		try {
			if (clearCache) {
				new ReceivedPhotoCleanTask(userKey, data).execute();
			}
			// insert returned photo data
			ArrayList<ReceivedPhotoData> receivedPhoto = data.getPhotos();
			ContentValues values = new ContentValues();
			for (ReceivedPhotoData rpd : receivedPhoto) {
				values.put(KEY_USER_KEY, userKey);
				values.put(KEY_PHOTO_ID, rpd.id);
				values.put(KEY_PHOTO_TITLE, rpd.title);
				values.put(KEY_PHOTO_URL, rpd.url);
				values.put(KEY_SENDER_NAME, rpd.fromName);
				values.put(KEY_SENDER_KEY, rpd.fromId);
				values.put(KEY_AVATAR_CACHE, rpd.fromAvatarUrl);
				values.put(KEY_TIMESTAMP, rpd.createdTime);

				int i = db.update(TABLE_PHOTO, values, KEY_USER_KEY + " = ? AND " + KEY_PHOTO_ID + " = ?",
						new String[] { userKey, rpd.id });
				if (i == 0) {
					// nothing updated because this is a new photo
					values.put(KEY_BLOCKED, UNBLOCKED);
					db.insert(TABLE_PHOTO, null, values);
				}
				values.clear();
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * only used in NSA
	 * 
	 * @param userKey
	 * @return list of all photos (shared and received) of current user
	 */
	public ArrayList<ReceivedPhotoData> getAllPhoto(String userKey) {
		ArrayList<ReceivedPhotoData> receivedPhoto = new ArrayList<ReceivedPhotoData>();
		if (userKey != null && !userKey.isEmpty()) {
			Cursor c;
			// NOTE: currently retrieving columns not returned to caller
			c = db.query(TABLE_PHOTO, new String[] { KEY_PHOTO_ID, KEY_PHOTO_TITLE, KEY_PHOTO_URL, KEY_SENDER_NAME,
					KEY_SENDER_KEY, KEY_RECEIVER, KEY_TIMESTAMP, KEY_AVATAR_CACHE, KEY_THUMBNAIL_CACHE, KEY_BLOCKED },
					KEY_USER_KEY + " = ?", new String[] { userKey }, KEY_PHOTO_ID, null, KEY_TIMESTAMP + " DESC");
			if (c != null && c.moveToFirst()) {
				ReceivedPhotoData rpd;
				while (!c.isAfterLast()) {
					rpd = new ReceivedPhotoData();
					rpd.id = c.getString(c.getColumnIndex(KEY_PHOTO_ID));
					rpd.title = c.getString(c.getColumnIndex(KEY_PHOTO_TITLE));
					rpd.url = c.getString(c.getColumnIndex(KEY_PHOTO_URL));
					rpd.fromName = c.getString(c.getColumnIndex(KEY_SENDER_NAME));
					rpd.fromAvatarUrl = c.getString(c.getColumnIndex(KEY_AVATAR_CACHE));
					try {
						rpd.fromId = c.getLong(c.getColumnIndex(KEY_SENDER_KEY));
					} catch (NumberFormatException e) {
						rpd.fromId = 0;
					}
					try {
						rpd.createdTime = c.getLong(c.getColumnIndex(KEY_TIMESTAMP));
					} catch (NumberFormatException e) {
						rpd.createdTime = 0;
					}
					String blocked = c.getString(c.getColumnIndex(KEY_BLOCKED));
					rpd.blocked = blocked != null && blocked.equals(BLOCKED);

					receivedPhoto.add(rpd);
					c.moveToNext();
				}
				c.close();
			}
		}
		return receivedPhoto;
	}

	/**
	 * only used in NSA
	 * 
	 * @param userKey
	 * @param data
	 * @param clearCache
	 *            whether to delete all shared photos of this user before
	 *            updating
	 * @return
	 */
	public boolean updateAllPhoto(final String userKey, final getSharedAndReceivedPhotos_outObj data, boolean clearCache) {
		if (userKey == null || userKey.isEmpty()) {
			return false;
		}
		boolean returnVal = false;

		if (clearCache) {
			// clearAllPhotos(userKey);
			new AllPhotoCleanTask(userKey, data).execute();
		}

		db.beginTransaction();
		try {
			// insert returned sent mail data
			ArrayList<ReceivedPhotoData> receivedPhoto = data.getPhotos();
			ContentValues values = new ContentValues();
			values.put(KEY_USER_KEY, userKey);
			for (ReceivedPhotoData rpd : receivedPhoto) {
				values.put(KEY_PHOTO_ID, rpd.id);
				values.put(KEY_PHOTO_TITLE, rpd.title);
				values.put(KEY_PHOTO_URL, rpd.url);
				values.put(KEY_SENDER_NAME, rpd.fromName);
				values.put(KEY_SENDER_KEY, rpd.fromId);
				values.put(KEY_AVATAR_CACHE, rpd.fromAvatarUrl);
				values.put(KEY_TIMESTAMP, rpd.createdTime);
				values.put(KEY_BLOCKED, rpd.blocked ? BLOCKED : UNBLOCKED);
				db.replace(TABLE_PHOTO, null, values);
			}
			db.setTransactionSuccessful();
			returnVal = true;
		} finally {
			db.endTransaction();
		}
		return returnVal;
	}

	/**
	 * 
	 * @param userKey
	 *            current user's key
	 * @param photoId
	 * @return
	 */
	public boolean deletePhoto(String userKey, String photoId) {
		File file = new File(getPhotoThumbnailUrl(userKey, photoId));
		boolean deleteOK = true;
		if (file.canRead() && file.length() > 0) {
			deleteOK = file.delete();
		}
		if (deleteOK) {
			return db.delete(TABLE_PHOTO, KEY_USER_KEY + " = ? AND " + KEY_PHOTO_ID + " = ?", new String[] { userKey,
					photoId }) == 1;
		} else {
			return false;
		}
	}

	/**
	 * internal delete functions
	 */
	private boolean clearFriendList(String userKey) {
		return db.delete(TABLE_FRIEND, KEY_USER_KEY + " = ?", new String[] { userKey }) > 0;
	}

	private boolean clearConversationList(String userKey) {
		return db.delete(TABLE_CONVERSATION, KEY_USER_KEY + " = ?", new String[] { userKey }) > 0;
	}

	private boolean clearInboxes(String userKey) {
		return db
				.delete(TABLE_MAILBOX, KEY_USER_KEY + " = ? AND " + KEY_TYPE + " = ?", new String[] { userKey, INBOX }) > 0;
	}

	private boolean clearOutboxes(String userKey) {
		return db.delete(TABLE_MAILBOX, KEY_USER_KEY + " = ? AND " + KEY_TYPE + " = ?",
				new String[] { userKey, OUTBOX }) > 0;
	}

	private boolean clearReceivedMailFromBox(String boxId) {
		return db.delete(TABLE_MAIL_MESSAGE, KEY_BOX_ID + " = ? AND " + KEY_TYPE + " = ?", new String[] { boxId,
				RECEIVED }) > 0;
	}

	private boolean clearSentMailFromBox(String boxId) {
		return db
				.delete(TABLE_MAIL_MESSAGE, KEY_BOX_ID + " = ? AND " + KEY_TYPE + " = ?", new String[] { boxId, SENT }) > 0;
	}

	private boolean clearSharedPhotos(String userKey) {
		return db.delete(TABLE_PHOTO, KEY_USER_KEY + " = ? AND " + KEY_SENDER_KEY + " = ?", new String[] { userKey,
				userKey }) > 0;
	}

	private boolean clearReceivedPhotos(String userKey) {
		return db.delete(TABLE_PHOTO, KEY_USER_KEY + " = ? AND " + KEY_SENDER_KEY + " <> ?", new String[] { userKey,
				userKey }) > 0;
	}

	private boolean clearAllPhotos(String userKey) {
		return db.delete(TABLE_PHOTO, KEY_USER_KEY + " = ?", new String[] { userKey }) > 0;
	}

	/**
	 * internal helper functions
	 */

	private String asJSONArrayString(ArrayList<String> input) {
		if (input == null || input.size() == 0) {
			return "";
		}
		JSONArray jArray = new JSONArray();
		for (String s : input) {
			jArray.put(s);
		}
		return jArray.toString();
	}

	private ArrayList<String> asStringArrayList(String JSONArrayString) {
		ArrayList<String> array = new ArrayList<String>();
		if (JSONArrayString == null || JSONArrayString.isEmpty()) {
			return array;
		}
		try {
			JSONArray jArray = new JSONArray(JSONArrayString);
			int limit = jArray.length();
			for (int i = 0; i < limit; i++) {
				array.add(jArray.getString(i));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	final private static String ID = "ID";
	final private static String NAME = "NAME";

	private ArrayList<SharedPhotoToUserData> asPhotoReceiverArrayList(String JSONArrayString) {
		ArrayList<SharedPhotoToUserData> array = new ArrayList<SharedPhotoToUserData>();
		if (JSONArrayString == null || JSONArrayString.isEmpty()) {
			return array;
		}
		try {
			JSONArray jArray = new JSONArray(JSONArrayString);
			int limit = jArray.length();
			SharedPhotoToUserData userdata;
			JSONObject user;
			for (int i = 0; i < limit; i++) {
				userdata = new SharedPhotoToUserData();
				user = jArray.getJSONObject(i);
				userdata.id = user.getString(ID);
				userdata.name = user.getString(NAME);
				array.add(userdata);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	private String asSharedPhotoToUserDataJSONArrayString(ArrayList<SharedPhotoToUserData> input) {
		if (input == null || input.size() == 0) {
			return "";
		}
		JSONArray jArray = new JSONArray();
		JSONObject jobj;
		for (SharedPhotoToUserData data : input) {
			jobj = new JSONObject();
			try {
				jobj.put(ID, data.id);
				jobj.put(NAME, data.name);
				jArray.put(jobj);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return jArray.toString();
	}

	/************************
	 * Bitmap Helper Method *
	 ************************/

	/**
	 * 
	 * @param userKey
	 * @return if avatar of user exist in local storage
	 */
	public boolean myAvatarExist(String userKey) {
		// String directory = mContext.getFilesDir().getAbsolutePath();
		// File targetDir = new File(directory + File.separator + userKey +
		// File.separator + FOLDER_FRIEND);
		// File targetFile = new File(targetDir, userKey + PNG);
		// return targetFile.exists();
		return friendAvatarExist(userKey, userKey);
	}

	/**
	 * 
	 * @param userKey
	 * @param targetWidth
	 * @return bitmap scaled down close to targetWidth
	 */
	public Bitmap getMyAvatar(String userKey, int targetWidth) {
		return getFriendAvatar(userKey, userKey, targetWidth);
	}

	public boolean saveMyAvatar(String userKey, Bitmap avatar) {
		return saveFriendAvatar(userKey, userKey, avatar);
	}

	/**
	 * 
	 * @param userKey
	 * @param friendKey
	 * @return if friend avatar exists in local storage
	 */
	public boolean friendAvatarExist(String userKey, String friendKey) {
		String directory = mContext.getFilesDir().getAbsolutePath();
		File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_FRIEND);
		File targetFile = new File(targetDir, friendKey + PNG);
		return targetFile.exists();
	}

	public String getFriendAvatarUrl(String userKey, String friendId) {
		return new StringBuilder().append(mContext.getFilesDir().getAbsolutePath()).append(File.separator)
				.append(userKey).append(File.separator).append(FOLDER_FRIEND).append(File.separator).append(friendId)
				.append(PNG).toString();
	}

	/**
	 * 
	 * @param userKey
	 * @param friendKey
	 * @param targetWidth
	 * @return bitmap scaled down close to targetWidth
	 */
	public Bitmap getFriendAvatar(String userKey, String friendKey, int targetWidth) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			// LOG.W(TAG, "avoid using getFriendAvatar from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		File temp = new File(directory + File.separator + userKey + File.separator + FOLDER_FRIEND + File.separator
				+ friendKey + PNG);
		if (temp.exists() && temp.canRead()) {
			String filePath = temp.getAbsolutePath();
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, options);
			double ratio = (double) options.outWidth / (double) targetWidth;
			int sampleSize = (int) Math.round(ratio);
			if (sampleSize % 2 == 1) {
				sampleSize--;
			}
			if (sampleSize <= 0) {
				sampleSize = 1;
			}
			options.inSampleSize = sampleSize;
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFile(filePath, options);
		}
		return null;
	}

	public boolean saveFriendAvatar(String userKey, String friendKey, Bitmap avatar) {
		if (avatar == null) {
			return false;
		}

		if (Looper.myLooper() == Looper.getMainLooper()) {
			// LOG.W(TAG, "avoid using saveFriendAvatar from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		try {
			File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_FRIEND);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			File targetFile = new File(targetDir, friendKey + PNG);
			FileOutputStream fos = new FileOutputStream(targetFile);
			avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 
	 * @param userKey
	 *            current user key
	 * @param contactKey
	 *            avatar owner key
	 * @param avatar
	 *            bitmap to be saved
	 */
	public void saveAvatarAsync(final String userKey, final String contactKey, final Bitmap avatar) {
		if (avatar == null) {
			return;
		}

		if (mWorkerThread == null) {
			mWorkerThread = new HandlerThread("worker", HandlerThread.NORM_PRIORITY);
			mWorkerThread.start();
		}
		if (mWorkerHandler == null) {
			mWorkerHandler = new Handler(mWorkerThread.getLooper());
		}

		mWorkerHandler.post(new Runnable() {
			@Override
			public void run() {
				String directory = mContext.getFilesDir().getAbsolutePath();
				try {
					File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_FRIEND);
					if (!targetDir.exists()) {
						targetDir.mkdirs();
					}
					File targetFile = new File(targetDir, contactKey + PNG);
					// if (targetFile.canRead() && targetFile.length() > 0) {
					// do not overwrite
					// return;
					// }
					FileOutputStream fos = new FileOutputStream(targetFile);
					avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @param userKey
	 * @param mailId
	 * @return if mail of mailId exist in local storage
	 */
	public boolean mailThumbnailExist(String userKey, String mailId) {
		String directory = mContext.getFilesDir().getAbsolutePath();
		File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_MAIL);
		File targetFile = new File(targetDir, mailId + PNG);
		return targetFile.exists();
	}

	public String getMailThumbnailUrl(String userKey, String mailId) {
		return new StringBuilder().append(mContext.getFilesDir().getAbsolutePath()).append(File.separator)
				.append(userKey).append(File.separator).append(FOLDER_MAIL).append(File.separator).append(mailId)
				.append(PNG).toString();
	}

	/**
	 * @param userKey
	 * @param mailId
	 * @return full thumbnail
	 */
	public Bitmap getMailThumbnail(String userKey, String mailId) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			LOG.W(TAG, "avoid using this from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		File temp = new File(directory + File.separator + userKey + File.separator + FOLDER_MAIL + File.separator
				+ mailId + PNG);
		if (temp.exists() && temp.canRead()) {
			String filePath = temp.getAbsolutePath();
			return BitmapFactory.decodeFile(filePath);
		} else {
			return null;
		}
	}

	public boolean saveMailThumbnail(String userKey, String mailId, Bitmap thumbnail) {
		if (thumbnail == null) {
			return false;
		}

		if (Looper.myLooper() == Looper.getMainLooper()) {
			// LOG.W(TAG, "avoid using this from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		try {
			File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_MAIL);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			File targetFile = new File(targetDir, mailId + PNG);
			FileOutputStream fos = new FileOutputStream(targetFile);
			thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public void saveMailThumbnailAsync(final String userKey, final String mailId, final Bitmap avatar) {
		if (avatar == null) {
			return;
		}

		if (mWorkerThread == null) {
			mWorkerThread = new HandlerThread("worker", HandlerThread.NORM_PRIORITY);
			mWorkerThread.start();
		}
		if (mWorkerHandler == null) {
			mWorkerHandler = new Handler(mWorkerThread.getLooper());
		}

		mWorkerHandler.post(new Runnable() {
			@Override
			public void run() {
				String directory = mContext.getFilesDir().getAbsolutePath();
				try {
					File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_MAIL);
					if (!targetDir.exists()) {
						targetDir.mkdirs();
					}
					File targetFile = new File(targetDir, mailId + PNG);
					if (targetFile.canRead() && targetFile.length() > 0) {
						// do not overwrite
						return;
					}
					FileOutputStream fos = new FileOutputStream(targetFile);
					avatar.compress(Bitmap.CompressFormat.PNG, 100, fos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * 
	 * @param userKey
	 * @param photoId
	 * @return if photo of photoId exist in local storage
	 */
	public boolean photoThumbnailExist(String userKey, String photoId) {
		String directory = mContext.getFilesDir().getAbsolutePath();
		File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_PHOTO);
		File targetFile = new File(targetDir, photoId + PNG);
		return targetFile.exists();
	}

	public String getPhotoThumbnailUrl(String userKey, String photoId) {
		return new StringBuilder().append(mContext.getFilesDir().getAbsolutePath()).append(File.separator)
				.append(userKey).append(File.separator).append(FOLDER_PHOTO).append(File.separator).append(photoId)
				.append(PNG).toString();
	}

	/**
	 * @param userKey
	 * @param photoId
	 * @return full thumbnail
	 */
	public Bitmap getPhotoThumbnail(String userKey, String photoId) {
		if (Looper.myLooper() == Looper.getMainLooper()) {
			LOG.W(TAG, "avoid using this from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		File temp = new File(directory + File.separator + userKey + File.separator + FOLDER_PHOTO + File.separator
				+ photoId + PNG);
		if (temp.exists() && temp.canRead()) {
			String filePath = temp.getAbsolutePath();
			return BitmapFactory.decodeFile(filePath);
		} else {
			return null;
		}
	}

	public boolean savePhotoThumbnail(String userKey, String photoId, Bitmap thumbnail) {
		if (thumbnail == null) {
			return false;
		}

		if (Looper.myLooper() == Looper.getMainLooper()) {
			LOG.W(TAG, "avoid using this from main thread");
		}
		String directory = mContext.getFilesDir().getAbsolutePath();
		try {
			File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_PHOTO);
			if (!targetDir.exists()) {
				targetDir.mkdirs();
			}
			File targetFile = new File(targetDir, photoId + PNG);
			FileOutputStream fos = new FileOutputStream(targetFile);
			thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * DEBUG functions
	 */
	public boolean dumpDatabase() {
		File internalDir = new File(mContext.getFilesDir().getAbsolutePath());
		File[] files = internalDir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				LOG.D(TAG, "file " + i + ": " + files[i].getAbsolutePath());
				if (files[i].isDirectory()) {
					File[] temp = files[i].listFiles();
					for (int j = 0; j < temp.length; j++) {
						LOG.D(TAG, "--> file " + j + ": " + temp[j].getAbsolutePath());
						if (temp[j].isDirectory()) {
							File[] tempj = temp[j].listFiles();
							for (int k = 0; k < tempj.length; k++) {
								LOG.D(TAG, "--> --> file " + k + ": " + tempj[k].getAbsolutePath());
							}
						}
					}
				}
			}
		} else {
			LOG.D(TAG, "no file found");
		}
		LOG.D(TAG, "dump database");
		File database = new File(mContext.getDatabasePath(DATABASE_NAME).getPath());
		File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		File dump = new File(downloadDir, "connect_dump");
		byte[] buffer = new byte[1024];
		try {
			FileInputStream fis = new FileInputStream(database);
			FileOutputStream fos = new FileOutputStream(dump);
			while (fis.read(buffer) > 0) {
				fos.write(buffer);
			}
			fis.close();
			fos.close();
			LOG.D(TAG, "dump ok");
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		LOG.D(TAG, "dump failed");
		return false;
	}

	private HandlerThread mWorkerThread;
	private Handler mWorkerHandler;

	public void savePhotoThumbnailAsync(final String userKey, final String photoId, final Bitmap thumbnail) {
		if (thumbnail == null) {
			return;
		}

		if (mWorkerThread == null) {
			mWorkerThread = new HandlerThread("worker", HandlerThread.NORM_PRIORITY);
			mWorkerThread.start();
		}
		if (mWorkerHandler == null) {
			mWorkerHandler = new Handler(mWorkerThread.getLooper());
		}

		mWorkerHandler.post(new Runnable() {
			@Override
			public void run() {
				String directory = mContext.getFilesDir().getAbsolutePath();
				try {
					File targetDir = new File(directory + File.separator + userKey + File.separator + FOLDER_PHOTO);
					if (!targetDir.exists()) {
						targetDir.mkdirs();
					}
					File targetFile = new File(targetDir, photoId + PNG);
					if (targetFile.canRead() && targetFile.length() > 0) {
						// do not overwrite
						return;
					}
					FileOutputStream fos = new FileOutputStream(targetFile);
					thumbnail.compress(Bitmap.CompressFormat.PNG, 100, fos);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private class ReceivedPhotoCleanTask extends AsyncTask<Void, Void, Boolean> {

		private ArrayList<ReceivedPhotoData> remoteData;
		private ArrayList<String> deleteList;
		private String userKey;

		public ReceivedPhotoCleanTask(String userKey, getReceivedPhotos_outObj receivedData) {
			LOG.D(TAG, "all photo clean init");
			this.remoteData = receivedData.getPhotos();
			this.userKey = userKey;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<String> keepList = new ArrayList<String>();
			// NOTE: string argument for query below
			keepList.add(0, userKey);
			keepList.add(0, userKey);
			keepList.add(UNBLOCKED);
			for (ReceivedPhotoData rpd : remoteData) {
				keepList.add(rpd.id);
			}

			if (remoteData.size() == 0 && keepList.size() == 3) { // keepList.size()
																	// == SIZE
																	// OF STRING
																	// ARGUMENT
																	// DEFINED
																	// ABOVE
				// we added nothing
				keepList.add(FAKE_PHOTO_ID);
			}

			Cursor c;
			c = db.rawQuery("SELECT " + KEY_PHOTO_ID + " FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND "
					+ KEY_SENDER_KEY + " <> ? AND (" + KEY_BLOCKED + " IS NULL OR " + KEY_BLOCKED + " = ?) AND "
					+ KEY_PHOTO_ID + " NOT IN ( " + getPlaceHolder(keepList.size() - 3) + " )", asStringArray(keepList));
			if (c != null && c.moveToFirst()) {
				deleteList = new ArrayList<String>();
				while (!c.isAfterLast()) {
					boolean deleteOK = deletePhoto(userKey, c.getString(0));
					if (deleteOK) {
						deleteList.add(c.getString(0));
					}
					c.moveToNext();
				}
				c.close();
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				LOG.D(TAG, "all photo clean db");
				if (deleteList.size() > 0) {
					deleteList.add(0, userKey);
					deleteList.add(0, userKey);
				}
				db.rawQuery("DELETE FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND " + KEY_SENDER_KEY
						+ " <> ? AND " + KEY_PHOTO_ID + " IN ( " + getPlaceHolder(deleteList.size() - 1) + " )",
						asStringArray(deleteList));
			}
		}
	};

	private class SharedPhotoCleanTask extends AsyncTask<Void, Void, Boolean> {

		private ArrayList<SharedPhotoData> remoteData;
		private ArrayList<String> deleteList;
		private String userKey;

		public SharedPhotoCleanTask(String userKey, getSharedPhotos_outObj receivedData) {
			LOG.D(TAG, "all photo clean init");
			this.remoteData = receivedData.getPhotos();
			this.userKey = userKey;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<String> keepList = new ArrayList<String>();
			keepList.add(0, userKey);
			keepList.add(0, userKey);
			for (SharedPhotoData spd : remoteData) {
				keepList.add(spd.id);
			}

			if (remoteData.size() == 0 && keepList.size() == 2) {
				// we added nothing
				keepList.add(FAKE_PHOTO_ID);
			}

			Cursor c;
			c = db.rawQuery("SELECT " + KEY_PHOTO_ID + " FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND "
					+ KEY_SENDER_KEY + " = ? AND " + KEY_PHOTO_ID + " NOT IN ( " + getPlaceHolder(keepList.size() - 2)
					+ " )", asStringArray(keepList));
			if (c != null && c.moveToFirst()) {
				deleteList = new ArrayList<String>();
				while (!c.isAfterLast()) {
					boolean deleteOK = deletePhoto(userKey, c.getString(0));
					if (deleteOK) {
						deleteList.add(c.getString(0));
					}
					c.moveToNext();
				}
				c.close();
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				LOG.D(TAG, "all photo clean db");
				if (deleteList.size() > 0) {
					deleteList.add(0, userKey);
					deleteList.add(0, userKey);
				}
				db.rawQuery("DELETE FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND " + KEY_SENDER_KEY
						+ " = ? AND " + KEY_PHOTO_ID + " IN ( " + getPlaceHolder(deleteList.size() - 1) + " )",
						asStringArray(deleteList));
			}
		}
	};

	private class AllPhotoCleanTask extends AsyncTask<Void, Void, Boolean> {
		private ArrayList<ReceivedPhotoData> remoteData;
		private ArrayList<String> deleteList;
		private String userKey;

		public AllPhotoCleanTask(String userKey, getSharedAndReceivedPhotos_outObj receivedData) {
			LOG.D(TAG, "all photo clean init");
			this.remoteData = receivedData.getPhotos();
			this.userKey = userKey;
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			ArrayList<String> keepList = new ArrayList<String>();
			keepList.add(0, userKey);
			for (ReceivedPhotoData rpd : remoteData) {
				keepList.add(rpd.id);
			}

			if (remoteData.size() == 0 && keepList.size() == 1) {
				// we added nothing
				keepList.add(FAKE_PHOTO_ID);
			}

			Cursor c;
			c = db.rawQuery("SELECT " + KEY_PHOTO_ID + " FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND "
					+ KEY_PHOTO_ID + " NOT IN ( " + getPlaceHolder(keepList.size() - 1) + " )", asStringArray(keepList));
			if (c != null && c.moveToFirst()) {
				deleteList = new ArrayList<String>();
				while (!c.isAfterLast()) {
					boolean deleteOK = deletePhoto(userKey, c.getString(0));
					if (deleteOK) {
						deleteList.add(c.getString(0));
					}
					c.moveToNext();
				}
				c.close();
				return true;
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				LOG.D(TAG, "all photo clean db");
				if (deleteList.size() > 0) {
					deleteList.add(0, userKey);
				}
				db.rawQuery("DELETE FROM " + TABLE_PHOTO + " WHERE " + KEY_USER_KEY + " = ? AND " + KEY_PHOTO_ID
						+ " IN ( " + getPlaceHolder(deleteList.size() - 1) + " )", asStringArray(deleteList));
			}
		}
	};

	private String getPlaceHolder(int length) {
		StringBuilder sb = new StringBuilder();
		sb.append("? ");
		for (int i = 1; i < length; i++) {
			sb.append(", ? ");
		}
		return sb.toString();
	}

	private String[] asStringArray(ArrayList<String> input) {
		String[] temp = new String[input.size()];
		for (int i = 0; i < temp.length; i++) {
			temp[i] = input.get(i);
		}
		input.clear();
		return temp;
	}
}