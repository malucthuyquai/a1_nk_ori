package com.fuhu.nabiconnect.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.fuhu.nabiconnect.log.LOG;

public class DatabaseHelper extends SQLiteOpenHelper {

    final private String TAG = DatabaseHelper.class.getSimpleName();

    DatabaseHelper(Context context, String databaseName, CursorFactory factory, int databaseVersion) {
        super(context, databaseName, null, databaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // USER_INFO
        db.execSQL("CREATE TABLE USER_INFO ( USER_KEY TEXT NOT NULL, FRIEND_CODE TEXT, SESSION_KEY TEXT, FIRST_NAME TEXT, LAST_NAME TEXT, USER_NAME TEXT, OSG_KEY TEXT, KID_ID TEXT, AVATAR_CACHE TEXT, CHARACTER TEXT, CHARACTER_COLOR TEXT, CLOTHING TEXT, ACCESSORY TEXT, BACKGROUND TEXT, PRIMARY KEY (USER_KEY ASC))");
        // FRIEND
        db.execSQL("CREATE TABLE FRIEND ( USER_KEY TEXT NOT NULL, FRIEND_KEY TEXT NOT NULL, FRIEND_USERNAME TEXT, AVATAR_CACHE TEXT, RELATIONSHIP TEXT (2) DEFAULT '-1', BLOCKED TEXT, OSG_USER_KEY TEXT, OSG_KID_ID TEXT, SOURCE TEXT NOT NULL, PRIMARY KEY (USER_KEY ASC, FRIEND_KEY, SOURCE))");
        // CONVERSATION
        db.execSQL("CREATE TABLE CONVERSATION ( USER_KEY TEXT NOT NULL, CONVERSATION_ID TEXT NOT NULL, ACTORS TEXT NOT NULL, UNREAD_COUNT TEXT, LAST_READ_MESSAGE TEXT, LAST_READ_TIMESTAMP TEXT, PRIMARY KEY (USER_KEY, CONVERSATION_ID))");
        // CHAT MESSAGE
        db.execSQL("CREATE TABLE CHAT_MESSAGE ( CONVERSATION_ID TEXT NOT NULL, MESSAGE_ID TEXT NOT NULL, USER_KEY TEXT NOT NULL, SENDER_KEY TEXT NOT NULL, TIMESTAMP TEXT NOT NULL, MESSAGE_BODY TEXT NOT NULL, PRIMARY KEY (MESSAGE_ID ASC))");
        // MAILBOX
        db.execSQL("CREATE TABLE MAILBOX ( USER_KEY TEXT NOT NULL, BOX_ID TEXT NOT NULL, FRIEND_KEY TEXT NOT NULL, TYPE TEXT NOT NULL, FRIEND_USERNAME TEXT NOT NULL, UNREAD_COUNT TEXT, AVATAR_CACHE TEXT, LAST_RECEIVED_TIMESTAMP TEXT, BLOCKED TEXT, PRIMARY KEY (USER_KEY ASC, BOX_ID ASC, TYPE ASC))");
        // MAIL_MESSAGE
        db.execSQL("CREATE TABLE MAIL_MESSAGE ( USER_KEY TEXT NOT NULL, MAIL_ID TEXT NOT NULL, BOX_ID TEXT NOT NULL, TYPE TEXT NOT NULL, FILE_URL TEXT, FILE_NAME TEXT, TIMESTAMP TEXT, PRIMARY KEY (MAIL_ID ASC, BOX_ID, TYPE))");
        // PHOTO
        db.execSQL("CREATE TABLE PHOTO ( USER_KEY TEXT NOT NULL, PHOTO_ID TEXT NOT NULL, PHOTO_TITLE TEXT, PHOTO_URL TEXT NOT NULL, SENDER_NAME TEXT, SENDER_KEY TEXT, RECEIVER TEXT, TIMESTAMP TEXT, AVATAR_CACHE TEXT, THUMBNAIL_CACHE TEXT, BLOCKED TEXT, PRIMARY KEY (USER_KEY, PHOTO_ID ASC))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        LOG.D(TAG, "upgrading from " + oldVersion + " to " + newVersion);
        switch (oldVersion) {
            case 1:
                // to disable deletion of parent / sibling
                db.execSQL("ALTER TABLE FRIEND ADD COLUMN RELATIONSHIP TEXT (2) DEFAULT '-1'");
            case 2:
                // to implement block friend function
                db.execSQL("ALTER TABLE FRIEND ADD COLUMN BLOCKED TEXT");
                db.execSQL("ALTER TABLE FRIEND ADD COLUMN OSG_USER_KEY TEXT");
                db.execSQL("ALTER TABLE FRIEND ADD COLUMN OSG_KID_ID TEXT");
            case 3:
                // to cache user data
                db.execSQL("ALTER TABLE USER_INFO ADD COLUMN CHARACTER TEXT");
                db.execSQL("ALTER TABLE USER_INFO ADD COLUMN CHARACTER_COLOR TEXT");
                db.execSQL("ALTER TABLE USER_INFO ADD COLUMN CLOTHING TEXT");
                db.execSQL("ALTER TABLE USER_INFO ADD COLUMN ACCESSORY TEXT");
                db.execSQL("ALTER TABLE USER_INFO ADD COLUMN BACKGROUND TEXT");
            case 4:
                // to implement mail data from blocked friend
                db.execSQL("ALTER TABLE MAILBOX ADD COLUMN BLOCKED TEXT");
            case 5:
                // to implement photo data from blocked friend
                db.execSQL("ALTER TABLE PHOTO ADD COLUMN BLOCKED TEXT");
            case 6:
                // to separate nabifriend list and nsa friend list, create new FRIEND table
                db.execSQL("DROP TABLE IF EXISTS FRIEND");
                // there is no need to copy existing data to new table because the new queries will return empty cursor anyway.
                db.execSQL("CREATE TABLE FRIEND ( USER_KEY TEXT NOT NULL, FRIEND_KEY TEXT NOT NULL, FRIEND_USERNAME TEXT, AVATAR_CACHE TEXT, RELATIONSHIP TEXT (2) DEFAULT '-1', BLOCKED TEXT, OSG_USER_KEY TEXT, OSG_KID_ID TEXT, SOURCE TEXT NOT NULL, PRIMARY KEY (USER_KEY ASC, FRIEND_KEY, SOURCE))");
            case 7:
            case 8:
        }
    }
}