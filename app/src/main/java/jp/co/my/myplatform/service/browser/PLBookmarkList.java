package jp.co.my.myplatform.service.browser;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLBookmarkList extends PLPopoverView {

	private PLBrowserView mBrowserView;

	private ListView mListView;

	public PLBookmarkList(View parentView, PLBrowserView browserView) {
		super(parentView, R.layout.popover_bookmark_list);
		mBrowserView = browserView;

		mListView = (ListView) findViewById(R.id.bookmark_list);

		initClickEvent();
		updateList();
	}

	public void updateList() {
		// TODO: 全件表示中。tabNo == -1を表示に変更
		PLBookmarkAdapter adapter = new PLBookmarkAdapter(getContext(), this);
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.bookmarkDirectoryNo.greaterThanOrEq(PLWebPageModel.BOOKMARK_DIRECTORY_NO_ROOT))
				.queryList();
		adapter.addAll(pageArray);
		mListView.setAdapter(adapter);
	}

	public void displayActionView(PLWebPageModel pageModel, View buttonView) {
		PLBookmarkActionView actionView = new PLBookmarkActionView(buttonView, pageModel, this);
		mBrowserView.addPopover(actionView);
	}

	private void initClickEvent() {
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				PLWebPageModel pageModel = (PLWebPageModel) listView.getItemAtPosition(position);
				mBrowserView.getCurrentWebView().loadPageModel(pageModel);

				removeFromNavigation();
			}
		});

		findViewById(R.id.close_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFromNavigation();
			}
		});
	}
}
