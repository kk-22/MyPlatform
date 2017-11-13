package jp.co.my.myplatform.service.browser;

import android.webkit.WebBackForwardList;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;

public class PLHistoryBrowserView extends PLBaseBrowserView {

	public static final String KEY_URL_HISTORIES = "KEY_URL_HISTORIES";

	/**
	 * 現在表示中の mUrlHistories の位置を指す
	 * -1 : 履歴なし
	 * mUrlHistories.size() : 履歴を表示していない
	 */
	private int mHistoryIndex;
	private MYArrayList<String> mUrlHistories; // 前回アプリ起動中の履歴

	public PLHistoryBrowserView() {
		super();
		mUrlHistories = MYLogUtil.loadArrayList(KEY_URL_HISTORIES);
		if (mUrlHistories == null) {
			mUrlHistories = new MYArrayList<>();
			mHistoryIndex = -1;
		} else {
			// 最後の履歴はloadFirstPageメソッドで開くページと被るため取り除く
			mUrlHistories.removeLast();
			mHistoryIndex = mUrlHistories.size(); // 最大値より1多い数
		}

		loadFirstPage();
	}

	@Override
	protected void willChangePage(String title, String url, boolean isFinished) {
		// TODO: 前のモデルを使いまわすべき
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.queryList();
		PLWebPageModel model = new PLWebPageModel(title, url, null);
		model.save();
		for (PLWebPageModel pageModel : pageArray) {
			pageModel.delete();
		}
		if (isFinished) {
			// 読み込み開始時のみ履歴保存処理を行う
			return;
		}

		if (0 <= mHistoryIndex && mHistoryIndex < mUrlHistories.size() && !mUrlHistories.get(mHistoryIndex).equals(url)) {
			// 戻る・進むボタン以外のページ読み込みは mHistoryIndex より先の履歴を削除
			mUrlHistories.removeToLastFromIndex(mHistoryIndex + 1);
			mHistoryIndex++;
		}

		MYArrayList<String> saveUrls = new MYArrayList<>(mUrlHistories);
		WebBackForwardList list = getCurrentWebView().copyBackForwardList() ;
		for (int i = 0 ; i < list.getSize(); i ++) {
			String urlString = list.getItemAtIndex(i).getUrl() ;
			saveUrls.add(urlString);
		}
		MYLogUtil.saveObject(saveUrls, KEY_URL_HISTORIES);
	}

	private void loadFirstPage() {
		PLModelContainer<PLWebPageModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.limit(1));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLWebPageModel>() {
			@Override
			public void onLoad(List<PLWebPageModel> modelLists) {
				if (modelLists.size() == 0) {
					getCurrentWebView().loadUrl("http://news.yahoo.co.jp/");
					return;
				}
				getCurrentWebView().loadUrl(modelLists.get(0).getUrl());
			}
		});
	}

	@Override
	protected boolean canGoHistory(boolean isBack) {
		boolean result = super.canGoHistory(isBack);
		if (result) {
			return result;
		}

		if (isBack) {
			return (mHistoryIndex > 0);
		}
		return (mHistoryIndex < mUrlHistories.size() - 1);
	}

	@Override
	protected void goHistory(boolean isBack) {
		if (super.canGoHistory(isBack)) {
			super.goHistory(isBack);
			return;
		}

		if (isBack) {
			mHistoryIndex--;
		} else {
			mHistoryIndex++;
		}
		getCurrentWebView().clearHistory();
		getCurrentWebView().loadUrl(mUrlHistories.get(mHistoryIndex));
	}
}
