package jp.co.my.myplatform.service.browser;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLBookmarkActionView extends PLPopoverView {

	private enum LIST_INDEX {
		LIST_INDEX_EDIT,
		LIST_INDEX_MOVE,
		LIST_INDEX_DELETE,
	}

	private PLWebPageModel mPageModel;
	private PLBookmarkList mBookmarkList;

	public PLBookmarkActionView(Context context, View parentView, PLWebPageModel pageModel, PLBookmarkList bookmarkList) {
		super(context, parentView, R.layout.popover_bookmark_action);
		mPageModel = pageModel;
		mBookmarkList = bookmarkList;

		String[] titles = {"編集", "移動", "削除"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
				R.layout.cell_browser_function,
				titles);

		ListView listView = (ListView) findViewById(R.id.action_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LIST_INDEX index = LIST_INDEX.values()[position];
				switch (index) {
					case LIST_INDEX_EDIT: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case LIST_INDEX_MOVE: {
						MYLogUtil.showToast("未実装");
						break;
					}
					case LIST_INDEX_DELETE: {
						MYLogUtil.showToast("ブックマーク削除：" +mPageModel.getTitle());
						mPageModel.delete();
						mBookmarkList.updateList();
						removeFromNavigation();
						break;
					}
				}
			}
		});
	}
}
