package jp.co.my.myplatform.service.news;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import jp.co.my.common.view.SlidingTabLayout;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsPagerView extends PLContentView {

	private ArrayList<PLNewsGroupModel> mNewsGroupArray;

	public PLNewsPagerView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_news_pager, this);
		mNewsGroupArray = new ArrayList<>();

		loadNewsGroup();
//		createViewPager();
	}

	private void loadNewsGroup() {
		PLSiteFetcher fetcher = new PLSiteFetcher();
		fetcher.startRequest(new PLSiteFetcher.PLCallbackListener() {
			 @Override
			 public void finishedRequest(ArrayList<PLNewsGroupModel> modelArray, ArrayList<PLNewsSiteModel> siteArray) {

			 }
		 });

//		mNewsGroupArray.add(new PLNewsGroupModel());
//		mNewsGroupArray.add(new PLNewsGroupModel());
//		mNewsGroupArray.add(new PLNewsGroupModel());
	}

	private void createViewPager() {
		ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
		viewPager.setAdapter(new PLNewsFragmentPagerAdapter());
		viewPager.setOffscreenPageLimit(mNewsGroupArray.size());

		SlidingTabLayout slidingTabLayout = (SlidingTabLayout) findViewById(R.id.sliding_tab);
		slidingTabLayout.setViewPager(viewPager);
		slidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {
			@Override
			public int getIndicatorColor(int position) {
				// タブ下部のスライドするバーの色
				String colorString = mNewsGroupArray.get(position).getColor();
				return Color.parseColor(colorString);
			}

			@Override
			public int getDividerColor(int position) {
				// タブ間の仕切り線の色
				return Color.WHITE;
			}
		});
	}

	private class PLNewsFragmentPagerAdapter extends PagerAdapter {
		public PLNewsFragmentPagerAdapter() {
			super();
		}

		@Override
		public Object instantiateItem(ViewGroup collection, int position) {
			View subView = new PLNewsListView(getContext());
			collection.addView(subView);
			return collection;
		}

		@Override
		public int getCount() {
			return mNewsGroupArray.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mNewsGroupArray.get(position).getTitle();
		}

		@Override
		public void setPrimaryItem(ViewGroup container, int position, Object object) {
			super.setPrimaryItem(container, position, object);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}
	}
}
