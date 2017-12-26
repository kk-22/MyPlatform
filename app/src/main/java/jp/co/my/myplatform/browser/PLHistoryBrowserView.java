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
	protected void willChangePage(String title, String url, boolean isFinished) {
		// TODO: delete
		MYLogUtil.outputLog("willChangePage isFinish=" +isFinished +" " +url);

		if (isFinished && url.equals(mLoadingUrl)) {
			// リダイレクト以外の読み込み完了時は履歴更新なし
			mLoadingUrl = null;
			return;
		}
		mLoadingUrl = url;

		if (mHistoryIndex == -1 || !mUrlHistories.get(mHistoryIndex).equals(url)) {
			// 進む戻るボタンによる移動以外のケースは履歴更新
			if (isFinished) {
				// リダイレクトでURLが変更されたため最後のURLを変更
				mUrlHistories.set(mHistoryIndex, url);
			} else {
				//  新ページ読み込みは mHistoryIndex より先の履歴を削除
				mUrlHistories.removeToLastFromIndex(mHistoryIndex + 1);
				mUrlHistories.add(url);
				mHistoryIndex++;
			}
		}
		MYLogUtil.saveObject(KEY_URL_HISTORIES, mUrlHistories, false)
				.putInt(KEY_URL_INDEX, mHistoryIndex)
				.apply();
	}

	private void loadFirstPage() {
		if (mUrlHistories.size() == 0) {
			mUrlHistories.add("https://www.google.co.jp");
			mHistoryIndex = 0;
		}
		String url = mUrlHistories.get(mHistoryIndex);
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

		MYLogUtil.outputLog("load next url " +nextUrl);

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
