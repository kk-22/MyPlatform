package jp.co.my.myplatform.mysen;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.mysen.army.PLMSArmyStrategy;


public class PLMSFieldView extends FrameLayout {
	static final int MIN_XY = 0;
	static final int MAX_X = 6;
	static final int MAX_Y = 8;

	private LinearLayout mVerticalLayout;

	private int mLandSize;		// 1マスの縦横サイズ
	private int mLeftMargin;	// mVerticalLinearの左の余白
	private int mTopMargin;		// mVerticalLinearの上の余白

	private PLMSArgument mArgument;
	private PLMSFieldModel mFieldModel;
	private MYArrayList<PLMSLandView> mLandViewArray;
	private MYArrayList<PLMSUnitView> mUnitViewArray;
	
	public PLMSFieldView(Context context, AttributeSet attrs, int defStyle){
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_field, this);
		mVerticalLayout = (LinearLayout) findViewById(R.id.vertical_linear);
		mLandViewArray = new MYArrayList<>();
		mUnitViewArray = new MYArrayList<>();
	}

	public PLMSFieldView(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSFieldView(Context context) {
		this(context, null);
	}

	public void initForPreview(PLMSFieldModel fieldModel) {
		if (mFieldModel != null) {
			mLandViewArray.clear();
			mVerticalLayout.removeAllViews();
		}
		mFieldModel = fieldModel;
		loadFieldView();
	}

	public void initForWar(PLMSArgument argument, PLMSFieldModel fieldModel) {
		if (mFieldModel != null) {
			for (PLMSUnitView unitView : mUnitViewArray) {
				removeView(unitView);
			}
			mUnitViewArray.clear();
			mLandViewArray.clear();
			mVerticalLayout.removeAllViews();
		}
		mArgument = argument;
		mFieldModel = fieldModel;

		loadFieldView();
		loadUnitView();
	}

	private void loadFieldView() {
		mLandSize = Math.min(getHeight() / MAX_Y, getWidth() / MAX_X);
		mLeftMargin = (getWidth() - mLandSize * MAX_X) / 2;
		mTopMargin = (getHeight() - mLandSize * MAX_Y) / 2;

		LinearLayout.LayoutParams landParams = new LinearLayout.LayoutParams(mLandSize, mLandSize);

		int[][] landNumbers = mFieldModel.getLandNumbers();
		for (int y = 0; y < MAX_Y; y++) {
			LinearLayout horizontalLayout = new LinearLayout(getContext());
			mVerticalLayout.addView(horizontalLayout);

			for (int x = 0; x < MAX_X; x++) {
				PLMSLandView landView = new PLMSLandView(getContext(), landNumbers[y][x]);
				landView.setPoint(new Point(x, y));
				horizontalLayout.addView(landView, landParams);
				mLandViewArray.add(landView);
			}
		}
	}

	private void loadUnitView() {
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
				FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
		params.height = mLandSize;
		params.width = mLandSize;

		mUnitViewArray = new MYArrayList<>();
		int armyCount = 0, unitCount;
		for (PLMSArmyStrategy army : mArgument.getArmyArray()) {
			MYArrayList<Point> initPointArray = (armyCount == 0) ?
					mFieldModel.getAttackerInitPointArray() : mFieldModel.getDefenderInitPointArray();

			unitCount = 0;
			for (PLMSUnitData unitData : army.getUnitDataArray()) {
				PLMSUnitView unitView = new PLMSUnitView(getContext(), unitData);
				mUnitViewArray.add(unitView);

				Point point = initPointArray.get(unitCount);
				PLMSLandView landView = getLandViewForPoint(point);
				unitView.moveToLand(landView);
				unitView.setX(mLeftMargin + point.x * mLandSize);
				unitView.setY(mTopMargin + point.y * mLandSize);
				addView(unitView, params);
				unitCount++;
			}
			armyCount++;
		}
	}

	// 座標取得
	public PointF pointOfLandView(PLMSLandView landView) {
		Point point = landView.getPoint();
		return new PointF(mLeftMargin + point.x * mLandSize, mTopMargin + point.y * mLandSize);
	}

	public PLMSLandView getLandViewForPoint(Point point) {
		if (point == null) {
			return null;
		}
		if (point.x < 0 || MAX_X <= point.x || point.y < 0 || MAX_Y <= point.y) {
			return null;
		}
		return mLandViewArray.get(point.x + point.y * MAX_X);
	}

	// getter and setter
	public MYArrayList<PLMSLandView> getLandViewArray() {
		return mLandViewArray;
	}

	public MYArrayList<PLMSUnitView> getUnitViewArray() {
		return mUnitViewArray;
	}

	public PLMSFieldModel getFieldModel() {
		return mFieldModel;
	}
}
