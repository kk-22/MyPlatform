package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Comparator;
import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsListAdapter extends ArrayAdapter<PLNewsPageModel> {

	private PLNewsListComparator mComparator;

	public PLNewsListAdapter(Context context) {
		super(context, R.layout.cell_news_page);
		mComparator = new PLNewsListComparator();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PLNewsPageModel page = getItem(position);
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			view = inflater.inflate(R.layout.cell_news_page, parent, false);
		}

		TextView dateText = (TextView) view.findViewById(R.id.date_text);
		TextView siteText = (TextView) view.findViewById(R.id.site_name_text);
		TextView pageText = (TextView) view.findViewById(R.id.page_title_text);

		dateText.setText(page.getPostedString());
		pageText.setText(page.getTitle());
		PLNewsSiteModel site = page.getSiteForeign().load();
		if (site != null) {
			siteText.setText(site.getName());
		}
		setBackgroundColorToView(view, page.isAlreadyRead());

		return view;
	}

	@Override
	public PLNewsPageModel getItem(int position) {
		return super.getItem(position);
	}

	public static void setBackgroundColorToView(View view, boolean isAlreadyRead) {
		TextView pageText = (TextView) view.findViewById(R.id.page_title_text);
		if (isAlreadyRead) {
			pageText.setTextColor(Color.LTGRAY);
		} else {
			pageText.setTextColor(Color.BLACK);
		}
	}

	public void renewalAllPage(List<PLNewsPageModel> pageArray) {
		clear();
		addAll(pageArray);
		sort(mComparator);
		notifyDataSetChanged();
	}

	private class PLNewsListComparator implements Comparator<PLNewsPageModel> {
		public int compare(PLNewsPageModel pageA, PLNewsPageModel pageB) {
			if (pageA.getPostedDate() == null) {
				return 1;
			}
			if (pageB.getPostedDate() == null) {
				return -1;
			}
			if (pageA.getPostedDate().before(pageB.getPostedDate())) {
				return 1;
			} else if (pageA.getPostedDate().after(pageB.getPostedDate())) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
