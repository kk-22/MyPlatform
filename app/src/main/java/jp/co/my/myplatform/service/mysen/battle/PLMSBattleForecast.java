package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

import static jp.co.my.myplatform.service.mysen.unit.PLMSSkillData.EffectType;


public class PLMSBattleForecast extends PLMSBaseForecast {

	private PLMSBattleUnit mLeftUnit;
	private PLMSBattleUnit mRightUnit;
	private MYArrayList<PLMSBattleUnit> mBattleUnitArray;
	private MYArrayList<PLMSBattleUnit> mAttackerArray; // 攻撃順配列
	private MYArrayList<PLMSBattleScene> mSceneArray;
	private int mThreeWayRatio; // 3すくみ補正値

	public PLMSBattleForecast(PLMSUnitView leftUnitView, PLMSLandView leftLandView,
							  PLMSUnitView rightUnitView, PLMSLandView rightLandView) {
		super();
		mLeftUnit = new PLMSBattleUnit(leftUnitView, leftLandView);
		mRightUnit = new PLMSBattleUnit(rightUnitView, rightLandView);

		mLeftUnit.setAnotherUnit(mRightUnit);
		mRightUnit.setAnotherUnit(mLeftUnit);
		mBattleUnitArray = new MYArrayList<>(2);
		mBattleUnitArray.add(mLeftUnit);
		mBattleUnitArray.add(mRightUnit);

		mThreeWayRatio = 20;

		for (PLMSBattleUnit battleUnit : mBattleUnitArray) {
			for (PLMSUnitView unitView : battleUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
				for (PLMSSkillData skillData : unitView.getUnitData().getPassiveSkillArray()) {
					skillData.executeStartBattleSkill(unitView, battleUnit, this);
				}
			}
		}

		mLeftUnit.initParamsWithThreeWayRatio(mThreeWayRatio);
		mRightUnit.initParamsWithThreeWayRatio(mThreeWayRatio);
		// 速さがスキルによって変わるため最後に計算
		initAttackerArray();
		createScene();
	}

	// 戦闘 Info に表示するダメージ値
	public int givingDamageOfBattleUnit(PLMSBattleUnit battleUnit) {
		PLMSBattleUnit enemyUnit = battleUnit.getAnotherUnit();
		int damage = battleUnit.getTotalAttack() - enemyUnit.getDefenseForEnemyAttack();
		return Math.max(0, damage);
	}

	private void createScene() {
		mSceneArray = new MYArrayList<>();
		int maxScene = mAttackerArray.size();
		for (int i = 0; i < maxScene; i++) {
			PLMSBattleUnit attackerUnit = mAttackerArray.get(i);
			PLMSBattleUnit defenderUnit = attackerUnit.getAnotherUnit();
			PLMSBattleScene scene = new PLMSBattleScene(attackerUnit, defenderUnit);
			mSceneArray.add(scene);

			if (scene.getAttackerRemainingHP() <= 0 || scene.getDefenderRemainingHP() <= 0) {
				 break;
			}
		}
	}

	// 攻撃順を返す
	private void initAttackerArray() {
		mAttackerArray =  new MYArrayList<>();

		// 反撃判定
		int distance = mLeftUnit.getUnitView().getUnitData().getBranch().getAttackRange();
		boolean canAttackRightAttacker = (mRightUnit.getUnitView().getUnitData().getBranch().getAttackRange() == distance);
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
			secondAttacker = firstAttacker.getAnotherUnit();
		} else {
			firstAttacker = mLeftUnit;
			secondAttacker = null;
		}
		mAttackerArray.add(firstAttacker);
		mAttackerArray.addIfNotNull(secondAttacker);

		// 追撃処理
		addChaseAttack(firstAttacker);
		addChaseAttack(secondAttacker);

		// 勇者武器による連続攻撃
		addConsecutiveAttack(firstAttacker);
		addConsecutiveAttack(secondAttacker);
	}

	private void addChaseAttack(PLMSBattleUnit attackerUnit) {
		if (attackerUnit == null || !attackerUnit.canChaseAttack()) {
			return;
		}
		if (attackerUnit.getSkillEffectArray().contains(EffectType.CONTINUOUSLY_CHASE_ATTACK)) {
			// 自分の攻撃直後に追撃攻撃
			int firstAttackIndex = mAttackerArray.indexOf(attackerUnit);
			mAttackerArray.add(firstAttackIndex + 1, attackerUnit);
		} else {
			mAttackerArray.add(attackerUnit);
		}
	}

	private void addConsecutiveAttack(PLMSBattleUnit attackerUnit) {
		if (attackerUnit == null) {
			return;
		}
		int numberOfConsecutiveAttack = attackerUnit.getNumberOfConsecutiveAttack();
		if (numberOfConsecutiveAttack <= 1) {
			return;
		}
		for (int i = mAttackerArray.size() - 1; i >= 0; i--) {
			PLMSBattleUnit turnUnit = mAttackerArray.get(i);
			if (!turnUnit.equals(attackerUnit)) {
				continue;
			}
			for (int j = 1; j < numberOfConsecutiveAttack; j++) {
				mAttackerArray.add(i, attackerUnit);
			}
		}
	}

	// getter
	@Override
	public PLMSBattleUnit getLeftUnit() {
		return mLeftUnit;
	}

	@Override
	public PLMSBattleUnit getRightUnit() {
		return mRightUnit;
	}

	@Override
	public String getInformationTitle() {
		return "攻撃";
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
