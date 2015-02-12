package com.fuhu.nabiconnect.photo.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.object.CircularSeekBar;
import com.fuhu.nabiconnect.photo.object.CircularSeekBar.OnCircularSeekBarChangeListener;
import com.fuhu.nabiconnect.photo.util.Exif;
import com.fuhu.nabiconnect.photo.util.ExifInterface;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

public class PhotoCameraFragment extends PhotoBaseFragment implements TextureView.SurfaceTextureListener {
    private static String TAG = "PhotoCameraFragment";

    private Camera mCamera;
    private Camera.Parameters CameraParameters;
    private CameraOrientationEventListener mCameraOrientationEventListener;

    private View rootView;
    private ImageButton mShutter;
    private ImageButton mGalleryButton;
    private ImageButton mSwitchCameraButton;
    private ImageView mPlusButton;
    private ImageView mMinusButton;
    private TextureView mTextureView;
    private CircularSeekBar mCircularSeekBar;

    private PhotoCameraGalleryFragment cameragalleryFragment;
    private PhotoCameraEditPhotoFragment cameraeditphotoFragment;

    private String PhotoPath;
    private String focusmode = "continuous-picture";

    public static int DEFAULT_CAMERA_PICTURE_WIDTH = 640;
    public static int DEFAULT_CAMERA_PICTURE_HEIGHT = 480;

    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
    private int camerarotate = 0;

    private ArrayList<String> PhotoPathList = new ArrayList<String>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // TODO Auto-generated method stub

        rootView = inflater.inflate(R.layout.photo_camera_fragment_layout, null);
        mAct.SetLastButtonSelect(false, true, false);
        findView();

        mCameraOrientationEventListener = new CameraOrientationEventListener(mAct, SensorManager.SENSOR_DELAY_NORMAL);

        if (mCameraOrientationEventListener.canDetectOrientation()) {
            mCameraOrientationEventListener.enable();
        }


