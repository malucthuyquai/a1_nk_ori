package com.fuhu.nabiconnect.mail;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.fuhu.data.InboxesData;
import com.fuhu.data.UserData;
import com.fuhu.nabiconnect.IOnMainBarItemSelectedListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.event.ApiBaseActivity;
import com.fuhu.nabiconnect.friend.dialog.CreateUserNameDialog;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.effect.EffectManager;
import com.fuhu.nabiconnect.mail.effect.EffectManager.IEffectManagerHolder;
import com.fuhu.nabiconnect.mail.fragment.MailComposeFragment;
import com.fuhu.nabiconnect.mail.fragment.MailInboxFragment;
import com.fuhu.nabiconnect.mail.fragment.MailMainBarFragment;

import java.util.Hashtable;


public class MailActivity extends ApiBaseActivity implements IOnMainBarItemSelectedListener, IEffectManagerHolder {

    public static final String TAG = "MailActivity";

    public static class ReplyReceiverData {
        private Bitmap avatarBitmap;
        private InboxesData inboxData;

        public ReplyReceiverData(InboxesData data, Bitmap avatar) {
            this.inboxData = data;
            this.avatarBitmap = avatar;
        }

        public Bitmap getAvatarBitmap() {
            return avatarBitmap;
        }

        public void setAvatarBitmap(Bitmap avatarBitmap) {
            this.avatarBitmap = avatarBitmap;
        }

        public InboxesData getInboxData() {
            return inboxData;
        }

        public void setInboxData(InboxesData inboxData) {
            this.inboxData = inboxData;
        }

    }

    private MailMainBarFragment m_MainBarFrag;
    private MailComposeFragment m_MailComposeFrag;
    private MailInboxFragment m_MailInboxFrag;
    private ReplyReceiverData m_CurrentReceiverData;

    public Hashtable<String, Bitmap> m_AvatarCacheTable = new Hashtable<String, Bitmap>();

    private EffectManager m_EffectManager;

    private CreateUserNameDialog m_CreateUserNameDialog;

    private boolean m_IsMommyMode;
    private String m_LogonUserKey;

    private boolean mNeedRelogin = false;

    public MailActivity() {
        super("nabiMail");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mail_activity_main);

        // get intent extra
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            m_IsMommyMode = extras.getBoolean(KEY_IS_MOMMY_MODE, false);
            m_LogonUserKey = extras.getString(KEY_LOGON_USER_KEY);
        }

        // create fragments
        m_MainBarFrag = new MailMainBarFragment();
        m_MailComposeFrag = new MailComposeFragment();
        m_MailInboxFrag = new MailInboxFragment();

        // check login function
        if (m_IsMommyMode) {
            if (m_LogonUserKey != null) {
                LOG.V(TAG, "onCreate() - there is logon userkey. Get data from cache.");
                UserData logonUser = getDatabaseAdapter().getUserData(m_LogonUserKey);

                if (logonUser != null) {
                    LOG.V(TAG, "onCreate() - use the cache as the logon data.");
                    loginUserSuccess(logonUser);
                } else {
                    LOG.V(TAG, "onCreate() - there is no cache in database.");
                    loginAccountNoKid();
                }
            } else {
                LOG.V(TAG, "onCreate() - there is no logon userkey in extra.");
                loginAccountNoKid();
            }
        } else {
            loginAccount();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        m_EffectManager = new EffectManager(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        m_AvatarCacheTable.clear();
        mNeedRelogin = true;
    }

    public boolean getNeedRelogin() {
        return mNeedRelogin;
    }

    public void setNeedRelogin(boolean needRelogin) {
        mNeedRelogin = needRelogin;
    }

    public void putAvatarCache(String userId, Bitmap avatar) {
        m_AvatarCacheTable.put(userId, avatar);
    }

    public Bitmap getAvatarCache(String userId) {
        if (m_AvatarCacheTable.containsKey(userId))
            return m_AvatarCacheTable.get(userId);
        else {
            LOG.V(TAG, "getAvatarCache() - there is no avatar cache for " + userId);
            return null;
        }
    }

    @Override
    public void OnMainBarItemSelected(int position) {
        OnMainBarItemSelected(position, null);
    }

    @Override
    public void OnMainBarItemSelected(int position, ReplyReceiverData data) {
        // notify listener
        m_MainBarFrag.OnMainBarIndexChanged(position);
        m_CurrentReceiverData = data;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        Fragment newFragment = null;

        switch (position) {
            case MailMainBarFragment.ITEM_COMPOSE_ID:
                newFragment = m_MailComposeFrag;

                //tracking
                Tracking.pushTrack(this, "nabiMail", "compose_message");

                break;
            case MailMainBarFragment.ITEM_INBOX_ID:
                newFragment = m_MailInboxFrag;

                //tracking
                Tracking.pushTrack(this, "nabiMail", "my_inbox");

                break;
            case MailMainBarFragment.ITEM_UNSENT_ID:
                newFragment = m_MailInboxFrag;

                //tracking
                Tracking.pushTrack(this, "nabiMail", "my_inbox");

                break;
        }

        // Replace whatever is in the fragment_container view with this
        // fragment,
        // and add the transaction to the back stack
        if (newFragment != null && !newFragment.isAdded())
            transaction.replace(R.id.right_fragment_container, newFragment);
        // transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();

    }

    public ReplyReceiverData getReceiverData() {
        return this.m_CurrentReceiverData;
    }

    public boolean isParentMode() {
        return this.m_IsMommyMode;
    }

    @Override
    public EffectManager getEffectManager() {
        return m_EffectManager;
    }

    @Override
    public void loginUserSuccess(UserData data) {
        super.loginUserSuccess(data);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        if (!m_MainBarFrag.isVisible()) {
            ft.replace(R.id.left_fragment_container, m_MainBarFrag, "leftfragment");
        }
        if (!m_MailComposeFrag.isVisible()) {
            if (!m_MailInboxFrag.isVisible()) {
                ft.replace(R.id.right_fragment_container, m_MailComposeFrag, "rightfragment");
                // ft.addToBackStack(null);
            }
        }
        ft.commit();
    }

    @Override
    public void loginUserFailure(String data) {
        super.loginUserFailure(data);

        this.showGeneralWarningDialog();
        finish();

    }

    @Override
    public void accountNeedsCreate(String data) {

        super.accountNeedsCreate(data);
        callDialogForAskCreateUserName(m_IsMommyMode);
    }

}
