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
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.core.PLWakeLockManager;
import jp.co.my.myplatform.news.PLBadWordModel;
import jp.co.my.myplatform.database.PLDatabase;
import jp.co.my.myplatform.news.PLNewsGroupModel;
import jp.co.my.myplatform.news.PLNewsPageModel;
import jp.co.my.myplatform.news.PLNewsSiteModel;
import jp.co.my.myplatform.popover.PLConfirmationPopover;
import jp.co.my.myplatform.popover.PLListPopover;

public class PLDebugContent extends PLContentView {

	private ListView mListView;
	private PLDebugListAdapter mAdapter;

	public PLDebugContent() {
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
							PLDebugContent.this.removeTopPopover();
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
		{
			itemList.add(new PLDebugTitleItem("Log"));
			itemList.add(new PLDebugButtonItem("表示", new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (MYLogUtil.openLogFile()) {
						PLCoreService.getNavigationController().hideNavigationIfNeeded();
					}
				}
			}));
			itemList.add(new PLDebugButtonItem("削除", new OnClickListener() {
				@Override
				public void onClick(View v) {
					new PLConfirmationPopover("メモを削除", new PLConfirmationPopover.PLConfirmationListener() {
						@Override
						public void onClickButton(boolean isYes) {
							MYLogUtil.deleteLogFile();
						}
					}, null);
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
}
