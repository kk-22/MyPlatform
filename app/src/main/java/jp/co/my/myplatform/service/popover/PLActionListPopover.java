package jp.co.my.myplatform.service.popover;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import jp.co.my.myplatform.R;

public class PLActionListPopover<T> extends PLPopoverView {

	private ListView mListView;
	private PLActionListListener<T> mListener;
	private List<String> mTitleList;
	private List<T> mObjectList;

	public PLActionListPopover(List<String> titleList, List<T> objectList, PLActionListListener<T> listener) {
		super(R.layout.popover_action_list);
		mTitleList = titleList;
		mObjectList = objectList;
		mListener = listener;
		mListView = (ListView) findViewById(R.id.list);

		initClickEvent();
		updateList();
	}

	public void removeObject(T ignoreObject) {
		int index = mObjectList.indexOf(ignoreObject);
		mObjectList.remove(index);
		mTitleList.remove(index);
		updateList();
	}

	public void updateList() {
		// TODO: 全件表示中。tabNo == -1を表示に変更
		PLActionListAdapter adapter = new PLActionListAdapter(getContext());
		adapter.addAll(mTitleList);
		mListView.setAdapter(adapter);
	}

	private void initClickEvent() {
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				T object = mObjectList.get(position);
				mListener.onItemClick(object, PLActionListPopover.this);
			}
		});

		findViewById(R.id.close_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFromContentView();
			}
		});
	}

	private class PLActionListAdapter extends ArrayAdapter<String> {

		private LayoutInflater mInflater;

		private PLActionListAdapter(Context context) {
			super(context, 0);
			mInflater = LayoutInflater.from(context);
		}

		@NonNull
		@Override
		public View getView(int position, View convertView, @NonNull ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.cell_action, parent, false);
			}
			String title = getItem(position);
			((TextView) convertView.findViewById(R.id.title_text)).setText(title);

			final T object = mObjectList.get(position);
			convertView.findViewById(R.id.action_button).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					mListener.onActionClick(object, PLActionListPopover.this, v);
				}
			});
			return convertView;
		}
	}

	public interface PLActionListListener<T> {
		// Listのセル押下イベント
		void onItemClick(T object, PLActionListPopover listPopover);
		// セルのアクションボタン押下イベント
		void onActionClick(T object, PLActionListPopover<T> listPopover, View buttonView);
	}
}
