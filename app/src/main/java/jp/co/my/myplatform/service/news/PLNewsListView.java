package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import jp.co.my.myplatform.R;

public class PLNewsListView extends FrameLayout {

	public PLNewsListView(Context context) {
		super(context);
	}
	public PLNewsListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public PLNewsListView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
	public PLNewsListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		LayoutInflater.from(getContext()).inflate(R.layout.view_news_list, this);
	}
}
