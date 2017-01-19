package jp.co.my.myplatform.service.wikipedia;

import android.os.Handler;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

public class PLWikipediaHtmlEncoder {

	private static final int ENCODE_VERSION = 1;

	private Handler mMainHandler;
	private PLWikipediaEncodeListener mListener;

	public PLWikipediaHtmlEncoder(PLWikipediaEncodeListener listener) {
		super();
		mListener = listener;
		mMainHandler = new Handler();
	}

	public void cancel() {
		mListener = null;
	}

	public void encodeHtml(PLWikipediaPageModel model) {
		String baseHtml = model.getOriginHtml();

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
		htmlStr = htmlStr.replaceAll(
				"<h2 class=\"section-heading in-block(.*?)<span(.*?)>(.*?)</span></h2>"
				, "<h1>■$3</1>");

		createLinkStr(model, htmlStr);
	}

	private void createLinkStr(PLWikipediaPageModel pageModel, String html) {
		// deprecated のため Android 7 以降で引数にパラメータ追加
		CharSequence sequence = Html.fromHtml(html);
		final SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
		URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
		for(URLSpan span : urls) {
			makeLinkClickable(strBuilder, span);
		}

		pageModel.setEncodedHtml(strBuilder.toString());
		pageModel.setEncodedVersion(ENCODE_VERSION);
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				if (mListener != null) {
					mListener.finishedEncode(strBuilder);
				}
			}
		});
	}

	private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
		int start = strBuilder.getSpanStart(span);
		int end = strBuilder.getSpanEnd(span);
		int flags = strBuilder.getSpanFlags(span);
		ClickableSpan clickable = new ClickableSpan() {
			public void onClick(View view) {
				if (mListener == null) {
					return;
				}
				String url = span.getURL();
				mListener.onClickLink(url);
			}
		};
		strBuilder.setSpan(clickable, start, end, flags);
		strBuilder.removeSpan(span);
	}

	public interface PLWikipediaEncodeListener {
		void finishedEncode(SpannableStringBuilder strBuilder);
		void onClickLink(String linkUrl);
	}
}
