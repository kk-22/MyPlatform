package jp.co.my.myplatform.service.popover;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import jp.co.my.myplatform.R;

public class PLListPopover extends PLPopoverView {

	private ListView mListView;

	public PLListPopover(String[] titles,
						 AdapterView.OnItemClickListener clickListener) {
		super(R.layout.popover_title_list);

		ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
				R.layout.cell_simple_title,
				titles);

		mListView = (ListView) findViewById(R.id.title_list);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(clickListener);
	}
}