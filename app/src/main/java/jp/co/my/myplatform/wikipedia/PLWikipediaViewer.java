package jp.co.my.myplatform.wikipedia;

import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYOtherUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.layout.PLRelativeLayoutController;
import jp.co.my.myplatform.database.PLModelContainer;
import jp.co.my.myplatform.popover.PLActionListPopover;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.popover.PLTextFieldPopover;

public class PLWikipediaViewer extends PLContentView
		implements PLWikipediaHtmlEncoder.PLWikipediaEncodeListener
		, PLActionListPopover.PLActionListListener<PLWikipediaPageModel>
		, PLWikipediaFetcher.PLWikipediaFetcherListener {

	private TextView mTextView;
	private TextView mLoadingText;
	private ScrollView mScrollView;
	private PLWikipediaPageModel mCurrentPageModel;
	private PLWikipediaFetcher mFetcher;
	private PLWikipediaHtmlEncoder mEncoder;
	private ArrayList<PLWikipediaPageModel> mPageList;

	private Handler mMainHandler;

	public PLWikipediaViewer() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_wikipedia_viewer, this);
		mTextView = (TextView) findViewById(R.id.html_text);
		mLoadingText = (TextView) findViewById(R.id.loading_text);
		mScrollView = (ScrollView) findViewById(R.id.text_scroll);
		mMainHandler = new Handler();
		mFetcher = new PLWikipediaFetcher(this);
		mEncoder = new PLWikipediaHtmlEncoder(this);
		mPageList = new ArrayList<>();

		initClickEvent();
		loadFirstPage();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mFetcher.cancelAllRequest();
		mEncoder.cancel();

		saveScrollPosition();
	}

	private void loadFirstPage() {
		PLModelContainer<PLWikipediaPageModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLWikipediaPageModel.class)
				.orderBy(PLWikipediaPageModel_Table.lastReadDate, false));
		container.loadList(new PLModelContainer.PLOnModelLoadThreadListener<PLWikipediaPageModel>() {
			@Override
			public void onLoad(List<PLWikipediaPageModel> modelLists) {
				if (modelLists.size() > 0) {
					mPageList.addAll(modelLists);
					loadPageModel(modelLists.get(0));
					return;
				}
				String url = "https://ja.wikipedia.org/wiki/%E7%B5%90%E5%9F%8E%E7%A7%80%E5%BA%B7";
				requestPage(url);
			}
		}, null);
	}

	private void requestPage(String url) {
		showIndicator();
		mFetcher.startFetchPage(url);
	}

	private void loadPageModel(final PLWikipediaPageModel pageModel) {
		if (mCurrentPageModel != null) {
			saveScrollPosition();
			showIndicator();
		}
		mCurrentPageModel = pageModel;
		new Thread(new Runnable() {
			@Override
			public void run() {
				mEncoder.encodeHtml(pageModel);
			}
		}).start();
	}

	private void saveScrollPosition() {
		mCurrentPageModel.setScrollPosition(mScrollView.getScrollY());
		mCurrentPageModel.save();
	}

	private void initClickEvent() {
		// toolbar
		findViewById(R.id.list_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				displayPageList();
			}
		});
		findViewById(R.id.search_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
					@Override
					public boolean onEnter(View v, String text) {
						String url = "https://ja.m.wikipedia.org/w/index.php?search=" +text;
						requestPage(url);
						return true;
					}
				}).showPopover();
			}
		});
		findViewById(R.id.delete_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String[] titles = {"削除"};
				new PLListPopover(titles, new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						removePageModel(mCurrentPageModel);
						PLWikipediaViewer.this.removeTopPopover();
						displayPageList();
					}
				}).showPopover();
			}
		});
	}

	private void displayPageList() {
		List<String> titleArray = new ArrayList<>();
		List<PLWikipediaPageModel> modelList = new ArrayList<>(mPageList);
		for (PLWikipediaPageModel model : modelList) {
			titleArray.add(model.getTitle());
		}

		Button listButton = (Button) findViewById(R.id.list_button);
		new PLActionListPopover<>(titleArray, modelList, PLWikipediaViewer.this)
				.showPopover(new PLRelativeLayoutController(listButton));
	}

	private void removePageModel(PLWikipediaPageModel pageModel) {
		mPageList.remove(pageModel);
		pageModel.delete();
	}

	private void showIndicator() {
		MYOtherUtil.runOnUiThread(getContext(), mMainHandler, new Runnable() {
			@Override
			public void run() {
				mLoadingText.setVisibility(VISIBLE);
			}
		});
	}

	private void hideIndicator() {
		mLoadingText.setVisibility(GONE);
	}

	// PLWikipediaEncodeListener
	@Override
	public void finishedEncode(SpannableStringBuilder strBuilder) {
		mTextView.setText(strBuilder);
		mTextView.setMovementMethod(LinkMovementMethod.getInstance());
		mScrollView.post(new Runnable() {
			@Override
			public void run() {
				mScrollView.setScrollY(mCurrentPageModel.getScrollPosition());
				hideIndicator();
			}
		});

		mCurrentPageModel.setLastReadDate(Calendar.getInstance());
		mCurrentPageModel.save();
	}

	@Override
	public void onClickLink(String linkUrl) {
		// 保存済みでないかチェック
		String fullUrl = "https://ja.wikipedia.org" + linkUrl;
		for (PLWikipediaPageModel model : mPageList) {
			if (model.getUrl().equals(fullUrl)) {
				MYLogUtil.showToast("既に登録済み " +model.getTitle());
				return;
			}
		}
		requestPage(fullUrl);
	}

	// PLActionListListener of page list
	@Override
	public void onItemClick(PLWikipediaPageModel object, PLActionListPopover listPopover) {
		loadPageModel(object);
		listPopover.removeFromContentView();
	}

	@Override
	public void onActionClick(final PLWikipediaPageModel object, final PLActionListPopover<PLWikipediaPageModel> listPopover, View buttonView) {
		String[] titles = {"削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				MYLogUtil.showToast("ページ削除：" +object.getTitle());
				removePageModel(object);
				listPopover.removeObject(object);
				// アクションPopoverだけ削除してリストは残す
				PLWikipediaViewer.this.removeTopPopover();
			}
		}).showPopover(new PLRelativeLayoutController(buttonView));
	}

	// PLWikipediaFetcherListener
	@Override
	public void finishedFetchPage(PLWikipediaPageModel pageModel) {
		if (pageModel != null) {
			mPageList.add(pageModel);
		}
		hideIndicator();
	}

	@Override
	public void openPage(PLWikipediaPageModel pageModel) {
		loadPageModel(pageModel);
	}
}
