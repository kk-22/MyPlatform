package jp.co.my.myplatform.browser;

import android.webkit.WebBackForwardList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;

public class PLHistoryBrowserContent extends PLBaseBrowserContent {

	public static final String KEY_URL_HISTORIES = "KEY_URL_HISTORIES";
	public static final String KEY_URL_INDEX = "KEY_URL_INDEX";
	public static final String KEY_SCRIPT_ENABLED = "KEY_SCRIPT_ENABLED";

	private int mHistoryIndex; // 現在表示中の mUrlHistories の位置を指す
	private MYArrayList<String> mUrlHistories; // 前回アプリ起動中の履歴
	private boolean mIsLoading; // リダイレクト判定用

	public PLHistoryBrowserContent() {
		super();
		mUrlHistories = MYLogUtil.loadArrayList(KEY_URL_HISTORIES);
		if (mUrlHistories == null) {
			mUrlHistories = new MYArrayList<>();
			mHistoryIndex = -1;
		} else {
			int savedIndex = MYLogUtil.getPreference().getInt(KEY_URL_INDEX, mUrlHistories.size() - 1);
			mHistoryIndex = Math.min(mUrlHistories.size() - 1, savedIndex);
			boolean scriptEnabled =  MYLogUtil.getPreference().getBoolean(KEY_SCRIPT_ENABLED, true);
			getCurrentWebView().getSettings().setJavaScriptEnabled(scriptEnabled);
		}

		loadFirstPage();
	}

	@Override
	protected void willLoadPage(String url) {
		if (!mIsLoading) {
			// 読み込み終了後のリンクタップ
			return;
		}
		String currentUrl = getCurrentWebView().getUrl();
		if (mUrlHistories.get(mHistoryIndex).equals(currentUrl)) {
			// 読み込み完了前のリンクタップ
			return;
		}
		// リダイレクト時のURLを履歴から除くためにindexをずらす
		mHistoryIndex = Math.max(0, mHistoryIndex - 1);
		mIsLoading = false;
	}

	@Override
	protected void willChangePage(String title, String url, boolean isFinished) {
		mIsLoading = !isFinished;
		if (isFinished) {
			// 読み込み完了時は履歴更新なし
			return;
		}

		PLWebView webView = getCurrentWebView();
		if (mHistoryIndex == -1 || !mUrlHistories.get(mHistoryIndex).equals(url)) {
			String currentUrl = webView.getUrl();
			if (mHistoryIndex >= 0
					&& !url.equals(currentUrl)
					&& !mUrlHistories.get(mHistoryIndex).equals(currentUrl)) {
				// google検索結果画面からページを開いた際のurlが検索結果のurlである問題用
				addToHistory(currentUrl);
			}
			addToHistory(url);
		}
		MYLogUtil.saveObject(KEY_URL_HISTORIES, mUrlHistories, false)
				.putInt(KEY_URL_INDEX, mHistoryIndex)
				.putBoolean(KEY_SCRIPT_ENABLED, webView.getSettings().getJavaScriptEnabled())
				.apply();
	}

	@Override
	String[] historyTitles() {
		int size = mUrlHistories.size();
		String[] titles = new String[size];
		for (int i = 0; i < size; i++) {
			String title = mUrlHistories.get(i);
			if (i == mHistoryIndex) {
				titles[i] = "■＞" +title;
			} else {
				titles[i] = title;
			}
		}
		return titles;
	}

	@Override
	void loadHistoryOfIndex(int index) {
		mHistoryIndex = index;
		loadCurrentHistoryUrl();
	}

	private void addToHistory(String url) {
		// 現在のページより先の履歴を削除
		mUrlHistories.removeToLastFromIndex(mHistoryIndex + 1);
		mUrlHistories.add(url);
		mHistoryIndex++;
	}

	private void loadFirstPage() {
		String url;
		if (mUrlHistories.size() == 0) {
			url = "https://news.yahoo.co.jp/";
		} else {
			url = mUrlHistories.get(mHistoryIndex);
		}
		getCurrentWebView().loadUrl(url);
	}

	@Override
	protected boolean canGoHistory(boolean isBack) {
		if (isBack) {
			return (mHistoryIndex > 0);
		}
		return (mHistoryIndex < mUrlHistories.size() - 1);
	}

	@Override
	protected void goHistory(boolean isBack) {
		String currentUrl = getCurrentWebView().getUrl();
		if (!mIsLoading && !mUrlHistories.get(mHistoryIndex).equals(currentUrl)) {
			// google検索結果画面からページを開いた際のurlが検索結果のurlである問題用
			addToHistory(currentUrl);
			if (!canGoHistory(isBack)) {
				// 遷移先が無くなった
				updateArrowButtonImage();
				return;
			}
		}

		if (isBack) {
			mHistoryIndex = Math.max(0, mHistoryIndex - 1);
		} else {
			mHistoryIndex = Math.min(mUrlHistories.size() - 1, mHistoryIndex + 1);
		}
		loadCurrentHistoryUrl();
	}

	private void loadCurrentHistoryUrl() {
		String nextUrl = mUrlHistories.get(mHistoryIndex);

		PLWebView webView = getCurrentWebView();
		webView.stopLoading();

		WebBackForwardList list = getCurrentWebView().copyBackForwardList();
		for (int i = list.getSize() - 1; 0 <= i; i--) {
			String urlString = list.getItemAtIndex(i).getUrl();
			if (nextUrl.equals(urlString)) {
				webView.goBackOrForward(i - list.getCurrentIndex());
				return;
			}
		}
		webView.loadUrl(nextUrl);
	}
}
