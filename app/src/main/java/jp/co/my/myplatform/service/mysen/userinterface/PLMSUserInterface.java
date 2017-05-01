package jp.co.my.myplatform.service.mysen.userinterface;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSInformationView;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSSupportUnit;
import jp.co.my.myplatform.service.mysen.land.PLMSColorCover;
import jp.co.my.myplatform.service.mysen.land.PLMSLandRoute;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

import static android.view.View.GONE;

public class PLMSUserInterface extends PLMSWarInterface
		implements View.OnTouchListener, View.OnDragListener, View.OnClickListener {
	// View関係
	private PLMSInformationView mInformation;
	private PLMSUnitView mMovingUnitView;
	private PLMSLandView mTempLandView;					// mMovingUnitView の現在の仮位置
	private PLMSLandRoute mTempRoute;
	private MYArrayList<PLMSLandRoute> mPrevRouteArray;

	// カバー
	// TODO: AreaManager にあるカバーもここに移動する
	private PLMSColorCover mAllDangerCover; // 全敵の危険範囲
	private PLMSColorCover mSelectDangerCover; // 選択している敵の危険範囲
	private boolean mIsShowingAllDangerArea; // 全敵の危険範囲表示中なら true
	private MYArrayList<PLMSUnitView> mSelectingDangerAreaUnit; // 攻撃範囲表示対象の PLMSUnitView

	// タップ判定
	private PointF mTouchDownPointF;
	private long mTouchDownTimeMillis; // ダブル・ロングタップ判定用。今回指を押した時間
	private long mPrevTouchTimeMillis; // ダブルタップ判定用。前回指を離した時間
	private boolean mIsDragging;
	private boolean mDidLongTap; // 今回のタップイベント中にロングタップしたらなら true

	public PLMSUserInterface(PLMSArgument argument, PLMSArmyStrategy armyStrategy) {
		super(argument, armyStrategy);
		mInformation = argument.getInformationView();
		mPrevRouteArray = new MYArrayList<>();

		mAllDangerCover = new PLMSColorCover(Color.argb(128, 205, 97, 155));
		mSelectDangerCover = new PLMSColorCover(Color.argb(128, 255, 0, 0));
		mSelectingDangerAreaUnit = new MYArrayList<>();
	}

	@Override
	public void enableInterface() {
		mAreaManager.showAvailableArea(mTargetArmy);
		updateDangerArea();

		for (PLMSUnitView unitView : mUnitArray) {
			unitView.setOnTouchListener(this);
		}
		for (PLMSLandView landView : mField.getLandViewArray()) {
			landView.setOnDragListener(this);
			landView.setOnClickListener(this);
		}
	}

	@Override
	public void disableInterface() {
		if (mMovingUnitView != null) {
			mMovingUnitView.standby();
			movedUnit(mTempLandView);
		}

		mAllDangerCover.hideAllCoverViews();
		mSelectDangerCover.hideAllCoverViews();
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
				mDidLongTap = false;
			}
			case MotionEvent.ACTION_MOVE: {
				if (unitView.getVisibility() == GONE) {
					// 同ユニットを既にドラッグイベント中
					break;
				}

				if (mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
					// startDrag メソッドにより ACTION_CANCEL が呼ばれ、ACTION_UP が呼ばれなくなる
					// ACTION_UP をクリックと判定するために閾値で判定
					float diffPoint = Math.abs(mTouchDownPointF.x - event.getX()) + Math.abs(mTouchDownPointF.y - event.getY());
					if (diffPoint > 10 || shouldLongTapEvent(unitView, event)) {
						beginDragEvent(unitView);
					}
				} else if (shouldLongTapEvent(unitView, event)) {
					// 移動対象ではないユニットをロングタップ
					if (mSelectingDangerAreaUnit.contains(unitView)) {
						mSelectingDangerAreaUnit.remove(unitView);
					} else {
						mSelectingDangerAreaUnit.add(unitView);
					}
					updateDangerArea();
				}
				break;
			}
			case MotionEvent.ACTION_UP: {
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
		if (shouldDoubleTapEvent() && (mMovingUnitView == null || mMovingUnitView.equals(unitView))) {
			// 移動中以外のユニットをダブルタップ時は行動終了しない
			if (mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
				// 未行動ユニットダブルタップで行動終了
				unitView.standby();
				mInformation.updateForUnitData(unitView);
				mAreaManager.getAvailableAreaCover().hideCoverView(unitView.getLandView());
				MYLogUtil.outputLog("finishMoveEvent by 移動前の地点タップ");
				finishMoveEvent();
				finishAction();
			}
			return;
		}

		if (mMovingUnitView == null) {
			if (mAreaManager.getAvailableAreaCover().isShowingCover(unitView.getLandView())) {
				beginMoveEvent(unitView);
			}
			mInformation.updateForUnitData(unitView);
			return;
		}

		mMovingUnitView.setVisibility(View.VISIBLE);
		if (unitView.equals(mMovingUnitView)) {
			// 移動中のユニットをタップ
			if (mTempLandView.equals(mMovingUnitView.getLandView())) {
				// 移動前の地点をタップで移動キャンセル
				MYLogUtil.outputLog("finishMoveEvent by 移動前の地点タップ");
				finishMoveEvent();
			} else if (mInformation.getInfoUnitView().equals(mMovingUnitView)) {
				// 仮移動中のユニットをタップで移動確定
				mMovingUnitView.standby();
				movedUnit(mTempLandView);
				finishAction();
			}
			mInformation.updateForUnitData(unitView);
			return;
		}

		// 移動中のユニットとは別のユニットをタップ
		PLMSLandView touchLandView = unitView.getLandView();
		PLMSLandView nextLandView;
		if (mAreaManager.getAttackAreaCover().isShowingCover(touchLandView)) {
			nextLandView = getMoveLandForAttack(unitView);
		} else if (mAreaManager.getSupportAreaCover().isShowingCover(touchLandView)) {
			nextLandView = getMoveLandForSupportSkill(unitView);
		} else {
			// information 更新のみ
			mInformation.updateForUnitData(unitView);
			return;
		}

		mAnimationManager.addAnimator(mAnimationManager.getMovementAnimation(mMovingUnitView, mTempLandView, nextLandView));
		moveToTempLand(nextLandView);
		if (mAreaManager.getAttackAreaCover().isShowingCover(touchLandView)) {
			PLMSBattleForecast forecast = mInformation.getBattleForecast();
			if (forecast != null && forecast.getRightUnit().getUnitView().equals(unitView)) {
				attackToUnit(mInformation.getBattleForecast());
			} else {
				// 初回タップ時は Info の更新のみ
				forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView, unitView, unitView.getLandView());
				mInformation.updateForBattleData(forecast);
			}
		} else {
			PLMSSupportForecast forecast = mInformation.getSupportForecast();
			if (forecast != null && forecast.getRightUnit().getUnitView().equals(unitView)) {
				supportToUnit(mInformation.getSupportForecast());
			} else {
				// 初回タップ時は Info の更新のみ
				forecast = new PLMSSupportForecast(mMovingUnitView, nextLandView, unitView, unitView.getLandView());
				mInformation.updateForSupportData(forecast);
			}
		}
	}

	@Override
	public boolean onDrag(View v, final DragEvent event) {
		MYLogUtil.outputLog(" onDrag event=" +event.getAction() +" v=" +v);
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
					PLMSLandView nextLandView = getMoveLandForAttack(unitView);
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, nextLandView, mPrevRouteArray.getLast()));

					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForBattleData(forecast);
				} else if (mAreaManager.getSupportAreaCover().isShowingCover(landView)) {
					// サポートスキル発動地点へのルート表示
					PLMSLandView nextLandView = getMoveLandForSupportSkill(unitView);
					mPrevRouteArray.addOrMoveLast(mAreaManager.showRouteArea(mMovingUnitView, nextLandView, mPrevRouteArray.getLast()));

					PLMSSupportForecast forecast = new PLMSSupportForecast(mMovingUnitView, nextLandView,
							unitView, unitView.getLandView());
					mInformation.updateForSupportData(forecast);
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
					PLMSBattleForecast forecast = new PLMSBattleForecast(mMovingUnitView, targetLandView,
							landView.getUnitView(), landView);
					attackToUnit(forecast);
				}
			};
		} else if (mAreaManager.getSupportAreaCover().isShowingCover(landView)) {
			targetLandView = mPrevRouteArray.getLast().getLast();
			animatorListener = new AnimatorListenerAdapter() {
				@Override
				public void onAnimationEnd(Animator animation) {
					PLMSSupportForecast forecast = new PLMSSupportForecast(mMovingUnitView, targetLandView,
							landView.getUnitView(), landView);
					supportToUnit(forecast);
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
			MYLogUtil.outputLog("finishMoveEvent by 移動前の地点へアニメーション移動");
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

	@Override
	public void toggleAllDangerArea() {
		mIsShowingAllDangerArea = !mIsShowingAllDangerArea;
		if (mIsShowingAllDangerArea) {
			updateDangerArea();
		} else {
			mAllDangerCover.hideAllCoverViews();
		}
	}

	private void updateDangerArea() {
		if (mIsShowingAllDangerArea) {
			mAllDangerCover.hideAllCoverViews();

			MYArrayList<PLMSUnitView> enemyUnitArray = mTargetArmy.getEnemyArmy().getAliveUnitViewArray();
			MYArrayList<PLMSLandView> dangerLandArray = mAreaManager.getAllAttackableLandArray(enemyUnitArray);
			mAllDangerCover.showCoverViews(dangerLandArray);
		}

		mSelectDangerCover.hideAllCoverViews();
		if (mSelectingDangerAreaUnit.size() > 0) {
			MYArrayList<PLMSUnitView> enemyAliveUnitArray = mTargetArmy.getEnemyArmy().getAliveUnitViewArray();
			MYArrayList<PLMSUnitView> targetUnitArray = enemyAliveUnitArray.filterByArray(mSelectingDangerAreaUnit);
			MYArrayList<PLMSLandView> dangerLandArray = mAreaManager.getAllAttackableLandArray(targetUnitArray);
			mSelectDangerCover.showCoverViews(dangerLandArray);
		}
	}

	private void beginDragEvent(PLMSUnitView unitView) {
		mInformation.updateForUnitData(unitView);
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
		MYLogUtil.outputLog(" startDrag unit=" +unitView.debugLog());
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
		MYLogUtil.outputLog("finishMoveEvent by 移動イベントキャンセル");
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

	private void attackToUnit(final PLMSBattleForecast forecast) {
		movedUnit(forecast.getLeftUnit().getLandView());

		// ダメージ表示
		mAnimationManager.addBattleAnimation(forecast);
		mAnimationManager.addAnimationCompletedRunnable(new Runnable() {
			@Override
			public void run() {
				PLMSUnitInterface leftUnit = forecast.getLeftUnit();
				if (leftUnit.isAlive()) {
					mInformation.updateForUnitData(leftUnit.getUnitView());
				} else {
					mInformation.clearInformation();
				}
				leftUnit.getUnitView().didAction();
				finishAction();
			}
		});
	}

	private void supportToUnit(PLMSSupportForecast forecast) {
		final PLMSSupportUnit supportUnit = forecast.getLeftUnit();
		movedUnit(supportUnit.getLandView());
		PLMSSkillData supportSkill = supportUnit.getUnitData().getSupportSkillData();
		supportSkill.executeSupportSkill(forecast);
		mAnimationManager.sendTempAnimators();
		mAnimationManager.addAnimationCompletedRunnable(new Runnable() {
			@Override
			public void run() {
				mInformation.updateForUnitData(supportUnit.getUnitView());
				supportUnit.getUnitView().didAction();
				finishAction();
			}
		});
	}

	private void finishAction() {
		if (!mArgument.getTurnManager().finishTurnIfNecessary()) {
			// ターン続行時
			updateDangerArea();
		}
	}

	private void movedUnit(PLMSLandView targetLandView) {
		mAreaManager.getAvailableAreaCover().hideCoverView(mMovingUnitView.getLandView());
		mMovingUnitView.moveToLand(targetLandView);
		MYLogUtil.outputLog("finishMoveEvent by 移動完了");
		finishMoveEvent();
	}

	// 攻撃地点の取得
	private PLMSLandView getMoveLandForAttack(PLMSUnitView targetUnitView) {
		MYArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getAttackLandArrayToTarget(targetUnitView, mMovingUnitView);
		return getMoveLandViewByRoute(targetAroundLandArray);
	}

	// サポートスキル発動地点の取得
	private PLMSLandView getMoveLandForSupportSkill(PLMSUnitView targetUnitView) {
		MYArrayList<PLMSLandView> targetAroundLandArray = mAreaManager.getSupportLandArrayToTarget(targetUnitView, mMovingUnitView);
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

	private boolean shouldLongTapEvent(PLMSUnitView unitView, MotionEvent event) {
		if (mDidLongTap) {
			// ロングタップイベントは連続では発生させない
			return false;
		}
		long currentTimeMillis = System.currentTimeMillis();
		if (currentTimeMillis - mTouchDownTimeMillis < 300) {
			return false;
		}
		if (event.getX() < 0 || unitView.getWidth() < event.getX()
				|| event.getY() < 0 || unitView.getHeight() < event.getY()) {
			// View の領域外にいる
			return false;
		}
		mDidLongTap = true;
		return true;
	}
}
