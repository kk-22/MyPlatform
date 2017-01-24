package jp.co.my.myplatform.service.browser;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLWebPageModel;
import jp.co.my.myplatform.service.model.PLWebPageModel_Table;

public class PLHistoryBrowserView extends PLBaseBrowserView {

	public PLHistoryBrowserView() {
		super();
		loadFirstPage();
	}

	@Override
	protected void willChangePage(String title, String url) {
		// TODO: 前のモデルを使いまわすべき
		List<PLWebPageModel> pageArray = SQLite.select().from(PLWebPageModel.class)
				.where(PLWebPageModel_Table.tabNo.eq(PLWebPageModel.TAB_NO_CURRENT))
				.queryList();
		PLWebPageModel model = new PLWebPageModel(title, url, null);
		model.save();
		for (PLWebPageModel pageModel : pageArray) {
			pageModel.delete();
		}
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
}
