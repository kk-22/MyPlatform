package jp.co.my.myplatform.news;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.Delete;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.database.PLModelContainer;
import jp.co.my.myplatform.overlay.PLNavigationController;
import jp.co.my.myplatform.popover.PLListPopover;

public class PLNewsListView extends FrameLayout {

	private SwipeRefreshLayout mSwipeLayout;
	private ProgressBar mProgressBar;
	private ListView mListView;
	private PLNewsListAdapter mAdapter;

	private PLNewsPagerContent mPagerContent;
	private PLNewsGroupModel mGroupModel;
	private PLRSSFetcher mRssFetcher;

	public PLNewsListView(PLNewsPagerContent pagerContent, PLNewsGroupModel group) {
		super(pagerContent.getContext());
		mPagerContent = pagerContent;
		mGroupModel = group;

		LayoutInflater.from(getContext()).inflate(R.layout.view_news_list, this);
		mListView = findViewById(R.id.page_list);
		mProgressBar = findViewById(R.id.progress_bar);
		mSwipeLayout = findViewById(R.id.news_refresh);

		initSwipeLayout();
		initFetcher();
		initListView();
		initButton();
	}

	private void initSwipeLayout() {
		mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				mRssFetcher.manualFetchIfNecessary();
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

	private void initFetcher() {
		mRssFetcher = new PLRSSFetcher(mGroupModel, mProgressBar, new PLRSSFetcher.PLRSSCallbackListener() {
			@Override
			public void finishedRequest(ArrayList<PLNewsPageModel> pageArray) {
				mSwipeLayout.setRefreshing(false);
				if (pageArray == null) {
					return;
				}

				mGroupModel.getPageContainer().setModelList(pageArray);
				mAdapter.renewalAllPage(pageArray);
				for (PLNewsPageModel page : pageArray) {
					if (page.isPartitionCell()) {
						int numberOfShowingCell = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition();
						// 新着線とその下の旧セルがいくつか見える位置へスクロール
						mListView.setSelection(pageArray.indexOf(page) - (int)(numberOfShowingCell * 0.8));
						break;
					}
				}
			}
		});
	}

	private void initListView() {
		mGroupModel.getPageContainer().loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLNewsPageModel>() {
			@Override
			public void onLoad(List<PLNewsPageModel> modelList) {
				mAdapter = new PLNewsListAdapter(getContext());
				mAdapter.sortList(modelList);
				mAdapter.renewalAllPage(modelList);
				mListView.setAdapter(mAdapter);
			}
		});
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLNewsPageModel pageModel = mAdapter.getItem(position);
				if (pageModel.isPartitionCell()) {
					return;
				}
				PLNavigationController navigation = PLCoreService.getNavigationController();

				PLNewsBrowserContent browserView = new PLNewsBrowserContent(pageModel);
				navigation.pushView(browserView);

				// 既読
				pageModel.setAlreadyRead(true);
				pageModel.save();
				PLNewsListAdapter.setBackgroundColorToView(view, true);
				mGroupModel.setReadDate(Calendar.getInstance());
				mGroupModel.save();
			}
		});
		mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
				PLNewsPageModel page = mAdapter.getItem(position);
				if (page.isPartitionCell()) {
					return false;
				}
				PLNewsSiteModel site = page.getSiteForeign();
				MYLogUtil.showToast(site.getName() +"\n" +site.getUrl());
				return true;
			}
		});
	}

	private void initButton() {
		findViewById(R.id.news_function_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showFunction();
			}
		});
	}

	private void showFunction() {
		String[] titles = {"Debug : delete 4 pages", "Update bad word", "Fetch RSS"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLCoreService.getNavigationController().getCurrentView().removeTopPopover();
				switch (position) {
					case 0: {
						List<PLNewsPageModel> pageList = PLNewsListView.this.mGroupModel.getPageContainer().getModelList();
						PLNewsListView.this.mAdapter.sortList(pageList);
						int size = pageList.size();
						int[] indexes = {0, 1, size - 2, size - 1};
						for (int index : indexes) {
							if (index < size) {
								pageList.get(index).delete();
							}
						}
						mGroupModel.getPageContainer().clear();
						initListView();
						MYLogUtil.showToast("deleted 4 pages");
						return;
					}
					case 1: {
						Delete.table(PLBadWordModel.class);
						mPagerContent.fetchBadWord();
						return;
					}
					case 2: {
						mRssFetcher.manualFetchIfNecessary();
						return;
					}
				}
			}
		}).showPopover();
	}
}
