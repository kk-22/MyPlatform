package jp.co.my.myplatform.service.mysen;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.PointF;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;

import static android.view.View.GONE;

public class PLMSUserInterface implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private PLMYAreaManager mAreaManager;
	private ArrayList<PLMSUnitView> mUnitArray;

	private PLMSUnitView mMovingUnit;
	private PLMSLandView mPrevLandView;
	private PointF mFirstTouchPointF;

	public PLMSUserInterface(PLMSInformationView information, PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mInformation = information;
		mField = field;
		mUnitArray = unitArray;
		mAreaManager = new PLMYAreaManager(field, mUnitArray);

		initEvent();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		MYLogUtil.outputLog(" touch=" +event.getAction() +" x=" +event.getX() +" y=" +event.getY());
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mFirstTouchPointF = new PointF(event.getX(), event.getY());
				beginMoveEvent((PLMSUnitView) v);
			}
			case MotionEvent.ACTION_MOVE: {
				if (mMovingUnit.getVisibility() == GONE) {
					// 既にドラッグ中
					break;
				}
				// startDrag メソッドにより ACTION_CANCEL が呼ばれ、ACTION_UP が呼ばれなくなる
				// ACTION_UP をクリックと判定するために閾値で判定
				if ((Math.abs(mFirstTouchPointF.x - event.getX()) + Math.abs(mFirstTouchPointF.y - event.getY())) > 50) {
					PLMSUnitView unitView = (PLMSUnitView) v;
					View.DragShadowBuilder shadow = new View.DragShadowBuilder(unitView);
					// API24 から startDragAndDrop
					unitView.startDrag(null, shadow, unitView, 0);
					unitView.setVisibility(GONE);
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (!mPrevLandView.equals(mMovingUnit.getLandView())) {
					// 移動後のクリック時のみ移動確定
					movedUnit();
				}
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
				PointF landPoint = mField.pointOfLandView(landView);
				// 指を離した位置からアニメーション移動
				float halfSize = mMovingUnit.getWidth() / 2;
				PointF touchPointF = new PointF(
						landPoint.x + event.getX() - halfSize,
						landPoint.y + event.getY() - halfSize);
				PLMSLandView targetLandView;
				if (landView.isShowingMoveArea() || landView.equals(mMovingUnit.getLandView())) {
					// 離した地形に仮配置
					targetLandView = landView;
				} else {
					// 元の位置に戻す
					targetLandView = mPrevLandView;
				}
				moveUnitWithAnimation(touchPointF, targetLandView);
				return true;
			}
			default:
				break;
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		MYLogUtil.outputLog("onClick");
		PLMSLandView landView = (PLMSLandView) v;
		if (mMovingUnit == null) {
			return;
		}
		PLMSLandView targetLandView;
		if (landView.isShowingMoveArea()) {
			// クリック地形に仮配置
			targetLandView = landView;
		} else {
			// 元の位置に戻す
			targetLandView = mMovingUnit.getLandView();
		}
		moveUnitWithAnimation(mPrevLandView, targetLandView);
	}

	private void beginMoveEvent(PLMSUnitView unitView) {
		if (mMovingUnit != null) {
			return;
		}
		mMovingUnit = unitView;
		mPrevLandView = unitView.getLandView();
		mAreaManager.showMoveArea(unitView);
	}

	private void finishMoveEvent() {
		if (mMovingUnit == null) {
			return;
		}
		mMovingUnit.setVisibility(View.VISIBLE);
		mMovingUnit = null;
		mPrevLandView = null;
		mAreaManager.hideAllMoveArea();
	}

	private void moveUnitWithAnimation(PLMSLandView fromLandView, PLMSLandView toLandView) {
		PointF fromPointF = mField.pointOfLandView(fromLandView);
		moveUnitWithAnimation(fromPointF, toLandView);
	}

	private void moveUnitWithAnimation(PointF fromPointF, PLMSLandView toLandView) {
		PointF targetPointF = mField.pointOfLandView(toLandView);
		PropertyValuesHolder holderX = PropertyValuesHolder.ofFloat(
				"x", fromPointF.x, targetPointF.x);
		PropertyValuesHolder holderY = PropertyValuesHolder.ofFloat(
				"y", fromPointF.y, targetPointF.y);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
				mMovingUnit, holderX, holderY);
		objectAnimator.setDuration(100);
		objectAnimator.start();

		if (toLandView.isShowingMoveArea()) {
			// イベント継続
			mPrevLandView = toLandView;
			mMovingUnit.setVisibility(View.VISIBLE);
		} else {
			finishMoveEvent();
		}
	}

	private void movedUnit() {
		mMovingUnit.moveToLand(mPrevLandView);
		finishMoveEvent();
	}

	private void initEvent() {
		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(this);
		}
		for (PLMSLandView landView : mField.getLandArray()) {
			landView.setOnDragListener(this);
			landView.setOnClickListener(this);
		}
	}
}
