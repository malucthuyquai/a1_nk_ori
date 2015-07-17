package com.fuhu.nabiconnect;

import android.content.Context;
import android.util.Log;

import com.fuhu.tracking.TrackClickManager;
import com.fuhu.tracking.TrackingFragment;
import com.fuhu.tracking.TrackingFragmentActivity;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by jacktseng on 2015/6/25.
 */
public class Tracking {
    public static final String TAG = Tracking.class.getSimpleName();

    //the track tag for build variants using
    public static final String TRACK_PHOTO_EDIT_PHOTO = "edit_photo";
    public static final String TRACK_PHOTO_EDIT_STICKER = "edit_photo_sticker";
    public static final String TRACK_PHOTO_STICKER_EFFECT = "stickers";
    public static final String TRACK_PHOTO_STICKER_EFFECT_A = "sticker_a";
    public static final String TRACK_PHOTO_STICKER_EFFECT_B = "sticker_b";
    public static final String TRACK_PHOTO_STICKER_EFFECT_C = "sticker_c";
    public static final String TRACK_PHOTO_STICKER_EFFECT_D = "sticker_d";
    public static final String TRACK_PHOTO_WALL_PAPER_TEXTURE_EFFECT= "change_background_texture";

    public static final String H_NSA_NABI_NSA = "nabiNSA";

    /**
     * track list holding the track sequence which you giving
     */
    public static final LinkedList<String> TRACK_LIST = new LinkedList<String>();


    private static boolean checkTrackFormat(String track) {
        boolean isError = true;
        do {
            if (track == null) break;
            if (track.isEmpty()) break;
            if (track.equals("null")) break;

            isError = false;
        } while(false);

//        Log.i(TAG, "checkTrackFormat - error=" + isError);

        return !isError;
    }

    /**
     *
     * Add new track to current track sequence.
     *
     * @param track The name of track.
     */
    public static void trackNext(String track) {
        if(!checkTrackFormat(track)) return;

        TRACK_LIST.add(track);

//        Log.i(TAG, "current=" + currentTrack());
    }

    /**
     *
     * Add new track to current track sequence.</br>
     * If this track was exist, the track sequence will be remove all after this track's track.
     *
     * @param track The name of track.
     */
    public static void trackNextSync(String track) {
        trackBack(track);
        trackNext(track);
    }

    /**
     * Returns the index within current track sequence of the first occurrence.
     *
     * @param track
     * @return index of given track of current track sequence.
     */
    public static int indexOfTrack(String track) {
        return TRACK_LIST.indexOf(track);
    }

    /**
     *
     * Back to previous track of current track sequence.
     *
     * @return The track which was be removed.
     */
    public static String trackBack() {
        String rtn = TRACK_LIST.removeLast();
//        Log.i(TAG, "trackBack() - current=" + currentTrack());

        return rtn;
    }

    /**
     *
     * Back to previous track of current track sequence according to special track of index.
     *
     * @param index The index of current track sequence.
     */
    public static void trackBack(int index) {
        if(index < 0) return; //if(index <= 0) return;
        if(index >= TRACK_LIST.size()) return;
//        if(TRACK_LIST.size() <= 1) return;

        int countOfRemove = TRACK_LIST.size() - index;
        for(int i=0; i<countOfRemove; i++)
            trackBack();

//        Log.i(TAG, "trackBack(int) - current=" + currentTrack());
    }

    /**
     *
     * Back to previous track of current track sequence according to special track of track name.
     *
     * @param track The special track name.
     */
    public static void trackBack(String track) {
        if(!checkTrackFormat(track)) return;

        int trackIdx = indexOfTrack(track);
        trackBack(trackIdx);
    }

    public static int getTrackLength() {
        return TRACK_LIST.size();
    }

    public static String getLastTrack() {
        return TRACK_LIST.peekLast();
    }

    /**
     *
     * Return the track sequence end of special track that is a sub track of current track sequence.
     *
     * @param specialTrack
     * @return The track sequence according to special track.
     */
    public static String getSpecialTrack(String specialTrack) {
        StringBuilder rtn = new StringBuilder();
        if(specialTrack != null && !specialTrack.isEmpty()) {
            Iterator<String> it = TRACK_LIST.iterator();
            while(it.hasNext()) {
                String track = it.next();
                rtn.append(track);
                if(track.equals(specialTrack))
                    break;
                if(it.hasNext())
                    rtn.append(":");
            }

//            Log.d(TAG, "getSpecialTrack=" + rtn.toString());
        }

        return rtn.toString();
    }

