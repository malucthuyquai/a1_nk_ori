package com.fuhu.nabiconnect.nsa.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.data.ReceivedPhotoData;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.nsa.fragment.FragmentNSA;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class NSAPhotoUtil {

	final private static String TAG = NSAPhotoUtil.class.getSimpleName();

	private static HashMap<String, Long> sAvatarTimestamps = new HashMap<String, Long>();

	final private static String FIELD_ID = "id";
	final private static String FIELD_CREATE_TIME = "createTime";
	final private static String FIELD_URL = "url";
	final private static String FIELD_TITLE = "title";
	final private static String FIELD_TO = "to";
	final private static String FIELD_FROM_NAME = "fromName";
	final private static String FIELD_FROM_ID = "fromId";
	final private static String FIELD_FROM_AVATAR_URL = "fromAvatarUrl";
	final private static String FIELD_SIZE = "size";
	final private static String FIELD_URL_TN = "url_tn";
	final private static String FIELD_SIZE_TN = "size_tn";

	public static ArrayList<String> getReceivedPhotoQueryField() {
		ArrayList<String> fields = new ArrayList<String>();
		// photo id
		fields.add(FIELD_ID);
		// timestamp
		fields.add(FIELD_CREATE_TIME);
		// url
		fields.add(FIELD_URL);
		// title
		fields.add(FIELD_TITLE);
		// sender name
		fields.add(FIELD_FROM_NAME);
		// sender id
		fields.add(FIELD_FROM_ID);
		// sender avatar url
		fields.add(FIELD_FROM_AVATAR_URL);
		return fields;
	}

	public static ArrayList<String> getSharedPhotoQueryField() {
		ArrayList<String> fields = new ArrayList<String>();
		// photo id
		fields.add(FIELD_ID);
		// timestamp
		fields.add(FIELD_CREATE_TIME);
		// url
		fields.add(FIELD_URL);
		// title
		fields.add(FIELD_TITLE);
		// receipient list
		fields.add(FIELD_TO);
		return fields;
	}

	public static ArrayList<String> getAllPhotoQueryField() {
		ArrayList<String> fields = new ArrayList<String>();
		// photo id
		fields.add(FIELD_ID);
		// timestamp
		fields.add(FIELD_CREATE_TIME);
		// url
		fields.add(FIELD_URL);
		// title
		fields.add(FIELD_TITLE);
		// sender name
		fields.add(FIELD_FROM_NAME);
		// sender id
		fields.add(FIELD_FROM_ID);
		// sender avatar url
		fields.add(FIELD_FROM_AVATAR_URL);
		// photo dimension
		fields.add(FIELD_SIZE);
		// thumbnail url
		fields.add(FIELD_URL_TN);
		// thumbnail dimension
		fields.add(FIELD_SIZE_TN);
		return fields;
	}

	public static View getPhotoView(Context context, PhotoGroup photoGroup, View convertView, AQuery aq,
			Bitmap defaultAvatar, HashSet<String> mDeleteFlag, View.OnClickListener delete_ocl, DatabaseAdapter db,
			int targetWidth, ColorMatrixColorFilter blackAndWhiteFilter) {
		ViewHolder holder;
		/**
		 * create view
		 */
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.nsa_item_photo_group, null);
			holder = new ViewHolder();
			aq.recycle(convertView);
			// 1
			aq.recycle(convertView.findViewById(R.id.photo_frame01));
			holder.iv_avatar01 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name01 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp01 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete01 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_long_height)), false);
			holder.iv_photo01 = aq.id(R.id.iv_photo).getImageView();
			// 2
			aq.recycle(convertView.findViewById(R.id.photo_frame02));
			holder.iv_avatar02 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name02 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp02 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete02 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_short_height)), false);
			holder.iv_photo02 = aq.id(R.id.iv_photo).getImageView();
			// 3
			aq.recycle(convertView.findViewById(R.id.photo_frame03));
			holder.iv_avatar03 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name03 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp03 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete03 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_short_height)), false);
			holder.iv_photo03 = aq.id(R.id.iv_photo).getImageView();
			// 4
			aq.recycle(convertView.findViewById(R.id.photo_frame04));
			holder.iv_avatar04 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name04 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp04 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete04 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_short_height)), false);
			holder.iv_photo04 = aq.id(R.id.iv_photo).getImageView();
			// 5
			aq.recycle(convertView.findViewById(R.id.photo_frame05));
			holder.iv_avatar05 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name05 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp05 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete05 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_long_height)), false);
			holder.iv_photo05 = aq.id(R.id.iv_photo).getImageView();
			// 6
			aq.recycle(convertView.findViewById(R.id.photo_frame06));
			holder.iv_avatar06 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name06 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp06 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete06 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_long_height)), false);
			holder.iv_photo06 = aq.id(R.id.iv_photo).getImageView();
			// 7
			aq.recycle(convertView.findViewById(R.id.photo_frame07));
			holder.iv_avatar07 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name07 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp07 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete07 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_long_height)), false);
			holder.iv_photo07 = aq.id(R.id.iv_photo).getImageView();
			// 8
			aq.recycle(convertView.findViewById(R.id.photo_frame08));
			holder.iv_avatar08 = aq.id(R.id.iv_avatar).getImageView();
			holder.tv_name08 = aq.id(R.id.tv_name).getTextView();
			holder.tv_timestamp08 = aq.id(R.id.tv_timestamp).getTextView();
			holder.iv_delete08 = aq.id(R.id.iv_delete).getImageView();
			aq.id(R.id.fl_photo).height(
					(int) Math.round(context.getResources().getDimension(R.dimen.nsa_photo_short_height)), false);
			holder.iv_photo08 = aq.id(R.id.iv_photo).getImageView();
			//
			convertView.setTag(holder);
			// set onclick
			holder.iv_delete01.setOnClickListener(delete_ocl);
			holder.iv_delete02.setOnClickListener(delete_ocl);
			holder.iv_delete03.setOnClickListener(delete_ocl);
			holder.iv_delete04.setOnClickListener(delete_ocl);
			holder.iv_delete05.setOnClickListener(delete_ocl);
			holder.iv_delete06.setOnClickListener(delete_ocl);
			holder.iv_delete07.setOnClickListener(delete_ocl);
			holder.iv_delete08.setOnClickListener(delete_ocl);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		/**
		 * load data
		 */

		// 1
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo01, holder.iv_avatar01, defaultAvatar, db,
				photoGroup.photo01.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo01, null, holder.iv_photo01, db, targetWidth);
		holder.tv_name01.setText(photoGroup.photo01.fromName);
		NSAUtil.setTypeface(context, holder.tv_name01, context.getString(R.string.roboto_medium));
		holder.tv_timestamp01.setText(getEllapsedTimeString(photoGroup.photo01.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp01, context.getString(R.string.roboto_regular));
		holder.iv_delete01.setTag(photoGroup.photo01);
		holder.iv_delete01.setSelected(mDeleteFlag.contains(photoGroup.photo01.id));
		// 2
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo02, holder.iv_avatar02, defaultAvatar, db,
				photoGroup.photo02.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo02, null, holder.iv_photo02, db, targetWidth);
		holder.tv_name02.setText(photoGroup.photo02.fromName);
		NSAUtil.setTypeface(context, holder.tv_name02, context.getString(R.string.roboto_medium));
		holder.tv_timestamp02.setText(getEllapsedTimeString(photoGroup.photo02.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp02, context.getString(R.string.roboto_regular));
		holder.iv_delete02.setTag(photoGroup.photo02);
		holder.iv_delete02.setSelected(mDeleteFlag.contains(photoGroup.photo02.id));
		// 3
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo03, holder.iv_avatar03, defaultAvatar, db,
				photoGroup.photo03.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo03, null, holder.iv_photo03, db, targetWidth);
		holder.tv_name03.setText(photoGroup.photo03.fromName);
		NSAUtil.setTypeface(context, holder.tv_name03, context.getString(R.string.roboto_medium));
		holder.tv_timestamp03.setText(getEllapsedTimeString(photoGroup.photo03.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp03, context.getString(R.string.roboto_regular));
		holder.iv_delete03.setTag(photoGroup.photo03);
		holder.iv_delete03.setSelected(mDeleteFlag.contains(photoGroup.photo03.id));
		// 4
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo04, holder.iv_avatar04, defaultAvatar, db,
				photoGroup.photo04.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo04, null, holder.iv_photo04, db, targetWidth);
		holder.tv_name04.setText(photoGroup.photo04.fromName);
		NSAUtil.setTypeface(context, holder.tv_name04, context.getString(R.string.roboto_medium));
		holder.tv_timestamp04.setText(getEllapsedTimeString(photoGroup.photo04.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp04, context.getString(R.string.roboto_regular));
		holder.iv_delete04.setTag(photoGroup.photo04);
		holder.iv_delete04.setSelected(mDeleteFlag.contains(photoGroup.photo04.id));
		// 5
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo05, holder.iv_avatar05, defaultAvatar, db,
				photoGroup.photo05.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo05, null, holder.iv_photo05, db, targetWidth);
		holder.tv_name05.setText(photoGroup.photo05.fromName);
		NSAUtil.setTypeface(context, holder.tv_name05, context.getString(R.string.roboto_medium));
		holder.tv_timestamp05.setText(getEllapsedTimeString(photoGroup.photo05.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp05, context.getString(R.string.roboto_regular));
		holder.iv_delete05.setTag(photoGroup.photo05);
		holder.iv_delete05.setSelected(mDeleteFlag.contains(photoGroup.photo05.id));
		// 6
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo06, holder.iv_avatar06, defaultAvatar, db,
				photoGroup.photo06.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo06, null, holder.iv_photo06, db, targetWidth);
		holder.tv_name06.setText(photoGroup.photo06.fromName);
		NSAUtil.setTypeface(context, holder.tv_name06, context.getString(R.string.roboto_medium));
		holder.tv_timestamp06.setText(getEllapsedTimeString(photoGroup.photo06.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp06, context.getString(R.string.roboto_regular));
		holder.iv_delete06.setTag(photoGroup.photo06);
		holder.iv_delete06.setSelected(mDeleteFlag.contains(photoGroup.photo06.id));
		// 7
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo07, holder.iv_avatar07, defaultAvatar, db,
				photoGroup.photo07.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo07, null, holder.iv_photo07, db, targetWidth);
		holder.tv_name07.setText(photoGroup.photo07.fromName);
		NSAUtil.setTypeface(context, holder.tv_name07, context.getString(R.string.roboto_medium));
		holder.tv_timestamp07.setText(getEllapsedTimeString(photoGroup.photo07.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp07, context.getString(R.string.roboto_regular));
		holder.iv_delete07.setTag(photoGroup.photo07);
		holder.iv_delete07.setSelected(mDeleteFlag.contains(photoGroup.photo07.id));
		// 8
		loadAvatar(context, aq, photoGroup.userKey, photoGroup.photo08, holder.iv_avatar08, defaultAvatar, db,
				photoGroup.photo08.blocked, blackAndWhiteFilter);
		loadPhoto(aq, photoGroup.userKey, photoGroup.photo08, null, holder.iv_photo08, db, targetWidth);
		holder.tv_name08.setText(photoGroup.photo08.fromName);
		NSAUtil.setTypeface(context, holder.tv_name08, context.getString(R.string.roboto_medium));
		holder.tv_timestamp08.setText(getEllapsedTimeString(photoGroup.photo08.createdTime));
		NSAUtil.setTypeface(context, holder.tv_timestamp08, context.getString(R.string.roboto_regular));
		holder.iv_delete08.setTag(photoGroup.photo08);
		holder.iv_delete08.setSelected(mDeleteFlag.contains(photoGroup.photo08.id));
		//
		return convertView;
	}

	public static void loadAvatar(final Context context, final AQuery aq, final String userKey,
			final ReceivedPhotoData photo, final ImageView imageView, final Bitmap defaultAvatar,
			final DatabaseAdapter db, boolean isBlocked, ColorMatrixColorFilter blackAndWhiteFilter) {
		if (isBlocked) {
			imageView.setColorFilter(blackAndWhiteFilter);
		} else {
			imageView.setColorFilter(null);
		}

		final String avatarUrl = photo.fromAvatarUrl.replace("https", "http");
		final String friendKey = Long.toString(photo.fromId);

		aq.id(imageView).tag(AQuery.TAG_1, avatarUrl);
		if (BitmapAjaxCallback.getMemoryCached(friendKey, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
			imageView.setImageBitmap(BitmapAjaxCallback.getMemoryCached(friendKey, FragmentNSA.AVATAR_TARGET_WIDTH));
		} else {
			// fetch newest from network
			final Bitmap cache = db.getFriendAvatar(userKey, Long.toString(photo.fromId),
					FragmentNSA.AVATAR_TARGET_WIDTH);
			final boolean hasCache = cache != null;
			BitmapAjaxCallback callback = new BitmapAjaxCallback() {
				@Override
				protected void callback(String url, ImageView imageView, Bitmap bmp, AjaxStatus status) {
					if (bmp != null) {
						// save once download is finished
						db.saveAvatarAsync(userKey, friendKey, bmp);
						this.memPut(friendKey, bmp);
						if (imageView.getTag(AQuery.TAG_1).equals(avatarUrl)) {
							if (hasCache) {
								imageView.setImageBitmap(bmp);
							} else {
								Drawable[] drawables = new Drawable[2];
								drawables[0] = new BitmapDrawable(context.getResources(), defaultAvatar);
								drawables[1] = new BitmapDrawable(context.getResources(), bmp);
								TransitionDrawable td = new TransitionDrawable(drawables);
								td.setCrossFadeEnabled(true);
								td.startTransition(300);
								imageView.setImageDrawable(td);
							}
						}
					} else {
						if (hasCache) {
							imageView.setImageBitmap(cache);
						} else {
							imageView.setImageBitmap(defaultAvatar);
						}
					}
				}
			};
			callback.memCache(false);
			callback.fileCache(false);
			callback.targetWidth(FragmentNSA.AVATAR_TARGET_WIDTH);
			callback.url(avatarUrl);
			callback.imageView(aq.getImageView());
			callback.preset(hasCache ? cache : defaultAvatar);
			aq.image(callback);
		}

		if (BitmapAjaxCallback.getMemoryCached(friendKey, FragmentNSA.AVATAR_TARGET_WIDTH) != null) {
			imageView.setImageBitmap(BitmapAjaxCallback.getMemoryCached(friendKey, FragmentNSA.AVATAR_TARGET_WIDTH));
		} else {
			aq.image(new File(db.getFriendAvatarUrl(userKey, Long.toString(photo.fromId))), true,
					FragmentNSA.AVATAR_TARGET_WIDTH, new BitmapAjaxCallback() {
						@Override
						protected void callback(String url, ImageView fileIv, final Bitmap fileBmp, AjaxStatus status) {
							aq.id(imageView).image(photo.fromAvatarUrl.replace("https", "http"), false, false,
									FragmentNSA.AVATAR_TARGET_WIDTH, 0, new BitmapAjaxCallback() {
										@Override
										protected void callback(String url, ImageView networkIv, Bitmap bmp,
												AjaxStatus status) {
											if (bmp != null) {
												if (checkAvatarTimestamp(url, friendKey)) {
													this.memPut(friendKey, bmp);
												}
												db.saveAvatarAsync(userKey, friendKey, bmp);
												Drawable target = new BitmapDrawable(networkIv.getResources(), bmp);
												Drawable preset = new BitmapDrawable(networkIv.getResources(),
														fileBmp == null ? defaultAvatar : fileBmp);
												Drawable[] ds = new Drawable[] { preset, target };
												TransitionDrawable td = new TransitionDrawable(ds);
												td.setCrossFadeEnabled(true);
												td.startTransition(bmp
														.sameAs(fileBmp == null ? defaultAvatar : fileBmp) ? 0 : 300);
												networkIv.setImageDrawable(td);
											}
										}
									}.preset(fileBmp == null ? defaultAvatar : fileBmp));
						}
					}.preset(defaultAvatar));
		}
	}

	private static boolean checkAvatarTimestamp(String url, String friendKey) {
		if (url == null) {
			return false;
		}
		int idx = url.indexOf("uploadTime-");
		if (idx == -1) {
			// time stamp not found
			return false;
		} else {
			long l = 0;
			try {
				l = Long.valueOf(url.substring(idx + 11, idx + 24));
			} catch (NumberFormatException e) {
				return false;
			}
			if (sAvatarTimestamps.containsKey(friendKey)) {
				// already in map, compare time stamp
				long oldTimeStamp = sAvatarTimestamps.get(friendKey);
				if (l > oldTimeStamp) {
					sAvatarTimestamps.put(friendKey, l);
					return true;
				} else {
					return false;
				}
			} else {
				// not yet in cache
				sAvatarTimestamps.put(friendKey, l);
				return true;
			}
		}
	}

	public static void loadPhoto(final AQuery aq, final String userKey, final ReceivedPhotoData photo,
			final ProgressBar progressBar, final ImageView imageView, final DatabaseAdapter db, int targetWidth) {
		aq.id(imageView);
		if (db.photoThumbnailExist(userKey, photo.id)) {
			// use local file
			aq.image(new File(db.getPhotoThumbnailUrl(userKey, photo.id)), false, targetWidth,
					new BitmapAjaxCallback() {
						@Override
						protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
							if (bm != null) {
								int duration = iv.getDrawable() == null ? 300 : 0;
								iv.setImageBitmap(bm);
								AlphaAnimation anim = new AlphaAnimation(0, 1);
								anim.setInterpolator(new DecelerateInterpolator());
								anim.setDuration(duration);
								iv.startAnimation(anim);
							}
						}
					});
		} else {
			// fetch from network
			if (progressBar != null) {
				aq.progress(progressBar);
			}
			aq.image(photo.tn_url.replace("https", "http"), false, false, targetWidth, 0, new BitmapAjaxCallback() {
				@Override
				protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
					if (bm != null) {
						db.savePhotoThumbnailAsync(userKey, photo.id, bm);
						iv.setImageBitmap(bm);
						AlphaAnimation anim = new AlphaAnimation(0, 1);
						anim.setInterpolator(new DecelerateInterpolator());
						anim.setDuration(300);
						iv.startAnimation(anim);
					} else {
						iv.setImageDrawable(null);
						// aq.id(imageView)
						// .progress(progressBar)
						// .image(photo.url.replace("https", "http"),
						// false, false,
						// FragmentNSA.PHOTO_TARGET_WIDTH, 0, new
						// BitmapAjaxCallback() {
						// @Override
						// protected void callback(String url, ImageView
						// iv, Bitmap bm,
						// AjaxStatus status) {
						// if (bm != null) {
						// db.savePhotoThumbnailAsync(userKey, photo.id,
						// bm);
						// iv.setImageBitmap(bm);
						// AlphaAnimation anim = new AlphaAnimation(0,
						// 1);
						// anim.setInterpolator(new
						// DecelerateInterpolator());
						// anim.setDuration(300);
						// iv.startAnimation(anim);
						// } else {
						// iv.setImageDrawable(null);
						// }
						// }
						// });
					}
				}
			});
		}
	}

	private static class ViewHolder {
		public ImageView iv_avatar01, iv_avatar02, iv_avatar03, iv_avatar04, iv_avatar05, iv_avatar06, iv_avatar07,
				iv_avatar08;
		public ImageView iv_delete01, iv_delete02, iv_delete03, iv_delete04, iv_delete05, iv_delete06, iv_delete07,
				iv_delete08;
		public TextView tv_name01, tv_name02, tv_name03, tv_name04, tv_name05, tv_name06, tv_name07, tv_name08;
		public TextView tv_timestamp01, tv_timestamp02, tv_timestamp03, tv_timestamp04, tv_timestamp05, tv_timestamp06,
				tv_timestamp07, tv_timestamp08;
		// public ProgressBar pb_loading01, pb_loading02, pb_loading03,
		// pb_loading04, pb_loading05, pb_loading06,
		// pb_loading07, pb_loading08;
		public ImageView iv_photo01, iv_photo02, iv_photo03, iv_photo04, iv_photo05, iv_photo06, iv_photo07,
				iv_photo08;
	}

	public static class PhotoGroup {
		public ReceivedPhotoData photo01, photo02, photo03, photo04, photo05, photo06, photo07, photo08;
		public String userKey;

		public PhotoGroup(String userKey, ReceivedPhotoData photo01, ReceivedPhotoData photo02,
				ReceivedPhotoData photo03, ReceivedPhotoData photo04, ReceivedPhotoData photo05,
				ReceivedPhotoData photo06, ReceivedPhotoData photo07, ReceivedPhotoData photo08) {
			this.userKey = userKey;
			this.photo01 = photo01;
			this.photo02 = photo02;
			this.photo03 = photo03;
			this.photo04 = photo04;
			this.photo05 = photo05;
			this.photo06 = photo06;
			this.photo07 = photo07;
			this.photo08 = photo08;
		}
	}

	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy/MM/dd");

	/**
	 * 
	 * @param l
	 *            photo create time from server
	 * @return
	 */
	public static String getEllapsedTimeString(long l) {
		long interval = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTimeInMillis() - l;
		String format;
		String s;
		if (interval > TimeUnit.DAYS.toMillis(1)) {
			// it's been a day, just show create date
			Date d = new Date(l);
			s = mDateFormat.format(d);
		} else if (interval > TimeUnit.HOURS.toMillis(1)) {
			long hour = TimeUnit.MILLISECONDS.toHours(interval);
			if (hour > 1) {
				format = "%d hours";
			} else {
				format = "%d hour";
			}
			s = String.format(format, hour);
		} else if (interval > TimeUnit.MINUTES.toMillis(1)) {
			long minute = TimeUnit.MILLISECONDS.toMinutes(interval);
			if (minute > 1) {
				format = "%d minutes";
			} else {
				format = "%d minute";
			}
			s = String.format(format, minute);
		} else if (interval > TimeUnit.SECONDS.toMillis(1)) {
			long second = TimeUnit.MILLISECONDS.toSeconds(interval);
			if (second > 1) {
				format = "%d seconds";
			} else {
				format = "%d second";
			}
			s = String.format(format, second);
		} else if (interval >= 0) {
			return "just now";
		} else {
			// must be negative interval, just show date
			Date d = new Date(l);
			s = mDateFormat.format(d);
		}
		return s;
	}
}