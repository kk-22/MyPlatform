package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

import static jp.co.my.myplatform.service.mysen.unit.PLMSSkillData.EffectType;


public class PLMSBattleResult {

	private PLMSBattleUnit mLeftUnit;
	private PLMSBattleUnit mRightUnit;
	private PLMSFieldView mFieldView;
	private MYArrayList<PLMSBattleUnit> mAttackerArray; // 攻撃順配列
	private MYArrayList<PLMSBattleScene> mSceneArray;

	public PLMSBattleResult(PLMSFieldView fieldView,
							PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);
		mFieldView = fieldView;

		mLeftUnit.setEnemyUnit(mRightUnit);
		mRightUnit.setEnemyUnit(mLeftUnit);

		// 反撃有無に応じたスキルがあるため、全距離反撃スキルを持つ被攻撃側スキルを優先
		for (PLMSSkillData skillData : mRightUnit.getUnitView().getUnitData().getPassiveSkillArray()) {
			skillData.executeAttackToMeSkill(mRightUnit, this);
		}
		for (PLMSSkillData skillData : mLeftUnit.getUnitView().getUnitData().getPassiveSkillArray()) {
			skillData.executeAttackToEnemySkill(mLeftUnit, this);
		}

		mLeftUnit.initParamsWithEnemyUnit();
		mRightUnit.initParamsWithEnemyUnit();
		// 速さがスキルによって変わるため最後に計算
		// TODO: スキル結果をBattleUnitの変数へセットし、その値をもとに攻撃順、追撃判定を行う
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
		PLMSBattleUnit firstAttacker = mLeftUnit;
		PLMSBattleUnit secondAttacker = firstAttacker.getEnemyUnit();


		int distance = firstAttacker.getUnitView().getUnitData().getWeapon().getAttackRange();
		boolean canAttackSecondAttacker = false;
		if (secondAttacker.getUnitView().getUnitData().getWeapon().getAttackRange() == distance
				|| secondAttacker.getSkillEffectArray().contains(EffectType.ALL_RANGE_COUNTER)) {
			canAttackSecondAttacker = true;
		}

		attackerArray.add(firstAttacker);
		if (canAttackSecondAttacker) {
			attackerArray.add(secondAttacker);
		}

		// 追撃判定
		if (firstAttacker.canChaseAttack(secondAttacker)) {
			attackerArray.add(firstAttacker);
		}
		if (canAttackSecondAttacker && secondAttacker.canChaseAttack(firstAttacker)) {
			attackerArray.add(secondAttacker);
		}
		return attackerArray;
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
}
