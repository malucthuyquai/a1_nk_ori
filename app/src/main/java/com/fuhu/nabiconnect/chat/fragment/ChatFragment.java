package com.fuhu.nabiconnect.chat.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.data.conversationData;
import com.fuhu.data.messageData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.chat.ChatActivity;
import com.fuhu.nabiconnect.chat.ChatActivity.ChatMessageData;
import com.fuhu.nabiconnect.chat.IOnChatMessageReceivedListener;
import com.fuhu.nabiconnect.chat.stickers.Sticker;
import com.fuhu.nabiconnect.chat.stickers.StickerCategory;
import com.fuhu.nabiconnect.chat.stickers.StickerManager;
import com.fuhu.nabiconnect.chat.widget.ChatStickerWidget;
import com.fuhu.nabiconnect.chat.widget.StickerCategoryWidget;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.ndnslibsoutstructs.chatPollMessage_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatFragment extends Tracking.TrackingInfoFragment implements IOnChatMessageReceivedListener {

    public static final String TAG = "ChatFragment";
    public static final int MSG_RELOAD_CHAT = 10001;
    public static final int MSG_LOAD_CHAT_HISTORY = 10002;
    public static final int RELOAD_CHAT_DURATION = 3000;
    public static final int LOAD_CHAT_HISTORY_DURATION = 0;

    private EditText m_TextMessage;

    private RelativeLayout mStickerContainer;
    private RelativeLayout m_ChatContainer;
    private Button m_PictureButton;
    private Button m_KeyboardButton;

    private boolean m_IsPictureLayoutShown = false;
    private int m_PictureLayoutHeight = 300;
    // private AnimationListener m_PictureLayoutAnimationListener;

    private TableLayout m_PictureCategoryListTable;

    private TableLayout m_PictureIconListTable;
    // private TableLayout m_ChatMessageTable;
    private ListView m_ChatMessageListView;
    private FriendData m_ChatFriend = new FriendData();
    private String m_CurrentConversationId = null;
    private ArrayList<ChatMessageData> m_Messages;// = new
    // ArrayList<ChatMessageData>();
    private ChatMessageAdapter m_ChatMessageAdapter;

    private StickerCategory m_CurrentStickerCategory;
    private Button m_SendButton;
    private Bitmap m_SelfIconBitmap;
    private Bitmap m_OtherIconBitmap;

    private ChatActivity m_Activity;

    private final boolean useListView = true;

    private int m_OriginalChatHistoryLength;
    private boolean m_IsRefreshing;

    private SharedPreferences m_LastConversationPreference;

    public ChatFragment() {
        super(ChatFragment.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return "chat";
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Activity = (ChatActivity) getActivity();
        m_PictureLayoutHeight = m_Activity.getResources().getDimensionPixelSize(
                R.dimen.chat_bar_sticker_container_height);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_chat_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mStickerContainer = (RelativeLayout) view.findViewById(R.id.image_container);
    }

    @Override
    public void onResume() {
        super.onResume();
        m_ChatContainer = (RelativeLayout) getView().findViewById(R.id.chat_container);
        m_PictureButton = (Button) getView().findViewById(R.id.picBtn);
        m_KeyboardButton = (Button) getView().findViewById(R.id.keyboard_button);

        // m_ChatFriend = m_Activity.getCurrentChatFriend();
        m_TextMessage = (EditText) getActivity().findViewById(R.id.chatET);
        m_PictureIconListTable = (TableLayout) getView().findViewById(R.id.image_icon_table);
        m_PictureCategoryListTable = (TableLayout) getView().findViewById(R.id.image_category_table);
        m_ChatMessageListView = (ListView) getView().findViewById(R.id.chat_message_list_view);

        updateCategoryTable();

        // m_ChatMessageTable = (TableLayout)
        // getView().findViewById(R.id.chat_message_table);

        // Set a listener to send a chat text message
        m_SendButton = (Button) getActivity().findViewById(R.id.sendBtn);
        m_SendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                //tracking
                Tracking.pushTrack(view.getContext(), "send_message");

                onSendMessageKeyPressed();
            }
        });

        m_TextMessage.setOnEditorActionListener(new EditText.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSendMessageKeyPressed();
                    return true;
                }
                return false;
            }
        });

        m_TextMessage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                //tracking
                Tracking.pushTrack(v.getContext(), "enter_message");

                if (m_IsPictureLayoutShown) {
                    hidePictureLayout();
                }
            }
        });

        m_ChatContainer.setOnClickListener(m_HideKeyboardOnClickListener);
        // m_ChatMessageTable.setOnClickListener(m_HideKeyboardOnClickListener);

        m_KeyboardButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //tracking
                Tracking.pushTrack(v.getContext(), "show_keyboard");

                InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                m_TextMessage.requestFocus();
                if (m_IsPictureLayoutShown) {
                    hidePictureLayout();
                }
            }
        });

        m_PictureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //tracking
                Tracking.pushTrack(v.getContext(), "show_stickers");

                if (m_IsPictureLayoutShown) {
                    hidePictureLayout();
                } else {
                    showPictureLayout();
                }
            }
        });

        m_IsRefreshing = false;

        // add api event listener
        addApiEventListener();

        // get the conversation ID
        ArrayList<String> actorList = new ArrayList<String>();
        actorList.add(m_ChatFriend.userID);
        actorList.add(m_Activity.getCurrentUserData().userKey);
        // m_Activity.createGetConversation(actorList);

        // load avatar from db
        Bitmap selfBitmap = m_Activity.getDatabaseAdapter().getMyAvatar(m_Activity.getCurrentUserData().userKey,
                m_Activity.getResources().getDimensionPixelSize(R.dimen.message_box_avatar_size));
        if (selfBitmap != null) {
            m_SelfIconBitmap = selfBitmap;
        } else {
            LOG.V(TAG, "onResume() - bitmap from db is null");
        }

        LoadAvatarBitmapTask loadSelfAvatar = new LoadAvatarBitmapTask();
        Utils.executeAsyncTask(loadSelfAvatar, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                m_SelfIconBitmap = bitmap;
                if (m_SelfIconBitmap != null) {
                    LOG.V(TAG, "Load selficon success");
                    // update database
                    m_Activity.getDatabaseAdapter().saveMyAvatar(m_Activity.getCurrentUserData().userKey, bitmap);
                } else {
                    LOG.V(TAG, "Load selficon failed");
                }
                if (m_ChatMessageAdapter != null) {
                    m_ChatMessageAdapter.notifyDataSetChanged();
                }
            }
        }, m_Activity.getCurrentUserData().avatarURL);

        // load from cache
        Bitmap contactBitmap = m_Activity.getDatabaseAdapter().getFriendAvatar(m_Activity.getCurrentUserData().userKey,
                m_ChatFriend.userID, m_Activity.getResources().getDimensionPixelSize(R.dimen.message_box_avatar_size));

        if (contactBitmap != null) {
            m_OtherIconBitmap = contactBitmap;
        } else {
            LOG.V(TAG, "ContactWidget() - no contact avatar in db");
        }

        LoadAvatarBitmapTask loadOtherAvatar = new LoadAvatarBitmapTask();
        Utils.executeAsyncTask(loadOtherAvatar, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                m_OtherIconBitmap = bitmap;
                if (m_OtherIconBitmap != null) {
                    LOG.V(TAG, "Load other icon success");
                    // update database
                    m_Activity.getDatabaseAdapter().saveFriendAvatar(m_Activity.getCurrentUserData().userKey,
                            m_ChatFriend.userID, bitmap);
                } else {
                    LOG.V(TAG, "Load other icon failed");
                }

                if (m_ChatMessageAdapter != null) {
                    m_ChatMessageAdapter.notifyDataSetChanged();
                }
            }
        }, m_ChatFriend.AvatarUrl);

        m_Messages = m_Activity.getChatTotalMessage();
        m_OriginalChatHistoryLength = m_Messages.size();
        if (m_Activity.getCurrenctConversationData() != null)
            m_CurrentConversationId = m_Activity.getCurrenctConversationData().m_ConversationId;
        m_ChatMessageAdapter = new ChatMessageAdapter(m_Activity, 0, m_Messages);
        m_ChatMessageListView.setAdapter(m_ChatMessageAdapter);
        m_ChatMessageListView.setOnTouchListener(m_ListViewTouchListener);

        if (!m_Activity.isFinishing()) {
            if (m_Activity.getNeedRelogin()) {
                // login is dirty
                if (m_Activity.isParentMode()) {
                    m_Activity.loginAccountNoKid();
                } else {
                    m_Activity.loginAccount();
                }
            } else {
                loadChatMessages();
            }
        }

        // reset state
        m_IsPictureLayoutShown = false;

        // store the last talk one's userkey into preference
        m_LastConversationPreference = m_Activity.getSharedPreferences(ChatActivity.LAST_CONVERSATION_PREF,
                Context.MODE_PRIVATE);
        m_LastConversationPreference.edit().putString(m_Activity.getCurrentUserData().userKey, m_ChatFriend.userID)
                .commit();
    }

    public boolean onBackPressed() {
        if (m_IsPictureLayoutShown) {
            hidePictureLayout();
            return true;
        } else {
            return false;
        }
    }

    public void setChatFriend(FriendData data) {
        m_ChatFriend = data;
    }

    private IApiEventListener m_OnChatPollUpdatedListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            if (m_CurrentConversationId == null) {
                LOG.W(TAG, "m_OnChatPollUpdatedListener - conversation is not created yet.");
                return;
            }

            if (isSuccess) {
                LOG.V(TAG, "m_OnChatPollUpdatedListener - success");
                chatPollMessage_outObj data = (chatPollMessage_outObj) obj;
                ArrayList<conversationData> conversations = data.getConversations();
                LOG.V(TAG, "conversations is " + conversations);

                // real data
                for (conversationData cData : conversations) {
                    if (cData.m_ConversationId.equals(m_CurrentConversationId)) {
                        // Add messages to chat board
                        ArrayList<messageData> messageData = cData.m_Messages;
                        Collections.sort(messageData, mMessageComparator);
                        // add sorted messages into the message list
                        for (int i = 0; i < messageData.size(); i++) {
                            LOG.V(TAG,
                                    "m_OnChatPollUpdatedListener - time stamp of this message : "
                                            + messageData.get(i).m_MessageTime);
                            addMessage(messageData.get(i));
                        }
                        break;
                    }
                }
                // send delay message
                m_Handler.sendEmptyMessageDelayed(MSG_RELOAD_CHAT, RELOAD_CHAT_DURATION);
            } else {
                LOG.V(TAG, "m_OnChatPollUpdatedListener - failed to get chat poll updated");
                if (!m_IsRefreshing)
                    m_Activity.showGeneralWarningDialog();
                else {
                    // send delay message
                    m_Handler.sendEmptyMessageDelayed(MSG_RELOAD_CHAT, RELOAD_CHAT_DURATION);
                }
            }
        }
    };

    private IApiEventListener m_SentChatMessageListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {

            String token = (String) obj;

            if (isSuccess) {
                LOG.V(TAG, "m_SendChatMessageListener - success");

                // find sent message from m_Messages
                if (m_Messages == null) {
                    return;
                }
                for (ChatMessageData data : m_Messages) {
                    messageData msgData = data.getMessageData();
                    if (msgData.m_SenderId.equals(m_Activity.getCurrentUserData().userKey)
                            && String.valueOf(msgData.m_MessageTime).equals(token)) {
                        NabiNotificationManager notificationManager = m_Activity.getNabiNotificationManager();

                        // check if this message is sticker
                        String gcmContent;
                        if (msgData.m_MessageContent.contains(StickerManager.STICKER_PREFIX)) {
                            gcmContent = m_Activity.getString(R.string.notification_chat_sticker_description);
                        } else {
                            gcmContent = msgData.m_MessageContent;
                        }

                        // notify gcm server
                        notificationManager.notifyServerByUserKey(m_ChatFriend.osgUserKey, m_ChatFriend.osgKidId,
                                m_Activity.getCurrentUserData().userName, gcmContent,
                                NabiNotificationManager.APPLICATION_NAME_CHAT, new GCMSenderEventCallback() {

                                    @Override
                                    public void onSendMessageSuccess() {
                                    }

                                    @Override
                                    public void onMessgaeSendingError(int errorCode) {
                                    }
                                });
                        break;
                    }
                }
            } else {
                LOG.V(TAG, "m_SendChatMessageListener - failed to send chat message");
                // set send failed indicator
                if (m_Messages == null) {
                    return;
                }

                for (ChatMessageData data : m_Messages) {
                    messageData msgData = data.getMessageData();
                    if (msgData.m_SenderId.equals(m_Activity.getCurrentUserData().userKey)
                            && String.valueOf(msgData.m_MessageTime).equals(token)) {
                        data.sendFailed = true;
                        if (m_ChatMessageAdapter != null) {
                            m_ChatMessageAdapter.notifyDataSetChanged();
                        }
                        break;
                    }
                }
            }
        }
    };

    private IApiEventListener mOnUserLogin = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                m_Activity.setNeedRelogin(false);
                loadChatMessages();
            }
        }
    };

    private IButtonClickListener m_OnButtonClickListener = new IButtonClickListener() {

        @Override
        public void onButtonClicked(int buttonId, String viewName, Object[] args) {

            if (viewName.equals(StickerCategoryWidget.TAG)) {
                StickerCategory selectedCategory = null;
                if (args != null)
                    selectedCategory = (StickerCategory) args[0];
                if (selectedCategory != null) {
                    LOG.V(TAG, "categoryId is " + selectedCategory.getId());
                    onCategorySelected(selectedCategory);
                } else {
                    LOG.V(TAG, "Plus icon is clicked, go to shop page");
                }

                //tracking
                Tracking.pushTrack(getActivity(), "select_sticker_sets_#" + selectedCategory.getName());
            } else if (viewName.equals(ChatStickerWidget.TAG)) {
                Sticker selectedSticker = null;
                if (args != null)
                    selectedSticker = (Sticker) args[0];
                LOG.V(TAG, "selectedSticker is " + selectedSticker.getId());
                LOG.V(TAG, "selectedSticker is " + selectedSticker.getResId());
                String content = StickerManager.createPrefixString(m_CurrentStickerCategory.getId(),
                        String.valueOf(selectedSticker.getId()));
                sendMessage(m_ChatFriend, content);

                //tracking
                Tracking.pushTrack(getActivity(), "select_sticker_#" + selectedSticker.getId());
            }


        }
    };

    private View.OnClickListener m_HideKeyboardOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(m_TextMessage.getWindowToken(), 0);
        }
    };

    private View.OnTouchListener m_ListViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;
                case MotionEvent.ACTION_MOVE:
                    break;
                case MotionEvent.ACTION_UP:
                    if (m_ChatMessageListView.getChildCount() > 0) {
                        if (m_ChatMessageListView.getChildAt(0).getTop() == 0
                                && m_ChatMessageListView.getFirstVisiblePosition() == 0) {
                            if (m_Activity.loadAllCHatHistory()) {
                                m_Handler.sendEmptyMessageDelayed(MSG_LOAD_CHAT_HISTORY, LOAD_CHAT_HISTORY_DURATION);
                            }
                        }
                    } else {
                        LOG.V(TAG, "there is no child count");
                    }
                    InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(m_TextMessage.getWindowToken(), 0);
                    break;
            }
            return false;
        }
    };

    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LOG.V(TAG, "Receive msg " + msg.what);
            switch (msg.what) {
                case MSG_RELOAD_CHAT:
                    if (!m_Activity.getNetworkManager().checkWifiProcess()) {
                        return;
                    }
                    m_IsRefreshing = true;
                    loadChatMessages();
                    break;
                case MSG_LOAD_CHAT_HISTORY:
                    if (m_ChatMessageAdapter != null) {
                        m_ChatMessageAdapter.notifyDataSetChanged();
                    }
                    m_ChatMessageListView.setSelection(m_Messages.size() - m_OriginalChatHistoryLength - 1);
                    break;
            }
        }
    };

    private void onSendMessageKeyPressed() {
        String text = m_TextMessage.getText().toString();
        if (text == null || text.trim().length() == 0) {
            LOG.V(TAG, "onSendMessageKeyPressed() - text is empty");
            return;
        }
        sendMessage(m_ChatFriend, text);
        m_TextMessage.setText(null);
    }

    private void sendMessage(FriendData receiver, String message) {
        // addMessage(m_Activity.getCurrentUserData().userKey, message);

        messageData data = new messageData();
        data.m_SenderId = m_Activity.getCurrentUserData().userKey;
        data.m_MessageContent = message;
        // TODO : enter the correct time stamp
        data.m_MessageTime = System.currentTimeMillis();
        addMessage(data);

        String escapeString = JSONObject.quote(message);
        escapeString = escapeString.substring(1, escapeString.length() - 1);
        LOG.V(TAG, "sendMessage() - remove start and end symbol, escapeString is " + escapeString);

        String utf8String = message;
        try {
            // convert from internal Java String format -> UTF-8
            utf8String = new String(escapeString.getBytes("UTF-8"), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            LOG.E(TAG, "sendMessage() - failed to convert string to utf-8", e);
        }
        LOG.V(TAG, "sendMessage() - utf8String is " + utf8String);
        // use message time as token
        m_Activity.sendMessage(m_CurrentConversationId, utf8String, String.valueOf(data.m_MessageTime));
    }

    private void loadChatMessages() {
        // get chat poll uppdate
        ArrayList<String> conversationList = new ArrayList<String>();
        conversationList.add(m_CurrentConversationId);
        m_Activity.getChatPollMessageUpdate(m_Activity.getCurrentUserData().userKey, conversationList);
    }

    private void updateCategoryTable() {
        m_PictureCategoryListTable.removeAllViews();
        TableRow tableRow = new TableRow(m_Activity);
        // get puchased category
        ArrayList<StickerCategory> list = getAvailableCategory();
        for (StickerCategory category : list) {
            StickerCategoryWidget widget = new StickerCategoryWidget(m_Activity, category);
            widget.addButtonListener(m_OnButtonClickListener);
            tableRow.addView(widget);
        }
        m_PictureCategoryListTable.addView(tableRow);
        if (list.size() > 0) {
            onCategorySelected(list.get(0));
        }
    }

    private void onCategorySelected(StickerCategory category) {
        m_PictureIconListTable.removeAllViews();
        m_CurrentStickerCategory = category;
        TableRow tableRow = new TableRow(m_Activity);
        // get default & puchased category
        ArrayList<Sticker> list = category.getStickerList();
        for (Sticker sticker : list) {
            ChatStickerWidget widget = new ChatStickerWidget(m_Activity, sticker);
            widget.addButtonListener(m_OnButtonClickListener);
            tableRow.addView(widget);
        }
        m_PictureIconListTable.addView(tableRow);
    }

    private ArrayList<StickerCategory> getAvailableCategory() {
        return StickerManager.totalCategories;
    }

    // private void addMessage(String senderId, CharSequence msg) {
    // if (useListView) {
    // return;
    // }
    //
    // TableRow tableRow = new TableRow(m_Activity);
    // int resId = StickerManager.getSticker((String) msg);
    // Bitmap avatarBitmap = null;
    // SendFrom from;
    // String showName;
    //
    // if (senderId.equalsIgnoreCase(m_Activity.getCurrentUserData().userKey)) {
    // from = SendFrom.MYSELF;
    // avatarBitmap = m_SelfIconBitmap;
    // showName = m_Activity.getCurrentUserData().userName;
    // } else {
    // from = SendFrom.FRIEND;
    // avatarBitmap = m_OtherIconBitmap;
    // showName = m_ChatFriend.userName;
    // }
    //
    // MessageBoxWidget msgBox = new MessageBoxWidget(m_Activity);
    //
    // if (resId != -1) {
    // msgBox.setInfo(from, avatarBitmap, null, null, resId);
    // } else {
    // msgBox.setInfo(from, avatarBitmap, showName, (String) msg, resId);
    // }
    // tableRow.addView(msgBox);
    // tableRow.setPadding(0,
    // m_Activity.getResources().getDimensionPixelOffset(R.dimen.message_box_padding),
    // 0, 0);

    // m_ChatMessageTable.addView(tableRow);
    // scroll to bottom
    // m_ChatScrollView.post(new Runnable() {
    // @Override
    // public void run() {
    // m_ChatScrollView.fullScroll(ScrollView.FOCUS_DOWN);
    // }
    // });
    // }

    private void addMessage(messageData data) {
        if (!useListView)
            return;

        m_Messages.add(new ChatMessageData(data));
        if (m_ChatMessageAdapter != null)
            m_ChatMessageAdapter.notifyDataSetChanged();

        scrollMyListViewToBottom();
    }

    @Override
    public void onPause() {
        LOG.V(TAG, "onPause() - start");
        removeApiEventListener();
        if (m_IsPictureLayoutShown) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_ChatContainer.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
            m_ChatContainer.requestLayout();
        }

        // if (m_ChatMessageTable != null) {
        // m_ChatMessageTable.removeAllViews();
        // }

        if (m_Handler != null) {
            m_Handler.removeMessages(MSG_RELOAD_CHAT);
        }

        m_CurrentConversationId = null;
        super.onPause();
    }

    public void showPictureLayout() {
        mStickerContainer.setVisibility(View.VISIBLE);
        InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(m_TextMessage.getWindowToken(), 0);
        TranslateAnimation slideUp = new TranslateAnimation(0, 0, 0, -m_PictureLayoutHeight);
        slideUp.setInterpolator(new DecelerateInterpolator());
        slideUp.setDuration(300);
        slideUp.setFillEnabled(true);
        // slideUp.setAnimationListener(m_PictureLayoutAnimationListener);
        slideUp.setAnimationListener(mOnPictureLayoutShown);
        m_ChatContainer.startAnimation(slideUp);
    }

    public void hidePictureLayout() {
        TranslateAnimation slideDown = new TranslateAnimation(0, 0, 0, m_PictureLayoutHeight);
        slideDown.setDuration(500);
        slideDown.setFillEnabled(true);
        // slideDown.setAnimationListener(m_PictureLayoutAnimationListener);
        slideDown.setAnimationListener(mOnPictureLayoutHidden);
        m_ChatContainer.startAnimation(slideDown);
    }

    @Override
    public void OnChatMessageReceived() {
    }

    private void addApiEventListener() {
        m_Activity.onLoginAccount.addEventListener(mOnUserLogin);
        m_Activity.onChatPollUpdated.addEventListener(m_OnChatPollUpdatedListener);
        m_Activity.onSendChatMessage.addEventListener(m_SentChatMessageListener);
    }

    private void removeApiEventListener() {
        m_Activity.onLoginAccount.addEventListener(mOnUserLogin);
        m_Activity.onChatPollUpdated.removeEventListener(m_OnChatPollUpdatedListener);
        m_Activity.onSendChatMessage.removeEventListener(m_SentChatMessageListener);
    }

    private class ChatMessageAdapter extends ArrayAdapter<ChatMessageData> {
        private LayoutInflater inflater;

        public ChatMessageAdapter(Context context, int textViewResourceId, List<ChatMessageData> objects) {
            super(context, textViewResourceId, objects);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ChatMessageHolder holder = new ChatMessageHolder();
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.chat_message_box_layout, parent, false);
                holder.m_ChatContentContainer = (RelativeLayout) convertView
                        .findViewById(R.id.chat_message_box_content_container);
                holder.m_ChatTimeStampContainer = (RelativeLayout) convertView
                        .findViewById(R.id.chat_message_box_timestamp_container);
                holder.m_SenderAvatar = (ImageView) convertView.findViewById(R.id.sender_avatar);
                holder.m_ReceiverAvatar = (ImageView) convertView.findViewById(R.id.receiver_avatar);
                holder.m_ChatTextContainer = (RelativeLayout) convertView.findViewById(R.id.chat_text_container);
                holder.m_Name = (TextView) convertView.findViewById(R.id.name);
                holder.m_Content = (TextView) convertView.findViewById(R.id.content);
                holder.m_SenderSticker = (ImageView) convertView.findViewById(R.id.sender_sticker);
                holder.m_ReceiverSticker = (ImageView) convertView.findViewById(R.id.receiver_sticker);
                holder.m_LeftIndicator = (ImageView) convertView.findViewById(R.id.left_indicator);
                holder.m_RightIndicator = (ImageView) convertView.findViewById(R.id.right_indicator);
                holder.m_TimeStampText = (TextView) convertView.findViewById(R.id.chat_timestamp_value);
                holder.m_SendFailedIndicator = (ImageView) convertView.findViewById(R.id.send_failed_indicator);
                convertView.setTag(holder);
            } else {
                holder = (ChatMessageHolder) convertView.getTag();
            }

            boolean isSendFailed = getItem(position).isSendFailed();
            final messageData data = getItem(position).getMessageData();

            if (ChatActivity.CHAT_TIME_STAMP_FAKE_MESSAGE.equals(data.m_MessageContent)) {
                holder.m_ChatContentContainer.setVisibility(View.INVISIBLE);
                holder.m_ChatTimeStampContainer.setVisibility(View.VISIBLE);

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(data.m_MessageTime);
                holder.m_TimeStampText.setText(cal.getTime().toString());
            } else {
                holder.m_ChatContentContainer.setVisibility(View.VISIBLE);
                holder.m_ChatTimeStampContainer.setVisibility(View.INVISIBLE);

                int stickerId = StickerManager.getSticker(data.m_MessageContent);
                boolean isFromSelf = data.m_SenderId.equalsIgnoreCase(m_Activity.getCurrentUserData().userKey);
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT);
                String name = null;

                setImageBitmapResource(holder.m_ReceiverAvatar, m_SelfIconBitmap, true);
                setImageBitmapResource(holder.m_SenderAvatar, m_OtherIconBitmap, true);

                holder.m_SenderAvatar.setVisibility(isFromSelf ? View.INVISIBLE : View.VISIBLE);
                holder.m_ReceiverAvatar.setVisibility(isFromSelf ? View.VISIBLE : View.INVISIBLE);

                holder.m_ChatTextContainer.setVisibility(stickerId > 0 ? View.INVISIBLE : View.VISIBLE);
                holder.m_LeftIndicator.setVisibility(stickerId > 0 ? View.INVISIBLE : View.VISIBLE);
                holder.m_RightIndicator.setVisibility(stickerId > 0 ? View.INVISIBLE : View.VISIBLE);

                if (isFromSelf) {
                    params.addRule(RelativeLayout.LEFT_OF, R.id.right_indicator);

                    RelativeLayout.LayoutParams sendFailedParams = new RelativeLayout.LayoutParams(
                            LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                    sendFailedParams.addRule(RelativeLayout.CENTER_VERTICAL);

                    if (stickerId < 0) {
                        // send failed indicator
                        sendFailedParams.addRule(RelativeLayout.LEFT_OF, R.id.chat_text_container);
                        sendFailedParams.rightMargin = m_Activity.getResources().getDimensionPixelSize(
                                R.dimen.message_box_send_failed_indicator_margin_right);

                        holder.m_ReceiverSticker.setVisibility(View.INVISIBLE);
                        holder.m_SenderSticker.setVisibility(View.INVISIBLE);

                        holder.m_LeftIndicator.setVisibility(View.INVISIBLE);
                        holder.m_RightIndicator.setVisibility(View.VISIBLE);
                        holder.m_ChatTextContainer.setBackgroundResource(R.drawable.chat_message_right);
                    } else {
                        // send failed indicator
                        sendFailedParams.addRule(RelativeLayout.LEFT_OF, R.id.receiver_sticker);

                        holder.m_ReceiverSticker.setVisibility(View.VISIBLE);
                        holder.m_SenderSticker.setVisibility(View.INVISIBLE);
                        setImageResource(holder.m_ReceiverSticker, stickerId, false);
                    }

                    // send failed indicator
                    holder.m_SendFailedIndicator.setLayoutParams(sendFailedParams);
                    holder.m_SendFailedIndicator.setVisibility(isSendFailed ? View.VISIBLE : View.INVISIBLE);
                    holder.m_SendFailedIndicator.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //tracking
                            Tracking.pushTrack(v.getContext(), "failed_to_send");

                            LOG.V(TAG, "Re-send the message");
                            m_Activity.sendMessage(m_CurrentConversationId, data.m_MessageContent,
                                    String.valueOf(data.m_MessageTime));

                            getItem(position).sendFailed = false;

                            notifyDataSetChanged();
                        }
                    });
                    name = m_Activity.getCurrentUserData().userName;
                } else {
                    holder.m_SendFailedIndicator.setVisibility(View.INVISIBLE);
                    params.addRule(RelativeLayout.RIGHT_OF, R.id.left_indicator);
                    if (stickerId < 0) {
                        holder.m_ReceiverSticker.setVisibility(View.INVISIBLE);
                        holder.m_SenderSticker.setVisibility(View.INVISIBLE);
                        holder.m_LeftIndicator.setVisibility(View.VISIBLE);
                        holder.m_RightIndicator.setVisibility(View.INVISIBLE);
                        holder.m_ChatTextContainer.setBackgroundResource(R.drawable.chat_message_left);
                    } else {
                        holder.m_ReceiverSticker.setVisibility(View.INVISIBLE);
                        holder.m_SenderSticker.setVisibility(View.VISIBLE);
                        setImageResource(holder.m_SenderSticker, stickerId, false);
                    }
                    name = m_ChatFriend.userName;
                }
                holder.m_ChatTextContainer.setLayoutParams(params);
                setTextResource(holder.m_Name, name);
                setTextResource(holder.m_Content, data.m_MessageContent);
            }
            return convertView;
        }

        private void setImageBitmapResource(ImageView view, Bitmap resId, boolean needDefault) {
            if (resId != null)
                view.setImageBitmap(resId);
            else if (needDefault)
                view.setImageResource(R.drawable.chat_avatar_default);
        }

        private void setImageResource(ImageView view, int resId, boolean needDefault) {
            if (resId > 0)
                view.setImageResource(resId);
            else if (needDefault)
                view.setImageResource(R.drawable.chat_avatar_default);
        }

        private void setTextResource(TextView view, String content) {
            if (content != null)
                view.setText(content);
        }
    }

    static class ChatMessageHolder {
        public RelativeLayout m_ChatContentContainer;
        public RelativeLayout m_ChatTimeStampContainer;
        public ImageView m_SenderAvatar;
        public ImageView m_ReceiverAvatar;
        public RelativeLayout m_ChatTextContainer;
        public TextView m_Name;
        public TextView m_Content;
        public ImageView m_SenderSticker;
        public ImageView m_ReceiverSticker;
        public ImageView m_LeftIndicator;
        public ImageView m_RightIndicator;
        public TextView m_TimeStampText;
        public ImageView m_SendFailedIndicator;
    }

    private void scrollMyListViewToBottom() {
        m_ChatMessageListView.post(new Runnable() {
            @Override
            public void run() {
                m_ChatMessageListView.setSelection(m_ChatMessageAdapter.getCount() - 1);
            }
        });
    }

    private Comparator<messageData> mMessageComparator = new Comparator<messageData>() {
        @Override
        public int compare(messageData s1, messageData s2) {
            return ((Long) s1.m_MessageTime).compareTo((Long) s2.m_MessageTime);
        }
    };

    private AnimationListener mOnPictureLayoutShown = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            m_PictureButton.setClickable(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_ChatContainer.getLayoutParams();
            if (m_IsPictureLayoutShown) {
                params.setMargins(0, 0, 0, 0);
                m_IsPictureLayoutShown = false;
            } else {
                params.setMargins(0, -m_PictureLayoutHeight, 0, 0);
                m_IsPictureLayoutShown = true;
            }
            m_ChatContainer.setLayoutParams(params);
            m_PictureButton.setClickable(true);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    };

    private AnimationListener mOnPictureLayoutHidden = new AnimationListener() {

        @Override
        public void onAnimationStart(Animation animation) {
            m_PictureButton.setClickable(false);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) m_ChatContainer.getLayoutParams();
            if (m_IsPictureLayoutShown) {
                params.setMargins(0, 0, 0, 0);
                m_IsPictureLayoutShown = false;
            } else {
                params.setMargins(0, -m_PictureLayoutHeight, 0, 0);
                m_IsPictureLayoutShown = true;
            }
            m_ChatContainer.setLayoutParams(params);
            m_PictureButton.setClickable(true);

            mStickerContainer.setVisibility(View.GONE);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    };
}