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
import com.twitter.sdk.android.core.models.MediaEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.BaseTweetView;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.internal.OverlayImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.browser.PLBaseBrowserView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.popover.PLConfirmationPopover;
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

	private void deleteTweet(final Tweet tweet) {
		if (!tweet.user.screenName.equals("dorann217")) {
			MYLogUtil.showErrorToast("自分以外のツイート");
			return;
		}

		new PLConfirmationPopover("削除", new PLConfirmationPopover.PLConfirmationListener() {
			@Override
			public void onClickButton(boolean isYes) {
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
			}
		}, null);
	}

	private void openTextLink(Tweet tweet) {
		List<String> textUrlList = new ArrayList<>();

		Pattern pattern = Pattern.compile("http[^ \n]*");
		Matcher matcher = pattern.matcher(tweet.text);
		while (matcher.find()) {
			String url = matcher.group();
			textUrlList.add(url);
		}
		if (textUrlList.size() > 0 && !entityUrls(tweet).isEmpty()) {
			// 添付画像と重複するURLは除く
			textUrlList.remove(textUrlList.size() - 1);
		}

		if (textUrlList.isEmpty()) {
			MYLogUtil.showToast("リンクなし");
			return;
		}
		if (textUrlList.size() == 1) {
			String url = textUrlList.get(0);
			openBrowser(url);
			return;
		}
		final String[] titles = textUrlList.toArray(new String[0]);
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				PLCoreService.getNavigationController().getCurrentView().removeTopPopover();
				openBrowser(titles[position]);
			}
		}).showPopover();
	}

	private void openBrowser(String url) {
		PLBaseBrowserView browserView = new PLBaseBrowserView();
		browserView.getCurrentWebView().loadUrl(url);
		PLCoreService.getNavigationController().pushView(browserView);
	}

	private List<String> entityUrls(Tweet tweet) {
		ArrayList<String> urlList = new ArrayList<>();
		if (tweet.extendedEtities == null || tweet.extendedEtities.media == null) {
			return urlList;
		}
		List<MediaEntity> entityList = tweet.extendedEtities.media;
		for (int i = 0; i < entityList.size(); i++) {
			MediaEntity entity = entityList.get(i);
			urlList.add(entity.mediaUrl);
		}
		return urlList;
	}

	private void customizeEvent(ViewGroup baseView, final Tweet tweet) {
		baseView.setOnClickListener(null);

		int childCount = baseView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = baseView.getChildAt(i);
			int id = view.getId();
			// 開発時の調査用コード
//			String className = view.getClass().getSimpleName();
//			if (id == -1) {
//				MYLogUtil.outputLog(" " +className +" no_id");
//			} else {
//				String resourceName = PLCoreService.getContext().getResources().getResourceEntryName(view.getId());
//				MYLogUtil.outputLog(" " +className +" " +resourceName);
//			}

			if (id == R.id.tw__tweet_author_avatar) {
				// ユーザ画像
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						String userName = tweet.user.screenName;
						openBrowser("https://mobile.twitter.com/" + userName);
					}
				});
			} else if (view instanceof OverlayImageView) {
				// 添付画像
				List<String> urlList = entityUrls(tweet);
				if (urlList.isEmpty()) {
					continue;
				}
				int currentIndex = Math.min(i, urlList.size() - 1);
				final String entityUrl = urlList.get(currentIndex);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openBrowser(entityUrl);
					}
				});
			} else if (id == R.id.tw__tweet_text) {
				// ツイート本文
				view.setOnTouchListener(null);
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						openTextLink(tweet);
					}
				});
			} else if (id == R.id.tw__tweet_like_button) {
				// お気に入りボタンのイベントは流用
			} else if (id == R.id.tw__tweet_share_button) {
				// シェアボタン
				view.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						deleteTweet(tweet);
					}
				});
			} else if (view instanceof ViewGroup) {
				// 再帰処理
				customizeEvent((ViewGroup) view, tweet);
			} else {
				// ActivityでないContextを渡す影響で起こる、タップ字のクラッシュ防止のためイベント削除
				// onClickTweetに流す
				view.setClickable(false);
			}
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		BaseTweetView tweetView = (BaseTweetView) mTweetAdapter.getView(position, convertView, parent);
		Tweet tweet = (Tweet) getItem(position);
		customizeEvent(tweetView, tweet);
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
