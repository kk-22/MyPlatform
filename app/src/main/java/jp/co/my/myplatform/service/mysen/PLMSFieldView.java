package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.ArrayList;

import jp.co.my.myplatform.R;


public class PLMSFieldView extends FrameLayout {
	static final int MAX_X = 6;
	static final int MAX_Y = 8;

	private int mLandSize;		// 1マスの縦横サイズ

	private ArrayList<PLMSLandView> mLandArray;
	private ArrayList<PLMSUnitView> mUnitArray;

	public void setUnitArray(ArrayList<PLMSUnitView> unitArray) {
		mUnitArray = unitArray;
	}

	public PLMSFieldView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_field, this);
		mLandArray = new ArrayList<>();
		mUnitArray = new ArrayList<>();


		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				loadField();
				loadUnit();
			}
		});
	}

	public PLMSFieldView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSFieldView(Context context) {
		this(context, null);
	}

	private void loadField() {
		mLandSize = Math.min(getHeight() / MAX_Y, getWidth() / MAX_X);

		LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_linear);
		LinearLayout.LayoutParams spaceParams = new LinearLayout.LayoutParams(0, 0, 1);
		LinearLayout.LayoutParams landParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		landParams.width = mLandSize;
		landParams.height = mLandSize;

		for (int y = 0; y < MAX_Y; y++) {
			LinearLayout horizontalLayout = new LinearLayout(getContext());
			verticalLayout.addView(horizontalLayout);

			Space leftSpace = new Space(getContext());
			horizontalLayout.addView(leftSpace, spaceParams);
			for (int x = 0; x < MAX_X; x++) {
				PLMSLandView landView = new PLMSLandView(getContext());
				landView.setPoint(new Point(x, y));
				horizontalLayout.addView(landView, landParams);
				mLandArray.add(landView);
			}
			Space rightSpace = new Space(getContext());
			horizontalLayout.addView(rightSpace, spaceParams);
		}
	}

	private void loadUnit() {
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		params.height = mLandSize;
		params.width = mLandSize;

		for (PLMSUnitView unitView : mUnitArray) {
			PLMSLandView landView = getLandViewForPoint(unitView.getCurrentPoint());
			landView.addView(unitView, params);
		}
	}

	public PLMSLandView getLandViewForPoint(Point point) {
		return mLandArray.get(point.x + point.y * MAX_X);
	}
}
