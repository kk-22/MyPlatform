package jp.co.my.myplatform.popover;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.co.my.myplatform.R;

public class PLListPopover extends PLPopoverView {

	private ListView mListView;

	// Deprecated. Use showItems method,
	public PLListPopover(String[] titles,
						 AdapterView.OnItemClickListener clickListener) {
		this();
		createList(titles);
		mListView.setOnItemClickListener(clickListener);
	}

	private PLListPopover() {
		super(R.layout.popover_title_list);
	}

	public static void showItems(final PLListItem... listItems) {
		final PLListPopover popover = new PLListPopover();
		int numberOfItems = listItems.length;
		String[] titles = new String[numberOfItems];
		for (int i = 0; i < numberOfItems; i++) {
			titles[i] = listItems[i].mTitle;
		}
		popover.createList(titles);

		popover.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				popover.removeFromContentView();

				PLListItem listItem = listItems[position];
				listItem.mClickedRunnable.run();
			}
		});
		popover.showPopover();
	}

	private void createList(String[] titles) {
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = (ListView) findViewById(R.id.title_list);
		mListView.setAdapter(adapter);
	}

	public static class PLListItem {
		String mTitle;
		Runnable mClickedRunnable;
		public PLListItem(String title, Runnable clickedRunnable) {
			mTitle = title;
			mClickedRunnable = clickedRunnable;
		}
	}
}
