package jp.co.my.myplatform.service.news;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLRSSFetcher {

	private String mRequestKey;
	private Handler mMainHandler;
	private ProgressBar mProgressBar;
	private PLNewsGroupModel mGroupModel;

	private int mRequestCount;								// 全リクエスト数
	private int mFetchedCount;								// 通信完了リクエスト数
	private ArrayList<PLNewsPageModel> mFetchedPageArray;	// レスポンスをパースしたデータ
	private PLRSSCallbackListener mListener;

	public PLRSSFetcher(PLNewsGroupModel group, ProgressBar progressBar) {
		mGroupModel = group;
		mProgressBar = progressBar;

		// TODO: Use key
		mRequestKey = this.getClass().getName() + group.getTitle();
		mMainHandler = new Handler();
		mFetchedPageArray = new ArrayList<>();
	}

	public boolean isFetching() {
		return (mRequestCount > 0 &&  mFetchedCount < mRequestCount);
	}

	public void startRequest(PLRSSCallbackListener listener) {
		mFetchedCount = 0;
		mFetchedPageArray.clear();

		mProgressBar.setVisibility(View.VISIBLE);
		mListener = listener;
		requestAllSite();
	}

	public void cancelAllRequest() {
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
		mFetchedCount = 0;
		mRequestCount = 0;
		mFetchedPageArray.clear();
		mListener = null;
		mProgressBar.setVisibility(View.GONE);
	}

	private void requestAllSite() {
		List<PLNewsSiteModel> siteList = mGroupModel.getSiteArray();
		mRequestCount = siteList.size();
		mProgressBar.setMax(mRequestCount);

		PLVolleyHelper volleyHelper = PLCoreService.getVolleyHelper();
		for (final PLNewsSiteModel site : siteList) {
			Response.Listener<InputStream> listener = new Response.Listener<InputStream>() {
				@Override
				public void onResponse(InputStream inputStream) {
					fetchedPage(site, inputStream);
				}
			};
			Response.ErrorListener error = new Response.ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					MYLogUtil.outputErrorLog("Fetch page error " + error.toString());
					countUpFetched();
				}
			};
			PLInputStreamRequest request = new PLInputStreamRequest(site.getUrl(),listener, error);
			volleyHelper.addRequest(request, this.getClass());
		}
	}

	private void fetchedPage(final PLNewsSiteModel site, final InputStream inputStream) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final List<PLNewsPageModel> pageList = PLRSSParser.getPageArrayFromInputStream(site, inputStream);
				mMainHandler.post(new Runnable() {
					@Override
					public void run() {
						if (pageList == null) {
							MYLogUtil.showErrorToast("parsedPageList is nul. siteTitle = " +site.getName());
						} else {
							mFetchedPageArray.addAll(pageList);
						}
						countUpFetched();
					}
				});
			}
		}).start();
	}

	private void countUpFetched() {
		mFetchedCount++;
		mProgressBar.setProgress(mFetchedCount);
		if (mFetchedCount < mRequestCount) {
			return;
		}

		// Finish all request
		mProgressBar.setVisibility(View.GONE);
		if (mListener != null) {
			mListener.finishedRequest();
		}
	}

	public static abstract class PLRSSCallbackListener {
		public abstract void finishedRequest();
	}

	public ArrayList<PLNewsPageModel> getFetchedPageArrayAndClear() {
		ArrayList<PLNewsPageModel> array = new ArrayList<>(mFetchedPageArray);
		mFetchedPageArray.clear();
		return array;
	}
}
