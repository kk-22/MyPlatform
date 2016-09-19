package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
//import jp.co.my.myplatform.service.model.PLNewsPageModel;

public class PLNewsListView extends FrameLayout {

	private ProgressBar mProgressBar;
	private ListView mListView;
	private ListAdapter mAdapter;

	private PLNewsGroupModel mGroupModel;
	private PLRSSFetcher mRssFetcher;
	private List<PLNewsPageModel> mPageList;

	public PLNewsListView(Context context) {
		this(context, null, 0, 0);
	}
	public PLNewsListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0, 0);
	}
	public PLNewsListView(Context context, AttributeSet attrs, int defStyleAttr) {
		this(context, attrs, defStyleAttr, 0);
	}
	public PLNewsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		LayoutInflater.from(getContext()).inflate(R.layout.view_news_list, this);
		mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
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
				// TODO: Bug of stop the thread.
				// TODO: "has been unable to grant a connection to thread"
//				PLDatabase.saveAllModel(mPageList);
				showList();
			}
		});
	}

	private void showList() {

	}

	public void setGroupModel(PLNewsGroupModel groupModel) {
		mGroupModel = groupModel;
		if (mRssFetcher != null) {
			mRssFetcher.cancelAllRequest();
		}
		mRssFetcher = new PLRSSFetcher(mGroupModel, mProgressBar);
	}
}
