package jp.co.my.myplatform.twitter;

import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.activity.PLMainActivity;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.popover.PLTextFieldPopover;
import jp.co.my.myplatform.view.PLFlickGestureRegistrant;
import jp.co.my.myplatform.view.PLSavePositionListView;
import retrofit2.Call;

public class PLTWListContent extends PLContentView {

	private PLSavePositionListView mListView;
	private TwitterListTimeline mTimeline;
	private PLTWListAdapter mAdapter;
	private PLFlickGestureRegistrant mFlickRegistrant;

	public PLTWListContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_tweet_list, this);
		mListView = (PLSavePositionListView) findViewById(R.id.tweet_list);
		addListView(mListView);

		initList();
		setOnClickEvent();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return mFlickRegistrant.transmitTapEvent(ev) ||  super.dispatchTouchEvent(ev);
	}

	private void initList() {
		Callback<TimelineResult<Tweet>> callback = new Callback<TimelineResult<Tweet>>() {
			@Override
			public void success(Result<TimelineResult<Tweet>> result) {
				TweetTimelineListAdapter tweetAdapter = new TweetTimelineListAdapter.Builder(getContext())
						.setTimeline(mTimeline)
						.setViewStyle(R.style.tw__TweetDarkWithActionsStyle)
						.build();
				mAdapter = new PLTWListAdapter(PLTWListContent.this, tweetAdapter);
				mListView.setAdapter(mAdapter);
			}
			@Override
			public void failure(TwitterException e) {
				MYLogUtil.showErrorToast("Timeline fetch error");
			}
		};
		mTimeline = new TwitterListTimeline.Builder()
				.slugWithOwnerScreenName("リスト", "dorann217")
				.build();
		mTimeline.next(null, callback);
	}

	private void setOnClickEvent() {
		findViewById(R.id.refresh_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshList();
			}
		});
		findViewById(R.id.tweet_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showTweetForm();
			}
		});
		findViewById(R.id.tweet_button).setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// ログイン
				Intent intent = new Intent();
				intent.setClassName(getContext().getPackageName(), "jp.co.my.myplatform.activity.PLMainActivity");
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra(PLMainActivity.KEY_LOGIN_TWITTER, true);
				getContext().startActivity(intent);
				PLCoreService.getNavigationController().hideNavigationIfNeeded();
				return true;
			}
		});

		mListView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				MYLogUtil.outputLog("onTouchEvent in twitter");
				return false;
			}
		});
		mFlickRegistrant = new PLFlickGestureRegistrant(getContext(), null, new PLFlickGestureRegistrant.PLFlickGestureListener() {
			@Override
			public void flickToRight() {
				PLCoreService.getNavigationController().popView();
			}

			@Override
			public void flickToLeft() {
			}

			@Override
			public boolean cancelFlickEvent(int direction) {
				return false;
			}
		});
	}

	public void showTweetForm() {
		new PLTextFieldPopover(new PLTextFieldPopover.OnEnterListener() {
			@Override
			public boolean onEnter(View v, String text) {
				// 投稿
				StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();
				Call<Tweet> tweet = statusesService.update(text, null, false, null, null, null, false, null, null);
				tweet.enqueue(new Callback<Tweet>() {
					@Override
					public void success(Result<Tweet> result) {
						MYLogUtil.showToast("tweet success");
						refreshAfterDelay();
					}
					@Override
					public void failure(TwitterException exception) {
						MYLogUtil.showErrorToast("Tweet error");
					}
				});
				return true;
			}
		}).showPopover();
	}

	public void refreshList() {
		mAdapter.getTweetAdapter().refresh(new Callback<TimelineResult<Tweet>>() {
			@Override
			public void success(Result<TimelineResult<Tweet>> result) {
				mListView.setAdapter(mAdapter);
			}
			@Override
			public void failure(TwitterException e) {
				MYLogUtil.showErrorToast("Timeline reload error");
			}
		});
	}

	private void refreshAfterDelay() {
		final Handler mainHandler = new Handler();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e){}
				mainHandler.post(new Runnable() {
					@Override
					public void run() {
						refreshList();
					}
				});
			}
		}).start();
	}
}
