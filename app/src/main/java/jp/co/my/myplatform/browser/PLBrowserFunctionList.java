package jp.co.my.myplatform.browser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLPopoverView;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLBrowserFunctionList extends PLPopoverView {

	private enum LIST_INDEX {
		LIST_INDEX_SCROLL_TOP,
		LIST_INDEX_SCROLL_BOTTOM,
		LIST_INDEX_OPEN_OTHER,
		LIST_INDEX_CLEAR_HISTORIES
	}

	private PLWebView mWebView;
	private PLBaseBrowserContent mBrowserContent;
	private PLWebPageModel mSavedPageModel;

	private ListView mListView;
	private ImageButton mAddBookmarkButton;
	private ImageButton mScriptButton;
	private ImageButton mReloadButton;

	public PLBrowserFunctionList(PLBaseBrowserContent browserContent) {
		super(R.layout.popover_browser_function);
		mBrowserContent = browserContent;
		mWebView = browserContent.getCurrentWebView();

		mSavedPageModel = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_NONE))
				.and(PLWebPageModel_Table.url.eq(mWebView.getUrl()))
				.querySingle();

		String[] titles = {"上までスクロール", "下までスクロール",
				"他のアプリで開く", "履歴クリア"};
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = findViewById(R.id.function_list);
		mListView.setAdapter(adapter);

		mAddBookmarkButton = findViewById(R.id.add_bookmark_button);
		mScriptButton = findViewById(R.id.script_button);
		mReloadButton = findViewById(R.id.reload_button);

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
						removeFromContentView();
						break;
					}
					case LIST_INDEX_SCROLL_BOTTOM: {
						mWebView.setScrollY(9999999);
						removeFromContentView();
						break;
					}
					case LIST_INDEX_OPEN_OTHER: {
						removeFromContentView();
						PLListPopover.showItems(
								new PLListPopover.PLListItem("ブラウザで開く", new Runnable() {
									@Override
									public void run() {
										mBrowserContent.removeTopPopover();
										PLCoreService.getNavigationController().hideNavigationIfNeeded();
										// ブラウザで開く
										Uri uri = Uri.parse(mWebView.getUrl());
										Intent intent = new Intent(Intent.ACTION_VIEW, uri);
										intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										getContext().startActivity(intent);
									}
								}),
								new PLListPopover.PLListItem("URLコピー", new Runnable() {
									@Override
									public void run() {
										ClipboardManager clipboardManager = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
										if (null != clipboardManager) {
											clipboardManager.setPrimaryClip(ClipData.newPlainText("", mWebView.getUrl()));
										}
									}
								}));
						break;
					}
					case LIST_INDEX_CLEAR_HISTORIES: {
						MYLogUtil.getPreferenceEditor()
								.remove(PLHistoryBrowserContent.KEY_URL_HISTORIES)
								.remove(PLHistoryBrowserContent.KEY_URL_INDEX)
								.apply();
						removeFromContentView();
						PLCoreService.getNavigationController().popView();
						break;
					}
				}
			}
		});

		findViewById(R.id.search_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeFromContentView();
				new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
					@Override
					public boolean onEnter(View v, String text) {
						String urlStr;
						if (text.startsWith("http")) {
							urlStr = text;
						} else {
							urlStr = "https://www.google.co.jp/search?q=" + text;
						}
						mWebView.loadUrl(urlStr);
						return true;
					}
				}).showPopover();
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
				removeFromContentView();
			}
		});
		mScriptButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// スクリプトボタン
				boolean nextScriptEnabled = !mWebView.getSettings().getJavaScriptEnabled();
				mWebView.getSettings().setJavaScriptEnabled(nextScriptEnabled);
				mWebView.reload();
				removeFromContentView();
			}
		});
		mReloadButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// リロードボタン
				mWebView.reload();
				removeFromContentView();
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
