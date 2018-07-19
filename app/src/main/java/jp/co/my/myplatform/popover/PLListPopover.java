package jp.co.my.myplatform.popover;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Arrays;

import jp.co.my.myplatform.R;

public class PLListPopover extends PLPopoverView {

	private ListView mListView;
	private PLListItem[] mItems;
	private String[] mTitles;

	// Deprecated. Use showItems method,
	public PLListPopover(String[] titles,
						 final AdapterView.OnItemClickListener clickListener) {
		this();
		createList(titles);
		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				clickListener.onItemClick(parent, view, position, id);
				if (getParent() != null) {
					removeFromContentView();
				}
			}
		});
	}

	private PLListPopover() {
		super(R.layout.popover_title_list);
	}

	public static void showItems(final PLListItem... listItems) {
		final PLListPopover popover = new PLListPopover();
		popover.mItems = listItems;

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

	public PLListPopover setMatchWidth() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mListView.getLayoutParams();
		params.width = LinearLayout.LayoutParams.MATCH_PARENT;
		mListView.setLayoutParams(params);
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (getClass() != obj.getClass()) {
			return false;
		}
		PLListPopover popover = (PLListPopover) obj;
		return Arrays.equals(mItems, popover.mItems) || Arrays.equals(mTitles, popover.mTitles);
	}

	private void createList(String[] titles) {
		mTitles = titles;
		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = findViewById(R.id.title_list);
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
