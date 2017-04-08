package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

import static jp.co.my.myplatform.service.mysen.unit.PLMSSkillData.EffectType;


public class PLMSBattleResult {

	private PLMSBattleUnit mLeftUnit;
	private PLMSBattleUnit mRightUnit;
	private PLMSFieldView mFieldView;
	private MYArrayList<PLMSBattleUnit> mBattleUnitArray;
	private MYArrayList<PLMSBattleUnit> mAttackerArray; // 攻撃順配列
	private MYArrayList<PLMSBattleScene> mSceneArray;

	private int mThreeWayRatio; // 3すくみ補正値

	public PLMSBattleResult(PLMSFieldView fieldView,
							PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);
		mFieldView = fieldView;
		mThreeWayRatio = 20;

		mLeftUnit.setEnemyUnit(mRightUnit);
		mRightUnit.setEnemyUnit(mLeftUnit);

		mBattleUnitArray = new MYArrayList<>(2);
		mBattleUnitArray.add(mLeftUnit);
		mBattleUnitArray.add(mRightUnit);
		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			for (PLMSUnitView unitView : battleUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
				for (PLMSSkillData skillData : unitView.getUnitData().getPassiveSkillArray()) {
					skillData.executeStartBattleSkill(unitView, battleUnit, this);
				}
			}
		}

		mLeftUnit.initParamsWithEnemyUnit(mThreeWayRatio);
		mRightUnit.initParamsWithEnemyUnit(mThreeWayRatio);
		// 速さがスキルによって変わるため最後に計算
		mAttackerArray = createAttackerArray();
		createScene();
	}

	// 戦闘 Info に表示するダメージ値
	public int givingDamageOfBattleUnit(PLMSBattleUnit battleUnit) {
		PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
		int damage = battleUnit.getTotalAttack() - enemyUnit.getDefenseForEnemyAttack();
		return Math.max(0, damage);
	}

	private void createScene() {
		mSceneArray = new MYArrayList<>();
		int maxScene = mAttackerArray.size();
		for (int i = 0; i < maxScene; i++) {
			PLMSBattleUnit attackerUnit = mAttackerArray.get(i);
			PLMSBattleUnit defenderUnit = attackerUnit.getEnemyUnit();
			PLMSBattleScene scene = new PLMSBattleScene(attackerUnit, defenderUnit);
			mSceneArray.add(scene);

			if (scene.getAttackerRemainingHP() <= 0 || scene.getDefenderRemainingHP() <= 0) {
				 break;
			}
		}
	}

	// 攻撃順を返す
	private MYArrayList<PLMSBattleUnit> createAttackerArray() {
		MYArrayList<PLMSBattleUnit> attackerArray = new MYArrayList<>();

		// 反撃判定
		int distance = mLeftUnit.getUnitView().getUnitData().getWeapon().getAttackRange();
		boolean canAttackRightAttacker = (mRightUnit.getUnitView().getUnitData().getWeapon().getAttackRange() == distance);
		if (mRightUnit.getSkillEffectArray().contains(EffectType.ALL_RANGE_COUNTER)) {
			canAttackRightAttacker = true;
		}
		if (canAttackRightAttacker && mLeftUnit.getSkillEffectArray().contains(EffectType.CHASE_ATTACK_IF_HAS_COUNTER)) {
			mLeftUnit.incrementChasePoint();
		}

		// 攻撃
		PLMSBattleUnit firstAttacker;
		PLMSBattleUnit secondAttacker;
		if (canAttackRightAttacker) {
			if (mRightUnit.getSkillEffectArray().contains(EffectType.PREEMPTIVE_ATTACK)) {
				firstAttacker = mRightUnit;
			} else {
				firstAttacker = mLeftUnit;
			}
			secondAttacker = firstAttacker.getEnemyUnit();
		} else {
			firstAttacker = mLeftUnit;
			secondAttacker = null;
		}
		attackerArray.add(firstAttacker);
		attackerArray.addIfNotNull(secondAttacker);

		// 追撃処理
		addChaseAttack(attackerArray, firstAttacker);
		addChaseAttack(attackerArray, secondAttacker);
		return attackerArray;
	}

	private void addChaseAttack(MYArrayList<PLMSBattleUnit> attackerArray, PLMSBattleUnit attackerUnit) {
		if (attackerUnit == null || !attackerUnit.canChaseAttack()) {
			return;
		}
		if (attackerUnit.getSkillEffectArray().contains(EffectType.CONTINUOUSLY_CHASE_ATTACK)) {
			// 自分の攻撃直後に追撃攻撃
			int firstAttackIndex = attackerArray.indexOf(attackerUnit);
			attackerArray.add(firstAttackIndex + 1, attackerUnit);
		} else {
			attackerArray.add(attackerUnit);
		}
	}

	// getter
	public PLMSBattleUnit getLeftUnit() {
		return mLeftUnit;
	}

	public PLMSBattleUnit getRightUnit() {
		return mRightUnit;
	}

	public MYArrayList<PLMSBattleScene> getSceneArray() {
		return mSceneArray;
	}

	public MYArrayList<PLMSBattleUnit> getAttackerArray() {
		return mAttackerArray;
	}

	public PLMSBattleUnit getBattleUnitOfUnitTeam(PLMSUnitInterface unit) {
		PLMSArmyStrategy armyStrategy = unit.getUnitData().getArmyStrategy();
		MYArrayList<PLMSUnitView> teamUnitArray = armyStrategy.getAllUnitViewArray();
		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			if (teamUnitArray.contains(battleUnit.getUnitView())) {
				return battleUnit;
			}
		}
		return null;
	}

	public PLMSUnitInterface getUnitOfBattle(PLMSUnitInterface unit) {
		PLMSUnitView unitView = unit.getUnitView();
		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			if (battleUnit.getUnitView().equals(unitView)) {
				return battleUnit;
			}
		}
		return unit;
	}

	// setter
	public void setThreeWayRatio(int threeWayRatio) {
		if (mThreeWayRatio < threeWayRatio) {
			mThreeWayRatio = threeWayRatio;
		}
	}
}
