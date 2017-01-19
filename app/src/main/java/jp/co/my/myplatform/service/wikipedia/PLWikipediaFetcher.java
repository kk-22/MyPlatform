package jp.co.my.myplatform.service.wikipedia;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Calendar;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;

public class PLWikipediaFetcher {

	private boolean mIsCanceled;
	private PLWikipediaFetcherListener mListener;

	public PLWikipediaFetcher(PLWikipediaFetcherListener listener) {
		super();
		mListener = listener;
	}

	public void cancelAllRequest() {
		mIsCanceled = true;
	}

	public void startFetchPage(final String url) {
		Response.Listener<String> listener = new Response.Listener<String>() {
			@Override
			public void onResponse(final String response) {
				PLWikipediaPageModel pageModel = new PLWikipediaPageModel();
				try {
					String title = url.replaceFirst(".*/", "");
					String decodeTitle = URLDecoder.decode(title, "UTF8");
					pageModel.setTitle(decodeTitle);
				} catch (UnsupportedEncodingException e) {
					MYLogUtil.showExceptionToast(e);
					pageModel.setTitle("UnsupportedEncodingException error");
				}
				pageModel.setUrl(url);
				pageModel.setOriginHtml(response);
				pageModel.setRegisteredDate(Calendar.getInstance());
				pageModel.save();

				if (mIsCanceled) {
					return;
				}
				mListener.finishedFetchPage(pageModel);
				mListener.openPage(pageModel);
			}
		};
		Response.ErrorListener error = new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				MYLogUtil.outputErrorLog("Fetch page error " + error.toString());
				mListener.finishedFetchPage(null);
			}
		};
		StringRequest request = new StringRequest(url, listener, error);
		PLVolleyHelper volleyHelper = PLCoreService.getVolleyHelper();
		volleyHelper.addRequest(request, this.getClass());
	}

	public interface PLWikipediaFetcherListener {
		void finishedFetchPage(PLWikipediaPageModel pageModel);
		void openPage(PLWikipediaPageModel pageModel);
	}
}
