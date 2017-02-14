package jp.co.my.myplatform.service.mysen;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.PointF;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;

public class PLMSUserInterface implements View.OnTouchListener, View.OnDragListener {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private ArrayList<PLMSUnitView> mUnitArray;

	private PLMSUnitView mMovingUnit;

	public PLMSUserInterface(PLMSInformationView information, PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mInformation = information;
		mField = field;
		mUnitArray = unitArray;

		initEvent();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				PLMSUnitView unitView = (PLMSUnitView) v;
				View.DragShadowBuilder shadow = new View.DragShadowBuilder(unitView);
				// API24 から startDragAndDrop
				unitView.startDrag(null, shadow, unitView, 0);
				unitView.setVisibility(View.GONE);

				mMovingUnit = unitView;
				break;
			}
			case MotionEvent.ACTION_UP: {
				// タップ直後に指を話すと呼ばれる。キャンセル扱い。
				mMovingUnit.setVisibility(View.VISIBLE);
				mMovingUnit = null;
				break;
			}
		}
		return true;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		PLMSLandView landView = (PLMSLandView) v;
		switch (event.getAction())	{
			case DragEvent.ACTION_DRAG_STARTED:
			case DragEvent.ACTION_DRAG_ENTERED:
			case DragEvent.ACTION_DRAG_LOCATION:
			case DragEvent.ACTION_DRAG_EXITED:
				return true;
			case DragEvent.ACTION_DROP: {
				MYLogUtil.outputLog("finish");

				float halfSize = mMovingUnit.getWidth() / 2;
				PointF landPoint = mField.pointOfLandView(landView);
				mMovingUnit.setVisibility(View.VISIBLE);
				// 指を離した位置からLandの位置へ移動
				PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat(
						"x",
						landPoint.x + event.getX() - halfSize,
						landPoint.x);
				PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat(
						"y",
						landPoint.y + event.getY() - halfSize,
						landPoint.y);
				ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
						mMovingUnit, holderX, holderY);
				objectAnimator.setDuration(100);
				objectAnimator.start();
				mMovingUnit = null;
				return true;
			}
			default:
				break;
		}
		return false;
	}

	private void initEvent() {
		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(this);
		}
		for (PLMSLandView landView : mField.getLandArray()) {
			landView.setOnDragListener(this);
		}
	}
}
