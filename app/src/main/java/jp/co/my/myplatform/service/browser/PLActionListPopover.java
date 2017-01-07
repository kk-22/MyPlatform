package jp.co.my.myplatform.service.browser;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;
import jp.co.my.myplatform.service.popover.PLListPopover;
import jp.co.my.myplatform.service.popover.PLPopoverView;
import jp.co.my.myplatform.service.layout.PLRelativeLayoutController;

public class PLActionListPopover extends PLPopoverView {

	private PLBrowserView mBrowserView;

	private ListView mListView;

	public PLActionListPopover(PLBrowserView browserView) {
		super(R.layout.popover_action_list);
		mBrowserView = browserView;

		mListView = (ListView) findViewById(R.id.list);

		initClickEvent();
		updateList();
	}

	public void updateList() {
		// TODO: 全件表示中。tabNo == -1を表示に変更
		PLActionListAdapter adapter = new PLActionListAdapter(getContext(), this);
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.bookmarkDirectoryNo.greaterThanOrEq(PLWebPageModel.BOOKMARK_DIRECTORY_NO_ROOT))
				.queryList();
		adapter.addAll(pageArray);
		mListView.setAdapter(adapter);
	}

	public void displayActionView(final PLWebPageModel pageModel, View buttonView) {
		String[] titles = {"編集", "移動", "削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				switch (position) {
					case 0: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case 1: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case 2: {
						MYLogUtil.showToast("ブックマーク削除：" +pageModel.getTitle());
						pageModel.delete();
						PLActionListPopover.this.updateList();
						removeFromContentView();
						break;
					}
				}
			}
		}).showPopover(new PLRelativeLayoutController(buttonView));
	}

	private void initClickEvent() {
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ListView listView = (ListView) parent;
				PLWebPageModel pageModel = (PLWebPageModel) listView.getItemAtPosition(position);
				mBrowserView.getCurrentWebView().loadPageModel(pageModel);

				removeFromContentView();
			}
		});

		findViewById(R.id.close_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFromContentView();
			}
		});
	}
}
