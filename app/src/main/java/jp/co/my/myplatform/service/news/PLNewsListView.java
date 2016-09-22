package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel_Table;
import jp.co.my.myplatform.service.overlay.PLNavigationController;

public class PLNewsListView extends FrameLayout {

	private SwipeRefreshLayout mSwipeLayout;
	private ProgressBar mProgressBar;
	private ListView mListView;
	private PLNewsListAdapter mAdapter;

	private PLNewsGroupModel mGroupModel;
	private PLRSSFetcher mRssFetcher;
	private List<PLNewsPageModel> mPageList;

	public PLNewsListView(Context context, PLNewsGroupModel group) {
		super(context);
		mGroupModel = group;

		LayoutInflater.from(getContext()).inflate(R.layout.view_news_list, this);
		mListView = (ListView) findViewById(R.id.page_list);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.news_refresh);

		mRssFetcher = new PLRSSFetcher(mGroupModel, mProgressBar);
		initSwipeLayout();
		initListView();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		fetchRSSIfNecessary();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mRssFetcher.cancelAllRequest();
	}

	private void fetchRSSIfNecessary() {
		fetchRSS();
	}

	private void fetchRSS() {
		mRssFetcher.startRequest(new PLRSSFetcher.PLRSSCallbackListener() {
			@Override
			public void finishedRequest() {
				mPageList = mRssFetcher.getFetchedPageArrayAndClear();
				showList();
				SQLite.delete(PLNewsPageModel.class)
						.where(PLNewsPageModel_Table.groupForeign_no.is(mGroupModel.getNo()))
						.async()
						.execute();
				PLDatabase.saveModelList(mPageList);
				mSwipeLayout.setRefreshing(false);
			}
		});
	}

	private void showList() {
		mAdapter.renewalAllPage(mPageList);
	}

	private void initSwipeLayout() {
		mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				MYLogUtil.outputLog("pull");
				fetchRSS();
			}
		});
		mSwipeLayout.setOnChildScrollUpCallback(new SwipeRefreshLayout.OnChildScrollUpCallback() {
			@Override
			public boolean canChildScrollUp(SwipeRefreshLayout parent, @Nullable View child) {
				if (mListView.getVisibility() == View.VISIBLE) {
					return ViewCompat.canScrollVertically(mListView, -1);
				}
				return false;
			}
		});
	}

	private void initListView() {
		mPageList = mGroupModel.getPageArray();
		mAdapter = new PLNewsListAdapter(getContext());
		mAdapter.renewalAllPage(mPageList);
		mListView.setAdapter(mAdapter);

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLNewsPageModel pageModel = mAdapter.getItem(position);
				PLNavigationController navigation = PLCoreService.getNavigationController();
				navigation.getCurrentView().setKeepCache(true);

				PLNewsBrowserView browserView = new PLNewsBrowserView(pageModel);
				navigation.pushView(browserView);

				// 既読
				pageModel.setAlreadyRead(true);
				pageModel.save();
				TextView pageText = (TextView) view.findViewById(R.id.page_title_text);
				pageText.setTextColor(Color.RED);
				mGroupModel.setReadDate(Calendar.getInstance());
				mGroupModel.save();
			}
		});
	}
}
