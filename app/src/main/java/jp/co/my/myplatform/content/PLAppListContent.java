package jp.co.my.myplatform.content;

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
import jp.co.my.myplatform.core.PLCoreService;

public class PLAppListContent extends PLContentView {

	ListView mListView;
	ArrayList<String> mPackageNameArray;

	public PLAppListContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_app_list, this);

		mPackageNameArray = getForegroundAppList(5);
		String[] titles = mPackageNameArray.toArray(new String[mPackageNameArray.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = findViewById(R.id.app_list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				String packageName = mPackageNameArray.get(position);
			 	Uri uri = Uri.parse("package:" +packageName);
				Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				getContext().startActivity(intent);

				PLCoreService.getNavigationController().hideNavigationIfNeeded();
			}
		});
	}

	public ArrayList<String> getForegroundAppList(int size) {
		Context context = getContext();
		ArrayList<String> nameList = new ArrayList<>();
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
					if (!nameList.contains(packageName)) {
						// 古い順に取得されるので先頭から追加して新しい順に直す
						nameList.add(0, packageName);
					}
				}
			}
			interval *= 10;
		}
		return nameList;
	}
}
