/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fuhu.nabiconnect.mail.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.data.InboxesData;
import com.fuhu.nabiconnect.IButtonClickListener;
import com.fuhu.nabiconnect.IOnMainBarItemSelectedListener;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.event.ApiEvent;
import com.fuhu.nabiconnect.event.IApiEventListener;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.mail.MailActivity;
import com.fuhu.nabiconnect.mail.MailActivity.ReplyReceiverData;
import com.fuhu.nabiconnect.mail.dialog.ChooseContactDialog;
import com.fuhu.nabiconnect.mail.dialog.EraseAllDialog;
import com.fuhu.nabiconnect.mail.dialog.MailReplyDialog;
import com.fuhu.nabiconnect.mail.dialog.MailSentFailedDialog;
import com.fuhu.nabiconnect.mail.effect.CameraEffect;
import com.fuhu.nabiconnect.mail.effect.Effect;
import com.fuhu.nabiconnect.mail.effect.EffectManager;
import com.fuhu.nabiconnect.mail.effect.EffectManager.IEffectManagerHolder;
import com.fuhu.nabiconnect.mail.effect.EraserEffect;
import com.fuhu.nabiconnect.mail.effect.EraserEffectAll;
import com.fuhu.nabiconnect.mail.effect.IColorWallPaperEffect;
import com.fuhu.nabiconnect.mail.effect.IEffectUpdatedListener;
import com.fuhu.nabiconnect.mail.effect.IEraserEffect;
import com.fuhu.nabiconnect.mail.effect.IMultipleWallPaperEffect;
import com.fuhu.nabiconnect.mail.effect.IPaintEffect;
import com.fuhu.nabiconnect.mail.effect.ISingleWallPaperEffect;
import com.fuhu.nabiconnect.mail.effect.IStickerEffect;
import com.fuhu.nabiconnect.mail.effect.ITextEffect;
import com.fuhu.nabiconnect.mail.effect.NoneEffect;
import com.fuhu.nabiconnect.mail.effect.PaintEffect;
import com.fuhu.nabiconnect.mail.effect.TextEffect;
import com.fuhu.nabiconnect.mail.widget.MailEffectButtonWidget;
import com.fuhu.nabiconnect.mail.widget.MailStickerWidget;
import com.fuhu.nabiconnect.mail.widget.MailWallpaperWidget;
import com.fuhu.nabiconnect.mail.widget.PaintingView;
import com.fuhu.nabiconnect.notification.NabiNotificationManager;
import com.fuhu.nabiconnect.photo.widget.PhotoSendingAnimationDialog;
import com.fuhu.nabiconnect.utils.LoadAvatarBitmapTask;
import com.fuhu.nabiconnect.utils.Utils;
import com.fuhu.nabiconnect.utils.stickerwidget.StickerButtonListener;
import com.fuhu.nabiconnect.utils.stickerwidget.StickerWidget;
import com.fuhu.ndnslibsoutstructs.friends_outObj;
import com.fuhu.nns.cmr.lib.ClientCloudMessageReceiver.GCMSenderEventCallback;

import java.io.IOException;
import java.util.ArrayList;

public class MailComposeFragment extends Fragment implements TextureView.SurfaceTextureListener {

    public static final String TAG = "MailComposeFragment";
    public static final int CAMREA_PREIVEW_WIDTH = 640;
    public static final int CAMREA_PREIVEW_HEIGHT = 480;

    public static final int MSG_COUNT_DOWN = 10001;

    private MailActivity m_Activity;

    private TableLayout m_EffectItemTable;
    private ScrollView m_EffectSubItemContainer;
    private TableLayout m_EffectSubItemTable;
    private ImageView m_EffectSubMenuSwitch;
    private EffectManager m_EffectManager;
    private RelativeLayout m_DrawingCanvas;
    private Button m_SendMailButton;
    private ChooseContactDialog m_ChooseContactDialog;
    private EraseAllDialog m_EraseAllDialog;
    private PaintingView m_MailPaintingView;
    private RelativeLayout m_CameraContainer;
    private Camera m_Camera;
    private TextureView m_CameraPreviewView;
    private Button m_CameraShutterButton;
    private Button m_SwitchCameraButton;
    private Button m_CameraSelfTimerButton;
    private RelativeLayout m_SelfTimerContainer;
    private ImageView m_SelfTimerImage;
    private RelativeLayout m_CameraButtonContainer;
    private RelativeLayout m_StickerContainer;
    private TableLayout m_StickerTable;
    private ArrayList<StickerWidget> m_StickerList = new ArrayList<StickerWidget>();
    private int m_StickerCounter;
    private Button m_ReplyDeleteButton;
    private RelativeLayout m_ReplyInfoContainer;
    private ImageView m_ReplyAvatar;
    private TextView m_ReplyName;
    private ReplyReceiverData m_ReceiverInfo;
    private IOnMainBarItemSelectedListener m_MainBarCallback;
    private MailReplyDialog m_ReplyDialog;
    private MailSentFailedDialog m_SentFailedDialog;
    private ArrayList<String> m_CurrentSelectedContactList = new ArrayList<String>();
    private ArrayList<FriendData> m_CurrentSelectedFriendDataList;
    private int m_MailThumbnailWidth;
    private int m_MailThumbnailHeight;

    public Bitmap rLayoutBitmap;
    private Button mail_confirm_button;
    private Button mail_cancel_button;
    // private Button mail_rotate_button;
    // private Button mail_resize_button;
    private RelativeLayout mail_text_second_container;
    private RelativeLayout mail_text_container;
    private EditText mail_edit_edittext;
    private int tvGetLine = 0;
    private int tv_w = 0;
    private int tv_h = 0;
    private boolean etModifyed = false;
    public Bitmap returnTextBitmap;
    private int selectedPaintID = 1;
    private PhotoSendingAnimationDialog m_PhotoSendingAnimationDialog;
    private LayoutParams m_mtsc_params;

    private int m_CurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private int m_CurrentCountDownNumber = 3;

