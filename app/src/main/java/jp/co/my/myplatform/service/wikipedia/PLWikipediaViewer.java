package jp.co.my.myplatform.service.wikipedia;

import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.model.PLModelContainer;

public class PLWikipediaViewer extends PLContentView {

	private TextView mTextView;
	private TextView mLoadingText;
	private PLWikipediaPageModel mCurrentPageModel;

	private Handler mMainHandler;

	public PLWikipediaViewer() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_wikipedia_viewer, this);
		mTextView = (TextView) findViewById(R.id.html_text);
		mLoadingText = (TextView) findViewById(R.id.loading_text);
		mMainHandler = new Handler();

		loadFirstPage();
	}

	private void loadFirstPage() {
		PLModelContainer<PLWikipediaPageModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLWikipediaPageModel.class)
				.orderBy(PLWikipediaPageModel_Table.lastReadDate, false)
				.limit(1));
		container.loadList(new PLModelContainer.PLOnModelLoadThreadListener<PLWikipediaPageModel>() {
			@Override
			public void onLoad(List<PLWikipediaPageModel> modelLists) {
				if (modelLists.size() == 0) {
					// 開発中コメントアウト。modelを複数作らないため
//					String url = "https://ja.wikipedia.org/wiki/%E7%B5%90%E5%9F%8E%E7%A7%80%E5%BA%B7";
//					requestPage(url);
					return;
				}
				loadPageModel(modelLists.get(0));
			}
		}, null);
	}

	private void requestPage(final String url) {
		showIndicator();
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(final String response) {
				new Thread(new Runnable() {
					@Override
					public void run() {
//						MYLogUtil.outputErrorLog("onResponse " + response);
						PLWikipediaPageModel pageModel = new PLWikipediaPageModel();
						pageModel.setTitle("title");
						pageModel.setUrl(url);
						pageModel.setHtml(response);
						pageModel.setRegisteredDate(Calendar.getInstance());
						pageModel.save();

						loadPageModel(pageModel);
					}
				}).start();
			}
		};
		Response.ErrorListener error = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MYLogUtil.outputErrorLog("Fetch page error " + error.toString());
				hideIndicator();
			}
		};
		StringRequest request = new StringRequest(url, listener, error);
		PLVolleyHelper volleyHelper = PLCoreService.getVolleyHelper();
		volleyHelper.addRequest(request, this.getClass());
	}

	private void loadPageModel(PLWikipediaPageModel pageModel) {
		mCurrentPageModel = pageModel;
		parseHtml(pageModel.getHtml());
	}

	private void parseHtml(String baseHtml) {
		MYLogUtil.outputLog("before parse");
		// 余分の削除
		// 改行文字削除
		String htmlStr = baseHtml.replaceAll("\n", "");
		// ヘッダ～BODYタグ冒頭部分まで削除
		htmlStr = htmlStr.replaceFirst(".*<h1 id=\"section_0\"(.*?)>", "");
		// 関連項目以下削除
		htmlStr = htmlStr.replaceFirst("<span[^<]*関連項目.*", "");
		// Javascript 削除
		htmlStr = htmlStr.replaceAll("<script>(.*?)</script>", "");
		// 編集リンク削除
		htmlStr = htmlStr.replaceAll("<a[^>]*>編集</a>", "");

		// HTMLタグの代用
		// li タグを削除して箇条書きに変更
		htmlStr = htmlStr.replaceAll("<li(.*?)>", "・");
		htmlStr = htmlStr.replaceAll("</li>", "<br><br>");

		// CSSの代用
		// 見出しタグの変更
		htmlStr = htmlStr.replaceAll("<h2 class=\"section-heading in-block(.*?)<span(.*?)>(.*?)</span></h2>"
				, "<h1>■$3</1>");
		final String parsedHtml = htmlStr;

		// deprecated のため Android 7 以降で引数にパラメータ追加
		MYLogUtil.outputLog("after parse");

		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				mTextView.setText(Html.fromHtml(parsedHtml));
				hideIndicator();
			}
		});
	}

	private void showIndicator() {
		MYOtherUtil.runOnUiThread(getContext(), mMainHandler, new Runnable() {
			@Override
			public void run() {
				mLoadingText.setVisibility(VISIBLE);
			}
		});
	}

	private void hideIndicator() {
		mLoadingText.setVisibility(GONE);
	}
}
