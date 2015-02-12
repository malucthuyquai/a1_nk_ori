package com.fuhu.nabiconnect.nsa.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.account.data.Kid;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.friend.avatar.GraphicsUtil;

import java.util.ArrayList;

public class ImageAdapter extends BaseAdapter {

	final private String TAG = ImageAdapter.class.getSimpleName();
	final private int FADE_DUR = 300;
	/** 0 for no down sampling */
	final private int KID_PHOTO_TARGET_WIDTH = 400;

	private ArrayList<Kid> mKids;

	private LayoutInflater inflater;
	private int mSelectedIdx;

	private float mEdgeLong, mEdgeShort;
	private int mStrokeWidth, mPadding;

	private Bitmap mDefaultAvatar;

	public ImageAdapter(Context context, ArrayList<Kid> kids) {
		inflater = LayoutInflater.from(context);
		mKids = kids;
		mEdgeLong = Math.round(context.getResources().getDisplayMetrics().heightPixels * .2875f);
		mEdgeShort = Math.round(context.getResources().getDisplayMetrics().heightPixels * .1833f);
		mStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.nsa_avatar_ring_width);
		mPadding = context.getResources().getDimensionPixelSize(R.dimen.nsa_avatar_ring_padding);
		mDefaultAvatar = GraphicsUtil.getCircleBitmap(
				BitmapFactory.decodeResource(context.getResources(), R.drawable.nsa_default_avatar), (int) mEdgeLong,
				mStrokeWidth, mPadding);
	}

	@Override
	public int getCount() {
		return mKids.size();
	}

	@Override
	public Kid getItem(int position) {
		return mKids.get(position);
	}

	public int getSelectedIdx() {
		return mSelectedIdx;
	}

	public void setSelectItem(int selectIdx) {
		if (mSelectedIdx != selectIdx) {
			mSelectedIdx = selectIdx;
			notifyDataSetChanged();
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.nsa_item_avatar, null);
			holder = new ViewHolder();
			holder.aq = new AQuery(convertView);
			holder.iv_avatar = holder.aq.id(R.id.iv_avatar).getImageView();
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
			holder.aq.ajaxCancel();
			holder.aq.recycle(convertView);
		}

		final Kid k = getItem(position);

		String url = k.getKidPhotos().getNum128();
		if (url == null || url.isEmpty()) {
			url = k.getThumbnails().getNum128();
		}

		if (url == null || url.isEmpty()) {
			holder.iv_avatar.setImageBitmap(mDefaultAvatar);
		} else {
			// using url
			Bitmap bmp = BitmapAjaxCallback.getMemoryCached(url.replace("https", "http"), KID_PHOTO_TARGET_WIDTH);
			if (bmp != null) {
				// if cached image available
				holder.iv_avatar.setImageBitmap(bmp);
			} else {
				holder.aq.id(holder.iv_avatar).image(url.replace("https", "http"), false, false,
						KID_PHOTO_TARGET_WIDTH, 0, new BitmapAjaxCallback() {
							@Override
							protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
								if (bm != null) {
									Bitmap circle = GraphicsUtil.getCircleBitmap(bm, (int) mEdgeLong, mStrokeWidth,
											mPadding);
									this.memPut(url, circle);
									Drawable target = new BitmapDrawable(iv.getResources(), circle);
									Drawable preset = new BitmapDrawable(iv.getResources(), mDefaultAvatar);
									Drawable[] ds = new Drawable[] { preset, target };
									TransitionDrawable td = new TransitionDrawable(ds);
									td.setCrossFadeEnabled(true);
									td.startTransition(FADE_DUR);
									iv.setImageDrawable(td);
								} else {
									iv.setImageBitmap(mDefaultAvatar);
								}
							}
						}.preset(mDefaultAvatar));
			}
		}

		// set layout parameter
		if (mSelectedIdx == position) {
			holder.iv_avatar.setLayoutParams(new LinearLayout.LayoutParams((int) mEdgeLong, (int) mEdgeLong));
		} else {
			holder.iv_avatar.setLayoutParams(new LinearLayout.LayoutParams((int) mEdgeShort, (int) mEdgeShort));
			holder.iv_avatar.setColorFilter(Color.parseColor("#55000000"));
		}
		return convertView;
	}

	private static class ViewHolder {
		public ImageView iv_avatar;
		public AQuery aq;
	}
}