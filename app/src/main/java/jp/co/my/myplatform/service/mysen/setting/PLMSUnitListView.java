package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;


public class PLMSUnitListView extends LinearLayout {

	private MYArrayList<PLMSUnitData> showingUnitArray;

	public PLMSUnitListView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER);
	}

	public PLMSUnitListView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSUnitListView(Context context) {
		this(context, null);
	}

	public void loadUnitList(MYArrayList<PLMSUnitData> unitDataArray) {
		removeAllViews();
		showingUnitArray = unitDataArray;

		for (int i = 0; i < 6; i++) {
			LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.mysen_cell_unit, null);
			if (i < unitDataArray.size()) {
				PLMSUnitData unitData = unitDataArray.get(i);
				ImageView imageView = (ImageView) linearLayout.findViewById(R.id.unit_image);
				imageView.setImageBitmap(unitData.getImage(getContext()));

				TextView textView = (TextView) linearLayout.findViewById(R.id.unit_name_text);
				textView.setText(unitData.getUnitModel().getName());
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					0, LayoutParams.MATCH_PARENT);
			params.weight = 1;
			addView(linearLayout, params);
		}
	}

	// getter
	public MYArrayList<PLMSUnitData> getShowingUnitArray() {
		return showingUnitArray;
	}
}
