package com.fuhu.nabiconnect.photo.fragment;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.adapter.PhotoImageAdapter;
import com.fuhu.nabiconnect.photo.util.Exif;
import com.fuhu.nabiconnect.photo.util.ExifInterface;
import com.fuhu.nabiconnect.photo.util.PhotoParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class PhotoCameraGalleryFragment extends PhotoBaseFragment {
	public static final String TAG = "PhotoCameraGalleryFragment";
	private static int PhotoSizeWidth = 1200;
	private static int PhotoSizeHeight = 900;
	
	private PhotoCameraEditPhotoFragment cameraeditphotoFragment;
	
	private PhotoImageAdapter adapter;
	private View rootView;
	private GridView LocalGalleryView;
	private ImageView ShowPhotoView;
	private ImageButton mVButton;
	private ImageButton mXButton;
	
	private ArrayList<Bitmap> resizePhoto = new ArrayList<Bitmap>();
	private ArrayList<String> PhotoPathList = new ArrayList<String>(); 
	private ArrayList<String> FolderPathList = new ArrayList<String>();
	
	private String NowPhotoPath;
	private int PhotoExifOrientation;
	
	ExifInterface exif;
	LoadingLocalPhotoPathTask task;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		LOG.W(TAG, "onCreateView");	
		rootView = inflater.inflate(R.layout.photo_cameragallery_fragment_layout, null);
		findView(); 
		
		SetResolutionParameter();
		
		PhotoPathList.clear();
		resizePhoto.clear();
		FolderPathList.clear();
		
		task = new LoadingLocalPhotoPathTask();
		task.execute();

		return rootView;
	}
	
	private void findView() {
		// TODO Auto-generated method stub
		ShowPhotoView = (ImageView) rootView.findViewById(R.id.Show_Local_Gallery);
		LocalGalleryView = (GridView) rootView.findViewById(R.id.Local_Gallery);
		
		mVButton = (ImageButton)rootView.findViewById(R.id.Popup_V_Button);
		mXButton = (ImageButton)rootView.findViewById(R.id.Popup_X_Button);
		mVButton.setOnClickListener(clickListener);
		mXButton.setOnClickListener(clickListener);
	}
	
	private void SetResolutionParameter() {
		LOG.I(TAG,"screen width = " + ScreenWidth);
		switch (ScreenWidth) {
		case 1920:
			PhotoSizeWidth = 1116;
			PhotoSizeHeight = 837;
			break;
		case 1280:
			PhotoSizeWidth = 744;
			PhotoSizeHeight = 558;			
			break;
		case 1600:
			PhotoSizeWidth = 930;
			PhotoSizeHeight = 698;
			break;
		case 1024:
			PhotoSizeWidth = 594;
			PhotoSizeHeight = 446;
			break;
		case 800:
			PhotoSizeWidth = 464;
			PhotoSizeHeight = 348;
			break;
		default:
			break;
		}
	}
	
	private void LoadAllFolderLocalGalleryPhoto(){
		File dcim = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM");
		//OrderByDate(dcim);
		
		if (dcim.isDirectory()) {
			if (dcim.listFiles() != null) {
				for (File f : dcim.listFiles()) {
							
					if (f.isDirectory())
						FolderPathList.add(f.getAbsolutePath());
					
					LOG.W(TAG, "test file list = " + f.getAbsolutePath());
				}
				
				for(int i=0; i<FolderPathList.size(); i++){
					
					PhotoPathList.addAll(LoadLocalGalleryPhoto(FolderPathList.get(i)));					
				}
				
				ShowLocalGalleryPhoto(PhotoPathList);
			}
		}		
	}
	
	private ArrayList<String> LoadLocalGalleryPhoto(String folderpath){
		File folder = new File(folderpath);
		
		OrderByDate(folder);
		
		ArrayList<String> filelist = new ArrayList<String>();
		if (folder.isDirectory()) {
			if (folder.listFiles() != null) {
				
				for (File f : folder.listFiles()) {
					// LOG.I(TAG,f.getAbsolutePath());
					if (f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1)	.equals("jpg")
							|| f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("png"))// ensure jpg file
					{
						
						filelist.add(f.getAbsolutePath());
					
					}
				}
			
				Collections.reverse(filelist);
				return filelist;					
			}		
		}else{
			return null;
		}
		return filelist;			
	}
	
	private void ShowLocalGalleryPhoto(ArrayList<String> photopathlist){
		
		LOG.I(TAG, "PhotoPathList size = " + photopathlist.size());
		
		exif = Exif.getExif(photopathlist.get(0));
		PhotoExifOrientation = Exif.getOrientation(exif);
		Bitmap tempbmp = BitmapFactory.decodeFile(photopathlist.get(0));
		
		if(PhotoExifOrientation == 90 || PhotoExifOrientation == 180 || PhotoExifOrientation == 270){
			
			Matrix matrix = new Matrix();
			matrix.postRotate(PhotoExifOrientation);
			tempbmp = Bitmap.createBitmap(tempbmp, 0, 0, tempbmp.getWidth(), tempbmp.getHeight(), matrix, true);
			
		}
		ShowPhotoView.setImageBitmap(tempbmp);

	}
	
	private void LoadLocalGalleryPhoto(){		
		
		File dcim = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera");
		OrderByDate(dcim);
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPurgeable = true;
				

		if (dcim.isDirectory()) {
			if (dcim.listFiles() != null) {
				
				for (File f : dcim.listFiles()) {
					// LOG.I(TAG,f.getAbsolutePath());
					if (f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("jpg")
							|| f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("png"))// ensure jpg file
					{
						
						PhotoPathList.add(f.getAbsolutePath());
					
					}
				}
			
				Collections.reverse(PhotoPathList);
				LOG.I(TAG, "PhotoPathList size = " + PhotoPathList.size());
				//Collections.reverse(resizePhoto);
				exif = Exif.getExif(PhotoPathList.get(0));
				PhotoExifOrientation = Exif.getOrientation(exif);
				Bitmap tempbmp = BitmapFactory.decodeFile(PhotoPathList.get(0), options);
				
				if(PhotoExifOrientation == 90 || PhotoExifOrientation == 180 || PhotoExifOrientation == 270){
					
					Matrix matrix = new Matrix();
					matrix.postRotate(PhotoExifOrientation);
					tempbmp = Bitmap.createBitmap(tempbmp, 0, 0, tempbmp.getWidth(), tempbmp.getHeight(), matrix, true);
					
				}
				ShowPhotoView.setImageBitmap(tempbmp);
				
			}
		}	
		
		ShowGridViewPhoto();		
	}

	private void ShowGridViewPhoto(){

		for(String path : PhotoPathList){
			LoadingLocalPhotoFileTask task = new LoadingLocalPhotoFileTask(160,120);
			task.execute(path);
		}
		
		adapter = new PhotoImageAdapter(mAct, resizePhoto);
		LocalGalleryView.setAdapter(adapter);
		LocalGalleryView.setVerticalScrollBarEnabled(false);
		LocalGalleryView.setOnItemClickListener(itemclicklistener);
		LOG.I(TAG, " show gridview");
		
	}
	
	private static Bitmap RotatePhotoFileByExif(String filePath, BitmapFactory.Options options){
		
		if(filePath != null){
			ExifInterface exif = Exif.getExif(filePath);
			int PhotoExifOrientation = 0;
			
			if (exif != null) {
				PhotoExifOrientation = Exif.getOrientation(exif);
			}
			
			LOG.I(TAG, "photo file list exif is " + PhotoExifOrientation);
			Bitmap bmp = BitmapFactory.decodeFile(filePath, options);
			
			if (PhotoExifOrientation == 90 || PhotoExifOrientation == 180 || PhotoExifOrientation == 270) {

				Matrix matrix = new Matrix();
				matrix.postRotate(PhotoExifOrientation);
				bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

			}
			
			return bmp;
		}else{
		
			return null;			
		}		
	}
	
	private static void OrderByDate(File file) {  
	  
		if(file.listFiles() != null){
			
			File[] fs = file.listFiles();
			Arrays.sort(fs, new Comparator<File>() {
				public int compare(File f1, File f2) {
					long diff = f1.lastModified() - f2.lastModified();
					if (diff > 0)
						return 1;
					else if (diff == 0)
						return 0;
					else
						return -1;
				}

				public boolean equals(Object obj) {
					return true;
				}

			});
		}
	}  
	
	private View.OnClickListener clickListener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.Popup_V_Button:
				
				if (!PhotoPathList.isEmpty()) {
/*
					boolean nowmode = mode.getBoolean("MODE", false);
					String logonUserKey = mode.getString("LOGON_USER_KEY", "");
					LOG.V(TAG, " nowmode = " + nowmode);

					Intent intent = new Intent(mCtx, CameraEditPhotoActivity.class);
					intent.putExtra("photoPath", PhotoPathList.get(Position));
					intent.putExtra(KEY_IS_MOMMY_MODE, nowmode);
					intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
					startActivity(intent);*/
					
					LOG.W(TAG, "mFragmentManager.getBackStackEntryCount() = "
							+ mAct.mFragmentManager.getBackStackEntryCount());

					if (cameraeditphotoFragment != null) {
						LOG.I(TAG, "cameraeditphotoFragment != null ");
						if (cameraeditphotoFragment.isAdded()) {
							LOG.I(TAG, "cameraeditphotoFragment isadded !! ");
							return;
						}

						Bundle bundle = new Bundle();
						bundle.putString(PhotoParameter.PHOTOPATH, NowPhotoPath);
						mAct.switchFragment(cameraeditphotoFragment,bundle,
								PhotoParameter.FRAGMENTTAG_EDITPHOTO);
					} else {
						Bundle bundle = new Bundle();
						bundle.putString(PhotoParameter.PHOTOPATH, NowPhotoPath);
						cameraeditphotoFragment = new PhotoCameraEditPhotoFragment();
						mAct.switchFragment(cameraeditphotoFragment,bundle,
								PhotoParameter.FRAGMENTTAG_EDITPHOTO);

					}
					
					
				}
				
				break;

			case R.id.Popup_X_Button:
				
				mAct.onBackPressed();
			/*	boolean nowmode = mode.getBoolean("MODE", false);
				String logonUserKey = mode.getString("LOGON_USER_KEY", "");
				LOG.V(TAG, " nowmode = " + nowmode);
				
				Intent intent = new Intent(mCtx, CameraActivity.class);
				intent.putExtra(KEY_IS_MOMMY_MODE, nowmode);
				intent.putExtra(KEY_LOGON_USER_KEY, logonUserKey);
			    startActivity(intent);*/
				
				break;
						
			}
		}
	};
	
	
	
	
	private AdapterView.OnItemClickListener itemclicklistener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> adaptarview, View view, int position, long arg3) {
			// TODO Auto-generated method stub
			Bitmap bmp = DecodeSampledBitmapFromFile(PhotoPathList.get(position), PhotoSizeWidth, PhotoSizeHeight);
			NowPhotoPath = PhotoPathList.get(position);
			ShowPhotoView.setImageBitmap(bmp);
			LOG.W(TAG, "view w = " + ShowPhotoView.getWidth() + "view h = " + ShowPhotoView.getHeight());
			
		}		
	};
	
	private class LoadingLocalPhotoPathTask extends AsyncTask<Void, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Void... arg0) {
			// TODO Auto-generated method stub		
			
			if (!isCancelled()) {
				File dcim = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera");
				OrderByDate(dcim);
				
				if (dcim.isDirectory()) {
					if (dcim.listFiles() != null) {
						
						for (File f : dcim.listFiles()) {
							// LOG.I(TAG,f.getAbsolutePath());
							if (f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1)	.equals("jpg")
									|| f.getAbsolutePath().substring(f.getAbsolutePath().lastIndexOf(".") + 1).equals("png"))// ensure jpg file
							{
								
								PhotoPathList.add(f.getAbsolutePath());
							
							}
						}
					
						Collections.reverse(PhotoPathList);
						LOG.I(TAG, "PhotoPathList size = " + PhotoPathList.size());					
					}
				}
				
				return true;
			} else {
				return false;
			}
			
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			if(result){
				Bitmap tempbmp = DecodeSampledBitmapFromFile(PhotoPathList.get(0), PhotoSizeWidth, PhotoSizeHeight);
				NowPhotoPath = PhotoPathList.get(0);
				ShowPhotoView.setImageBitmap(tempbmp);
				ShowGridViewPhoto();
			}else{				
				
			}
		}		
	}
	
	private class LoadingLocalPhotoFileTask extends AsyncTask<String, Void, Bitmap>{

		private int reqWidth = 0;
		private int reqHeight = 0;
		
		public LoadingLocalPhotoFileTask(int w, int h){
			this.reqWidth = w;
			this.reqHeight = h;			
		}
		
		@Override
		protected Bitmap doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			String filepath = (String)arg0[0];
			
			Bitmap bmp = DecodeSampledBitmapFromFile(filepath, reqWidth, reqHeight);
			return bmp;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);			
			resizePhoto.add(result);
			adapter.notifyDataSetChanged();
		}	
		
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(res, resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,	reqHeight);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(res, resId, options);
	}
	
	public static Bitmap DecodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight){
		
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,	reqHeight);
		LOG.W(TAG, "options.inSampleSize = " + calculateInSampleSize(options, reqWidth,	reqHeight));
		options.inJustDecodeBounds = false;
		
		return RotatePhotoFileByExif(filePath, options);	
		//return BitmapFactory.decodeFile(filePath, options);
	}	
	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
			task = null;
		}
	}
}
