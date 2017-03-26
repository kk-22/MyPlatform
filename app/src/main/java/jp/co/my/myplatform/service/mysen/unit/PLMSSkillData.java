package jp.co.my.myplatform.service.mysen.unit;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public class PLMSSkillData {

	private PLMSSkillModel mSkillModel;
	private TimingType mTimingType;
	private RequirementType mRequirementType;
	private TargetType mTargetType;
	private EffectType mEffectType;

	public PLMSSkillData(PLMSSkillModel skillModel) {
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
		if (!canExecuteSkill((unitView))) {
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				PLMSUnitData unitData = unitView.getUnitData();
				int statusType = mSkillModel.getStatusType();
				int value = mSkillModel.getEffectValue();
				if ((statusType & STATUS_ATTACK) != 0) {
					unitData.setBuffAttack(value);
				}
				if ((statusType & STATUS_SPEED) != 0) {
					unitData.setBuffSpeed(value);
				}
				if ((statusType & STATUS_DEFENCE) != 0) {
					unitData.setBuffDefense(value);
				}
				if ((statusType & STATUS_MAGIC_DEFENCE) != 0) {
					unitData.setBuffMagicDefense(value);
				}
				break;
			}
			default:
				return;
		}
		return;
	}

	private boolean canExecuteSkill(PLMSUnitView unitView) {
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

	// getter
	public PLMSSkillModel getSkillModel() {
		return mSkillModel;
	}

	private static final int STATUS_ATTACK = 0x0001;
	private static final int STATUS_SPEED = 0x0002;
	private static final int STATUS_DEFENCE = 0x0004;
	private static final int STATUS_MAGIC_DEFENCE = 0x0008;
	private static final int STATUS_HP = 0x0010;

	private enum TimingType {
		START_TURN(1),
		START_BATTLE(11), ATTACK_TO_ENEMY(12), ATTACK_TO_ME(13), FINISH_BATTLE(14),
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

	private enum EffectType {
		ONE_TURN_BUFF(1), BATTLE_BUFF(2), WEAPON_BATTLE_BUFF(3),
		CURRENT_HP(11),
		THREE_WAY_INTENSIFICATION(41), ALL_RANGE_COUNTER(42), WEAKNESS_ATTACK(43);

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
