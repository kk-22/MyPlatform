package jp.co.my.myplatform.service.browser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLWebPageModel;

public class PLBookmarkAdapter extends ArrayAdapter<PLWebPageModel> {

	private Context mContext;
	private LayoutInflater mInflater;
	private PLBookmarkList mBookmarkList;

	public PLBookmarkAdapter(Context context, PLBookmarkList bookmarkList) {
		super(context, 0);
		mContext = context;
		mInflater = LayoutInflater.from(context);
		mBookmarkList = bookmarkList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.cell_bookmark, parent, false);
		}
		final PLWebPageModel pageModel = getItem(position);
		((TextView) convertView.findViewById(R.id.title_text)).setText(pageModel.getTitle());

		convertView.findViewById(R.id.action_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mBookmarkList.displayActionView(pageModel, v);
			}
		});
		return convertView;
	}
}
