package jp.co.my.myplatform.service.news;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLNewsListAdapter extends ArrayAdapter<PLNewsPageModel> {

	public PLNewsListAdapter(Context context) {
		super(context, 0);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).isPartitionCell() ? 0 : 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		PLNewsPageModel page = getItem(position);
		if (page.isPartitionCell()) {
			return createPartitionCell(page, convertView, parent);
		} else {
			return createNewsPageCell(page, convertView, parent);
		}
	}

	public static void setBackgroundColorToView(View view, boolean isAlreadyRead) {
		TextView pageText = (TextView) view.findViewById(R.id.page_title_text);
		if (isAlreadyRead) {
			pageText.setTextColor(Color.LTGRAY);
		} else {
			pageText.setTextColor(Color.BLACK);
		}
	}

	public void renewalAllPage(List<PLNewsPageModel> pageList) {
		clear();
		addAll(pageList);
		notifyDataSetChanged();
	}

	private View createPartitionCell(PLNewsPageModel page, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.cell_news_partition, parent, false);
		}
		TextView dateText = (TextView) convertView.findViewById(R.id.date_text);
		TextView titleText = (TextView) convertView.findViewById(R.id.partition_title_text);

		dateText.setText(page.getPostedString());
		titleText.setText(page.getTitle());
		return convertView;
	}

	private View createNewsPageCell(PLNewsPageModel page, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(getContext());
			convertView = inflater.inflate(R.layout.cell_news_page, parent, false);
		}
		TextView dateText = (TextView) convertView.findViewById(R.id.date_text);
		TextView siteText = (TextView) convertView.findViewById(R.id.site_name_text);
		TextView pageText = (TextView) convertView.findViewById(R.id.page_title_text);

		dateText.setText(page.getPostedString());
		pageText.setText(page.getTitle());
		PLNewsSiteModel site = page.getSiteForeign().load();
		if (site != null) {
			siteText.setText(site.getName());
		}
		setBackgroundColorToView(convertView, page.isAlreadyRead());
		return convertView;
	}

	public static void sortList(List<PLNewsPageModel> pageList) {
		Collections.sort(pageList, sComparator);
	}

	public static Comparator<PLNewsPageModel> sComparator = new Comparator<PLNewsPageModel>() {
		@Override
		public int compare(PLNewsPageModel pageA, PLNewsPageModel pageB) {
			if (pageB.getPositionNo() < pageA.getPositionNo()) {
				return 1; // Noが低い方が上
			} else if (pageA.getPositionNo() < pageB.getPositionNo()) {
				return -1;
			}

			if (pageA.getPostedDate() == null) {
				return 1;
			} else if (pageB.getPostedDate() == null) {
				return -1;
			}
			if (pageA.getPostedDate().before(pageB.getPostedDate())) {
				return 1; // 日付が新しい方が上
			} else if (pageA.getPostedDate().after(pageB.getPostedDate())) {
				return -1;
			} else {
				return 0;
			}
		}
	};
}
