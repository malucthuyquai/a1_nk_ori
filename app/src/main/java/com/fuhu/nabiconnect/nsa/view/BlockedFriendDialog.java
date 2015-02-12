package com.fuhu.nabiconnect.nsa.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.fuhu.data.FriendData;
import com.fuhu.nabiconnect.R;

import java.util.ArrayList;
import java.util.List;

public class BlockedFriendDialog extends Dialog {

	final private String TAG = BlockedFriendDialog.class.getSimpleName();

	private ArrayList<FriendData> mFriends = new ArrayList<FriendData>();

	private ListView mListView;
	private ListViewAdapter mListViewAdapter;

	private View.OnClickListener mUnblockCallback;

	public BlockedFriendDialog(Context context, View.OnClickListener unblockCallback) {
		super(context, R.style.Theme_GeneralCustomDialog);
		setContentView(R.layout.dialog_blocked_friend);
		setCanceledOnTouchOutside(false);
		findViewById(R.id.ll_root).setOnClickListener(dismiss_ocl);
		findViewById(R.id.btn_ok).setOnClickListener(ok_ocl);

		mListViewAdapter = new ListViewAdapter(context, 0, mFriends);
		mListView = (ListView) findViewById(R.id.lv_blocked_friend);
		mListView.setAdapter(mListViewAdapter);
		mUnblockCallback = unblockCallback;
	}

	public void setFriendList(ArrayList<FriendData> friendList) {
		mFriends.clear();
		mFriends.addAll(friendList);
		mListViewAdapter.notifyDataSetChanged();
	}

	public void removeFriend(FriendData fd) {
		mFriends.remove(fd);
		if (mFriends.size() == 0) {
			this.dismiss();
		}
		mListViewAdapter.notifyDataSetChanged();
	}

	private View.OnClickListener ok_ocl = new View.OnClickListener() {
		public void onClick(View v) {
			BlockedFriendDialog.this.dismiss();
		}
	};

	private View.OnClickListener dismiss_ocl = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			BlockedFriendDialog.this.dismiss();
		}
	};

	private class ListViewAdapter extends ArrayAdapter<FriendData> {

		private LayoutInflater inflater;

		public ListViewAdapter(Context context, int textViewResourceId, List<FriendData> objects) {
			super(context, textViewResourceId, objects);
			inflater = LayoutInflater.from(context);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.item_blocked_friend, parent, false);
				holder = new ViewHolder();
				holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
				holder.btn_unblock = (Button) convertView.findViewById(R.id.btn_unblock);
				holder.btn_unblock.setText(R.string.unblock);
				holder.btn_unblock.setOnClickListener(mUnblockCallback);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_name.setText(getItem(position).userName);
			holder.btn_unblock.setTag(getItem(position));
			return convertView;
		}
	}

	static class ViewHolder {
		public TextView tv_name;
		public Button btn_unblock;
	}
}