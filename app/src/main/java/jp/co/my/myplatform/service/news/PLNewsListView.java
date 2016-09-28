package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.overlay.PLNavigationController;
import jp.co.my.myplatform.service.popover.PLListPopover;

public class PLNewsListView extends FrameLayout {

	private SwipeRefreshLayout mSwipeLayout;
	private ProgressBar mProgressBar;
	private ListView mListView;
	private PLNewsListAdapter mAdapter;

	private PLNewsGroupModel mGroupModel;
	private PLRSSFetcher mRssFetcher;

	public PLNewsListView(Context context, PLNewsGroupModel group) {
		super(context);
		mGroupModel = group;

		LayoutInflater.from(getContext()).inflate(R.layout.view_news_list, this);
		mListView = (ListView) findViewById(R.id.page_list);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
		mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.news_refresh);

		initSwipeLayout();
		initListView();
		initFetcher();
		initButton();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mRssFetcher.cancelAllRequest();
	}

	private void initFetcher() {
		mRssFetcher = new PLRSSFetcher(mGroupModel, mProgressBar, new PLRSSFetcher.PLRSSCallbackListener() {
			@Override
			public void finishedRequest(ArrayList<PLNewsPageModel> pageArray) {
				// 新旧PageModelのマージ
				// TODO: Move to async?
				MYLogUtil.outputLog("start " +mGroupModel.getTitle());
				final ArrayList<PLNewsPageModel> removePageArray = new ArrayList<>();
				final ArrayList<PLNewsPageModel> nextPageArray = new ArrayList<>();
				for (PLNewsPageModel oldPage : mGroupModel.getPageContainer().getModelList()) {
					int newIndex = pageArray.indexOf(oldPage);
					if (newIndex == -1) {
						removePageArray.add(oldPage);
					} else {
						PLNewsPageModel newPage = pageArray.get(newIndex);
						pageArray.remove(newIndex);

						oldPage.setTitle(newPage.getTitle());
						oldPage.setPostedDate(newPage.getPostedDate());
						nextPageArray.add(oldPage);
					}
				}
				PLNewsPageModel partition = new PLNewsPageModel();
				partition.associateGroup(mGroupModel);
				if (pageArray.size() == 0) {
					partition.setPostedDate(Calendar.getInstance());
					partition.setTitle("新着なし");
				} else {
					mAdapter.sortList(pageArray);
					PLNewsPageModel newPage = pageArray.get(pageArray.size() - 1);
					Calendar calendar = newPage.getPostedDate();
					calendar.set(Calendar.MINUTE, -1);
					partition.setPostedDate(calendar);
					partition.setTitle("新着" +pageArray.size() +"件");
				}
				nextPageArray.addAll(pageArray);
				nextPageArray.add(partition);

				MYLogUtil.outputLog("end " +mGroupModel.getTitle());
				mGroupModel.getPageContainer().setModelList(nextPageArray);
				mAdapter.renewalAllPage(nextPageArray);
				mListView.setSelection(nextPageArray.indexOf(partition) - 7);
				mSwipeLayout.setRefreshing(false);

				mGroupModel.setFetchedDate(Calendar.getInstance());
				FlowManager.getDatabase(PLDatabase.class).beginTransactionAsync(new ITransaction() {
					@Override
					public void execute(DatabaseWrapper databaseWrapper) {
						for (PLNewsPageModel site : removePageArray) {
							MYLogUtil.outputLog("delete " +site.getTitle());
							site.delete();
						}
						PLDatabase.saveModelList(nextPageArray, true);
						mGroupModel.save();
					}
				}).build().execute();
			}
		});
	}

	private void initSwipeLayout() {
		mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				MYLogUtil.outputLog("pull");
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

	private void initListView() {
		mGroupModel.getPageContainer().loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLNewsPageModel>() {
			@Override
			public void onLoad(List<PLNewsPageModel> modelList) {
				mAdapter = new PLNewsListAdapter(getContext());
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
				navigation.getCurrentView().setKeepCache(true);

				PLNewsBrowserView browserView = new PLNewsBrowserView(pageModel);
				navigation.pushView(browserView);

				// 既読
				pageModel.setAlreadyRead(true);
				pageModel.save();
				PLNewsListAdapter.setBackgroundColorToView(view, true);
				mGroupModel.setReadDate(Calendar.getInstance());
				mGroupModel.save();
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
		String[] titles = {"Debug : delete 4 pages"};
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
				}
			}
		}).showPopover();
	}
}
