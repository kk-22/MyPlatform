package jp.co.my.myplatform.service.browser;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLBrowserView extends PLNavigationView {
	private PLWebView mCurrentWebView;
	private ImageButton mBackButton, mForwardButton, mShowButton;
	private ProgressBar mProgressBar1, mProgressBar2;
	private LinearLayout mToolbar;

	public PLBrowserView() {
		super();
		isKeepCache = true;

		LayoutInflater.from(getContext()).inflate(R.layout.navigation_browser, this);
		mBackButton = (ImageButton) findViewById(R.id.back_button);
		mForwardButton = (ImageButton) findViewById(R.id.forward_button);
		mShowButton = (ImageButton) findViewById(R.id.show_toolbar_button);
		mProgressBar1 = (ProgressBar) findViewById(R.id.progressBar1);
		mProgressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
		mCurrentWebView = (PLWebView) findViewById(R.id.su_web_view);
		mToolbar = (LinearLayout) findViewById(R.id.browser_toolbar);
		initButtonEvent();

		mCurrentWebView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if (mCurrentWebView == null) {
					return;
				}
				updateProgressBar(mProgressBar1, progress);
				updateProgressBar(mProgressBar2, progress);
				updateArrowButtonImage();

				if (progress == 100) {
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

		// TODO:仮で読み込み
		PLWebPageModel bookmark = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.querySingle();
		if (bookmark == null) {
			mCurrentWebView.loadUrl("http://news.yahoo.co.jp/");
		} else {
			mCurrentWebView.loadUrl(bookmark.getUrl());
		}
	}

	@Override
	public void viewWillDisappear() {
		if (mCurrentWebView != null) {
			mCurrentWebView.destroy();
			mCurrentWebView = null;
		}

		super.viewWillDisappear();
	}

	private void onBackKey() {
		if (removeTopPopover()) {
			// 子ビューがあったときのみWebView戻る処理を行わない
			return;
		}

		mCurrentWebView.goBack();
		updateArrowButtonImage();
	}
	private void onForwardKey() {
		mCurrentWebView.goForward();
		updateArrowButtonImage();
	}

	private void updateArrowButtonImage() {
		if (mCurrentWebView.canGoBack()) {
			mBackButton.setBackgroundResource(R.drawable.back_arrow_on);
		} else {
			mBackButton.setBackgroundResource(R.drawable.back_arrow_off);
		}
		if (mCurrentWebView.canGoForward()) {
			mForwardButton.setBackgroundResource(R.drawable.forward_arrow_on);
		} else {
			mForwardButton.setBackgroundResource(R.drawable.forward_arrow_off);
		}
	}

	private void updateProgressBar(ProgressBar progressBar, int progress) {
		progressBar.setVisibility(View.VISIBLE);
		progressBar.setProgress(progress);
		if (progress == 100) {
			progressBar.setVisibility(View.GONE);
		}
	}

	private void initButtonEvent() {
		mCurrentWebView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					onBackKey();
					return true;
				}
				return false;
			}
		});

		mBackButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackKey();
			}
		});
		mForwardButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onForwardKey();
			}
		});
		findViewById(R.id.hide_toolbar_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbar.setVisibility(View.GONE);
				mShowButton.setVisibility(View.VISIBLE);
			}
		});
		findViewById(R.id.bookmark_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLBookmarkList(v, PLBrowserView.this).showPopover();
			}
		});
		findViewById(R.id.function_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLBrowserFunctionList(v, PLBrowserView.this).showPopover();
			}
		});
		findViewById(R.id.show_toolbar_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mToolbar.setVisibility(View.VISIBLE);
				mShowButton.setVisibility(View.GONE);
			}
		});
	}

	public PLWebView getCurrentWebView() {
		return mCurrentWebView;
	}
}
