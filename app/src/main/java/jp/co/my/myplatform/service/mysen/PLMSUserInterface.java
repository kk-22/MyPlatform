package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

import static android.view.View.GONE;

public class PLMSUserInterface implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private PLMSAreaManager mAreaManager;
	private ArrayList<PLMSUnitView> mUnitArray;
	private PLMSArmyStrategy mTargetArmy;				// 操作対象のArmy
	private PLMSAnimationManager mAnimationManager;

	private PointF mTouchDownPointF;
	private long mTouchDownTimeMillis; // ダブルタップ判定用
	private long mPrevTouchTimeMillis; // ダブルタップ判定用
	private boolean mIsDragging;

	private PLMSUnitView mMovingUnitView;
	private PLMSLandView mTempLandView;					// mMovingUnitView の現在の仮位置
	private PLMSLandRoute mTempRoute;
	private MYArrayList<PLMSLandRoute> mPrevRouteArray;

	public PLMSUserInterface(PLMSArgument argument, PLMSArmyStrategy armyStrategy) {
		mInformation = argument.getInformationView();
		mField = argument.getFieldView();
		mUnitArray = mField.getUnitViewArray();
		mAnimationManager = argument.getAnimationManager();
		mAreaManager = argument.getAreaManager();
		mTargetArmy = armyStrategy;

		mPrevRouteArray = new MYArrayList<>();
	}

	public void enableInterface() {
		mAreaManager.showAvailableArea(mTargetArmy);
		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(this);
		}
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.setOnDragListener(this);
			landView.setOnClickListener(this);
		}
	}

	public void disableInterface() {
		if (mMovingUnitView != null) {
			mMovingUnitView.standby();
			movedUnit(mTempLandView);
		}

		mAreaManager.getAvailableAreaCover().hideAllCoverViews();
		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(null);
		}
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.setOnDragListener(null);
			landView.setOnClickListener(null);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		MYLogUtil.outputLog(" touch=" +event.getAction() +" x=" +event.getX() +" y=" +event.getY());
		PLMSUnitView unitView = (PLMSUnitView) v;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: {
				mTouchDownPointF = new PointF(event.getX(), event.getY());
				mTouchDownTimeMillis = System.currentTimeMillis();
				if (!mAreaManager.getAttackAreaCover().isShowingCover(unitView.getLandView())
						&& !mAreaManager.getSupportAreaCover().isShowingCover(unitView.getLandView())) {
					// 攻撃対象の敵ならACTION_UP時にバトル表示するため更新しない
					mInformation.updateForUnitData(unitView);
				}
			}
			case MotionEvent.ACTION_MOVE: {
				if (unitView.getVisibility() == GONE || !mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
					// 同ユニットを既にドラッグイベント中 or 移動対象でないユニット
					break;
				}
				// startDrag メソッドにより ACTION_CANCEL が呼ばれ、ACTION_UP が呼ばれなくなる
				// ACTION_UP をクリックと判定するために閾値で判定
				if ((Math.abs(mTouchDownPointF.x - event.getX()) + Math.abs(mTouchDownPointF.y - event.getY())) > 10) {
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
					mIsDragging = true;

					mPrevTouchTimeMillis = 0;
					mTouchDownTimeMillis = 0;
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
				if (shouldDoubleTapEvent()) {
					if (mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
						// 未行動ユニットダブルタップで行動終了
						mMovingUnitView.standby();
						mInformation.updateForUnitData(mMovingUnitView);
						mAreaManager.getAvailableAreaCover().hideCoverView(mMovingUnitView.getLandView());
						finishMoveEvent();
					}
					break;
				}
				if (event.getX() < 0 || unitView.getWidth() < event.getX()
						|| event.getY() < 0 || unitView.getHeight() < event.getY()) {
					// タップ位置から指を動かして UnitView 外に移動した場合は何もしない
					break;
				}

				onClickUnitView(unitView);
				break;
			}
		}
		return true;
	}

	private void onClickUnitView(PLMSUnitView unitView) {
		if (mMovingUnitView == null) {
			if (mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
				beginMoveEvent(unitView);
			}
			return;
		}

		mMovingUnitView.setVisibility(View.VISIBLE);
		if (!unitView.equals(mMovingUnitView)) {
			PLMSLandView touchLandView = unitView.getLandView();
			PLMSLandView nextLandView = null;
			if (mAreaManager.getAttackAreaCover().isShowingCover(touchLandView)) {
				nextLandView = getMoveLandForAttack(touchLandView);
			} else if (mAreaManager.getSupportAreaCover().isShowingCover(touchLandView)) {
				nextLandView = getMoveLandForSupportSkill(unitView);
			} else {
				// ACTION_DOWN の information 更新のみ
				return;
			}

			mAnimationManager.addAnimator(mAnimationManager.getMovementAnimation(mMovingUnitView, mTempLandView, nextLandView));
			moveToTempLand(nextLandView);
			if (mAreaManager.getAttackAreaCover().isShowingCover(touchLandView)) {
				if (unitView.equals(mInformation.getRightUnitView())) {
					attackToUnit(nextLandView, unitView);
				} else {
					// 初回タップ時は Info の更新のみ
					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForBattleData(forecast);
				}
			} else {
				if (unitView.equals(mInformation.getRightUnitView())) {
					supportToUnit(nextLandView, unitView);
				} else {
					// 初回タップ時は Info の更新のみ
					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForBattleData(forecast);
				}
			}
		} else if (mTempLandView.equals(mMovingUnitView.getLandView())) {
			finishMoveEvent();
		} else {
			// 移動後のユニットクリック時のみ移動確定
			mMovingUnitView.standby();
			mInformation.updateForUnitData(mMovingUnitView);
			movedUnit(mTempLandView);
		}
	}

	@Override
	public boolean onDrag(View v, final DragEvent event) {
		final PLMSLandView landView = (PLMSLandView) v;
		PLMSUnitView unitView = landView.getUnitView();
		switch (event.getAction())	{
			case DragEvent.ACTION_DRAG_ENTERED: {
				// ルート表示更新
				if (mAreaManager.getMoveAreaCover().isShowingCover(landView)) {
					// ドラッグ地点へのルート表示
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, landView, mPrevRouteArray.getLast()));
				} else if (mAreaManager.canAttackToLandView(landView)) {
					// 攻撃地点へのルート表示
					PLMSLandView nextLandView = getMoveLandForAttack(landView);
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, nextLandView, mPrevRouteArray.getLast()));

					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForBattleData(forecast);
				} else if (mAreaManager.getSupportAreaCover().isShowingCover(landView)) {
					// サポートスキル発動地点へのルート表示
					PLMSLandView nextLandView = getMoveLandForSupportSkill(unitView);
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, nextLandView, mPrevRouteArray.getLast()));

					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForBattleData(forecast);
				} else if (landView.equals(mMovingUnitView.getLandView())) {
					// 攻撃時の位置が変わるため、ルートを更新
					mPrevRouteArray.addOrMoveLast(new PLMSLandRoute(landView));
					mAreaManager.getRouteCover().hideAllCoverViews();
				} else {
					if (mTempRoute != null) {
						// 移動不可地点のため仮位置へのルート表示
						mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mTempRoute));
					} else {
						// 移動不可地点かつ仮位置がないためルート非表示
						mAreaManager.getRouteCover().hideAllCoverViews();
					}
					if (unitView != null) {
						// 味方もしくは攻撃範囲外の敵
						mInformation.updateForUnitData(unitView);
					}
				}
				break;
			}
			case DragEvent.ACTION_DRAG_EXITED: {
				mInformation.updateForUnitData(mMovingUnitView);
				break;
			}
			case DragEvent.ACTION_DROP: {
				mIsDragging = false;
				finishDragEvent(event, landView);
				break;
			}
			case DragEvent.ACTION_DRAG_ENDED: {
				// 全 LandView の数だけ呼ばれるため mIsDragging により1度だけ実行する
				if (mIsDragging) {
					mIsDragging = false;
					// ACTION_DRAG_ENDED 内で setVisibility 実行による ConcurrentModificationException を防ぐ
					mMovingUnitView.post(new Runnable() {
						@Override
						public void run() {
							finishDragEvent(event, landView);
						}
					});
				}
			}
			default:
				break;
		}
		return true;
	}

	private void finishDragEvent(DragEvent event, final PLMSLandView landView) {
		// アニメーションする UnitView　が他の UnitView の裏に隠れないようにする
		mMovingUnitView.bringToFront();
		mMovingUnitView.setVisibility(View.VISIBLE);

		PointF landPoint = mField.pointOfLandView(landView);
		// 指を離した位置からアニメーション移動
		float halfSize = mMovingUnitView.getWidth() / 2;
		PointF touchPointF = new PointF(
				landPoint.x + event.getX() - halfSize,
				landPoint.y + event.getY() - halfSize);
		final PLMSLandView targetLandView;
		Animator.AnimatorListener animatorListener = null;
		if (mAreaManager.getMoveAreaCover().isShowingCover(landView) || landView.equals(mMovingUnitView.getLandView())) {
			// 離した地形に仮配置
			targetLandView = landView;
		} else if (mAreaManager.canAttackToLandView(landView)) {
			targetLandView = mPrevRouteArray.getLast().getLast();
			animatorListener = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					attackToUnit(targetLandView, landView.getUnitView());
				}
			};
		} else if (mAreaManager.getSupportAreaCover().isShowingCover(landView)) {
			targetLandView = mPrevRouteArray.getLast().getLast();
			animatorListener = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					supportToUnit(targetLandView, landView.getUnitView());
				}
			};
		} else {
			// 元の位置に戻す
			targetLandView = mTempLandView;
		}
		Animator animator = mAnimationManager.getMovementAnimation(mMovingUnitView, touchPointF, targetLandView);
		if (animatorListener != null) {
			animator.addListener(animatorListener);
		}
		mAnimationManager.addAnimator(animator);
		if (mAreaManager.getMoveAreaCover().isShowingCover(targetLandView)
				|| mAreaManager.canAttackToLandView(landView)
				|| mAreaManager.getSupportAreaCover().isShowingCover(landView)) {
			// 移動イベント継続
			moveToTempLand(targetLandView);
		} else {
			finishMoveEvent();
		}
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
			mAnimationManager.addAnimator(mAnimationManager.getMovementAnimation(mMovingUnitView, mTempLandView, landView));
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
		mAreaManager.showActionArea(unitView);
	}

	private void cancelMoveEvent() {
		mAnimationManager.addAnimator(
				mAnimationManager.getMovementAnimation(mMovingUnitView, mTempLandView, mMovingUnitView.getLandView()));
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
		final PLMSUnitView attackerUnitView = mMovingUnitView;
		movedUnit(attackerLandView);

		// ダメージ表示
		PLMSBattleForecast forecast = new PLMSBattleForecast(attackerUnitView, attackerLandView,
				defenderUnitView, defenderUnitView.getLandView());
		mAnimationManager.addBattleAnimation(forecast, new Runnable() {
			@Override
			public void run() {
				attackerUnitView.didAction();
			}
		});
	}

	private void supportToUnit(PLMSLandView skillLandView, PLMSUnitView targetUnitView) {
		PLMSUnitView unitView = mMovingUnitView;
		movedUnit(skillLandView);
		PLMSSkillData supportSkill = unitView.getUnitData().getSupportSkillData();
		supportSkill.executeSupportSkill(unitView, skillLandView, targetUnitView);
	}

	private void movedUnit(PLMSLandView targetLandView) {
		mAreaManager.getAvailableAreaCover().hideCoverView(mMovingUnitView.getLandView());
		mMovingUnitView.moveToLand(targetLandView);
		finishMoveEvent();
	}

	// 攻撃地点の取得
	private PLMSLandView getMoveLandForAttack(PLMSLandView targetLandView) {
		int range = mMovingUnitView.getUnitData().getWeapon().getAttackRange();
		MYArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getAroundLandArray(targetLandView.getPoint(), range);
		return getMoveLandViewByRoute(targetAroundLandArray);
	}

	// サポートスキル発動地点の取得
	private PLMSLandView getMoveLandForSupportSkill(PLMSUnitView targetUnitView) {
		MYArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getSupportLandArrayForTarget(targetUnitView, mMovingUnitView);
		return getMoveLandViewByRoute(targetAroundLandArray);
	}

	// 引数で渡した移動候補の中から過去のルートに近い LandView を返す
	private PLMSLandView getMoveLandViewByRoute(MYArrayList<PLMSLandView> candidateLandArray) {
		for (int i = mPrevRouteArray.indexOfLast(); 0 <= i; i--) {
			PLMSLandRoute lastRoute = mPrevRouteArray.get(i);
			PLMSLandView moveLandView = lastRoute.getLast();
			if (candidateLandArray.contains(moveLandView)) {
				// ドラッグ時に通ったルートを使用
				return moveLandView;
			}
		}

		// 移動可能範囲取得
		ArrayList<PLMSLandView> movableLandArray = mAreaManager.getMovableLandArray(mMovingUnitView);
		movableLandArray.add(mMovingUnitView.getLandView()); // 現在地も追加
		// 移動候補を移動可能範囲で絞り込み
		ArrayList<PLMSLandView> moveLandArray = new ArrayList<>();
		for (PLMSLandView candidateLandView : candidateLandArray) {
			if (movableLandArray.contains(candidateLandView)) {
				moveLandArray.add(candidateLandView);
			}
		}

		// 現在地から最も近い地点を優先
		PLMSLandView nearestLandView = null;
		int smallestDistance = 99;
		Point targetPoint = mMovingUnitView.getLandView().getPoint();
		for (PLMSLandView landView : moveLandArray) {
			int distance = MYMathUtil.difference(targetPoint, landView.getPoint());
			if (distance < smallestDistance) {
				smallestDistance = distance;
				nearestLandView = landView;
			}
		}
		return nearestLandView;
	}

	private boolean shouldDoubleTapEvent() {
		long upTimeMillis = System.currentTimeMillis();
		if (mPrevTouchTimeMillis > 0 && upTimeMillis - mPrevTouchTimeMillis < 250) {
			mPrevTouchTimeMillis = 0;
			mTouchDownTimeMillis = 0;
			return true;
		}

		if (upTimeMillis - mTouchDownTimeMillis < 250) {
			mPrevTouchTimeMillis = mTouchDownTimeMillis;
		} else {
			mPrevTouchTimeMillis = 0;
		}
		mTouchDownTimeMillis = 0;
		return false;
	}
}
