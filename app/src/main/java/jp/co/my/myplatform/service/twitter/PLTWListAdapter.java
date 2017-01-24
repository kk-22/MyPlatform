package jp.co.my.myplatform.service.twitter;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.BaseTweetView;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.browser.PLBaseBrowserView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.popover.PLListPopover;
import retrofit2.Call;

/*
 TweetTimelineListAdapterクラスはタップ時に強制的にActivityを起動するため、
 タップイベントをカスタマイズするためのクラス
 */
public class PLTWListAdapter implements ListAdapter {

	private PLTWListView mListView;
	private TweetTimelineListAdapter mTweetAdapter;

	public PLTWListAdapter(PLTWListView listView, TweetTimelineListAdapter tweetAdapter) {
		super();
		mListView = listView;
		mTweetAdapter = tweetAdapter;
	}

	private void onClickTweet(final Tweet tweet) {
		String[] titles = {"投稿者", "リンク", "削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLCoreService.getNavigationController().getCurrentView().removeTopPopover();
				switch (position) {
					case 0: {
						String userName = tweet.user.screenName;
						openBrowser("https://mobile.twitter.com/" +userName);
						break;
					}
					case 1: {
						Pattern pattern = Pattern.compile("http[^ \n]*");
						Matcher matcher = pattern.matcher(tweet.text);
						if (!matcher.find()) {
							MYLogUtil.showErrorToast("リンクなし");
							return;
						}
						String url = matcher.group();
						openBrowser(url);
						break;
					}
					case 2: {
						if (!tweet.user.screenName.equals("dorann217")) {
							MYLogUtil.showErrorToast("自分以外のツイート");
							return;
						}

						StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();
						Call<Tweet> callTweet = statusesService.destroy(tweet.id, false);
						callTweet.enqueue(new Callback<Tweet>() {
							@Override
							public void success(Result<Tweet> result) {
								MYLogUtil.showToast("destroy success");
								mListView.refreshList();
							}
							@Override
							public void failure(TwitterException exception) {
								MYLogUtil.showErrorToast("destroy error");
							}
						});
						break;
					}
				}
			}
		}).showPopover();
	}

	private void openBrowser(String url) {
		PLBaseBrowserView browserView = new PLBaseBrowserView();
		browserView.getCurrentWebView().loadUrl(url);
		PLCoreService.getNavigationController().pushView(browserView);
	}

	private void removeAllClickListener(ViewGroup baseView) {
		// クラッシュ防止のため全イベントを削除
		int childCount = baseView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = baseView.getChildAt(i);
			view.setOnTouchListener(null);
			view.setClickable(false);
			if (view instanceof ViewGroup) {
				removeAllClickListener((ViewGroup) view);
			}
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		BaseTweetView tweetView = (BaseTweetView) mTweetAdapter.getView(position, convertView, parent);
		removeAllClickListener(tweetView);
		tweetView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Tweet tweet = (Tweet) getItem(position);
				onClickTweet(tweet);
			}
		});
		return tweetView;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mTweetAdapter.areAllItemsEnabled();
	}

	@Override
	public boolean isEnabled(int position) {
		return mTweetAdapter.isEnabled(position);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mTweetAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mTweetAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public int getCount() {
		return mTweetAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mTweetAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mTweetAdapter.getItemId(position);
	}

	@Override
	public boolean hasStableIds() {
		return mTweetAdapter.hasStableIds();
	}

	@Override
	public int getItemViewType(int position) {
		return mTweetAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mTweetAdapter.getViewTypeCount();
	}

	@Override
	public boolean isEmpty() {
		return mTweetAdapter.isEmpty();
	}

	public TweetTimelineListAdapter getTweetAdapter() {
		return mTweetAdapter;
	}
}