    private ArrayList<MailEffectButtonWidget> m_CurrentItems = new ArrayList<MailEffectButtonWidget>();
    private ArrayList<MailEffectButtonWidget> m_CurrentSubItems = new ArrayList<MailEffectButtonWidget>();
    private IEffectUpdatedListener m_EffectUpdatedListener = new IEffectUpdatedListener() {

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onEffectUpdated(Effect newEffect, Effect oldEffect) {

            LOG.V(TAG, "onEffectUpdated() - newEffect is " + newEffect + " , oldEffect is " + oldEffect);

            // update UI
            updateItems(newEffect);

            // effect sub menu
            ArrayList<Effect> subItems = newEffect.getSubItems();
            if (subItems != null) {

                m_EffectSubItemTable.removeAllViews();
                m_CurrentSubItems.clear();
                // show sub menu
                for (Effect innerEffect : subItems) {
                    MailEffectButtonWidget widget = new MailEffectButtonWidget(m_Activity, innerEffect);
                    m_CurrentSubItems.add(widget);
                    TableRow tableRow = new TableRow(m_Activity);
                    tableRow.addView(widget);

                    tableRow.setPadding(0,
                            m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_effect_bar_item_padding_top),
                            0, 0);

                    m_EffectSubItemTable.addView(tableRow);
                }

                m_EffectSubItemContainer.setVisibility(View.VISIBLE);
                m_EffectSubMenuSwitch.setVisibility(View.VISIBLE);
                m_EffectItemTable.setVisibility(View.INVISIBLE);

                // set first effect
                // if(m_EffectManager != null)
                // m_EffectManager.applyEffect(((MailEffectButtonWidget)m_CurrentSubItems.get(0)).getEffect());

            }

            // handle old effect
            if (oldEffect instanceof CameraEffect) {
                m_CameraContainer.setVisibility(View.INVISIBLE);

                if (m_Handler != null)
                    m_Handler.removeMessages(MSG_COUNT_DOWN);
            } else if (oldEffect instanceof TextEffect) {
                mail_text_container.setVisibility(View.INVISIBLE);
            } else if (oldEffect instanceof IStickerEffect) {
                m_StickerContainer.setVisibility(View.INVISIBLE);
            } else if (oldEffect instanceof IMultipleWallPaperEffect) {
                m_StickerContainer.setVisibility(View.INVISIBLE);
            } else if (oldEffect instanceof IColorWallPaperEffect) {
                m_StickerContainer.setVisibility(View.INVISIBLE);
            }

            // handle new effect
            m_MailPaintingView.setEditable(false);
            if (newEffect instanceof ISingleWallPaperEffect) {
                m_DrawingCanvas.setBackgroundResource(((ISingleWallPaperEffect) newEffect).getWallPaperResId());
            } else if (newEffect instanceof IPaintEffect) {
                LOG.V(TAG, "IPaintEffect");

                m_MailPaintingView.setPaint(((IPaintEffect) newEffect).getPaint());
                m_MailPaintingView.setEditable(true);

                setStickerEditable(false);
            } else if (newEffect instanceof IEraserEffect) {
                LOG.V(TAG, "IEraserEffect");

                m_MailPaintingView.setEraser(((IEraserEffect) newEffect).getPaint());
                m_MailPaintingView.setEditable(true);

                setStickerEditable(false);
            } else if (newEffect instanceof ITextEffect) {
                mail_edit_edittext.setTextColor(((ITextEffect) newEffect).getTextColor());
                selectedPaintID = ((ITextEffect) newEffect).getSelectedID();

                setStickerEditable(false);
            } else if (newEffect instanceof EraserEffectAll) {
                // popup dialog
                m_EraseAllDialog = new EraseAllDialog(m_Activity);
                m_EraseAllDialog.addButtonListener(new IButtonClickListener() {

                    @Override
                    public void onButtonClicked(int buttonId, String viewName, Object[] args) {
                        if (buttonId == EraseAllDialog.YES_BUTTON_ID) {
                            for (StickerWidget sw : m_StickerList) {
                                m_DrawingCanvas.removeView(sw);
                            }
                            m_StickerList.clear();
                            m_MailPaintingView.cleanAllCanvas();
                        }
                        m_EraseAllDialog.dismiss();
                    }
                });

                m_EraseAllDialog.show();
            } else if (newEffect instanceof IStickerEffect) {
                m_StickerContainer.setVisibility(View.VISIBLE);
                updateStickerTable(((IStickerEffect) newEffect).getStickerResId());
                hideStickerControl();
                setStickerEditable(true);
            } else if (newEffect instanceof PaintEffect) {
                // auto select the first effect
                if (m_EffectManager != null)
                    m_EffectManager.applyEffect(((MailEffectButtonWidget) m_CurrentSubItems.get(0)).getEffect());
            } else if (newEffect instanceof EraserEffect) {
                // auto select the second effect
                if (m_EffectManager != null)
                    m_EffectManager.applyEffect(((MailEffectButtonWidget) m_CurrentSubItems.get(1)).getEffect());
            } else if (newEffect instanceof CameraEffect) {
                m_CameraContainer.setVisibility(View.VISIBLE);
                m_SelfTimerContainer.setVisibility(View.INVISIBLE);
                m_CameraButtonContainer.setVisibility(View.VISIBLE);

                setStickerEditable(false);

            } else if (newEffect instanceof TextEffect) {
                // TODO try to draw Textview here
                createEditTextView();
                LOG.V(TAG, "TextEffect");

                setStickerEditable(false);
            } else if (newEffect instanceof NoneEffect) {
                mail_text_second_container.setBackground(null);
                mail_edit_edittext.setVisibility(View.INVISIBLE);
                mail_confirm_button.setVisibility(View.INVISIBLE);
                mail_cancel_button.setVisibility(View.INVISIBLE);
                // mail_rotate_button.setVisibility(View.INVISIBLE);
                // mail_resize_button.setVisibility(View.INVISIBLE);

                m_StickerContainer.setVisibility(View.INVISIBLE);

                m_CameraContainer.setVisibility(View.INVISIBLE);

                if (m_Handler != null)
                    m_Handler.removeMessages(MSG_COUNT_DOWN);

                setStickerEditable(true);
            } else if (newEffect instanceof IMultipleWallPaperEffect) {
                m_StickerContainer.setVisibility(View.VISIBLE);
                updateWallPaperTable(((IMultipleWallPaperEffect) newEffect).getWallPaperResId());
            } else if (newEffect instanceof IColorWallPaperEffect) {
                m_StickerContainer.setVisibility(View.VISIBLE);
                updateColorWallPaperTable(((IColorWallPaperEffect) newEffect).getWallPaperResId());
            }
        }
    };

    private Camera.PictureCallback m_JpegCallBack = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] jpegData, Camera camera) {
            LOG.V(TAG, "JPEG callback");
            Bitmap previewBitmap = m_CameraPreviewView.getBitmap();

            if (previewBitmap != null) {
                LOG.V(TAG, "Bitmap width :" + previewBitmap.getWidth() + " , height :" + previewBitmap.getHeight());
                // m_TakenPicture.setImageBitmap(previewBitmap);
                // add sticker
                addStickerIntoCenter(previewBitmap);
            }

            // restart preview
            m_Camera.startPreview();

            // change to none effect
            m_EffectManager.clearEffect();
        }

    };

    private IApiEventListener m_GetFriendEventListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                friends_outObj data = (friends_outObj) obj;

                if (data == null) {
                    LOG.V(TAG, "m_GetFriendEventListener - data is null");
                    return;
                }

                if (m_ReceiverInfo == null) {
                    m_ChooseContactDialog = new ChooseContactDialog(m_Activity, data);

                    m_ChooseContactDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // TODO Auto-generated method stub
                            LOG.I(TAG, "choose a contact dialog dimiss!");
                            m_SendMailButton.setEnabled(true); // add by ricky
                        }
                    });

                    m_ChooseContactDialog.addButtonListener(new IButtonClickListener() {

                        @Override
                        public void onButtonClicked(int buttonId, String viewName, Object[] args) {
                            if (buttonId == ChooseContactDialog.YES_BUTTON_ID) {
                                ArrayList<FriendData> friendList = (ArrayList<FriendData>) args[0];

                                // store friend list
                                m_CurrentSelectedFriendDataList = friendList;

                                if (friendList == null || friendList.size() == 0) {
                                    m_ChooseContactDialog.dismiss();
                                } else {
                                    m_ChooseContactDialog.dismiss();

                                    // create target id list
                                    m_CurrentSelectedContactList.clear();
                                    // ArrayList<String> idList = new
                                    // ArrayList<String>();
                                    for (FriendData fdata : friendList) {
                                        m_CurrentSelectedContactList.add(fdata.userID);
                                    }

                                    // hide sticker controls
                                    hideStickerControl();

                                    // send mail
                                    sendMail(m_CurrentSelectedContactList);

									/*
                                     * // create bitmap and send Bitmap bitmap =
									 * getrlLayoutBitmap(m_DrawingCanvas,
									 * m_DrawingCanvas.getWidth(),
									 * m_DrawingCanvas.getHeight());
									 * m_Activity.sendMail
									 * (m_Activity.getCurrentUserData().userKey,
									 * idList,
									 * Utils.saveToInternalSorage(m_Activity,
									 * bitmap), "mailName");
									 * 
									 * // show sending mail dialog
									 * m_PhotoSendingAnimationDialog = new
									 * PhotoSendingAnimationDialog(m_Activity);
									 * m_PhotoSendingAnimationDialog
									 * .setCancelable(false);
									 * m_PhotoSendingAnimationDialog.show();
									 */
                                }

                            } else if (buttonId == ChooseContactDialog.CANCEL_BUTTON_ID) {
                                m_ChooseContactDialog.dismiss();
                            }

                        }
                    });

                    m_ChooseContactDialog.show();
                } else {
                    // get the friend data of the receiver from friend list
                    m_CurrentSelectedFriendDataList = new ArrayList<FriendData>();
                    for (FriendData fdata : data.getFriends()) {
                        if (fdata.userID.equals(m_ReceiverInfo.getInboxData().userId)) {
                            LOG.V(TAG, "m_GetFriendEventListener - receiver's friend data is ");
                            fdata.dumpData();
                            m_CurrentSelectedFriendDataList.add(fdata);
                            break;
                        }
                    }

                    m_ReplyDialog = new MailReplyDialog(m_Activity, m_ReceiverInfo);
                    m_ReplyDialog.addButtonListener(m_ButtonClickListener);
                    m_ReplyDialog.setCancelable(true);
                    m_ReplyDialog.setCanceledOnTouchOutside(true);
                    m_ReplyDialog.show();

                    m_SendMailButton.setEnabled(true); // add by ricky
                }

            } else {
                LOG.V(TAG, "m_GetFriendEventListener - failed to get friend list");
                m_Activity.showGeneralWarningDialog();
                m_SendMailButton.setEnabled(true); // add by ricky
            }

        }
    };

    private IApiEventListener m_SendMailEventListener = new IApiEventListener() {

        @Override
        public void onEvent(ApiEvent event, boolean isSuccess, Object obj) {
            if (isSuccess) {
                LOG.V(TAG, "m_SendMailEventListener - success");
                if (m_ChooseContactDialog != null)
                    m_ChooseContactDialog.dismiss();
                String mailSentString = m_Activity.getResources().getString(R.string.MailHasBeenSent);
                m_PhotoSendingAnimationDialog.sentSuccess(mailSentString);

                // notify GCM server
                if (m_CurrentSelectedFriendDataList != null) {
                    NabiNotificationManager notificationManager = m_Activity.getNabiNotificationManager();
                    for (FriendData fData : m_CurrentSelectedFriendDataList) {
                        notificationManager.notifyServerByUserKey(fData.osgUserKey, fData.osgKidId,
                                m_Activity.getCurrentUserData().userName,
                                m_Activity.getString(R.string.notification_mail_description),
                                NabiNotificationManager.APPLICATION_NAME_MAIL, new GCMSenderEventCallback() {

                                    @Override
                                    public void onSendMessageSuccess() {
                                    }

                                    @Override
                                    public void onMessgaeSendingError(int errorCode) {
                                    }
                                });
                    }
                }

                Handler handler = new Handler();
                handler.postDelayed(runnable_dismiss, 1300);
            } else {
                LOG.V(TAG, "m_SendMailEventListener - failed to sent mail");
                m_PhotoSendingAnimationDialog.dismiss();

                m_SentFailedDialog = new MailSentFailedDialog(m_Activity);
                m_SentFailedDialog.addButtonListener(m_ButtonClickListener);
                m_SentFailedDialog.show();
            }

        }
    };

    final Runnable runnable_dismiss = new Runnable() {
        public void run() {
            m_PhotoSendingAnimationDialog.dismiss();

            // after replying, go back to inbox list
            if (m_ReceiverInfo != null) {
                m_MainBarCallback.OnMainBarItemSelected(MailMainBarFragment.ITEM_INBOX_ID);
            }
        }
    };

    private Handler m_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LOG.V(TAG, "Receive msg " + msg.what);

            switch (msg.what) {
                case MSG_COUNT_DOWN:

                    LOG.V(TAG, "m_CurrentCountDownNumber is " + m_CurrentCountDownNumber);
                    if (m_CurrentCountDownNumber != 0) {
                        m_SelfTimerContainer.setVisibility(View.VISIBLE);
                        switch (m_CurrentCountDownNumber) {
                            case 3:
                                m_SelfTimerImage.setImageResource(R.drawable.mail_self_timer_3);
                                break;
                            case 2:
                                m_SelfTimerImage.setImageResource(R.drawable.mail_self_timer_2);
                                break;
                            case 1:
                                m_SelfTimerImage.setImageResource(R.drawable.mail_self_timer_1);
                                break;
                        }

                        m_CurrentCountDownNumber--;
                        m_Handler.sendEmptyMessageDelayed(MSG_COUNT_DOWN, 1000);
                    } else {
                        m_SelfTimerContainer.setVisibility(View.INVISIBLE);
                        takePicture();
                        m_CurrentCountDownNumber = 3;
                    }

                    break;

            }
            super.handleMessage(msg);
        }
    };

    private IButtonClickListener m_ButtonClickListener = new IButtonClickListener() {

        @Override
        public void onButtonClicked(int buttonId, String viewName, Object[] args) {
            if (MailStickerWidget.TAG.equals(viewName)) {
                Bitmap bitmap = (Bitmap) args[0];
                if (bitmap != null) {
                    LOG.V(TAG, "bitmap height is " + bitmap.getHeight());

                    // add sticker
                    addStickerIntoCenter(bitmap);

                    // change to none effect
                    m_EffectManager.clearEffect();
                }
            } else if (MailWallpaperWidget.TAG.equals(viewName)) {
                switch (buttonId) {
                    case MailWallpaperWidget.BUTTON_ID_COLOR:

                        int colorId = (Integer) args[0];
                        m_DrawingCanvas.setBackgroundColor(m_Activity.getResources().getColor(colorId));
                        m_StickerContainer.setVisibility(View.INVISIBLE);

                        break;
                    case MailWallpaperWidget.BUTTON_ID_IMAGE:

                        int imageResId = (Integer) args[0];
                        m_DrawingCanvas.setBackgroundResource(imageResId);
                        m_StickerContainer.setVisibility(View.INVISIBLE);

                        break;
                }
            } else if (MailReplyDialog.TAG.equals(viewName)) {
                // close dialog
                if (m_ReplyDialog != null && m_ReplyDialog.isShowing())
                    m_ReplyDialog.dismiss();

                switch (buttonId) {
                    case MailReplyDialog.YES_BUTTON_ID:

                        ReplyReceiverData data = (ReplyReceiverData) args[0];
                        if (data != null) {
                            // create target id list
                            ArrayList<String> idList = new ArrayList<String>();
                            idList.add(data.getInboxData().userId);

                            // send mail
                            sendMail(idList);

                        } else {
                            LOG.E(TAG, "m_ButtonClickListener - ReplyReceiverData is null");
                        }

                        break;
                    case MailReplyDialog.CANCEL_BUTTON_ID:
                        break;
                }

            } else if (MailSentFailedDialog.TAG.equals(viewName)) {
                // close dialog
                if (m_SentFailedDialog != null && m_SentFailedDialog.isShowing())
                    m_SentFailedDialog.dismiss();

                switch (buttonId) {
                    case MailSentFailedDialog.CLOSE_BUTTON_ID:
                    case MailSentFailedDialog.X_BUTTON_ID:

                        break;
                    case MailSentFailedDialog.OK_BUTTON_ID:
                        // send mail
                        sendMail(m_CurrentSelectedContactList);
                        break;
                }
            }

        }

    };

    private void updateItems(Effect effect) {
        // update widget status
        for (MailEffectButtonWidget widget : m_CurrentItems) {
            widget.setSelected(widget.getEffect() == effect);
        }

        for (MailEffectButtonWidget widget : m_CurrentSubItems) {
            widget.setSelected(widget.getEffect() == effect);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Activity = (MailActivity) this.getActivity();
        m_StickerCounter = 0;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.mail_compose_view, container, false);
    }

    @Override
    public void onPause() {

        removeApiEventListener();

        if (m_Handler != null) {
            m_Handler.removeMessages(MSG_COUNT_DOWN);
        }

        if (m_SelfTimerContainer != null) {
            m_SelfTimerContainer.setVisibility(View.INVISIBLE);
        }

        if (m_PhotoSendingAnimationDialog != null && m_PhotoSendingAnimationDialog.isShowing()) {
            m_PhotoSendingAnimationDialog.dismiss();
        }

        if (m_SentFailedDialog != null && m_SentFailedDialog.isShowing()) {
            m_SentFailedDialog.dismiss();
        }

        // change to none effect
        m_EffectManager.clearEffect();
        leaveSubItemBar();

        releaseCamera();

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

        m_EffectItemTable = (TableLayout) getView().findViewById(R.id.mail_effect_items_table);
        m_EffectSubItemContainer = (ScrollView) getView().findViewById(R.id.mail_effect_subitems_scroll_view);
        m_EffectSubItemTable = (TableLayout) getView().findViewById(R.id.mail_effect_subitems_table);
        m_EffectSubMenuSwitch = (ImageView) getView().findViewById(R.id.mail_effect_bar_switch);
        m_DrawingCanvas = (RelativeLayout) getView().findViewById(R.id.mail_drawing_canvas_container);
        m_SendMailButton = (Button) getView().findViewById(R.id.mail_send_button);
        m_MailPaintingView = (PaintingView) getView().findViewById(R.id.mail_painting_view);
        m_CameraContainer = (RelativeLayout) getView().findViewById(R.id.mail_camera_container);
        m_CameraShutterButton = (Button) getView().findViewById(R.id.mail_camera_shutter_button);
        m_SwitchCameraButton = (Button) getView().findViewById(R.id.mail_switch_camera_button);
        m_CameraSelfTimerButton = (Button) getView().findViewById(R.id.mail_camera_self_timer_button);
        m_CameraPreviewView = (TextureView) getView().findViewById(R.id.camera_preview_container);
        m_SelfTimerContainer = (RelativeLayout) getView().findViewById(R.id.self_timer_container);
        m_SelfTimerImage = (ImageView) getView().findViewById(R.id.self_timer_image);
        m_CameraButtonContainer = (RelativeLayout) getView().findViewById(R.id.camera_button_container);
        m_StickerContainer = (RelativeLayout) getView().findViewById(R.id.mail_sticker_container);
        m_StickerTable = (TableLayout) getView().findViewById(R.id.mail_sticker_widget_table);
        m_ReplyDeleteButton = (Button) getView().findViewById(R.id.mail_delete_reply_button);
        m_ReplyInfoContainer = (RelativeLayout) getView().findViewById(R.id.mail_reply_info_container);
        ;
        m_ReplyAvatar = (ImageView) getView().findViewById(R.id.mail_reply_info_receiver_avatar);
        m_ReplyName = (TextView) getView().findViewById(R.id.mail_reply_info_receiver_name);
        ;

        mail_confirm_button = (Button) getView().findViewById(R.id.mail_confirm_button);
        mail_cancel_button = (Button) getView().findViewById(R.id.mail_cancel_button);
        // mail_rotate_button = (Button)
        // getView().findViewById(R.id.mail_rotate_button);
        // mail_resize_button = (Button)
        // getView().findViewById(R.id.mail_resize_button);
        mail_text_second_container = (RelativeLayout) getView().findViewById(R.id.mail_text_second_container);
        mail_text_container = (RelativeLayout) getView().findViewById(R.id.mail_text_container);
        mail_edit_edittext = (EditText) getView().findViewById(R.id.mail_edit_edittext);
        m_mtsc_params = mail_text_second_container.getLayoutParams();

        try {
            m_MainBarCallback = (IOnMainBarItemSelectedListener) m_Activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(m_Activity.toString() + " must implement OnMainBarItemSelectedListener");
        }

        if (m_Activity instanceof IEffectManagerHolder) {

            m_EffectManager = ((IEffectManagerHolder) m_Activity).getEffectManager();

            // clear table
            m_EffectItemTable.removeAllViews();

            // generate effect list
            ArrayList<Effect> effectList = m_EffectManager.getAllEffects();
            m_CurrentItems.clear();
            for (Effect effect : effectList) {

                MailEffectButtonWidget widget = new MailEffectButtonWidget(m_Activity, effect);

                m_CurrentItems.add(widget);

                TableRow tableRow = new TableRow(m_Activity);
                tableRow.addView(widget);

                tableRow.setPadding(0,
                        m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_effect_bar_item_padding_top), 0, 0);

                m_EffectItemTable.addView(tableRow);
            }

            // add effect update listener
            m_EffectManager.addEffectUpdatedListener(m_EffectUpdatedListener);
        } else {
            LOG.E(TAG, "onResume() -  cannot get effect manager");
        }

        m_EffectSubMenuSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                leaveSubItemBar();
            }
        });

        m_SendMailButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (mail_text_container.getVisibility() == View.VISIBLE) {
                    // entering text
                    return;
                }

                //remove sticker controls
                for (StickerWidget widget : m_StickerList) {
                    widget.hideControl();
                }

                if (!m_Activity.getNetworkManager().checkWifiProcess())
                    return;

                // load friend list
                m_Activity.getFriendList(m_Activity.getCurrentUserData().userKey);
                m_SendMailButton.setEnabled(false); // add by ricky
                /*
                 * if(m_ReceiverInfo == null) { // load friend list
				 * m_Activity.getFriendList
				 * (m_Activity.getCurrentUserData().userKey); } else {
				 * m_ReplyDialog = new MailReplyDialog(m_Activity,
				 * m_ReceiverInfo);
				 * m_ReplyDialog.addButtonListener(m_ButtonClickListener);
				 * m_ReplyDialog.setCancelable(true);
				 * m_ReplyDialog.setCanceledOnTouchOutside(true);
				 * m_ReplyDialog.show(); }
				 */
            }
        });

        m_CameraShutterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
        m_SwitchCameraButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (Camera.getNumberOfCameras() > 1) {
                    m_Camera.stopPreview();

                    // NB: if you don't release the current camera before
                    // switching, you app will crash
                    m_Camera.release();

                    // swap the id of the camera to be used
                    if (m_CurrentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        m_CurrentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                    } else {
                        m_CurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                    }
                    m_Camera = Camera.open(m_CurrentCameraId);
                    setPreviewSize(m_Camera);
                    try {
                        // setCameraDisplayOrientation(mActivity,currentCameraId,mCamera);
                        m_Camera.setPreviewTexture(m_CameraPreviewView.getSurfaceTexture());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    m_Camera.startPreview();
                } else {

                    LOG.V(TAG, "There is only one camera.");
                }
            }

        });
        m_CameraSelfTimerButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                m_CameraButtonContainer.setVisibility(View.INVISIBLE);
                m_Handler.sendEmptyMessage(MSG_COUNT_DOWN);
            }
        });
        m_DrawingCanvas.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                LOG.V(TAG, "m_DrawingCanvas has been clicked");
                hideStickerControl();

            }
        });
        m_ReplyDeleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                m_MainBarCallback.OnMainBarItemSelected(MailMainBarFragment.ITEM_INBOX_ID);
            }
        });

        m_CameraPreviewView.setSurfaceTextureListener(this);

        addApiEventListener();

        // initialize state
        m_EffectManager.clearEffect();
        // m_MailPaintingView.setPencil();
        m_MailPaintingView.setEnabled(false);
        m_CurrentCountDownNumber = 3;

        m_MailThumbnailWidth = m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_content_widget_image_width);
        m_MailThumbnailHeight = m_Activity.getResources().getDimensionPixelSize(
                R.dimen.mail_content_widget_image_height);

        // open camera
        try {
            LOG.V(TAG, "onResume() - Camera.open start");
            if (Camera.getNumberOfCameras() <= 1) {
                m_CurrentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                m_SwitchCameraButton.setVisibility(View.INVISIBLE);
            }
            m_Camera = Camera.open(m_CurrentCameraId);
            setPreviewSize(m_Camera);

            if (m_CameraPreviewView.isAvailable()) {
                LOG.V(TAG, "onResume() - start preview");
                onSurfaceTextureAvailable(m_CameraPreviewView.getSurfaceTexture(), m_CameraPreviewView.getWidth(),
                        m_CameraPreviewView.getHeight());
            }
            LOG.V(TAG, "onResume() - Camera.open end");
        } catch (Throwable tr) {
            LOG.E(TAG, "onResume() - Failed to open camrea.", tr);
        }

        m_ReceiverInfo = m_Activity.getReceiverData();
        if (m_ReceiverInfo != null) {
            m_ReplyDeleteButton.setVisibility(View.VISIBLE);
            m_ReplyInfoContainer.setVisibility(View.VISIBLE);
            InboxesData data = m_ReceiverInfo.getInboxData();
            Bitmap avatar = m_ReceiverInfo.getAvatarBitmap();
            m_ReplyName.setText(data.userName);

            if (avatar == null) {
                LoadAvatarBitmapTask loadSelfAvatar = new LoadAvatarBitmapTask();
                Utils.executeAsyncTask(loadSelfAvatar, new LoadAvatarBitmapTask.IOnBitmapLoaded() {

                    @Override
                    public void onBitmapLoaded(Bitmap bitmap) {
                        m_ReceiverInfo.setAvatarBitmap(bitmap);
                        m_ReplyAvatar.setImageBitmap(bitmap);
                    }
                }, data.avatarURL);
            } else
                m_ReplyAvatar.setImageBitmap(avatar);
        } else {
            m_ReplyDeleteButton.setVisibility(View.INVISIBLE);
            m_ReplyInfoContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void setPreviewSize(Camera camera) {
        if (camera == null) {
            LOG.E(TAG, "setPreviewSize() - camrea is null");
            return;
        }

        Camera.Parameters params = camera.getParameters();
        params.setPreviewSize(CAMREA_PREIVEW_WIDTH, CAMREA_PREIVEW_HEIGHT);
        camera.setParameters(params);
    }

    private void hideStickerControl() {
        if (m_StickerList != null) {
            for (StickerWidget widget : m_StickerList)
                widget.hideControl();
        }
    }

    private void setStickerEditable(boolean editable) {
        if (m_StickerList != null) {
            for (StickerWidget widget : m_StickerList)
                widget.setEditable(editable);
        }
    }

    private void addStickerIntoCenter(Bitmap bitmap) {
        addStickerIntoCenter(bitmap, true);
    }

    private void addTextSticker(Bitmap bitmap) {
        if (m_StickerList == null) {
            m_StickerList = new ArrayList<StickerWidget>();
        }
        StickerWidget sw = new StickerWidget(m_Activity, bitmap, new StickerButtonListener() {

            @Override
            public void onClick(StickerWidget sw) {
                m_DrawingCanvas.removeView(sw);
                m_StickerList.remove(sw);
            }

            @Override
            public void onGainFocus(int index) {
                for (StickerWidget widget : m_StickerList) {
                    if (widget.getIndex() != index) {
                        widget.hideControl();
                    }
                }
            }
        }, false);
        sw.setIndex(m_StickerCounter);
        sw.setMinScale(1f / 1.5f);
        m_StickerCounter++;

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        m_DrawingCanvas.addView(sw, params);
        m_StickerList.add(sw);
    }

    private void addStickerIntoCenter(Bitmap bitmap, boolean showStickerControl) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addSticker(bitmap, showStickerControl, params);
    }

    /*
     * private void addSticker(Bitmap bitmap) { addSticker(bitmap, true, null);
     * } private void addSticker(Bitmap bitmap, RelativeLayout.LayoutParams
     * params) { addSticker(bitmap, true, params); } private void
     * addSticker(Bitmap bitmap, boolean showStickerControl) {
     * addSticker(bitmap, showStickerControl, null); }
     */
    private void addSticker(Bitmap bitmap, boolean showStickerControl, RelativeLayout.LayoutParams params) {
        if (m_StickerList == null)
            m_StickerList = new ArrayList<StickerWidget>();

        // add sticker
        StickerWidget sw = new StickerWidget(m_Activity, bitmap, new StickerButtonListener() {

            @Override
            public void onClick(StickerWidget sw) {
                m_DrawingCanvas.removeView(sw);
                m_StickerList.remove(sw);
            }

            @Override
            public void onGainFocus(int index) {
                // hide other stickers
                for (StickerWidget widget : m_StickerList) {
                    if (widget.getIndex() != index) {
                        widget.hideControl();
                    }
                }

            }
        }, showStickerControl);

        // add index for each sticker
        sw.setIndex(m_StickerCounter);
        m_StickerCounter++;

        if (params != null)
            m_DrawingCanvas.addView(sw, params);
        else
            m_DrawingCanvas.addView(sw);
        m_StickerList.add(sw);
    }

    private void sendMail(ArrayList<String> idList) {
        if (!m_Activity.getNetworkManager().checkWifiProcess())
            return;

        // create bitmap and send
        Bitmap bitmap = getrlLayoutBitmap(m_DrawingCanvas, m_DrawingCanvas.getWidth(), m_DrawingCanvas.getHeight());
        Bitmap thumbnailBitmap = Bitmap.createScaledBitmap(bitmap, m_MailThumbnailWidth, m_MailThumbnailHeight, false);

        // m_Activity.sendMail(m_Activity.getCurrentUserData().userKey, idList,
        // Utils.saveToInternalSorage(m_Activity, bitmap), "mailName");

        m_Activity.sendMailWithThumbnail(m_Activity.getCurrentUserData().userKey, idList,
                Utils.saveToInternalSorage(m_Activity, bitmap), "mailName",
                Utils.createSizeString(m_DrawingCanvas.getWidth(), m_DrawingCanvas.getHeight()),
                Utils.saveToInternalSorage(m_Activity, thumbnailBitmap, "mailThumbnail.png"),
                Utils.createSizeString(m_MailThumbnailWidth, m_MailThumbnailHeight));

        // show sending mail dialog
        m_PhotoSendingAnimationDialog = new PhotoSendingAnimationDialog(m_Activity);
        m_PhotoSendingAnimationDialog.setCancelable(false);
        m_PhotoSendingAnimationDialog.show();
    }

    private void updateStickerTable(ArrayList<Integer> imageList) {
        m_StickerTable.removeAllViews();
        MailStickerWidget widget = null;
        TableRow tableRow = new TableRow(m_Activity);
        for (int i = 0; i < imageList.size(); i++) {
            if (i % 2 == 0) {
                widget = new MailStickerWidget(m_Activity);
                widget.addButtonListener(m_ButtonClickListener);
                widget.setTopImage(imageList.get(i));

                tableRow.addView(widget);

            } else {
                widget.setBottomImage(imageList.get(i));
            }
        }
        // hide the vertical divider of the last one widget
        if (widget != null)
            widget.hideVerticalDivider();
        m_StickerTable.addView(tableRow);
    }

    private void updateWallPaperTable(ArrayList<Pair<Integer, Integer>> imageList) {
        m_StickerTable.removeAllViews();

        MailWallpaperWidget widget = null;
        TableRow tableRow = new TableRow(m_Activity);
        for (int i = 0; i < imageList.size(); i++) {
            Pair<Integer, Integer> pair = imageList.get(i);
            if (i % 2 == 0) {

                widget = new MailWallpaperWidget(m_Activity);
                widget.addButtonListener(m_ButtonClickListener);
                widget.setTopImage(pair.first, pair.second, -1);

                tableRow.addView(widget);

            } else {
                widget.setBottomImage(pair.first, pair.second, -1);
            }
        }
        // hide the vertical divider of the last one widget
        if (widget != null)
            widget.hideVerticalDivider();

        m_StickerTable.addView(tableRow);

    }

    private void updateColorWallPaperTable(ArrayList<Integer> imageList) {
        m_StickerTable.removeAllViews();

        MailWallpaperWidget widget = null;
        TableRow tableRow = new TableRow(m_Activity);
        for (int i = 0; i < imageList.size(); i++) {
            int colorId = imageList.get(i);
            if (i % 2 == 0) {

                widget = new MailWallpaperWidget(m_Activity);
                widget.addButtonListener(m_ButtonClickListener);
                widget.setTopImage(-1, -1, colorId);

                tableRow.addView(widget);

            } else {
                widget.setBottomImage(-1, -1, colorId);
            }
        }
        // hide the vertical divider of the last one widget
        if (widget != null)
            widget.hideVerticalDivider();

        m_StickerTable.addView(tableRow);

    }

    private void takePicture() {
        if (m_Camera != null)
            m_Camera.takePicture(null, null, m_JpegCallBack);
    }

    private void releaseCamera() {
        LOG.V(TAG, "releaseCamera() - start");
        if (m_Camera != null) {
            m_Camera.stopPreview();
            m_Camera.release();
            m_Camera = null;
        } else
            LOG.V(TAG, "releaseCamera() - m_Camera is null");
        LOG.V(TAG, "releaseCamera() - end");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        LOG.V(TAG, "onSurfaceTextureAvailable()");

        try {
            m_Camera.setPreviewTexture(surface);
            m_Camera.startPreview();
        } catch (Throwable tr) {
            // Something bad happened
            LOG.E(TAG, "onSurfaceTextureAvailable() - failed to start preview", tr);
        }

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LOG.V(TAG, "onSurfaceTextureDestroyed");
        releaseCamera();
        return true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // TODO Auto-generated method stub

    }

    private void addApiEventListener() {
        LOG.V(TAG, "addApiEventListener() - start");

        m_Activity.onGetFriendList.addEventListener(m_GetFriendEventListener);
        m_Activity.onSendMail.addEventListener(m_SendMailEventListener);

        LOG.V(TAG, "addApiEventListener() - end");
    }

    private void removeApiEventListener() {
        LOG.V(TAG, "removeApiEventListener() - start");

        m_Activity.onGetFriendList.removeEventListener(m_GetFriendEventListener);
        m_Activity.onSendMail.removeEventListener(m_SendMailEventListener);

        LOG.V(TAG, "removeApiEventListener() - end");
    }

    /**
     * This also dismisses the keyboard if it is present.
     */
    private void leaveSubItemBar() {
        m_EffectSubMenuSwitch.setVisibility(View.INVISIBLE);
        m_EffectSubItemContainer.setVisibility(View.INVISIBLE);
        m_EffectItemTable.setVisibility(View.VISIBLE);
        m_EffectManager.clearEffect();
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                mail_edit_edittext.getWindowToken(), 0);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void createEditTextView() {
        InputMethodManager imm = (InputMethodManager) m_Activity.getSystemService(m_Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        mail_edit_edittext.requestFocus();
        if (mail_edit_edittext.getText().length() > 0) {
            // after second times to create it
            m_EffectManager.applyEffect(((MailEffectButtonWidget) m_CurrentSubItems.get(selectedPaintID)).getEffect());
        } else {
            if (m_EffectManager != null) {
                // default color == 1(orange)
                m_EffectManager.applyEffect(((MailEffectButtonWidget) m_CurrentSubItems.get(1)).getEffect());
            }
        }
        // reset edittext
        mail_edit_edittext.setText("");

        iniEdittextView();

        mail_confirm_button.setOnClickListener(new Button.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (mail_edit_edittext.getText().toString().length() >= 0) {
                    mail_text_second_container.setBackground(null);
                    mail_text_container.setVisibility(View.INVISIBLE);
                    Bitmap bitmap = getTextBitmap();

                    addTextSticker(bitmap);

                    // add sticker
                    // addStickerIntoCenter(bitmap, false);

                    // editText has been modify.
                    etModifyed = true;
                }
                leaveSubItemBar();
                InputMethodManager imm = (InputMethodManager) m_Activity
                        .getSystemService(m_Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mail_edit_edittext.getWindowToken(), 0);
            }
        });

        mail_cancel_button.setOnClickListener(new Button.OnClickListener() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mail_text_container.setVisibility(View.INVISIBLE);
                mail_text_second_container.setVisibility(View.INVISIBLE);
                leaveSubItemBar();
                etModifyed = false;
                InputMethodManager imm = (InputMethodManager) m_Activity
                        .getSystemService(m_Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mail_edit_edittext.getWindowToken(), 0);
            }
        });

        mail_edit_edittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                m_mtsc_params.height = RelativeLayout.LayoutParams.WRAP_CONTENT;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                int lines = mail_edit_edittext.getLineCount();
                if (lines >= 6) {
                    String str = s.toString();
                    // get cursor position(Start and End at same place)
                    int cursorStart = mail_edit_edittext.getSelectionStart();
                    int cursorEnd = mail_edit_edittext.getSelectionEnd();
                    if (cursorStart == cursorEnd && cursorStart < str.length() && cursorStart >= 1) {
                        str = str.substring(0, cursorStart - 1) + str.substring(cursorStart);
                    } else {
                        str = str.substring(0, s.length() - 1);
                    }
                    mail_edit_edittext.setText(str);
                    // move the cursor to the end of string.
                    mail_edit_edittext.setSelection(mail_edit_edittext.getText().length());
                }
            }
        });

    }

    private Bitmap getTextBitmap() {
        // String newString = mail_edit_edittext.getText().toString();
        // tvGetLine = mail_edit_edittext.getLineCount();
        // int textWidth =
        // m_Activity.getResources().getDimensionPixelOffset(R.dimen.mail_cut_area_width);
        // int textHeight =
        // m_Activity.getResources().getDimensionPixelOffset(R.dimen.mail_cut_area_height);

        // if (tvGetLine == 1) {
        // tv_w = newString.length() * textWidth;
        // tv_h = textHeight;
        // } else if (tvGetLine > 1) {
        // tv_w = 14 * textWidth;
        // tv_h = tvGetLine * textHeight;
        // }

        // editText has been modify.
        etModifyed = true;
        returnTextBitmap = convertTextviewToBitmap(mail_edit_edittext);
        return returnTextBitmap;
    }

    private Bitmap convertTextviewToBitmap(EditText et) {
        // get 24 dp
        int padding = Math.round(24 * getActivity().getResources().getDisplayMetrics().density);

        Bitmap returnBitmap;
        Bitmap tempTextBitmap;
        // tempTextBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        int width = et.getWidth();
        int height = et.getHeight();

        tempTextBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        // to make a square bitmap
        if (width >= height) {
            returnBitmap = Bitmap.createBitmap(width + 2 * padding, width + 2 * padding, Config.ARGB_8888);

        } else {
            returnBitmap = Bitmap.createBitmap(height + 2 * padding, height + 2 * padding, Config.ARGB_8888);
        }

        Canvas returnCanvas = new Canvas(returnBitmap);
        Canvas tempTextCanvas = new Canvas(tempTextBitmap);
        // c.drawColor(Color.WHITE); // add textview bg color here.
        // original_tv
        // .layout(original_tv.getLeft(), original_tv.getTop(),
        // original_tv.getRight(), original_tv.getBottom());
        // original_tv.draw(c);
        et.draw(tempTextCanvas);
        returnCanvas.drawBitmap(tempTextBitmap, (returnBitmap.getWidth() - tempTextBitmap.getWidth()) / 2,
                (returnBitmap.getHeight() - tempTextBitmap.getHeight()) / 2, null);
        // Log.e(TAG, "w: " + w);
        // Log.e(TAG, "h: " + h);
        // Log.e(TAG, "left: " + original_tv.getLeft());
        // Log.e(TAG, "right: " + original_tv.getRight());
        // Log.e(TAG, "width: " + original_tv.getWidth());
        // return tempTextBitmap;
        return returnBitmap;
    }

    private Bitmap getrlLayoutBitmap(RelativeLayout rl_v, int w, int h) {
        rl_v.measure(MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
        Bitmap allLayoutBitmap = Bitmap.createBitmap(rl_v.getWidth(), rl_v.getHeight(), Config.ARGB_8888);
        Canvas rlCanvas = new Canvas(allLayoutBitmap);
        rl_v.draw(rlCanvas);
        return allLayoutBitmap;
    }

    private void iniEdittextView() {
        mail_text_second_container.setVisibility(View.VISIBLE);
        Drawable dr = m_Activity.getResources().getDrawable(R.drawable.mail_sticker_frame);
        mail_text_second_container.setBackgroundDrawable(dr);
        mail_text_container.setVisibility(View.VISIBLE);
        mail_edit_edittext.setVisibility(View.VISIBLE);
        mail_confirm_button.setVisibility(View.VISIBLE);
        mail_cancel_button.setVisibility(View.VISIBLE);
        // mail_rotate_button.setVisibility(View.VISIBLE);
        // mail_resize_button.setVisibility(View.VISIBLE);
        // mail_resize_button.bringToFront();
        // mail_rotate_button.bringToFront();
        // monospace font.
        mail_edit_edittext.setTypeface(Typeface.MONOSPACE);

        // m_mtsc_params.height =
        // m_Activity.getResources().getDimensionPixelSize(R.dimen.mail_edittext_ini_height);
        mail_edit_edittext.setMaxLines(5);
    }

}