        return rootView;
    }

    private void findView() {
        mCircularSeekBar = (CircularSeekBar) rootView.findViewById(R.id.CircularSeekBar);
        mCircularSeekBar.setOnSeekBarChangeListener(new OnCircularSeekBarChangeListener() {

            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                if (mCamera != null) {
                    // TODO Auto-generated method stub

                    CameraParameters = mCamera.getParameters();

                    if (CameraParameters.isZoomSupported() && progress <= 100 && progress >= 0) {

                        double zoomNum;

                        zoomNum = ((double) progress / (double) 100 * mCamera.getParameters().getMaxZoom());
                        //LOG.W(TAG, "zoom number:"+zoomNum);
                        CameraParameters.setZoom((int) zoomNum);
                        SetCameraPictureSize();
                        mCamera.setParameters(CameraParameters);
                        LOG.W(TAG, "zoomNum = " + zoomNum + " progress = " + progress);

                    }
                }
            }
        });


        mTextureView = (TextureView) rootView.findViewById(R.id.TextureView);
        mTextureView.setSurfaceTextureListener(this);

        mShutter = (ImageButton) rootView.findViewById(R.id.Shutter);
        mShutter.setOnClickListener(clickListener);

        mSwitchCameraButton = (ImageButton) rootView.findViewById(R.id.SwitchCamera);
        mSwitchCameraButton.setOnClickListener(clickListener);

        mPlusButton = (ImageView) rootView.findViewById(R.id.ZoomIn_Plus);
        mPlusButton.setOnClickListener(clickListener);

        mMinusButton = (ImageView) rootView.findViewById(R.id.ZoomOut_Minus);
        mMinusButton.setOnClickListener(clickListener);

        mGalleryButton = (ImageButton) rootView.findViewById(R.id.CameraGotoGalleryButton);
        mGalleryButton.setOnClickListener(clickListener);


    }

    private void SetCameraPictureSize() {

        int cw = 0, pre = 0;
        CameraParameters.setPictureSize(DEFAULT_CAMERA_PICTURE_WIDTH, DEFAULT_CAMERA_PICTURE_HEIGHT);
        for (Size cs : CameraParameters.getSupportedPictureSizes()) {
            if ((cs.width * 3 / 4) == cs.height && cs.width < 2100 && cs.width > 640) {  // width:height = 4:3
                cw = cs.width;
                if (cw > pre) {
                    CameraParameters.setPictureSize(cs.width, cs.height);
                    LOG.W(TAG, "set picture size width = " + cs.width + "  height = " + cs.height);
                }
                pre = cw;
            }
        }
    }

    private void SetGalleryButton(String path) {

        LOG.I(TAG, "Screen width size:" + ScreenWidth);
        LOG.I(TAG, "Screen height size:" + ScreenHeight);

        ExifInterface exif = Exif.getExif(path);
        int PhotoExifOrientation = 0;
        if (exif != null)
            PhotoExifOrientation = Exif.getOrientation(exif);

        BitmapFactory.Options options = GetBitmapOptions();

        if (ScreenHeight == 1114) {//LG  1920x1114
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 155, PhotoExifOrientation));
        } else if (ScreenHeight == 1128) {//Dream tab  1920x1128
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 145, PhotoExifOrientation));
        } else if (ScreenHeight == 1046) {//Genymotion
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 145, PhotoExifOrientation));
        } else if (ScreenHeight == 552) {//nabi2
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 63, PhotoExifOrientation));
        } else if (ScreenHeight == 1104) {//1920x1200
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 160, PhotoExifOrientation));
        } else if (ScreenHeight == 1034) {//1920x1034  hp
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 150, PhotoExifOrientation));
        } else if (ScreenHeight == 1008) {//1920x1008
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 145, PhotoExifOrientation));
        } else if (ScreenHeight == 828) {//1600x900
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 85, PhotoExifOrientation));
        } else if (ScreenHeight == 836) {
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 110, PhotoExifOrientation));
        } else if (ScreenHeight == 720) {//nabiXD 1366x720
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 105, PhotoExifOrientation));
        } else if (ScreenHeight == 424) {
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 55, PhotoExifOrientation));
        } else if (ScreenWidth == 1280) {
            mGalleryButton.setImageBitmap(getCroppedBitmap(BitmapFactory.decodeFile(path, options), 90, PhotoExifOrientation));
        } else {
            mGalleryButton.setImageBitmap(null);
        }
    }

    public static Bitmap getCroppedBitmap(Bitmap bmp, int radius, int exiforientation) {

        if (bmp == null)
            return null;

        Bitmap sbmp;
        LOG.I(TAG, "bmp.getWidth():" + bmp.getWidth());
        LOG.I(TAG, "bmp.getHeight():" + bmp.getHeight());
        LOG.I(TAG, "bmp.exiforientation:" + exiforientation);
        if (bmp.getWidth() != radius || bmp.getHeight() != radius)
            sbmp = Bitmap.createScaledBitmap(bmp, radius, radius, false);
        else
            sbmp = bmp;

        bmp.recycle();

        Bitmap GalleryButtonBitmap = Bitmap.createBitmap(sbmp.getWidth(), sbmp.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(GalleryButtonBitmap);
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, sbmp.getWidth(), sbmp.getHeight());

        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(Color.parseColor("#BAB399"));
        canvas.drawCircle(sbmp.getWidth() / 2 + 0.7f, sbmp.getHeight() / 2 + 0.7f, sbmp.getWidth() / 2 + 0.1f, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(sbmp, rect, rect, paint);

        if (exiforientation == 90 || exiforientation == 180 || exiforientation == 270) {
            Matrix matrix = new Matrix();
            matrix.postRotate(exiforientation);
            GalleryButtonBitmap = Bitmap.createBitmap(GalleryButtonBitmap, 0, 0, GalleryButtonBitmap.getWidth(), GalleryButtonBitmap.getHeight(), matrix, true);
        }

        return GalleryButtonBitmap;
    }

    private Camera.PictureCallback jpeg = new Camera.PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
            //Bitmap bmp = mTextureView.getBitmap();
            LOG.W(TAG, "Camera bmp w = " + bmp.getWidth() + " h = " + bmp.getHeight());

            FileOutputStream fop;
            Calendar now = Calendar.getInstance();
            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);
            int hour = now.get(Calendar.HOUR_OF_DAY);
            int minute = now.get(Calendar.MINUTE);
            int second = now.get(Calendar.SECOND);
            String photoName = new String("IMG_" + year + "" + month + "" + day + "_" + hour + "" + minute + "" + second);

            try {
                File rootPath = Environment.getExternalStorageDirectory();
                File photoFile = new File(rootPath.getAbsolutePath() + "/DCIM/Camera");
                PhotoPath = photoFile + "/" + photoName + ".jpg";

                if (new File(rootPath.getAbsolutePath() + "/DCIM/Camera").isDirectory()) {
                    fop = new FileOutputStream(PhotoPath);
                } else {
                    photoFile.mkdir();
                    fop = new FileOutputStream(PhotoPath);
                }

                bmp.compress(Bitmap.CompressFormat.JPEG, 100, fop);

                fop.close();

                mCamera.stopPreview();
                mCamera.setPreviewCallback(null);
                mCamera.release();
                mCamera = null;

                mAct.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + PhotoPath)));


                ExifInterface exif = Exif.getExif(data);


                try {
                    exif.writeExif(data, PhotoPath);
                } catch (FileNotFoundException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                int get = Exif.getOrientation(exif);
                LOG.W(TAG, "Exif rotate = " + get);

                mShutter.setEnabled(true);
                mGalleryButton.setEnabled(true);

					/*boolean nowmode = mode.getBoolean("MODE", false);
                    String logonUserKey = mode.getString("LOGON_USER_KEY", "");
					LOG.V(TAG, " nowmode = " + nowmode);

					Intent intent = new Intent(mCtx, CameraEditPhotoActivity.class);
					intent.putExtra("photoPath", PhotoPath);
					//intent.putExtra("camerarotate", camerarotate);
					intent.putExtra(KEY_IS_MOMMY_MODE, nowmode);
					intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
					startActivity(intent);*/


                if (cameraeditphotoFragment != null) {
                    LOG.I(TAG, "cameraeditphotoFragment != null ");
                    if (cameraeditphotoFragment.isAdded()) {
                        LOG.I(TAG, "cameraeditphotoFragment isadded !! ");
                        return;
                    }

                    Bundle bundle = new Bundle();
                    bundle.putString(PhotoParameter.PHOTOPATH, PhotoPath);
                    mAct.switchFragment(cameraeditphotoFragment, bundle,
                            PhotoParameter.FRAGMENTTAG_EDITPHOTO);
                } else {
                    Bundle bundle = new Bundle();
                    bundle.putString(PhotoParameter.PHOTOPATH, PhotoPath);
                    cameraeditphotoFragment = new PhotoCameraEditPhotoFragment();
                    mAct.switchFragment(cameraeditphotoFragment, bundle,
                            PhotoParameter.FRAGMENTTAG_EDITPHOTO);

                }


            } catch (FileNotFoundException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    };

    private AutoFocusCallback myAutoFocusCallback = new AutoFocusCallback() {

        public void onAutoFocus(boolean autoFocusSuccess, Camera arg1) {

            LOG.W(TAG, "autoFocus is = " + autoFocusSuccess);
            if (autoFocusSuccess) {

                mCamera.setParameters(CameraParameters);

                LOG.E(TAG, "before take picture");
                LOG.E(TAG, "CameraParameters getPictureSize().width = "
                        + CameraParameters.getPictureSize().width
                        + " CameraParameters getPictureSize().height = "
                        + CameraParameters.getPictureSize().height);
                LOG.E(TAG, "camerarotate = " + camerarotate);
                mCamera.takePicture(null, null, jpeg);
            } else {
                mCamera.setParameters(CameraParameters);
                mCamera.takePicture(null, null, jpeg);

            }
        }
    };


    private View.OnClickListener clickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.Shutter:

                    if (mCamera != null) {
                        mCamera.autoFocus(myAutoFocusCallback);
                    }

                    mShutter.setEnabled(false);
                    mGalleryButton.setEnabled(false);
                    break;

                case R.id.SwitchCamera:

                    if (Camera.getNumberOfCameras() > 1) {
                        mCamera.stopPreview();

                        //NB: if you don't release the current camera before switching, you app will crash
                        mCamera.release();
                        mCamera = null;
                        mCircularSeekBar.setProgress(0);
                        //swap the id of the camera to be used
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                            StartCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                            LOG.W(TAG, "CAMERA_FACING_FRONT");
                        } else {
                            StartCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                            LOG.W(TAG, "CAMERA_FACING_BACK !");
                        }
                    }
                    break;
                case R.id.CameraGotoGalleryButton:

                    if (mCamera != null) {
                        mCamera.setPreviewCallback(null);
                        mCamera.stopPreview();
                        mCamera.release();
                        mCamera = null;
                    }

				/*boolean nowmode = mode.getBoolean("MODE", false);
                String logonUserKey = mode.getString("LOGON_USER_KEY", "");
				LOG.V(TAG, " nowmode = " + nowmode);
								
				Intent intent = new Intent(mCtx, CameraGalleryActivity.class);
				intent.putExtra(KEY_IS_MOMMY_MODE, nowmode);
				intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
				startActivity(intent);		*/


                    LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = "
                            + mAct.mFragmentManager.getBackStackEntryCount());
                    if (cameragalleryFragment != null) {
                        LOG.I(TAG, "cameragalleryFragment != null ");
                        if (cameragalleryFragment.isAdded()) {
                            LOG.I(TAG, "cameragalleryFragment isadded !! ");
                            return;
                        }

                        mAct.switchFragment(cameragalleryFragment,
                                PhotoParameter.FRAGMENTTAG_CAMERAGALLERY);
                    } else {

                        cameragalleryFragment = new PhotoCameraGalleryFragment();
                        mAct.switchFragment(cameragalleryFragment,
                                PhotoParameter.FRAGMENTTAG_CAMERAGALLERY);

                    }

                    break;
                case R.id.ZoomIn_Plus:

                    if (mCamera != null) {
                        // TODO Auto-generated method stub

                        CameraParameters = mCamera.getParameters();

                        if (CameraParameters.isZoomSupported()) {
                            int zoomNum = CameraParameters.getZoom();
                            int zoommax = mCamera.getParameters().getMaxZoom();
                            LOG.W(TAG, " plus zoommax = " + zoommax);

                            if (zoomNum < (mCamera.getParameters().getMaxZoom())) {

                                zoomNum = zoomNum + (zoommax / 7);

                                if (zoomNum > (mCamera.getParameters().getMaxZoom())) {
                                    zoomNum = mCamera.getParameters().getMaxZoom();
                                }

                            }

                            LOG.W(TAG, "plus zoomNum = " + (int) zoomNum);
                            LOG.W(TAG, "plus ProgresszoomNum = " + (zoomNum * 100 / zoommax));
                            mCircularSeekBar.setProgress(zoomNum * 100 / zoommax);
                            CameraParameters.setZoom((int) zoomNum);
                            SetCameraPictureSize();
                            mCamera.setParameters(CameraParameters);
                        }
                    }

                    break;
                case R.id.ZoomOut_Minus:

                    if (mCamera != null) {
                        // TODO Auto-generated method stub

                        CameraParameters = mCamera.getParameters();
                        if (CameraParameters.isZoomSupported()) {
                            int zoomNum = CameraParameters.getZoom();
                            int zoommax = mCamera.getParameters().getMaxZoom();

                            if (zoomNum > 0) {

                                zoomNum = zoomNum - (zoommax / 7);

                                if (zoomNum < 0)
                                    zoomNum = 0;

                            }

                            LOG.W(TAG, "minus ProgresszoomNum = " + (zoomNum * 100 / zoommax));
                            mCircularSeekBar.setProgress(zoomNum * 100 / zoommax);
                            CameraParameters.setZoom(zoomNum);
                            SetCameraPictureSize();
                            mCamera.setParameters(CameraParameters);
                        }
                    }
                    break;
            }
        }
    };

    private void LoadPictureAndSetGalleryButton() {

        File dcim = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera");
        if (dcim.exists()) {
            if (dcim.isDirectory()) {
                // LOG.I(TAG, "dcim.isHidden:" + dcim.isHidden());
                // LOG.I(TAG, "dcim.exists:" + dcim.exists());

                if (dcim.listFiles() != null) {
                    String temp = null;
                    try {

                        for (File f : dcim.listFiles()) {
                            if (f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("jpg")
                                    || f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("png"))// ensure jpg
                            // file
                            {
                                PhotoPathList.add(f.getAbsolutePath());
                                LOG.I(TAG, "file getAbsolutePath = " + f.getAbsolutePath());
                            }
                        }// end for

                        LOG.I(TAG, "PhotoPathList size = " + PhotoPathList.size());

                        if (PhotoPathList != null && PhotoPathList.size() != 0) {

                            temp = PhotoPathList.get(PhotoPathList.size() - 1);
                            // LOG.I(TAG, "temp:" + temp);
                            if (temp != null) {
                                SetGalleryButton(temp);
                            }
                        }

                    } catch (ArrayIndexOutOfBoundsException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    private void StartCamera(int cameraid) {

        if (mTextureView.isAvailable()) {

            cameraId = cameraid;
            focusmode = "continuous-picture";
            LOG.W(TAG, "start preview");
            onSurfaceTextureAvailable(mTextureView.getSurfaceTexture(), 640, 480);

        }

    }

    public class CameraOrientationEventListener extends OrientationEventListener {

        public CameraOrientationEventListener(Context context, int rate) {
            super(context, rate);
            // TODO Auto-generated constructor stub
        }

        @Override
        public void onOrientationChanged(int orientation) {
            // TODO Auto-generated method stub
            LOG.I(TAG, "orientation = " + orientation);
            try {
                //setCameraDisplayOrientation(mAct, currentCameraId, mCamera);
                if (mCamera != null)
                    mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            if (orientation == ORIENTATION_UNKNOWN)
                return;
            else {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(cameraId, info);

                orientation = (orientation + 45) / 90 * 90;

                int rotation = 0;

                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    rotation = (info.orientation - orientation + 360) % 360;
                } else {
                    rotation = (info.orientation + orientation) % 360;
                }

                if (mCamera != null) {

                    CameraParameters.setRotation(rotation);
                    camerarotate = rotation;
                }
                LOG.I(TAG, "camera setparameters = " + rotation);
            }
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        mShutter.setEnabled(true);
        mCircularSeekBar.setProgress(0);
        LoadPictureAndSetGalleryButton();

        if (Camera.getNumberOfCameras() < 2) {
            mSwitchCameraButton.setVisibility(View.INVISIBLE);
        }

        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            StartCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            LOG.W(TAG, "CAMERA_FACING_BACK !");
        } else {
            StartCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            LOG.W(TAG, "CAMERA_FACING_FRONT");
        }

        if (PhotoPathList.size() == 0) {
            mGalleryButton.setVisibility(View.INVISIBLE);
        } else {
            mGalleryButton.setVisibility(View.VISIBLE);
            mGalleryButton.setEnabled(true);
        }
    }

    @Override
    public void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        mGalleryButton.setImageBitmap(null);
        PhotoPathList.clear();

        LOG.I(TAG, "NabiCamera onStop");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

        mCameraOrientationEventListener.disable();

    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
    }

    private void ShowCameraParameterInfoFunction() {

        for (Size cs : CameraParameters.getSupportedPictureSizes())
            LOG.W(TAG, "camera support set picturesize width = " + cs.width + " height = " + cs.height);

        for (String mode : CameraParameters.getSupportedFocusModes())
            LOG.W(TAG, "camera support fouce mode : " + mode);

        for (String mode : CameraParameters.getSupportedWhiteBalance())
            LOG.W(TAG, "camera support whitebalance mode : " + mode);

        for (String mode : CameraParameters.getSupportedColorEffects())
            LOG.W(TAG, "camera support coloreffects mode : " + mode);

        for (Size ps : CameraParameters.getSupportedPreviewSizes())
            LOG.W(TAG, "camera support set previewsize width = " + ps.width + " height = " + ps.height);

        for (String mode : CameraParameters.getSupportedSceneModes())
            LOG.W(TAG, "camera support scene mode : " + mode);

        for (Size ps : CameraParameters.getSupportedJpegThumbnailSizes())
            LOG.W(TAG, "camera support jpegthumb width = " + ps.width + " height = " + ps.height);

        LOG.W(TAG, "camera current scene mode : " + CameraParameters.getSceneMode());

        LOG.W(TAG, "camera current white balance mode : " + CameraParameters.getWhiteBalance());

    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width,
                                          int height) {
        // TODO Auto-generated method stub
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraId);
                currentCameraId = cameraId;
                CameraParameters = mCamera.getParameters();
                SetCameraPictureSize();

                for (String mode : CameraParameters.getSupportedFocusModes()) {
                    if (mode.equals(focusmode)) {
                        CameraParameters.setFocusMode(focusmode);
                        LOG.W(TAG, "camera set : " + mode);
                    }
                }
                //"continuous-picture"
                //LOG.W(TAG, "camera current white balance mode : " + CameraParameters.getWhiteBalance());
                mCamera.setParameters(CameraParameters);
                mCameraOrientationEventListener.enable();
                try {
                    //setCameraDisplayOrientation(this,currentCameraId,mCamera);
                    mCamera.setPreviewTexture(surface);
                    mCamera.startPreview();
                } catch (IOException ioe) {
                    // Something bad happened
                }

            } catch (RuntimeException e) {
                LOG.E(TAG, "camera dead  !!");
                mAct.finish();
            }

        }

    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width,
                                            int height) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        // TODO Auto-generated method stub
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // TODO Auto-generated method stub

    }

}
