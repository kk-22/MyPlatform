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

	private PLMSUnitView mMovingUnitView;
	private PLMSLandView mPrevLandView;			// mMovingUnitView の現在の仮位置
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
		PLMSUnitView unitView = (PLMSUnitView) v;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mFirstTouchPointF = new PointF(event.getX(), event.getY());
				mInformation.updateForUnitData(unitView);
			}
			case MotionEvent.ACTION_MOVE: {
				if (unitView.getVisibility() == GONE) {
					// 同ユニットを既にドラッグイベント中
					break;
				}
				// startDrag メソッドにより ACTION_CANCEL が呼ばれ、ACTION_UP が呼ばれなくなる
				// ACTION_UP をクリックと判定するために閾値で判定
				if ((Math.abs(mFirstTouchPointF.x - event.getX()) + Math.abs(mFirstTouchPointF.y - event.getY())) > 10) {
					if (!unitView.equals(mMovingUnitView)) {
						if (mMovingUnitView != null) {
							cancelMoveEvent();
						}
						beginMoveEvent(unitView);
					}

					View.DragShadowBuilder shadow = new View.DragShadowBuilder(unitView);
					// API24 から startDragAndDrop
					unitView.startDrag(null, shadow, unitView, 0);
					unitView.setVisibility(GONE);
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (mMovingUnitView == null) {
					beginMoveEvent(unitView);
				} else if (!unitView.equals(mMovingUnitView)) {
					PLMSLandView touchLandView = unitView.getLandView();
					if (touchLandView.getAttackAreaCover().isShowingMoveArea()) {
						moveUnitForAttack(mMovingUnitView, touchLandView);
					} else {
						// ACTION_DOWN の information 更新のみ
					}
				} else if (mPrevLandView.equals(mMovingUnitView.getLandView())) {
					finishMoveEvent();
				} else {
					// 移動後のユニットクリック時のみ移動確定
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
		PLMSUnitView unitView = landView.getUnitView();
		switch (event.getAction())	{
			case DragEvent.ACTION_DRAG_ENTERED: {
				if (unitView == null || unitView.equals(mMovingUnitView)) {
					// ユニット不在、もしくは移動前のユニット位置を通過
				} else {
					// TODO:敵か味方かで分岐
//					mInformation.updateForUnitData(unitView);
					mInformation.updateForBattleData(mMovingUnitView, unitView);
				}
				break;
			}
			case DragEvent.ACTION_DRAG_EXITED: {
				mInformation.updateForUnitData(mMovingUnitView);
				break;
			}
			case DragEvent.ACTION_DROP: {
				PointF landPoint = mField.pointOfLandView(landView);
				// 指を離した位置からアニメーション移動
				float halfSize = mMovingUnitView.getWidth() / 2;
				PointF touchPointF = new PointF(
						landPoint.x + event.getX() - halfSize,
						landPoint.y + event.getY() - halfSize);
				PLMSLandView targetLandView;
				if (landView.getMoveAreaCover().isShowingMoveArea() || landView.equals(mMovingUnitView.getLandView())) {
					// 離した地形に仮配置
					targetLandView = landView;
				} else {
					// 元の位置に戻す
					targetLandView = mPrevLandView;
				}
				moveUnitWithAnimation(touchPointF, targetLandView);
				break;
			}
			default:
				break;
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		PLMSLandView landView = (PLMSLandView) v;
		MYLogUtil.outputLog(" onClick x=" +landView.getPoint().x +" y=" +landView.getPoint().y);
		if (mMovingUnitView == null) {
			return;
		}
		mInformation.updateForUnitData(mMovingUnitView);
		if (landView.getMoveAreaCover().isShowingMoveArea()) {
			// クリック地形に仮配置
			moveUnitWithAnimation(mPrevLandView, landView);
		} else if (landView.getAttackAreaCover().isShowingMoveArea()) {
			// 何もしない
		} else {
			// 元の位置に戻す
			cancelMoveEvent();
		}
	}

	private void beginMoveEvent(PLMSUnitView unitView) {
		mMovingUnitView = unitView;
		mPrevLandView = unitView.getLandView();
		mAreaManager.showMoveAndAttackArea(unitView);

		mInformation.updateForUnitData(unitView);
	}

	private void cancelMoveEvent() {
		moveUnitWithAnimation(mPrevLandView, mMovingUnitView.getLandView());
	}

	private void finishMoveEvent() {
		if (mMovingUnitView == null) {
			return;
		}
		mMovingUnitView.setVisibility(View.VISIBLE);
		mMovingUnitView = null;
		mPrevLandView = null;
		mAreaManager.hideAllMoveAndAttackArea();
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
				mMovingUnitView, holderX, holderY);
		objectAnimator.setDuration(100);
		objectAnimator.start();

		if (toLandView.getMoveAreaCover().isShowingMoveArea()) {
			// イベント継続
			mPrevLandView = toLandView;
			mMovingUnitView.setVisibility(View.VISIBLE);
		} else {
			finishMoveEvent();
		}
	}

	private void movedUnit() {
		mMovingUnitView.moveToLand(mPrevLandView);
		finishMoveEvent();
	}

	private void moveUnitForAttack(PLMSUnitView moveUnitView, PLMSLandView targetLandView) {
		ArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(moveUnitView);
		// 元の位置も追加
		movableLandArray.add(moveUnitView.getLandView());

		int range = moveUnitView.getUnitData().getBranch().getAttackRange();
		ArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getAroundLandView(targetLandView.getPoint(), range);

		ArrayList<PLMSLandView> moveLandArray = new ArrayList<>();		// 移動先候補
		for (PLMSLandView aroundLandView : targetAroundLandArray) {
			if (movableLandArray.contains(aroundLandView)) {
				moveLandArray.add(aroundLandView);
			}
		}
		moveUnitWithAnimation(mPrevLandView, moveLandArray.get(0));
	}

	private void initEvent() {
		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(this);
		}
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.setOnDragListener(this);
			landView.setOnClickListener(this);
		}
	}
}
