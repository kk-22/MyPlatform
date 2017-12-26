package jp.co.my.myplatform.debug;

import android.app.ActivityManager;
import android.content.Context;
import android.os.PowerManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLWakeLockManager;
import jp.co.my.myplatform.database.PLAllModelFetcher;
import jp.co.my.myplatform.news.PLBadWordModel;
import jp.co.my.myplatform.database.PLBaseModel;
import jp.co.my.myplatform.database.PLDatabase;
import jp.co.my.myplatform.news.PLNewsGroupModel;
import jp.co.my.myplatform.news.PLNewsPageModel;
import jp.co.my.myplatform.news.PLNewsSiteModel;
import jp.co.my.myplatform.mysen.PLMSFieldModel;
import jp.co.my.myplatform.mysen.PLMSUnitModel;
import jp.co.my.myplatform.mysen.PLMSWarContent;
import jp.co.my.myplatform.mysen.unit.PLMSSkillModel;
import jp.co.my.myplatform.popover.PLListPopover;
import jp.co.my.myplatform.wikipedia.PLWikipediaPageModel;

public class PLDebugView extends PLContentView {

	private ListView mListView;
	private PLDebugListAdapter mAdapter;

	public PLDebugView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_debug, this);
		mListView = (ListView) findViewById(R.id.debug_list);

		mAdapter = new PLDebugListAdapter(getContext());
		initDebugItem();
		mListView.setAdapter(mAdapter);
	}

	private void initDebugItem() {
		ArrayList<PLDebugAbstractItem> itemList = new ArrayList<>();
		{
			itemList.add(new PLDebugTitleItem("Database"));
			itemList.add(new PLDebugButtonItem("other DB", new OnClickListener() {
				@Override
				public void onClick(View v) {
					String[] titles = {"データベース消去", "PLNewsPageModel", "Group Site BadWord", "PLBadWordModel"};
					new PLListPopover(titles, new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							PLDebugView.this.removeTopPopover();
							if (position == 0) {
//								PLCoreService.getNavigationController().pushView(PLAppListView.class);
//								getContext().deleteDatabase(PLDatabase.NAME + ".db");
//								MYLogUtil.outputLog("Delete DB. Please restart app");
								MYLogUtil.outputLog("Can not delete DB");
								return;
							}

							ArrayList<Class> classArray = new ArrayList<>();
							if (position == 1) {
								classArray.add(PLNewsPageModel.class);
							} else if (position == 2) {
								classArray.add(PLBadWordModel.class);
								classArray.add(PLNewsSiteModel.class);
								classArray.add(PLNewsGroupModel.class);
							} else {
								classArray.add(PLBadWordModel.class);
							}
							deleteTable(classArray);
						}
					}).showPopover();
				}
			}));
			itemList.add(new PLDebugButtonItem("wikipedia", new OnClickListener() {
				@Override
				public void onClick(View v) {
					String[] titles = {"PLWikipediaPageModel"};
					new PLListPopover(titles, new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							PLDebugView.this.removeTopPopover();
							ArrayList<Class> classAray = new ArrayList<>();
							classAray.add(PLWikipediaPageModel.class);
							deleteTable(classAray);
						}
					}).showPopover();
				}
			}));
			itemList.add(new PLDebugButtonItem("MySen", new OnClickListener() {
				@Override
				public void onClick(View v) {
					PLListPopover.showItems(
							new PLListPopover.PLListItem("Unit & Skill & Field 削除して再取得", new Runnable() {
								@Override
								public void run() {
									List<Class> classList = Arrays.<Class>asList(PLMSFieldModel.class, PLMSSkillModel.class, PLMSUnitModel.class);
									deleteTable(classList);
									PLAllModelFetcher fetcher = new PLAllModelFetcher(classList, new PLAllModelFetcher.PLAllModelFetcherListener() {
										@Override
										public void finishedAllFetchModels(MYArrayList<MYArrayList<PLBaseModel>> modelArrays) {
											if (modelArrays == null) {
												MYLogUtil.showErrorToast("UnitModel or SkillModel or FieldModel の取得に失敗");
												return;
											}
											MYArrayList<PLBaseModel> fieldArray = modelArrays.get(0);
											PLDatabase.saveModelList(fieldArray);
											saveMysenUnitAndSkill(modelArrays.get(2), modelArrays.get(1));

											MYLogUtil.showToast("unit skill field の保存完了");
											PLCoreService.getNavigationController().pushView(PLMSWarContent.class);
										}
									});
									fetcher.startAllModelFetch();
								}})
							, new PLListPopover.PLListItem("Unit & Skill 削除して再取得", new Runnable() {
								@Override
								public void run() {
									List<Class> classList = Arrays.<Class>asList(PLMSSkillModel.class, PLMSUnitModel.class);
									deleteTable(classList);
									new PLAllModelFetcher(classList, new PLAllModelFetcher.PLAllModelFetcherListener() {
										@Override
										public void finishedAllFetchModels(MYArrayList<MYArrayList<PLBaseModel>> modelArrays) {
											if (modelArrays == null) {
												MYLogUtil.showErrorToast("UnitModel or SkillModel の取得に失敗");
												return;
											}
											saveMysenUnitAndSkill(modelArrays.get(1), modelArrays.get(0));

											MYLogUtil.showToast("unit skill の保存完了");
											PLCoreService.getNavigationController().pushView(PLMSWarContent.class);
										}
									}).startAllModelFetch();
								}
							})
							, new PLListPopover.PLListItem("Field 削除して再取得", new Runnable() {
								@Override
								public void run() {
									List<Class> classList = Collections.<Class>singletonList(PLMSFieldModel.class);
									deleteTable(classList);
									new PLAllModelFetcher(classList, new PLAllModelFetcher.PLAllModelFetcherListener() {
										@Override
										public void finishedAllFetchModels(MYArrayList<MYArrayList<PLBaseModel>> modelArrays) {
											if (modelArrays == null) {
												MYLogUtil.showErrorToast("PLMSFieldModel の取得に失敗");
												return;
											}
											MYArrayList<PLBaseModel> fieldArray = modelArrays.get(0);
											PLDatabase.saveModelList(fieldArray);
											MYLogUtil.showToast("field の保存完了");
											PLCoreService.getNavigationController().pushView(PLMSWarContent.class);
										}
									}).startAllModelFetch();
								}
							})
					);
				}
			}));
		}
		{
			PLWakeLockManager manager = PLWakeLockManager.getInstance();
			PowerManager.WakeLock wakeLock = manager.getWakeLock();
			String isHoldStr = "null";
			if (wakeLock != null) {
				isHoldStr = String.valueOf(manager.getWakeLock().isHeld());
			}
			itemList.add(new PLDebugTitleItem("WakeLock"));
			itemList.add(new PLDebugValueItem("isHold", isHoldStr));
			itemList.add(new PLDebugValueItem(
					"CPU count", Integer.toString(manager.getKeepCPUCount()),
					"screen count", Integer.toString(manager.getKeepScreenCount())));
		}
		{
			itemList.add(new PLDebugTitleItem("Memory"));
			String packageName = getContext().getPackageName();
			ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
			List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
			for (ActivityManager.RunningServiceInfo serviceInfo : services) {
				if (!serviceInfo.service.getPackageName().equals(packageName)) {
					continue;
				}
				MYLogUtil.outputLog("app:service: pkg = " + serviceInfo.service.getPackageName() + " pid = " + serviceInfo.pid);

				int[] processIds = {serviceInfo.pid};
				android.os.Debug.MemoryInfo[] memories = activityManager.getProcessMemoryInfo(processIds);
				for (android.os.Debug.MemoryInfo info : memories) {
					int totalPss = info.getTotalPss();
					itemList.add(new PLDebugValueItem("current", (totalPss / 1000) + "MB"));
				}
				break;
			}
			itemList.add(new PLDebugButtonItem("Exit GC", new OnClickListener() {
				@Override
				public void onClick(View v) {
					System.gc();
					initDebugItem();
				}
			}));
		}
		mAdapter.renewalAllPage(itemList);
	}

	private void deleteTable(List<Class> classArray) {
		DatabaseWrapper database = FlowManager.getDatabase(PLDatabase.NAME).getHelper().getDatabase();
		for (Class klass : classArray) {
			ModelAdapter modelAdapter = FlowManager.getModelAdapter(klass);
			database.execSQL("DROP TABLE IF EXISTS " + modelAdapter.getTableName());
			database.execSQL(modelAdapter.getCreationQuery());
		}
	}

	private void saveMysenUnitAndSkill(MYArrayList<PLBaseModel> unitArray, MYArrayList<PLBaseModel> skillArray) {
		MYArrayList<PLMSSkillModel> skillModelArray = new MYArrayList<>();
		for (PLBaseModel baseModel : skillArray) {
			skillModelArray.add((PLMSSkillModel) baseModel);
		}
		int numberOfUnit = unitArray.size();
		for (int i = 0; i < numberOfUnit; i++) {
			PLMSUnitModel unitModel = (PLMSUnitModel) unitArray.get(i);
			unitModel.setAllSkill(skillModelArray);
		}
		PLDatabase.saveModelList(unitArray);
	}
}