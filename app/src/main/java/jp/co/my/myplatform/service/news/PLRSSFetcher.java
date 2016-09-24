package jp.co.my.myplatform.service.news;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLRSSFetcher {

	private Handler mMainHandler;
	private ProgressBar mProgressBar;
	private PLNewsGroupModel mGroupModel;

	private int mRequestCount;								// 全リクエスト数
	private int mFetchedCount;								// 通信完了リクエスト数
	private boolean mIsManualFetching;						// 手動更新である場合TRUE
	private boolean mIsCanceling;							// キャンセル済み
	private ArrayList<PLNewsPageModel> mFetchedPageArray;	// レスポンスをパースしたデータ
	private PLRSSCallbackListener mListener;

	public PLRSSFetcher(PLNewsGroupModel group, ProgressBar progressBar, PLRSSCallbackListener listener) {
		mGroupModel = group;
		mProgressBar = progressBar;
		mListener = listener;

		mMainHandler = new Handler();
		mFetchedPageArray = new ArrayList<>();
		autoFetchIfNecessary();
	}

	public void autoFetchIfNecessary() {
		if (isFetching()) {
			MYLogUtil.showToast("Fetching now");
			return;
		}
		if (isFinished()) {
			MYLogUtil.showToast("fetch was finished");
			return;
		}
		Calendar cacheCalendar =  mGroupModel.getFetchedDate();
		if (cacheCalendar != null) {
			cacheCalendar.add(Calendar.MINUTE, mGroupModel.getUpdateInterval());
			Calendar currentCalendar = Calendar.getInstance();
			if (currentCalendar.compareTo(cacheCalendar) < 0) {
				return;
			}
		}
		startRequest();
	}

	public void manualFetchIfNecessary() {
		mIsManualFetching = true;
		if (isFetching()) {
			MYLogUtil.showToast("Fetching now");
			return;
		}

		if (isFinished()) {
			finishAllFetch();
		} else {
			startRequest();
		}
	}

	public void cancelAllRequest() {
		mIsCanceling = true;

		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
		mFetchedCount = 0;
		mRequestCount = 0;
		mIsManualFetching = false;
		mFetchedPageArray.clear();
		mProgressBar.setVisibility(View.GONE);
	}

	private void startRequest() {
		mFetchedCount = 0;
		mFetchedPageArray.clear();
		mIsCanceling = false;

		mProgressBar.setVisibility(View.VISIBLE);
		requestAllSite();
	}

	private void requestAllSite() {
		mGroupModel.getSiteContainer().loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLNewsSiteModel>() {
			@Override
			public void onLoad(List<PLNewsSiteModel> siteList) {
				if (mIsCanceling) {
					return;
				}
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
							countUpFetch();
						}
					};
					PLInputStreamRequest request = new PLInputStreamRequest(site.getUrl(),listener, error);
					volleyHelper.addRequest(request, this.getClass());
				}
			}
		});
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
						countUpFetch();
					}
				});
			}
		}).start();
	}

	private void countUpFetch() {
		mFetchedCount++;
		mProgressBar.setProgress(mFetchedCount);
		if (mFetchedCount < mRequestCount) {
			return;
		}

		// Finish all request
		if (mIsManualFetching) {
			finishAllFetch();
		}
	}

	private void finishAllFetch() {
		ArrayList<PLNewsPageModel> array = new ArrayList<>(mFetchedPageArray);
		mFetchedPageArray.clear();
		mIsManualFetching = false;
		mFetchedCount = 0;
		mRequestCount = 0;
		mProgressBar.setVisibility(View.GONE);

		mListener.finishedRequest(array);
	}

	private boolean isFetching() {
		return (mRequestCount > 0 &&  mFetchedCount < mRequestCount);
	}

	private boolean isFinished() {
		return (mRequestCount > 0 &&  mFetchedCount == mRequestCount);
	}

	public static abstract class PLRSSCallbackListener {
		public abstract void finishedRequest(ArrayList<PLNewsPageModel> pageArray);
	}
}
