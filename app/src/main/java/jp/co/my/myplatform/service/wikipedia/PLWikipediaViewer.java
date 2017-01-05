package jp.co.my.myplatform.service.wikipedia;

import android.text.Html;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;

public class PLWikipediaViewer extends PLContentView {

	private TextView mTextView;
	private TextView mLoadingText;

	public PLWikipediaViewer() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_wikipedia_viewer, this);
		mTextView = (TextView) findViewById(R.id.html_text);
		mLoadingText = (TextView) findViewById(R.id.loading_text);

		String url = "https://ja.wikipedia.org/wiki/%E7%B5%90%E5%9F%8E%E7%A7%80%E5%BA%B7";
		requestPage(url);
	}

	private void requestPage(String url) {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
//				MYLogUtil.outputErrorLog("onResponse " + response);
				parseHtml(response);
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

		showIndicator();
	}

	private void parseHtml(String baseHtml) {
		MYLogUtil.outputLog("before parse");
		// 余分の削除
		// 改行文字削除
		String parsedHtml = baseHtml.replaceAll("\n", "");
		// ヘッダ～BODYタグ冒頭部分まで削除
		parsedHtml = parsedHtml.replaceFirst(".*<h1 id=\"section_0\"(.*?)>", "");
		// 関連項目以下削除
		parsedHtml = parsedHtml.replaceFirst("<span[^<]*関連項目.*", "");
		// Javascript 削除
		parsedHtml = parsedHtml.replaceAll("<script>(.*?)</script>", "");
		// 編集リンク削除
		parsedHtml = parsedHtml.replaceAll("<a[^>]*>編集</a>", "");

		// HTMLタグの代用
		// li タグを削除して箇条書きに変更
		parsedHtml = parsedHtml.replaceAll("<li(.*?)>", "・");
		parsedHtml = parsedHtml.replaceAll("</li>", "<br><br>");

		// CSSの代用
		// 見出しタグの変更
		parsedHtml = parsedHtml.replaceAll("<h2 class=\"section-heading in-block(.*?)<span(.*?)>(.*?)</span></h2>"
				, "<h1>■$3</1>");

		// deprecated のため Android 7 以降で引数にパラメータ追加
		MYLogUtil.outputLog("after parse");
		mTextView.setText(Html.fromHtml(parsedHtml));
		hideIndicator();
	}

	private void showIndicator() {
		mLoadingText.setVisibility(VISIBLE);
	}

	private void hideIndicator() {
		mLoadingText.setVisibility(GONE);
	}
}
