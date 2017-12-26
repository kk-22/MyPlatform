package jp.co.my.myplatform.debug;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;

public class PLDebugListAdapter extends ArrayAdapter<PLDebugAbstractItem> {

	public PLDebugListAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {
		PLDebugAbstractItem item = getItem(position);
		if (PLDebugTitleItem.class.isInstance(item)) {
			return 0;
		} else if (PLDebugValueItem.class.isInstance(item)) {
			return 1;
		} else if (PLDebugButtonItem.class.isInstance(item)) {
			return 2;
		}
		MYLogUtil.showErrorToast("Unknown item class:" +item.getClass().getName());
		return -1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PLDebugAbstractItem item = getItem(position);
		if (convertView == null) {
			convertView = item.createCell(getContext(), parent);
		}
		return item.updateView(convertView, parent);
	}

	public void renewalAllPage(List<PLDebugAbstractItem> itemList) {
		clear();
		addAll(itemList);
		notifyDataSetChanged();
	}
}
