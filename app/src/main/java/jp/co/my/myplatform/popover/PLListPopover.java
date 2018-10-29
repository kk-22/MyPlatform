package jp.co.my.myplatform.popover;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.Arrays;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.layout.PLAbstractLayoutController;

public class PLListPopover extends PLPopoverView {

	private ListView mListView;
	private PLListItem[] mItems;
	private String[] mTitles;
	private int mCellResource;

	// Deprecated. Use showItems method,
	public PLListPopover(String[] titles,
						 final AdapterView.OnItemClickListener clickListener) {
		this(titles);
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

	private PLListPopover(String[] titles) {
		super(R.layout.popover_title_list);
		mTitles = titles;
		mCellResource = R.layout.cell_simple_title;
		mListView = findViewById(R.id.title_list);
	}

	public static void showItems(final PLListItem... listItems) {
		int numberOfItems = listItems.length;
		String[] titles = new String[numberOfItems];
		for (int i = 0; i < numberOfItems; i++) {
			titles[i] = listItems[i].mTitle;
		}

		final PLListPopover popover = new PLListPopover(titles);
		popover.mItems = listItems;
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

	public PLListPopover setBigWidth() {
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mListView.getLayoutParams();
		params.width = (int)(PLCoreService.getNavigationController().getWidth() * 0.75);
		mListView.setLayoutParams(params);
		return this;
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

	@Override
	void didFinishLayout() {
		super.didFinishLayout();
		mListView.post(new Runnable() {
			@Override
			public void run() {
				mListView.setSelection(mListView.getCount() - 1);
			}
		});
	}

	@Override
	public void showPopover(PLAbstractLayoutController layout, PLContentView contentView) {
		if (mListView.getAdapter() == null) {
			ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
					mCellResource,
					mTitles);
			mListView.setAdapter(adapter);
		}
		super.showPopover(layout, contentView);
	}

	public static class PLListItem {
		String mTitle;
		Runnable mClickedRunnable;
		public PLListItem(String title, Runnable clickedRunnable) {
			mTitle = title;
			mClickedRunnable = clickedRunnable;
		}
	}

	public PLListPopover setCellResource(int cellResource) {
		mCellResource = cellResource;
		return this;
	}
}
