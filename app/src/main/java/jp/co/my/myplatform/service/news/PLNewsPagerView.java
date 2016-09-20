package jp.co.my.myplatform.service.news;

import android.graphics.Color;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.view.SlidingTabLayout;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.model.PLDatabase;
import jp.co.my.myplatform.service.model.PLNewsGroupModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsPagerView extends PLContentView {

	private List<PLNewsGroupModel> mNewsGroupArray;
	private PLSiteFetcher mSiteFetcher;

	public PLNewsPagerView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_news_pager, this);
		mNewsGroupArray = new ArrayList<>();

		mSiteFetcher = new PLSiteFetcher();
		loadNewsGroup();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mSiteFetcher.cancelAllRequest();
	}

	private void loadNewsGroup() {
		mNewsGroupArray = SQLite.select().from(PLNewsGroupModel.class).queryList();
		if (mNewsGroupArray.size() > 0) {
			createViewPager();
			return;
//			Delete.table(PLNewsGroupModel.class);
//			Delete.table(PLNewsSiteModel.class);
		}

		fetchNewsGroup();
	}

	private void fetchNewsGroup() {
		mSiteFetcher.startRequest(new PLSiteFetcher.PLSiteCallbackListener() {
			 @Override
			 public void finishedRequest(ArrayList<PLNewsGroupModel> groupArray, ArrayList<PLNewsSiteModel> siteArray) {
				 if (groupArray.size() == 0 || siteArray.size() == 0) {
					 return;
				 }
				 for (PLNewsSiteModel site : siteArray) {
					 PLNewsGroupModel group = groupArray.get(site.getGroupNo() - 1);
					 group.getSiteArray().add(site);
					 site.associateGroup(group);
				 }
				 mNewsGroupArray = groupArray;
				 PLDatabase.saveModelList(groupArray);
				 PLDatabase.saveModelList(siteArray);
				 createViewPager();
			 }
		 });
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
			PLNewsListView listView = new PLNewsListView(getContext());
			listView.setGroupModel(mNewsGroupArray.get(position));
			collection.addView(listView);
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
