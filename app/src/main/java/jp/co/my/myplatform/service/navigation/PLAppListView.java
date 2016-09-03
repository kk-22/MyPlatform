package jp.co.my.myplatform.service.navigation;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;

public class PLAppListView extends PLNavigationView {

	ListView mListView;
	ArrayList<ComponentName> mComponentArray;

	public PLAppListView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.navigation_app_list, this);

		mComponentArray = getForegroundAppList(5);
		int componentCuont = mComponentArray.size();
		String[] titles = new String[componentCuont];
		for (int i = 0; i < componentCuont; i++) {
			ComponentName componentName = mComponentArray.get(i);
			titles[i] = componentName.getPackageName();
		}

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = (ListView) findViewById(R.id.app_list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				ComponentName componentName = mComponentArray.get(position);
			 	Uri uri = Uri.parse("package:" +componentName.getPackageName());
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);

				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
	}

	public ArrayList<ComponentName> getForegroundAppList(int size) {
		Context context = getContext();
		ArrayList<ComponentName> nameList = new ArrayList<ComponentName>();
		long currentTime = System.currentTimeMillis();
		UsageStatsManager usageStats = (UsageStatsManager) context.getSystemService("usagestats");
		long interval = 3600;
		while (nameList.size() < size && interval < 100000000) {
			UsageEvents events = usageStats.queryEvents(currentTime - interval, currentTime);
			while (events.hasNextEvent()) {
				UsageEvents.Event event = new UsageEvents.Event();
				if (events.getNextEvent(event) && event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
					// eventTypeがMOVE_TO_FOREGROUNDのものだけ取ったらgetRunningTasksっぽかったのでフィルタしています。
					// 使いやすいようにcomponentNameに変えています。
					String packageName = event.getPackageName();
					ComponentName name = new ComponentName(packageName, event.getClassName());
					// 古い順に取得されるので先頭から追加して新しい順に直す
					nameList.add(0, name);
				}
			}
			interval *= 10;
		}
		return nameList;
	}
}
