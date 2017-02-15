package jp.co.my.myplatform.service.mysen;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;

import jp.co.my.myplatform.R;


public class PLMSFieldView extends FrameLayout {
	static final int MIN_XY = 0;
	static final int MAX_X = 6;
	static final int MAX_Y = 8;

	private LinearLayout mVerticalLinear;

	private int mLandSize;		// 1マスの縦横サイズ
	private int mLeftMargin;	// mVerticalLinearの左の余白
	private int mTopMargin;		// mVerticalLinearの上の余白

	private ArrayList<PLMSLandView> mLandArray;
	private ArrayList<PLMSUnitView> mUnitArray;
	
	public PLMSFieldView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_field, this);
		mVerticalLinear = (LinearLayout) findViewById(R.id.vertical_linear);
		mLandArray = new ArrayList<>();
		mUnitArray = new ArrayList<>();
	}

	public PLMSFieldView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSFieldView(Context context) {
		this(context, null);
	}

	public void putChildViews(ArrayList<PLMSUnitView> unitArray) {
		mUnitArray = unitArray;
		loadField();
		loadUnit();
	}

	private void loadField() {
		mLandSize = Math.min(getHeight() / MAX_Y, getWidth() / MAX_X);
		mLeftMargin = (getWidth() - mLandSize * MAX_X) / 2;
		mTopMargin = (getHeight() - mLandSize * MAX_Y) / 2;

		LinearLayout verticalLayout = (LinearLayout) findViewById(R.id.vertical_linear);
		LinearLayout.LayoutParams landParams = new LinearLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		landParams.width = mLandSize;
		landParams.height = mLandSize;

		for (int y = 0; y < MAX_Y; y++) {
			LinearLayout horizontalLayout = new LinearLayout(getContext());
			verticalLayout.addView(horizontalLayout);

			for (int x = 0; x < MAX_X; x++) {
				PLMSLandView landView = new PLMSLandView(getContext());
				landView.setPoint(new Point(x, y));
				horizontalLayout.addView(landView, landParams);
				mLandArray.add(landView);
			}
		}
	}

	private void loadUnit() {
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		params.height = mLandSize;
		params.width = mLandSize;

		for (PLMSUnitView unitView : mUnitArray) {
			Point point = unitView.getCurrentPoint();
			unitView.setX(mLeftMargin + point.x * mLandSize);
			unitView.setY(mTopMargin + point.y * mLandSize);
			addView(unitView, params);
		}
	}

	// 座標取得
	public PointF pointOfLandView(PLMSLandView landView) {
		Point point = landView.getPoint();
		return new PointF(mLeftMargin + point.x * mLandSize, mTopMargin + point.y * mLandSize);
	}

	public PLMSLandView getLandViewForPoint(Point point) {
		return mLandArray.get(point.x + point.y * MAX_X);
	}

	public ArrayList<PLMSLandView> getLandArray() {
		return mLandArray;
	}
}
