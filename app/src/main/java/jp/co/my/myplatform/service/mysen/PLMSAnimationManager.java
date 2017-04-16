package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.graphics.PointF;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleForecast;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleScene;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

import static android.animation.PropertyValuesHolder.ofFloat;

public class PLMSAnimationManager extends AnimatorListenerAdapter {

	public static final int ANIMATOR_BUFF = 1;
	public static final int ANIMATOR_HP = 2;
	public static final int ANIMATOR_NUMBER = 3;

	private PLMSArgument mArgument;
	private PLMSFieldView mFieldView;

	private MYArrayList<Animator> mAnimatorArray; // 実行中・実行予定のアニメーター
	private MYArrayList<MYArrayList<Animator>> mTempAnimators; // 種類毎のアニメーターの一時保存

	public PLMSAnimationManager(PLMSArgument argument) {
		mArgument = argument;
		mFieldView = argument.getFieldView();

		mAnimatorArray = new MYArrayList<>();
		mTempAnimators = new MYArrayList<>(ANIMATOR_NUMBER);
		for (int i = 0; i < ANIMATOR_NUMBER; i++) {
			mTempAnimators.add(new MYArrayList<Animator>());
		}
	}

	public Animator getMovementAnimation(PLMSUnitView moveUnitView,
										 PLMSLandView fromLandView,
										 PLMSLandView toLandView) {
		PointF fromPointF = mFieldView.pointOfLandView(fromLandView);
		return getMovementAnimation(moveUnitView, fromPointF, toLandView);
	}

	public Animator getMovementAnimation(PLMSUnitView moveUnitView,
										 PointF fromPointF,
										 PLMSLandView toLandView) {
		PointF targetPointF = mFieldView.pointOfLandView(toLandView);
		PropertyValuesHolder holderX = ofFloat(
				"x", fromPointF.x, targetPointF.x);
		PropertyValuesHolder holderY = ofFloat(
				"y", fromPointF.y, targetPointF.y);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
				moveUnitView, holderX, holderY);
		objectAnimator.setDuration(100);
		return objectAnimator;
	}

	public void addBattleAnimation(final PLMSBattleForecast battleForecast) {
		int numberOfScene = battleForecast.getSceneArray().size();
		for (int i = 0; i < numberOfScene; i++) {
			final PLMSBattleScene scene = battleForecast.getSceneArray().get(i);

			final PLMSUnitView attackerUnitView = scene.getAttackerUnit().getUnitView();
			final PLMSUnitView defenderUnitView = scene.getDefenderUnit().getUnitView();
			PLMSLandView attackerLandView = attackerUnitView.getLandView();

			// 攻撃アニメーション
			PLMSLandView defenderLandView = defenderUnitView.getLandView();
			Point attackerPoint = attackerLandView.getPoint();
			Point defenderPoint = defenderLandView.getPoint();
			PointF currentPointF = mFieldView.pointOfLandView(attackerLandView);

			int moveWidth = attackerUnitView.getWidth();
			MYArrayList<Animator> animatorArray = new MYArrayList<>();
			if (attackerPoint.x != defenderPoint.x) {
				int diffX = (attackerPoint.x < defenderPoint.x) ? moveWidth : -moveWidth;
				PropertyValuesHolder holder = PropertyValuesHolder.ofFloat(
						"x",
						currentPointF.x,
						currentPointF.x + diffX,
						currentPointF.x);
				ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
						attackerUnitView, holder);
				animator.setDuration(300);
				animatorArray.add(animator);
			}
			if (attackerPoint.y != defenderPoint.y) {
				int diffY = (attackerPoint.y < defenderPoint.y) ? moveWidth : -moveWidth;
				PropertyValuesHolder holder = ofFloat(
						"y",
						currentPointF.y,
						currentPointF.y + diffY,
						currentPointF.y);
				ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(
						attackerUnitView, holder);
				animator.setDuration(300);
				animatorArray.add(animator);
			}

			// ダメージアニメーション
			AnimatorSet damageAnimator = defenderLandView.getUnitView().getHPBar()
					.getDamageAnimatorArray(scene.getDefenderRemainingHP(), scene.getDefenderDiffHP());
			animatorArray.addIfNotNull(damageAnimator);

			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.playTogether(animatorArray);
			animatorSet.addListener(new AnimatorListenerAdapter() {
				@Override
				public void onAnimationStart(Animator animation) {
					// 攻撃で敵 UnitView の裏に隠れないように最前面へ
					attackerUnitView.bringToFront();
				}
			});
			addAnimator(animatorSet);
		}

		// デバフリセット（戦闘終了スキル発動前に必要）
		battleForecast.getLeftUnit().getUnitData().resetDebuffParams();

		// 戦闘終了スキルアニメーション
		PLMSBattleUnit leftUnit = battleForecast.getLeftUnit();
		for (PLMSSkillData skillData : leftUnit.getUnitData().getPassiveSkillArray()) {
			skillData.executeFinishBattleSkill(leftUnit, battleForecast);
		}
		PLMSBattleUnit rightUnit = battleForecast.getRightUnit();
		for (PLMSSkillData skillData : rightUnit.getUnitData().getPassiveSkillArray()) {
			skillData.executeFinishBattleSkill(rightUnit, battleForecast);
		}
		sendTempAnimators();
	}

	public Animator getFluctuateHPAnimation(final PLMSUnitView unitView, final int remainingHP, final int diffHP) {
		return unitView.getHPBar().getDamageAnimatorArray(remainingHP, diffHP);
	}

	public void addTempAnimator(Animator animator, int typeNo) {
		mTempAnimators.get(typeNo).add(animator);
	}

	public void sendTempAnimators() {
		for (MYArrayList<Animator> animatorArray : mTempAnimators) {
			if (animatorArray.size() == 0) {
				continue;
			}
			AnimatorSet animatorSet = new AnimatorSet();
			animatorSet.playTogether(animatorArray);
			addAnimator(animatorSet);
			animatorArray.clear();
		}
	}

	public void addAnimator(Animator animator) {
		animator.addListener(this);
		if (mAnimatorArray.size() == 0) {
			animator.start();
		}
		mAnimatorArray.add(animator);
	}

	public void addTogetherAnimatorArray(MYArrayList<Animator> animatorArray) {
		if (animatorArray.size() == 0) {
			return;
		}
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(animatorArray);
		addAnimator(animatorSet);
	}

	// 最期のアニメーション完了時に実行する Runnable をセット
	public void addAnimationCompletedRunnable(final Runnable runnable) {
		Animator lastAnimator = mAnimatorArray.getLast();
		if (lastAnimator == null) {
			runnable.run();
			return;
		}
		lastAnimator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				runnable.run();
			}
		});
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		mAnimatorArray.remove(animation);
		if (mAnimatorArray.size() > 0) {
			mAnimatorArray.getFirst().start();
		}
	}
}