    /**
     * Return the current track sequence.
     *
     * @return The current track sequence.
     */
    public static String currentTrack() {
        StringBuilder rtn = new StringBuilder();
        Iterator<String> it = TRACK_LIST.iterator();
        while(it.hasNext()) {
            rtn.append(it.next());
            if(it.hasNext())
                rtn.append(":");
        }

//        Log.d(TAG, "current track=" + rtn.toString());

        return rtn.toString();
    }

    /**
     *
     * Send the action to track service by tracking library.
     *
     */
    private static void pushTrackToServer(Context ctx, String action) {

        TrackClickManager tcm = new TrackClickManager() {
            @Override
            public String getActionName(Object... objects) {
                return null;
            }
        };
        tcm.uploadToTrackingServer(ctx, action);

        Log.d(TAG, "push action - " + action);
    }

    /**
     *
     * Update the action with current track sequence to server immediately.
     *
     * @param ctx
     * @param action The action you want to upgrade.
     */
    public static void pushTrack(Context ctx, String action) {

        if(!checkTrackFormat(action)) return;

        String track = currentTrack() + ":" + action;
        pushTrackToServer(ctx, track);

    }

    /**
     *
     * Update the action with the track sequence of current track sequence which end of  </br>
     * the last track to server immediately.
     *
     * @param ctx
     * @param lastTrack
     * @param action
     */
    public static void pushTrack(Context ctx, String lastTrack, String action) {
        String track = getSpecialTrack(lastTrack) + ":" + action;
        pushTrackToServer(ctx, track);

    }

    /**
     * This class extends Track library for activity using to record the track.
     * The fragment will trackSyncNext(), trackBack() automatically at onResume and onPause. </br>
     * If you need to keep the track that don't track back when activity be paused, you can call </br>
     * passTrackBackOnce() to pass trackBack() once at onPause, or setEnableTrackBack() to false </br>
     * which will let trackBack() never be called at onPause.
     */
    public static abstract class TrackingInfoActivity extends TrackingFragmentActivity {

        private boolean mIsTrackBackPassOnce = false;
        private boolean mIsTrackBack = true;

        public TrackingInfoActivity(String name) {
            super(name);
        }

        @Override
        protected void onResume() {
            super.onResume();
            trackNextSync(getPageName());
        }

        @Override
        protected void onPause() {
            super.onPause();

            if(mIsTrackBack && !mIsTrackBackPassOnce)
                trackBack(getPageName());

            mIsTrackBackPassOnce = false;
        }

        /**
         * Let trackBack() pass once when fragment be paused.
         */
        public void passTrackBackOnce() {
            mIsTrackBackPassOnce = true;
        }

        /**
         * Set trackBack() never be executed when fragment be paused.
         *
         * @param isTrackBack
         */
        public void setEnableTrackBack(boolean isTrackBack) {
            mIsTrackBack = isTrackBack;
        }
    }

    /**
     * This class extends Track library for fragment using to record the track. </br>
     * The fragment will trackNextSync(), trackBack() automatically at onResume and onPause. </br>
     * If you need to keep the track that don't track back when fragment be paused, you can call</br>
     * passTrackBackOnce() to pass trackBack() once at onPause.
     */
    public static abstract class TrackingInfoFragment extends TrackingFragment implements OnTrackListener {

        public static String TRACK_SPECIAL;

        private boolean mIsTrackBackPassOnce = false;

        public TrackingInfoFragment(String name) {
            super(name);
        }

        @Override
        public void onResume() {
            super.onResume();
            trackNextSync(getTrack());
        }

        @Override
        public void onPause() {
            super.onPause();

            if(!mIsTrackBackPassOnce)
                trackBack(getTrack());

            mIsTrackBackPassOnce = false;
        }

        public void passTrackBackOnce() {
            mIsTrackBackPassOnce = true;
        }
    }

    public static interface OnTrackListener {
        public String getTrack();
    }

}
