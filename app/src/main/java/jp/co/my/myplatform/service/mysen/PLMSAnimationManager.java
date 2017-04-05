package jp.co.my.myplatform.service.mysen;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Point;
import android.graphics.PointF;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleResult;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleScene;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

import static android.animation.PropertyValuesHolder.ofFloat;

public class PLMSAnimationManager extends AnimatorListenerAdapter {

	private PLMSArgument mArgument;
	private PLMSFieldView mFieldView;

	private MYArrayList<Animator> mAnimatorArray;
	private MYArrayList<Animator> mTogetherAnimatorArray; // 同時実行するアニメーションの一時保存

	public PLMSAnimationManager(PLMSArgument argument) {
		mArgument = argument;
		mFieldView = argument.getFieldView();

		mAnimatorArray = new MYArrayList<>();
		mTogetherAnimatorArray = new MYArrayList<>();
	}

	public void addMoveAnimation(PLMSUnitView moveUnitView,
								 PLMSLandView fromLandView,
								 PLMSLandView toLandView,
								 Animator.AnimatorListener animatorListener) {
		PointF fromPointF = mFieldView.pointOfLandView(fromLandView);
		addMoveAnimation(moveUnitView, fromPointF, toLandView, animatorListener);
	}

	public void addMoveAnimation(PLMSUnitView moveUnitView,
								 PointF fromPointF,
								 PLMSLandView toLandView,
								 Animator.AnimatorListener animatorListener) {
		PointF targetPointF = mFieldView.pointOfLandView(toLandView);
		PropertyValuesHolder holderX = ofFloat(
				"x", fromPointF.x, targetPointF.x);
		PropertyValuesHolder holderY = ofFloat(
				"y", fromPointF.y, targetPointF.y);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(
				moveUnitView, holderX, holderY);
		objectAnimator.setDuration(100);
		if (animatorListener != null) {
			objectAnimator.addListener(animatorListener);
		}
		addAnimator(objectAnimator);
	}

	public void addBattleAnimation(final PLMSBattleResult battleResult, final Runnable lastRunnable) {
		int numberOfScene = battleResult.getSceneArray().size();
		for (int i = 0; i < numberOfScene; i++) {
			final PLMSBattleScene scene = battleResult.getSceneArray().get(i);

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
		battleResult.getLeftUnit().getUnitData().resetDebuffParams();

		// 戦闘終了スキルアニメーション
		PLMSBattleUnit leftUnit = battleResult.getLeftUnit();
		for (PLMSSkillData skillData : leftUnit.getUnitData().getPassiveSkillArray()) {
			skillData.executeFinishBattleSkill(leftUnit, battleResult);
		}
		PLMSBattleUnit rightUnit = battleResult.getRightUnit();
		for (PLMSSkillData skillData : rightUnit.getUnitData().getPassiveSkillArray()) {
			skillData.executeFinishBattleSkill(rightUnit, battleResult);
		}
		sendTogetherAnimator();

		mAnimatorArray.getLast().addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				PLMSBattleUnit attackerUnit = battleResult.getLeftUnit();
				if (attackerUnit.getResultHP() <= 0) {
					mArgument.getInformationView().clearInformation();
				} else {
					mArgument.getInformationView().updateForUnitData(attackerUnit.getUnitView());
				}
				lastRunnable.run();
			}
		});
	}

	public Animator getFluctuateHPAnimation(final PLMSUnitView unitView, final int remainingHP, final int diffHP) {
		return unitView.getHPBar().getDamageAnimatorArray(remainingHP, diffHP);
	}

	public void addTogetherAnimator(Animator animator) {
		mTogetherAnimatorArray.add(animator);
	}

	public void sendTogetherAnimator() {
		if (mTogetherAnimatorArray.size() == 0) {
			return;
		}
		AnimatorSet animatorSet = new AnimatorSet();
		animatorSet.playTogether(mTogetherAnimatorArray);
		addAnimator(animatorSet);
		mTogetherAnimatorArray.clear();
	}

	private void addAnimator(Animator animator) {
		animator.addListener(this);
		if (mAnimatorArray.size() == 0) {
			animator.start();
		}
		mAnimatorArray.add(animator);
	}

	@Override
	public void onAnimationEnd(Animator animation) {
		mAnimatorArray.remove(animation);
		if (mAnimatorArray.size() > 0) {
			mAnimatorArray.getFirst().start();
		}
	}
}
