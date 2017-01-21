package jp.co.my.myplatform.service.twitter;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.TwitterListTimeline;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;

public class PLTWListView extends PLContentView {

	ListView mListVIew;

	public PLTWListView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_tweet_list, this);
		mListVIew = (ListView) findViewById(R.id.tweet_list);

		mListVIew.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,
									View view, int pos, long id) {

				MYLogUtil.showToast("onItemClick");
			}
		});

		updateList();
	}

	private void updateList() {
		Callback<TimelineResult<Tweet>> callback2 = new Callback<TimelineResult<Tweet>>() {
			@Override
			public void success(Result<TimelineResult<Tweet>> result) {
			}

			@Override
			public void failure(TwitterException e) {
				MYLogUtil.showErrorToast("Timeline fetch error");
			}
		};

		TwitterListTimeline timeline  = new TwitterListTimeline.Builder()
				.slugWithOwnerScreenName("リスト", "dorann217")
				.build();
		timeline.next(null, callback2);
		TweetTimelineListAdapter tweetAdapter = new TweetTimelineListAdapter.Builder(getContext())
				.setTimeline(timeline)
				.setViewStyle(R.style.tw__TweetDarkWithActionsStyle)
				.build();
		PLTWListAdapter myAdapter = new PLTWListAdapter(tweetAdapter);
		mListVIew.setAdapter(myAdapter);
	}
}
