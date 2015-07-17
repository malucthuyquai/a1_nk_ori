package com.fuhu.nabiconnect.nsa.fragment;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.fuhu.nabiconnect.R;
import com.fuhu.nabiconnect.Tracking;
import com.fuhu.nabiconnect.nsa.util.NSAUtil;
import com.fuhu.nabiconnect.utils.DatabaseAdapter;

public class FragmentMailViewer extends Tracking.TrackingInfoFragment {

	private final String TAG = FragmentMailViewer.class.getSimpleName();

	private AQuery aq;
	private ImageView iv_content;

	final private RectF mRect = new RectF();
	final private float[] mMatrix = new float[9];

	private DatabaseAdapter db;

    public FragmentMailViewer() {
        super(FragmentMailViewer.class.getSimpleName());
    }

    @Override
    public String getTrack() {
        return null;
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.nsa_fragment_mail_viewer, null);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		aq = new AQuery(view);
//		iv_content = (ImageView) view;
        iv_content = (ImageView) view.findViewById(R.id.nsa_f_mail_viewer_image); //jack@150707 fixed for tracking modify
		iv_content.setOnTouchListener(otl);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		db = DatabaseAdapter.getInstance(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		refreshTouchArea();
	}

	private View.OnTouchListener otl = new View.OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getActionMasked();
			switch (action) {
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (mRect.contains(event.getX(), event.getY())) {
					;
				} else {
					getActivity().onBackPressed();

                    //tracking
                    Tracking.pushTrack(v.getContext(), "dialog_mail_content_close");
				}
			}
			return true;
		}
	};

	public void setData(String userKey, String mailId, String url) {
		int targetWidth = NSAUtil.getDeviceWidth(getActivity());
		Bitmap bmp = null;
		if (db.mailThumbnailExist(userKey, mailId)) {
			bmp = db.getMailThumbnail(userKey, mailId);
		}

		aq.clear().image(url.replace("https", "http"), true, false, targetWidth, 0, new BitmapAjaxCallback() {
			@Override
			protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
				if (bm != null) {
					Drawable preset = iv.getDrawable();
					if (preset != null) {
						// already showing thumbnail
						Drawable target = new BitmapDrawable(iv.getResources(), bm);
						Drawable[] ds = new Drawable[] { preset, target };
						TransitionDrawable td = new TransitionDrawable(ds);
						td.setCrossFadeEnabled(false);
						td.startTransition(300);
						iv.setImageDrawable(td);
					} else {
						iv.setImageBitmap(bm);
						AlphaAnimation anim = new AlphaAnimation(0, 1);
						anim.setInterpolator(new DecelerateInterpolator());
						anim.setDuration(300);
						iv.startAnimation(anim);
					}
					refreshTouchArea();
				} else {
					mRect.set(0, 0, 0, 0);
					aq.clear();
				}
			}
		}.preset(bmp));
	}

	/**
	 * makes area covered by image unresponsive to touch event
	 */
	private void refreshTouchArea() {
		BitmapDrawable bd = null;
		Drawable d = iv_content.getDrawable();
		if (d != null) {
			if (d instanceof TransitionDrawable) {
				TransitionDrawable td = (TransitionDrawable) d;
				bd = (BitmapDrawable) td.getDrawable(1);
			} else {
				bd = (BitmapDrawable) iv_content.getDrawable();
			}
		}
		// get image view center point
		int w2 = iv_content.getWidth() / 2;
		int h2 = iv_content.getHeight() / 2;
		// get bitmap center point
		int iw2 = bd == null ? w2 : getImageWidth(bd) / 2;
		int ih2 = bd == null ? h2 : getImageHeight(bd) / 2;
		mRect.set(w2 - iw2, h2 - ih2, w2 + iw2, h2 + ih2);
	}

	private int getImageWidth(BitmapDrawable bd) {
		if (bd != null) {
			Bitmap bmp = bd.getBitmap();
			iv_content.getImageMatrix().getValues(mMatrix);
			if (bmp != null) {
				return Math.round(bmp.getWidth() * mMatrix[Matrix.MSCALE_X]);
			}
		}
		return iv_content.getWidth();
	}

	private int getImageHeight(BitmapDrawable bd) {
		if (bd != null) {
			Bitmap bmp = bd.getBitmap();
			iv_content.getImageMatrix().getValues(mMatrix);
			if (bmp != null) {
				return Math.round(bmp.getHeight() * mMatrix[Matrix.MSCALE_Y]);
			}
		}
		return iv_content.getHeight();
	}
}