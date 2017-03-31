package jp.co.my.myplatform.service.mysen.unit;

import android.animation.Animator;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleResult;
import jp.co.my.myplatform.service.mysen.battle.PLMSBattleUnit;

public class PLMSSkillData {

	private PLMSArgument mArgument;
	private PLMSSkillModel mSkillModel;
	private TimingType mTimingType;
	private RequirementType mRequirementType;
	private TargetType mTargetType;
	private EffectType mEffectType;

	public PLMSSkillData(PLMSArgument argument, PLMSSkillModel skillModel) {
		mArgument = argument;
		mSkillModel = skillModel;

		if (skillModel == null) {
			return;
		}
		mTimingType = TimingType.getType(skillModel.getTimingType());
		mRequirementType = RequirementType.getType(skillModel.getRequirementType());
		mTargetType = TargetType.getType(skillModel.getTargetType());
		mEffectType = EffectType.getType(skillModel.getEffectType());
	}

	public void executeStartTurnSkill(PLMSUnitView unitView, int numberOfTurn) {
		if (mTimingType != TimingType.START_TURN) {
			return;
		}
		if (!canExecuteSkill(unitView)) {
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				PLMSUnitData unitData = unitView.getUnitData();
				int statusType = mSkillModel.getStatusType();
				int value = mSkillModel.getEffectValue();
				if ((statusType & SKILL_ATTACK) != 0) {
					unitData.setBuffOfNo(PLMSUnitData.PARAMETER_ATTACK, value);
				}
				if ((statusType & SKILL_SPEED) != 0) {
					unitData.setBuffOfNo(PLMSUnitData.PARAMETER_SPEED, value);
				}
				if ((statusType & SKILL_DEFENSE) != 0) {
					unitData.setBuffOfNo(PLMSUnitData.PARAMETER_DEFENSE, value);
				}
				if ((statusType & SKILL_MAGIC_DEFENSE) != 0) {
					unitData.setBuffOfNo(PLMSUnitData.PARAMETER_MAGIC_DEFENSE, value);
				}
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 StartTurn スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeAttackToEnemySkill(PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		if (mTimingType == TimingType.START_BATTLE) {
			executeStartBattleSkill(battleUnit, battleResult);
			return;
		}

		if (mTimingType != TimingType.ATTACK_TO_ENEMY) {
			return;
		}
		PLMSUnitView unitView = battleUnit.getUnitView();
		PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
		if (!canExecuteSkill(unitView) || !canExecuteBattleSkill(battleUnit, enemyUnit)) {
			return;
		}
		switch (mEffectType) {
			case BATTLE_BUFF: {
				int statusType = mSkillModel.getStatusType();
				int value = mSkillModel.getEffectValue();
				if ((statusType & SKILL_ATTACK) != 0) {
					battleUnit.setBattleBuffOfNo(PLMSUnitData.PARAMETER_ATTACK, value);
				}
				if ((statusType & SKILL_SPEED) != 0) {
					battleUnit.setBattleBuffOfNo(PLMSUnitData.PARAMETER_SPEED, value);
				}
				if ((statusType & SKILL_DEFENSE) != 0) {
					battleUnit.setBattleBuffOfNo(PLMSUnitData.PARAMETER_DEFENSE, value);
				}
				if ((statusType & SKILL_MAGIC_DEFENSE) != 0) {
					battleUnit.setBattleBuffOfNo(PLMSUnitData.PARAMETER_MAGIC_DEFENSE, value);
				}
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 AttackToEnemy スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeAttackToMeSkill(PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		if (mTimingType == TimingType.START_BATTLE) {
			executeStartBattleSkill(battleUnit, battleResult);
			return;
		}

		if (mTimingType != TimingType.ATTACK_TO_ME) {
			return;
		}
		PLMSUnitView unitView = battleUnit.getUnitView();
		PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
		if (!canExecuteSkill(unitView) || !canExecuteBattleSkill(battleUnit, enemyUnit)) {
			return;
		}
		switch (mEffectType) {
			case ALL_RANGE_COUNTER:
				battleUnit.addSkill(this);
				break;
			default:
				MYLogUtil.showErrorToast("未実装 AttackToMe スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeStartBattleSkill(PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		switch (mEffectType) {
			case THREE_WAY_INTENSIFICATION:
				battleResult.setThreeWayRatio(mSkillModel.getEffectValue());
				break;
			default:
				MYLogUtil.showErrorToast("未実装 Battle スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public Animator executeFinishBattleSkill(PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		boolean isAttacker = battleResult.getLeftUnit().equals(battleUnit);
		if (mTimingType != TimingType.FINISH_BATTLE
				&& !(mTimingType == TimingType.FINISH_ATTACK_BATTLE && isAttacker)
				&& !(mTimingType == TimingType.FINISH_DEFENCE_BATTLE && !isAttacker)) {
			return null;
		}

		PLMSUnitView unitView = battleUnit.getUnitView();
		if (!canExecuteSkill(unitView) || battleUnit.getResultHP() <= 0) {
			return null;
		}

		switch (mEffectType) {
			case FLUCTUATE_HP: {
				int diffHP = mSkillModel.getEffectValue();
				int remainingHP = unitView.getUnitData().calculateSkillRemainingHP(battleUnit.getResultHP(), diffHP);
				return mArgument.getAnimationManager().getFluctuateHPAnimation(unitView, remainingHP, diffHP);
			}
			default:
				MYLogUtil.showErrorToast("未実装 FinishBattle スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
		return null;
	}

	private boolean canExecuteSkill(PLMSUnitView unitView) {
		if (mSkillModel == null) {
			return false;
		}
		switch (mRequirementType) {
			case NONE:
				return true;
			case LESS_THAN_MY_HP: {
				PLMSUnitData unitData = unitView.getUnitData();
				return unitData.getCurrentHP() <= unitData.getMaxHP() * mSkillModel.getRequirementValue() / 100;
			}
			case GREATER_THAN_MY_HP: {
				PLMSUnitData unitData = unitView.getUnitData();
				return unitData.getCurrentHP() >= unitData.getMaxHP() * mSkillModel.getRequirementValue() / 100;
			}
			case LESS_THAN_TEAM_HP:
			case ENEMY_HAS_TARGET_WEAPON:
			default:
				// 各 execute メソッドで判定
				return true;
		}
	}

	private boolean canExecuteBattleSkill(PLMSBattleUnit myBattleUnit, PLMSBattleUnit enemyBattleUnit) {
		return true;
	}

	// getter
	public PLMSSkillModel getSkillModel() {
		return mSkillModel;
	}

	public EffectType getEffectType() {
		return mEffectType;
	}

	private static final int SKILL_ATTACK = 0x0001;
	private static final int SKILL_SPEED = 0x0002;
	private static final int SKILL_DEFENSE = 0x0004;
	private static final int SKILL_MAGIC_DEFENSE = 0x0008;
	private static final int SKILL_HP = 0x0010;

	private enum TimingType {
		START_TURN(1),
		START_BATTLE(11), ATTACK_TO_ENEMY(12), ATTACK_TO_ME(13),
		FINISH_BATTLE(16), FINISH_ATTACK_BATTLE(17), FINISH_DEFENCE_BATTLE(18),
		MY_MOVEMENT(21), TEAM_MOVEMENT(22), ENEMY_MOVEMENT(23),
		HEAL_BY_STAFF(31);

		final int id;
		TimingType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private TimingType getType(int no) {
			TimingType[] types = TimingType.values();
			for (TimingType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の TimingType=" +no);
			return START_TURN;
		}
	}

	private enum RequirementType {
		NONE(0),
		LESS_THAN_MY_HP(1), GREATER_THAN_MY_HP(2), LESS_THAN_TEAM_HP(3),
		ENEMY_HAS_TARGET_WEAPON(11);

		final int id;
		RequirementType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private RequirementType getType(int no) {
			RequirementType[] types = RequirementType.values();
			for (RequirementType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の RequirementType=" +no);
			return NONE;
		}
	}

	private enum TargetType {
		NONE(0),
		SELF(1), TEAM_IN_RANGE(2), SAME_BRANCH_IN_RANGE(3), TEAM_OTHER_THAN_ME(4), ALL_TEAM_MEMBER(5),
		ENEMY(11), ENEMY_IN_RANGE(12);

		final int id;
		TargetType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private TargetType getType(int no) {
			TargetType[] types = TargetType.values();
			for (TargetType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の TargetType=" +no);
			return NONE;
		}
	}

	public enum EffectType {
		ONE_TURN_BUFF(1), BATTLE_BUFF(2), WEAPON_BATTLE_BUFF(3),
		FLUCTUATE_HP(11),
		THREE_WAY_INTENSIFICATION(41), ALL_RANGE_COUNTER(42), PROTECT_WEAKNESS_ATTACK(43);

		final int id;
		EffectType(final int id) {
			this.id = id;
		}
		public int getInt() {
			return this.id;
		}

		static private EffectType getType(int no) {
			EffectType[] types = EffectType.values();
			for (EffectType type : types) {
				if (type.getInt() == no) {
					return type;
				}
			}
			MYLogUtil.showErrorToast("未実装の EffectType=" +no);
			return ONE_TURN_BUFF;
		}
	}
}
