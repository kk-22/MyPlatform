package jp.co.my.myplatform.news;

import android.graphics.Color;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.view.SlidingTabLayout;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.database.PLDatabase;
import jp.co.my.myplatform.database.PLModelContainer;

public class PLNewsPagerView extends PLContentView {

	private Handler mHandler;
	private List<PLNewsGroupModel> mNewsGroupArray;
	private PLSiteFetcher mSiteFetcher;

	public PLNewsPagerView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_news_pager, this);
		mNewsGroupArray = new ArrayList<>();

		mHandler = new Handler();
		mSiteFetcher = new PLSiteFetcher();
		loadNewsGroup();
	}

	@Override
	public void viewWillDisappear() {
		super.viewWillDisappear();
		mSiteFetcher.cancelAllRequest();
		PLCoreService.getVolleyHelper().cancelRequest(this.getClass());
	}

	private void loadNewsGroup() {
		BaseModelQueriable<PLNewsGroupModel> query = SQLite.select().from(PLNewsGroupModel.class);
		PLModelContainer<PLNewsGroupModel> container = new PLModelContainer<>(query);
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLNewsGroupModel>() {
			@Override
			public void onLoad(List<PLNewsGroupModel> modelList) {
				mNewsGroupArray = modelList;
				if (mNewsGroupArray.size() == 0) {
					fetchNewsGroup();
					return;
				}

				createViewPager();
				// ニュースページ取得時用にキャッシュ作成
				for (PLNewsGroupModel group : mNewsGroupArray) {
					group.getBadWordContainer().loadList(null, null);
				}
			}
		});
	}

	private void fetchNewsGroup() {
		mSiteFetcher.startRequest(new PLSiteFetcher.PLSiteCallbackListener() {
			 @Override
			 public void finishedRequest(ArrayList<PLNewsGroupModel> groupArray, ArrayList<ArrayList<PLNewsSiteModel>> siteListArray) {
				 if (mNewsGroupArray == null || mSiteFetcher == null) {
					 return;
				 }
				 int groupCount = groupArray.size();
				 int siteGroupCount = siteListArray.size();
				 if (groupCount == 0 || siteGroupCount == 0 || groupCount != siteGroupCount) {
					 MYLogUtil.showErrorToast("count error. group=" +groupCount +" site=" +siteGroupCount);
					 return;
				 }

				 PLDatabase.saveModelList(groupArray);
				 for (int i = 0; i < groupCount; i++) {
					 ArrayList<PLNewsSiteModel> siteArray = siteListArray.get(i);
					 PLNewsGroupModel group = groupArray.get(i);
					 group.getSiteContainer().setModelList(siteArray);

					 int siteCount = siteArray.size();
					 for (int j = 0; j < siteCount; j++) {
						 PLNewsSiteModel site = siteArray.get(j);
						 site.associateGroup(group);
					 }
					 PLDatabase.saveModelList(siteArray);
				 }
				 mNewsGroupArray = groupArray;
				 createViewPager();

				 fetchBadWord();
			 }
		 });
	}

	public void fetchBadWord() {
		(new PLBadWordFetcher()).startRequest(new PLBadWordFetcher.PLWordCallbackListener() {
			@Override
			public void finishedBadWordRequest(ArrayList<ArrayList<PLBadWordModel>> wordListArray) {
				if (mNewsGroupArray == null || wordListArray == null) {
					return;
				}
				int wordListCount = wordListArray.size();
				for (int i = 0; i < wordListCount; i++) {
					ArrayList<PLBadWordModel> wordArray = wordListArray.get(i);
					PLNewsGroupModel group = mNewsGroupArray.get(i);
					group.getBadWordContainer().setModelList(wordArray);

					int siteCount = wordArray.size();
					for (int j = 0; j < siteCount; j++) {
						PLBadWordModel site = wordArray.get(j);
						site.associateGroup(group);
					}
					PLDatabase.saveModelList(wordArray);
				}
				MYLogUtil.showToast("Update bad word");
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
			PLNewsListView listView = new PLNewsListView(PLNewsPagerView.this, mNewsGroupArray.get(position));
			collection.addView(listView);
			return listView;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView((View) object);
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
