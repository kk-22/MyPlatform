package jp.co.my.myplatform.service.news;

import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.core.PLVolleyHelper;
import jp.co.my.myplatform.service.model.PLBadWordModel;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLRSSFetcher {

	private enum FetchState {
		FETCH_STATE_NONE,					// 動作なし・キャンセル後
		FETCH_STATE_MANUAL_FETCHING,		// 手動更新中
		FETCH_STATE_AUTO_FETCHING,			// 自動更新中
		FETCH_STATE_AUTO_FETCH_FINISHED,	// 自動更新完了で手動更新待ち
	}

	private Handler mMainHandler;
	private ProgressBar mProgressBar;
	private PLNewsGroupModel mGroupModel;

	private int mRequestCount;								// 全リクエスト数
	private int mFetchedCount;								// 通信完了リクエスト数
	private ArrayList<PLNewsPageModel> mParsedPageArray;	// レスポンスをパースしたページ
	private ArrayList<PLNewsPageModel> mResultPageArray;		// 一覧に表示するページ
	private PLRSSCallbackListener mListener;
	private FetchState mState;

	public PLRSSFetcher(PLNewsGroupModel group, ProgressBar progressBar, PLRSSCallbackListener listener) {
		mGroupModel = group;
		mProgressBar = progressBar;
		mListener = listener;

		mMainHandler = new Handler();
		mParsedPageArray = new ArrayList<>();
		mState = FetchState.FETCH_STATE_NONE;
		autoFetchIfNecessary();
	}

	public void autoFetchIfNecessary() {
		switch (mState) {
			case FETCH_STATE_MANUAL_FETCHING:
			case FETCH_STATE_AUTO_FETCHING:
				MYLogUtil.showToast("Fetching now");return;
			case FETCH_STATE_AUTO_FETCH_FINISHED:
				MYLogUtil.showToast("fetch was finished");return;
			case FETCH_STATE_NONE:
				break;
		}
		Calendar cacheCalendar =  mGroupModel.getFetchedDate();
		boolean hasPage = (mGroupModel.getPageContainer().count() > 0);
		if (hasPage && cacheCalendar != null) {
			cacheCalendar.add(Calendar.MINUTE, mGroupModel.getUpdateInterval());
			Calendar currentCalendar = Calendar.getInstance();
			if (currentCalendar.compareTo(cacheCalendar) < 0) {
				return;
			}
		}

		if (!hasPage || mGroupModel.isAutoUpdate()) {
			mState = FetchState.FETCH_STATE_MANUAL_FETCHING;
		} else {
			mState = FetchState.FETCH_STATE_AUTO_FETCHING;
		}
		startRequest();
	}

	public void manualFetchIfNecessary() {
		switch (mState) {
			case FETCH_STATE_NONE: {
				mState = FetchState.FETCH_STATE_MANUAL_FETCHING;
				startRequest();
				break;
			}
			case FETCH_STATE_AUTO_FETCHING:
				mState = FetchState.FETCH_STATE_MANUAL_FETCHING;
			case FETCH_STATE_MANUAL_FETCHING:
				MYLogUtil.showToast("Fetching now");break;
			case FETCH_STATE_AUTO_FETCH_FINISHED:
				finishAllFetch();break;
		}
	}

	public void cancelAllRequest() {
		mState = FetchState.FETCH_STATE_NONE;

		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
		mFetchedCount = 0;
		mRequestCount = 0;
		mParsedPageArray.clear();
		mResultPageArray = null;
		mProgressBar.setVisibility(View.GONE);
	}

	private void startRequest() {
		mFetchedCount = 0;
		mParsedPageArray.clear();
		mResultPageArray = null;

		mProgressBar.setVisibility(View.VISIBLE);
		requestAllSite();
	}

	private void requestAllSite() {
		mGroupModel.getSiteContainer().loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLNewsSiteModel>() {
			@Override
			public void onLoad(List<PLNewsSiteModel> siteList) {
				if (mState == FetchState.FETCH_STATE_NONE) {
					return;
				}
				mRequestCount = siteList.size();
				mProgressBar.setMax(mRequestCount);

				PLVolleyHelper volleyHelper = PLCoreService.getVolleyHelper();
				for (final PLNewsSiteModel site : siteList) {
					Response.Listener<InputStream> listener = new Response.Listener<InputStream>() {
						@Override
						public void onResponse(InputStream inputStream) {
							parseRssOnThread(site, inputStream);
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

	private void parseRssOnThread(final PLNewsSiteModel site, final InputStream inputStream) {
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
							mParsedPageArray.addAll(pageList);
						}
						countUpFetch();
					}
				});
			}
		}).start();
	}

	private void countUpFetch() {
		if (mState == FetchState.FETCH_STATE_NONE) {
			return;
		}
		mFetchedCount++;
		mProgressBar.setProgress(mFetchedCount);
		if (mFetchedCount < mRequestCount) {
			return;
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				mergeAllPage();
			}
		}).start();
	}

	private void mergeAllPage() {
		// 重複・バッドワード持ちページの削除
		PLNewsListAdapter.sortList(mParsedPageArray);
		final ArrayList<PLNewsPageModel> fetchPageArray = new ArrayList<>();
		List<PLBadWordModel> wordList = mGroupModel.getBadWordContainer().getModelList();
		int parsedCount = mParsedPageArray.size();
		for (int i = 0; i < parsedCount ; i++) {
			PLNewsPageModel page = mParsedPageArray.get(i);
			if (mParsedPageArray.lastIndexOf(page) > i) {
				continue;
			}
			String title = page.getTitle();
			boolean wordResult = true;
			for (PLBadWordModel badWord : wordList) {
				if (title.contains(badWord.getWord())) {
					wordResult = false;
					break;
				}
			}
			if (wordResult) {
				fetchPageArray.add(page);
			}
		}
		mParsedPageArray.clear();

		// 新旧PageModelのマージ
		final ArrayList<PLNewsPageModel> removePageArray = new ArrayList<>();
		ArrayList<PLNewsPageModel> remainPageArray = new ArrayList<>();
		for (PLNewsPageModel oldPage : mGroupModel.getPageContainer().getModelList()) {
			int newIndex = fetchPageArray.indexOf(oldPage);
			if (newIndex == -1) {
				removePageArray.add(oldPage);
			} else {
				PLNewsPageModel newPage = fetchPageArray.get(newIndex);
				fetchPageArray.remove(newIndex);

				oldPage.setTitle(newPage.getTitle());
				oldPage.setPostedDate(newPage.getPostedDate());
				remainPageArray.add(oldPage);
			}
		}

		mGroupModel.setFetchedDate(Calendar.getInstance());
		if (fetchPageArray.size() == 0) {
			MYLogUtil.showToast(mGroupModel.getTitle() +" 新着ニュースなし");
			mGroupModel.save();
		} else {
			PLNewsPageModel partition = new PLNewsPageModel();
			partition.associateGroup(mGroupModel);
			partition.setPostedDate(Calendar.getInstance());
			partition.setTitle("新着" + fetchPageArray.size() + "件");

			mResultPageArray = new ArrayList<>(fetchPageArray);
			mResultPageArray.add(partition);
			mResultPageArray.addAll(remainPageArray);
			for (int i = 0; i < mResultPageArray.size(); i++) {
				mResultPageArray.get(i).setPositionNo(i);
			}

			FlowManager.getDatabase(PLDatabase.class).beginTransactionAsync(new ITransaction() {
				@Override
				public void execute(DatabaseWrapper databaseWrapper) {
					for (PLNewsPageModel site : removePageArray) {
						MYLogUtil.outputLog("delete " + site.getTitle());
						site.delete();
					}
					PLDatabase.saveModelList(fetchPageArray, true);
					mGroupModel.save();
				}
			}).build().execute();
		}

		// Finish all request
		mMainHandler.post(new Runnable() {
			@Override
			public void run() {
				switch (mState) {
					case FETCH_STATE_NONE:
					case FETCH_STATE_AUTO_FETCH_FINISHED:
						break;
					case FETCH_STATE_MANUAL_FETCHING:
						finishAllFetch();break;
					case FETCH_STATE_AUTO_FETCHING: {
						mState = FetchState.FETCH_STATE_AUTO_FETCH_FINISHED;
						break;
					}
				}
			}
		});
	}

	private void finishAllFetch() {
		mListener.finishedRequest(mResultPageArray);

		mResultPageArray = null;
		mState = FetchState.FETCH_STATE_NONE;
		mProgressBar.setVisibility(View.GONE);
	}

	public static abstract class PLRSSCallbackListener {
		public abstract void finishedRequest(ArrayList<PLNewsPageModel> pageArray);
	}
}
