package jp.co.my.myplatform.browser;

import android.webkit.WebBackForwardList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;

public class PLHistoryBrowserView extends PLBaseBrowserView {

	public static final String KEY_URL_HISTORIES = "KEY_URL_HISTORIES";
	public static final String KEY_URL_INDEX = "KEY_URL_INDEX";

	private int mHistoryIndex; // 現在表示中の mUrlHistories の位置を指す
	private MYArrayList<String> mUrlHistories; // 前回アプリ起動中の履歴
	private String mLoadingUrl; // onPageStartedの引数のURL
	private boolean mIsLoading; // リダイレクト判定用

	public PLHistoryBrowserView() {
		super();
		mUrlHistories = MYLogUtil.loadArrayList(KEY_URL_HISTORIES);
		if (mUrlHistories == null) {
			mUrlHistories = new MYArrayList<>();
			mHistoryIndex = -1;
		} else {
			int savedIndex = MYLogUtil.getPreference().getInt(KEY_URL_INDEX, mUrlHistories.size() - 1);
			mHistoryIndex = Math.min(mUrlHistories.size() - 1, savedIndex);
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
		mLoadingUrl = null;
		mIsLoading = false;
	}

	@Override
	protected void willChangePage(String title, String url, boolean isFinished) {
		mIsLoading = !isFinished;
		if (isFinished) {
			// 読み込み完了時は履歴更新なし
			return;
		}

		if (mHistoryIndex == -1 || !mUrlHistories.get(mHistoryIndex).equals(url)) {
			// 進む戻るボタン以外でのページ移動は、現在のページより先の履歴を削除
			mUrlHistories.removeToLastFromIndex(mHistoryIndex + 1);
			mUrlHistories.add(url);
			mHistoryIndex++;
		}
		MYLogUtil.saveObject(KEY_URL_HISTORIES, mUrlHistories, false)
				.putInt(KEY_URL_INDEX, mHistoryIndex)
				.apply();
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
		if (isBack) {
			mHistoryIndex = Math.max(0, mHistoryIndex - 1);
		} else {
			mHistoryIndex = Math.min(mUrlHistories.size() - 1, mHistoryIndex + 1);
		}
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
