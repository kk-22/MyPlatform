package jp.co.my.myplatform.service.browser;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;
import jp.co.my.myplatform.service.popover.PLPopoverView;

public class PLBrowserFunctionList extends PLPopoverView {

	private enum LIST_INDEX {
		LIST_INDEX_SCROLL_TOP,
		LIST_INDEX_SCROLL_BOTTOM,
		LIST_INDEX_OPEN_BROWSER,
		LIST_INDEX_RELEASE_BROWSER;
	}

	private PLWebView mWebView;
	private PLBrowserView mBrowserView;
	private PLWebPageModel mSavedPageModel;

	private ListView mListView;
	private ImageButton mAddBookmarkButton;
	private ImageButton mScriptButton;
	private ImageButton mReloadButton;

	public PLBrowserFunctionList(View parentView, PLBrowserView browserView) {
		super(parentView, R.layout.popover_browser_function);
		mBrowserView = browserView;
		mWebView = browserView.getCurrentWebView();

		mSavedPageModel = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_NONE))
				.and(PLWebPageModel_Table.url.eq(mWebView.getUrl()))
				.querySingle();

		String[] titles = {"上までスクロール", "下までスクロール", "ブラウザで開く", "インスタンス破棄"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = (ListView) findViewById(R.id.function_list);
		mListView.setAdapter(adapter);

		mAddBookmarkButton = (ImageButton) findViewById(R.id.add_bookmark_button);
		mScriptButton = (ImageButton) findViewById(R.id.script_button);
		mReloadButton = (ImageButton) findViewById(R.id.reload_button);

		initClickEvent();
		updateAddBookmarkImage();
		updateScriptImage();
	}

	private void initClickEvent() {
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				LIST_INDEX index = LIST_INDEX.values()[position];
				switch (index) {
					case LIST_INDEX_SCROLL_TOP: {
						mWebView.setScrollY(0);
						removeFromNavigation();
						break;
					}
					case LIST_INDEX_SCROLL_BOTTOM: {
						mWebView.setScrollY(9999999);
						removeFromNavigation();
						break;
					}
					case LIST_INDEX_OPEN_BROWSER: {
						//ブラウザで開く
						Uri uri = Uri.parse(mWebView.getUrl());
						Intent intent = new Intent(Intent.ACTION_VIEW, uri);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getContext().startActivity(intent);
						break;
					}
					case LIST_INDEX_RELEASE_BROWSER: {
						MYLogUtil.showToast("No action");
						break;
					}
				}
			}
		});

		mAddBookmarkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// ブックマーク追加
				if (mSavedPageModel == null) {
					mSavedPageModel = new PLWebPageModel(mWebView.getTitle(), mWebView.getUrl(), PLWebPageModel.BOOKMARK_DIRECTORY_NO_ROOT);
					mSavedPageModel.setEnableScript(mWebView.getSettings().getJavaScriptEnabled());
					mSavedPageModel.save();
					MYLogUtil.showToast("ブックマーク追加；" + mSavedPageModel.getTitle());
				} else {
					MYLogUtil.showToast("ブックマーク削除：" + mSavedPageModel.getTitle());
					mSavedPageModel.delete();
					mSavedPageModel = null;
				}
				removeFromNavigation();
			}
		});
		mScriptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// スクリプトボタン
				boolean nextScriptEnabled = !mWebView.getSettings().getJavaScriptEnabled();
				mWebView.getSettings().setJavaScriptEnabled(nextScriptEnabled);
				mWebView.reload();
				removeFromNavigation();
			}
		});
		mReloadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// リロードボタン
				mWebView.reload();
				removeFromNavigation();
			}
		});
	}

	private void updateAddBookmarkImage() {
		if (mSavedPageModel == null) {
			mAddBookmarkButton.setBackgroundResource(R.drawable.add_favorite);
		} else {
			mAddBookmarkButton.setBackgroundResource(R.drawable.added_favorite);
		}
	}

	private void updateScriptImage() {
		if (mWebView.getSettings().getJavaScriptEnabled()) {
			mScriptButton.setBackgroundResource(R.drawable.script_on);
		} else {
			mScriptButton.setBackgroundResource(R.drawable.script_off);
		}
	}
}
