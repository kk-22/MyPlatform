package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel_Table;

public class PLNewsListView extends FrameLayout {

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

		mPageList = mGroupModel.getPageArray();
		mAdapter = new PLNewsListAdapter(context);
		mAdapter.renewalAllPage(mPageList);
		mListView.setAdapter(mAdapter);

		mRssFetcher = new PLRSSFetcher(mGroupModel, mProgressBar);
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
			}
		});
	}

	private void showList() {
		mAdapter.renewalAllPage(mPageList);
	}
}
