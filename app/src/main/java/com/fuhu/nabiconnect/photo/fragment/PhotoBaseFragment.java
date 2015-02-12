package com.fuhu.nabiconnect.photo.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.log.LOG;
import com.fuhu.nabiconnect.photo.PhotoActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class PhotoBaseFragment extends Fragment {
	private final String TAG = "PhotoBaseFragment";
	
	protected PhotoActivity mAct;
	protected int ScreenWidth, ScreenHeight;
	protected FragmentManager PhotoActFragmentManager;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mAct = (PhotoActivity) getActivity();
		PhotoActFragmentManager = mAct.mFragmentManager;
		this.ScreenWidth = mAct.ScreenWidth;
		this.ScreenHeight = mAct.ScreenHeight;
		
	}

	public String ChangePhotoTimeFormatAndCalculateToString(long createtime) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss.SSS");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		String time = sdf.format(createtime);

		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("GMT")); // get machine now time
		int now_year = now.get(Calendar.YEAR);
		int now_month = (now.get(Calendar.MONTH) + 1);
		int now_day = now.get(Calendar.DAY_OF_MONTH);
		int now_hour = now.get(Calendar.HOUR_OF_DAY);
		int now_minute = now.get(Calendar.MINUTE);
		int now_second = now.get(Calendar.SECOND);

		LOG.I(TAG, "machine now time = " + now_year + "/" + now_month + "/"
				+ now_day + " " + now_hour + ":" + now_minute + ":"
				+ now_second);

		long durationTime = now.getTimeInMillis() - createtime;
		LOG.I(TAG, "durationTime = " + durationTime);

		int duration_sec = (int) (durationTime / 1000);
		int duration_minute = duration_sec / 60;
		int duration_hour = duration_minute / 60;
		int duration_day = duration_hour / 24;
		LOG.I(TAG, "duration time sec = " + duration_sec + " minute = "
				+ duration_minute + " hour = " + duration_hour + " day = "
				+ duration_day);
		// LOG.I(TAG, "duration time mon = "+ duration_month +" year = " +
		// duration_year);

		if (duration_hour >= 24 || duration_hour < 0) {

			sdf = new SimpleDateFormat("yyyy/MM/dd");
			String photocreatetime = sdf.format(createtime);
			LOG.I(TAG, "return photocreatetime = " + photocreatetime);
			return photocreatetime;

		} else if (duration_hour > 0) {
			if (duration_hour > 1) {
				String hour = new String(duration_hour
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_hours));
				return hour;
			} else {
				String hour = new String(duration_hour
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_hour));
				return hour;
			}
		} else if (duration_minute > 0) {
			if (duration_minute > 1) {
				String minute = new String(duration_minute
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_minutes));
				return minute;
			} else {
				String minute = new String(duration_minute
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_minute));
				return minute;
			}
		} else {
			if (duration_sec > 1) {
				String second = new String(duration_sec
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_seconds));
				return second;
			} else {
				String second = new String(duration_sec
						+ " "
						+ getResources().getString(
								R.string.photo_photowidget_second));
				return second;
			}
		}
	}
	
	protected BitmapFactory.Options GetBitmapOptions() {

		final BitmapFactory.Options Options = new BitmapFactory.Options();
		Options.inJustDecodeBounds = false;
		Options.inPurgeable = true;
		Options.inSampleSize = 2;

		return Options;
	}
	
	

}
