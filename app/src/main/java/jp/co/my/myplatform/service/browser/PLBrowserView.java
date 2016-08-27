package jp.co.my.myplatform.service.browser;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLBrowserView extends PLNavigationView {
	private PLWebView mCurrentWebView;
	private ProgressBar mProgressBar1, mProgressBar2;
	private FrameLayout mContentLayout;
//	private ArrayList<PLPopoverView> mCoverViewArray;

	public PLBrowserView() {
		super();
//		mCoverViewArray = new ArrayList<>();

		LayoutInflater.from(getContext()).inflate(R.layout.navigation_browser, this);
		mProgressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		mContentLayout = (FrameLayout) findViewById(R.id.content_frame);

		mCurrentWebView = (PLWebView) findViewById(R.id.su_web_view);

		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				updateProgressBar(mProgressBar1, progress);
				updateProgressBar(mProgressBar2, progress);

				if (progress == 100) {
					// TODO: 仮で保存
					List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
							.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
							.queryList();
					PLWebPageModel model = new PLWebPageModel(mCurrentWebView.getTitle(), mCurrentWebView.getUrl(), null);
					model.save();
					for (PLWebPageModel pageModel : pageArray) {
						pageModel.delete();
					}
				}
			}
		});
		mCurrentWebView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					onBackKey(v);
					return true;
				}
				return false;
			}
		});

		// TODO:仮で読み込み
		PLWebPageModel bookmark = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.querySingle();
		if (bookmark == null) {
			mCurrentWebView.loadUrl("http://news.yahoo.co.jp/");
		} else {
			mCurrentWebView.loadUrl(bookmark.getUrl());
		}

		findViewById(R.id.search_edit).setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					onBackKey(v);
					return true;
				}
				if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
					// キーボードを閉じて検索
					InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
					inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);

					EditText editText = (EditText) v;
					String inputStr = editText.getText().toString();
					editText.setText("");
					if (inputStr.length() == 0) {
						return false;
					}
					String urlStr = "https://www.google.co.jp/search?q=" + inputStr;
					mCurrentWebView.loadUrl(urlStr);
					return true;
				}
				return false;
			}
		});
//		findViewById(R.id.bookmark_button).setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				PLBookmarkList list = new PLBookmarkList(getContext(), v, PLBrowserView.this);
//				addOverCoverView(list);
//			}
//		});
 		findViewById(R.id.function_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLBrowserFunctionList list = new PLBrowserFunctionList(getContext(), v, PLBrowserView.this);
				addPopover(list);
			}
		});
	}

	private void onBackKey(View view) {
		if (removeTopPopover()) {
			// 子ビューがあったときのみWebView戻る処理を行わない
			return;
		}

		if (mCurrentWebView.canGoBack()) {
			mCurrentWebView.goBack();
		} else {
			MYLogUtil.showToast("can not go back");
		}
	}

	public PLWebView getCurrentWebView() {
		return mCurrentWebView;
	}

	private void updateProgressBar(ProgressBar progressBar, int progress) {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(progress);
		if (progress == 100) {
			progressBar.setVisibility(View.GONE);
		}
	}
}
