package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.Land.PLMSLandRoute;

import static android.animation.Animator.AnimatorListener;
import static android.animation.PropertyValuesHolder.ofFloat;
import static android.view.View.GONE;

public class PLMSUserInterface implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private PLMSAreaManager mAreaManager;
	private ArrayList<PLMSUnitView> mUnitArray;

	private PointF mFirstTouchPointF;
	private PLMSUnitView mMovingUnitView;
	private PLMSLandView mTempLandView;            // mMovingUnitView の現在の仮位置
	private PLMSLandRoute mTempRoute;
	private MYArrayList<PLMSLandRoute> mPrevRouteArray;

	public PLMSUserInterface(PLMSInformationView information, PLMSFieldView field, ArrayList<PLMSUnitView> unitArray) {
		mInformation = information;
		mField = field;
		mUnitArray = unitArray;
		mAreaManager = new PLMSAreaManager(field, mUnitArray);

		mPrevRouteArray = new MYArrayList<>();

		initEvent();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		MYLogUtil.outputLog(" touch=" +event.getAction() +" x=" +event.getX() +" y=" +event.getY());
		PLMSUnitView unitView = (PLMSUnitView) v;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mFirstTouchPointF = new PointF(event.getX(), event.getY());
				if (!mAreaManager.getAttackAreaCover().isShowingCover(unitView.getLandView())) {
					// 攻撃対象の敵ならACTION_UP時にバトル表示するため更新しない
					mInformation.updateForUnitData(unitView);
				}
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
					break;
				}

				mMovingUnitView.setVisibility(View.VISIBLE);
				if (!unitView.equals(mMovingUnitView)) {
					PLMSLandView touchLandView = unitView.getLandView();
					if (mAreaManager.getAttackAreaCover().isShowingCover(touchLandView)) {
						// 攻撃範囲内の敵タップ
						PLMSLandView nextLandView = moveUnitForAttack(touchLandView);
						moveToTempLand(nextLandView);
						if (unitView.equals(mInformation.getRightUnitView())) {
							// 攻撃
							attackToUnit(nextLandView, unitView);
						} else {
							// 初回タップ時は Info の更新のみ
							mInformation.updateForBattleData(mMovingUnitView, unitView);
						}
					} else {
						// ACTION_DOWN の information 更新のみ
					}
				} else if (mTempLandView.equals(mMovingUnitView.getLandView())) {
					finishMoveEvent();
				} else {
					// 移動後のユニットクリック時のみ移動確定
					movedUnit(mTempLandView);
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
				// ルート表示更新
				if (mAreaManager.getMoveAreaCover().isShowingCover(landView)) {
					// ドラッグ地点へのルート表示
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, landView, mPrevRouteArray.getLast()));
				} else if (mAreaManager.canAttackToLandView(landView)) {
					// 攻撃可能地点へのルート表示
					PLMSLandView nextLandView = moveUnitForAttack(landView);
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, nextLandView, mPrevRouteArray.getLast()));
				} else if (landView.equals(mMovingUnitView.getLandView())) {
					// 攻撃時の位置が変わるため、ルートを更新
					mPrevRouteArray.addOrMoveLast(new PLMSLandRoute(landView));
					mAreaManager.getRouteCover().hideCoverViews();
				} else if (mTempRoute != null) {
					// 移動不可地点のため仮位置へのルート表示
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mTempRoute));
				} else {
					// 移動不可地点かつ仮位置がないためルート非表示
					mAreaManager.getRouteCover().hideCoverViews();
				}

				// Infoの更新
				if (unitView == null || unitView.equals(mMovingUnitView)) {
					// ユニット不在、もしくは移動前のユニット位置を通過
				} else if (mAreaManager.getAttackAreaCover().isShowingCover(landView)) {
					// 攻撃範囲内の敵
					mInformation.updateForBattleData(mMovingUnitView, unitView);
				} else {
					// 味方もしくは攻撃範囲外の敵
					mInformation.updateForUnitData(unitView);
				}
				break;
			}
			case DragEvent.ACTION_DRAG_EXITED: {
				mInformation.updateForUnitData(mMovingUnitView);
				break;
			}
			case DragEvent.ACTION_DROP: {
				// アニメーションする UnitView　が他の UnitView の裏に隠れないようにする
				mMovingUnitView.bringToFront();
				mMovingUnitView.setVisibility(View.VISIBLE);

				PointF landPoint = mField.pointOfLandView(landView);
				// 指を離した位置からアニメーション移動
				float halfSize = mMovingUnitView.getWidth() / 2;
				PointF touchPointF = new PointF(
						landPoint.x + event.getX() - halfSize,
						landPoint.y + event.getY() - halfSize);
				PLMSLandView targetLandView;
				AnimatorListener animatorListener = null;
				if (mAreaManager.getMoveAreaCover().isShowingCover(landView) || landView.equals(mMovingUnitView.getLandView())) {
					// 離した地形に仮配置
					targetLandView = landView;
				} else if (mAreaManager.canAttackToLandView(landView)) {
					targetLandView = mPrevRouteArray.getLast().getLast();
					animatorListener = makeAttackListener(targetLandView, landView.getUnitView());
				} else {
					// 元の位置に戻す
					targetLandView = mTempLandView;
				}
				moveUnitWithAnimation(touchPointF, targetLandView, animatorListener);
				if (mAreaManager.getMoveAreaCover().isShowingCover(targetLandView)
						|| mAreaManager.canAttackToLandView(landView)) {
					// 移動イベント継続
					moveToTempLand(targetLandView);
				} else {
					finishMoveEvent();
				}
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
		if (mAreaManager.getMoveAreaCover().isShowingCover(landView)) {
			// クリック地形に仮配置
			moveUnitWithAnimation(mTempLandView, landView, null);
			moveToTempLand(landView);
		} else if (mAreaManager.getAttackAreaCover().isShowingCover(landView)) {
			// ユニットがいればOnTouchListenerが呼ばれる。このLandView上にユニットはいないので動作なし
		} else {
			// 元の位置に戻す
			cancelMoveEvent();
		}
	}

	private void beginMoveEvent(PLMSUnitView unitView) {
		mMovingUnitView = unitView;
		mTempLandView = unitView.getLandView();
		mPrevRouteArray.addOrMoveLast(new PLMSLandRoute(unitView.getLandView()));
		mAreaManager.showMoveAndAttackArea(unitView);
	}

	private void cancelMoveEvent() {
		moveUnitWithAnimation(mTempLandView, mMovingUnitView.getLandView(), null);
		finishMoveEvent();
	}

	private void finishMoveEvent() {
		if (mMovingUnitView == null) {
			return;
		}
		mMovingUnitView = null;
		mTempLandView = null;
		mTempRoute = null;
		mPrevRouteArray.clear();
		mAreaManager.hideAllAreaCover();
	}

	private void moveUnitWithAnimation(PLMSLandView fromLandView,
									   PLMSLandView toLandView,
									   AnimatorListener animatorListener) {
		PointF fromPointF = mField.pointOfLandView(fromLandView);
		moveUnitWithAnimation(fromPointF, toLandView, animatorListener);
	}

	private void moveUnitWithAnimation(PointF fromPointF,
									   PLMSLandView toLandView,
									   AnimatorListener animatorListener) {
		PointF targetPointF = mField.pointOfLandView(toLandView);
		PropertyValuesHolder holderX = ofFloat(
				"x", fromPointF.x, targetPointF.x);
		PropertyValuesHolder holderY = ofFloat(
				"y", fromPointF.y, targetPointF.y);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
				mMovingUnitView, holderX, holderY);
		objectAnimator.setDuration(100);
		if (animatorListener != null) {
			objectAnimator.addListener(animatorListener);
		}
		objectAnimator.start();
	}

	private void moveToTempLand(PLMSLandView tempLandView) {
		mTempLandView = tempLandView;

		PLMSLandRoute nextRoute;
		PLMSLandRoute lastRoute = mPrevRouteArray.getLast();
		if (lastRoute == null || !tempLandView.equals(lastRoute.getLast())) {
			nextRoute = mAreaManager.showRouteArea(mMovingUnitView, tempLandView, lastRoute);
		} else {
			// 移動先が同じ場合は表示を更新しいない
			nextRoute = lastRoute;
		}
		mPrevRouteArray.clear();
		mPrevRouteArray.addOrMoveLast(nextRoute);
		mTempRoute = nextRoute;
	}

	private void attackToUnit(PLMSLandView attackerLandView,
							  PLMSUnitView defenderUnitView) {
		PLMSUnitView attackerUnitView = mMovingUnitView;
		movedUnit(attackerLandView);

		// 攻撃で敵 UnitView の裏に隠れないように最前面へ
		attackerUnitView.bringToFront();

		// 攻撃アニメーション
		PLMSLandView defenderLandView = defenderUnitView.getLandView();
		Point attackerPoint = attackerLandView.getPoint();
		Point defenderPoint = defenderLandView.getPoint();
		PointF currentPointF = mField.pointOfLandView(attackerLandView);

		int moveWidth = attackerUnitView.getWidth();
		PropertyValuesHolder holderX = null, holderY = null;
		if (attackerPoint.x != defenderPoint.x) {
			int diffX = (attackerPoint.x < defenderPoint.x) ? moveWidth : -moveWidth;
			holderX = PropertyValuesHolder.ofFloat(
					"x",
					currentPointF.x,
					currentPointF.x + diffX,
					currentPointF.x);
		}
		if (attackerPoint.y != defenderPoint.y) {
			int diffY = (attackerPoint.y < defenderPoint.y) ? moveWidth : -moveWidth;
			holderY = PropertyValuesHolder.ofFloat(
					"y",
					currentPointF.y,
					currentPointF.y + diffY,
					currentPointF.y);
		}
		if (holderX == null) {
			ObjectAnimator.ofPropertyValuesHolder(
					attackerUnitView, holderY)
					.setDuration(300).start();
		} else if (holderY == null) {
			ObjectAnimator.ofPropertyValuesHolder(
					attackerUnitView, holderX)
					.setDuration(300).start();
		} else {
			ObjectAnimator.ofPropertyValuesHolder(
					attackerUnitView, holderX, holderY)
					.setDuration(300).start();
		}
	}

	private void movedUnit(PLMSLandView targetLandView) {
		mMovingUnitView.moveToLand(targetLandView);
		finishMoveEvent();
	}

	// 移動先のLandViewを返す
	private PLMSLandView moveUnitForAttack(PLMSLandView targetLandView) {
		PLMSLandView moveLandView = getAttackLandView(targetLandView);
		moveUnitWithAnimation(mTempLandView, moveLandView, null);
		return moveLandView;
	}

	private PLMSLandView getAttackLandView(PLMSLandView targetLandView) {
		// 攻撃可能地点の取得
		int range = mMovingUnitView.getUnitData().getWeapon().getAttackRange();
		ArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getAroundLandArray(targetLandView.getPoint(), range);

		for (int i = mPrevRouteArray.indexOfLast(); 0 <= i; i--) {
			PLMSLandRoute lastRoute = mPrevRouteArray.get(i);
			PLMSLandView moveLandView = lastRoute.getLast();
			if (targetAroundLandArray.contains(moveLandView)) {
				// ドラッグ時に通ったルートを使用
				return moveLandView;
			}
		}

		// 移動可能範囲取得
		ArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(mMovingUnitView);
		movableLandArray.add(mMovingUnitView.getLandView());            // 現在地も追加
		// 攻撃可能かつ移動可能な地点の絞り込み
		ArrayList<PLMSLandView> moveLandArray = new ArrayList<>();        // 移動先候補
		for (PLMSLandView aroundLandView : targetAroundLandArray) {
			if (movableLandArray.contains(aroundLandView)) {
				moveLandArray.add(aroundLandView);
			}
		}
		return moveLandArray.get(0);
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

	private AnimatorListener makeAttackListener(final PLMSLandView attackerLandView,
												final PLMSUnitView defenderUnitView) {
		return new AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
			}

			@Override
			public void onAnimationEnd(Animator animation) {
				attackToUnit(attackerLandView, defenderUnitView);
			}

			@Override
			public void onAnimationCancel(Animator animation) {
			}

			@Override
			public void onAnimationRepeat(Animator animation) {
			}
		};
	}
}
