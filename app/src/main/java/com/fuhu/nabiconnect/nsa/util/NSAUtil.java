package com.fuhu.nabiconnect.nsa.util;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import com.fuhu.account.data.Kid;
import com.fuhu.data.FriendData;
import com.fuhu.data.InboxesData;
import com.fuhu.data.MailData;
import com.fuhu.data.OutboxesData;
import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.data.UserData;
import com.fuhu.data.conversationData;
import com.fuhu.data.messageData;

import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class NSAUtil {

	final private static String TAG = NSAUtil.class.getSimpleName();

	private static final int ITEM_COUNT = 20;
	private static final String URL_TOMATO = "http://mmrcglobal.org/wp-content/uploads/2010/11/Tomato.jpg";

	public static UserData getFakeUserData() {
		UserData userData = new UserData();
		userData.userKey = "userKey";
		userData.friendCode = "friendCode";
		userData.sessionKey = "sessionKey";
		userData.firstName = "firstName";
		userData.lastName = "lastName";
		userData.userName = "userName";
		userData.Character = 0;
		userData.CharacterBackground = 0;
		userData.CharacterClothing = 0;
		userData.CharacterColor = 0;
		userData.osgKey = "osgKey";
		userData.kidID = "kidId";
		userData.avatarURL = "avatarUrl";
		userData.CharacterAccessories = new ArrayList<Long>();
		return userData;
	}

	public static ArrayList<Kid> getFakeKidList() {
		ArrayList<Kid> kids = new ArrayList<Kid>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			Kid k = new Kid();
			k.setAgeGroup(Kid.AGE_GROUP_1);
			k.setGender(Kid.BOY);
			k.setKidId(Long.valueOf(getRandomId()));
			k.setkidName(getRandomId());
			k.setKidPhotoPath(URL_TOMATO);
			k.setKidCoins(0);
			k.setKidSessionId(UUID.randomUUID().toString());
			k.setKidUserName(getRandomId());
			kids.add(k);
		}
		return kids;
	}

	public static ArrayList<FriendData> getFakeFriendList() {
		ArrayList<FriendData> data = new ArrayList<FriendData>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			FriendData fd = new FriendData();
			fd.AvatarUrl = URL_TOMATO;
			fd.userID = getRandomId();
			fd.userName = getRandomId();
			fd.relationship = FriendData.FRIEND;
			data.add(fd);
		}
		return data;
	}

	public static ArrayList<conversationData> getFakeConversationList(long myKidId, ArrayList<FriendData> friends) {
		ArrayList<conversationData> data = new ArrayList<conversationData>();
		ArrayList<String> actors = new ArrayList<String>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			conversationData cd = new conversationData();
			actors.clear();
			actors.add(Long.toString(myKidId));
			actors.add(friends.get(i).userID);
			cd.m_Actors = new ArrayList<String>();
			cd.m_Actors.addAll(actors);
			cd.m_ConversationId = getRandomId();
			cd.m_LastReadMessage = "last read message";
			cd.m_LastReadTimestamp = new Date().getTime();
			cd.m_Messages = new ArrayList<messageData>();
			cd.m_Messages.addAll(getFakeMessages(myKidId));
			cd.m_UnreadMessageCount = (int) (Math.random() * 99);
			data.add(cd);
		}
		return data;
	}

	public static ArrayList<messageData> getFakeMessages(long mKidId) {
		return getFakeMessages(mKidId, null);
	}

	public static ArrayList<messageData> getFakeMessages(long mKidId, String targetId) {
		ArrayList<messageData> messages = new ArrayList<messageData>();
		String fakeId;
		if (targetId == null) {
			fakeId = getRandomId();
		} else {
			fakeId = targetId;
		}
		for (int i = 0; i < ITEM_COUNT; i++) {
			messageData md = new messageData();
			md.m_MessageContent = Integer.toString((int) (Math.random() * 1000000));
			md.m_MessageId = UUID.randomUUID().toString();
			md.m_MessageTime = new Date().getTime();
			md.m_SenderId = i % 2 == 0 ? fakeId : Long.toString(mKidId);
			messages.add(md);
		}
		return messages;
	}

	public static ArrayList<InboxesData> getFakeInboxes() {
		ArrayList<InboxesData> inboxes = new ArrayList<InboxesData>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			InboxesData id = new InboxesData();
			id.avatarURL = URL_TOMATO;
			id.inboxID = getRandomId();
			id.lastTimeOfNewReceive = new Date().getTime();
			id.newReceiveCount = 10 + (int) (10 * Math.random());
			id.userId = getRandomId();
			id.userName = getRandomId();
			inboxes.add(id);
		}
		return inboxes;
	}

	public static ArrayList<OutboxesData> getFakeOutboxes() {
		ArrayList<OutboxesData> outboxes = new ArrayList<OutboxesData>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			OutboxesData od = new OutboxesData();
			od.avatarURL = URL_TOMATO;
			od.lastTimeOfNewReceive = new Date().getTime();
			od.outboxID = getRandomId();
			od.userId = getRandomId();
			od.userName = getRandomId();
			outboxes.add(od);
		}
		return outboxes;
	}

	public static ArrayList<MailData> getFakeMailMessage() {
		ArrayList<MailData> mail = new ArrayList<MailData>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			MailData md = new MailData();
			md.fileUrl = URL_TOMATO;
			md.mailId = Long.valueOf(getRandomId());
			md.timeRecieved = new Date().getTime();
			md.userFileName = UUID.randomUUID().toString();
			mail.add(md);
		}
		return mail;
	}

	public static ArrayList<ReceivedPhotoData> getFakePhoto() {
		ArrayList<ReceivedPhotoData> photos = new ArrayList<ReceivedPhotoData>();
		for (int i = 0; i < ITEM_COUNT; i++) {
			ReceivedPhotoData rpd = new ReceivedPhotoData();
			rpd.createdTime = new Date().getTime();
			rpd.fromAvatarUrl = URL_TOMATO;
			rpd.fromId = Long.valueOf(getRandomId());
			rpd.fromName = getRandomId();
			rpd.id = UUID.randomUUID().toString();
			rpd.title = getRandomId();
			rpd.url = URL_TOMATO;
			photos.add(rpd);
		}
		return photos;
	}

	private static String getRandomId() {
		return Integer.toString((int) (Math.random() * 10000000));
	}

	public static int getDeviceWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static void setTypeface(Context context, TextView tv, String typeface) {
		tv.setTypeface(Typeface.createFromAsset(context.getAssets(), typeface));
	}
}
