package jp.co.my.myplatform.service.mysen.unit;

import android.animation.Animator;
import android.graphics.Point;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYMathUtil;
import jp.co.my.common.util.MYPointUtil;
import jp.co.my.myplatform.service.mysen.PLMSAnimationManager;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
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
		if (mRequirementType == RequirementType.NUMBER_OF_TURN
				&& numberOfTurn % mSkillModel.getRequirementValue() != 1) {
			return;
		}

		MYArrayList<PLMSUnitView> targetArray = getTargetUnitViewArray(unitView, null);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case FLUCTUATE_HP: {
				PLMSUnitData unitData = unitView.getUnitData();
				int diffHP = mSkillModel.getEffectValue();
				int remainingHP = unitData.calculateSkillRemainingHP(unitData.getCurrentHP(), diffHP);
				PLMSAnimationManager animationManager = mArgument.getAnimationManager();
				Animator animator = animationManager.getFluctuateHPAnimation(unitView, remainingHP, diffHP);
				animationManager.addTogetherAnimator(animator);
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 StartTurn スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeStartBattleSkill(PLMSUnitView skillUnitView,
										PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		if (mTimingType == null) {
			return;
		}
		boolean isAttacker = battleResult.getLeftUnit().equals(battleUnit);
		boolean isBattleUnit = battleUnit.getUnitView().equals(skillUnitView);
		switch (mTimingType) {
			case START_BATTLE: if (!isBattleUnit) {return;} break;
			case ATTACK_TO_ENEMY: if (!isBattleUnit || !isAttacker) {return;} break;
			case ATTACK_TO_ME: if (!isBattleUnit || isAttacker) {return;} break;
			case START_TEAM_BATTLE: if (isBattleUnit) {return;} break;
			default: return;
		}

		PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
		if (!canExecuteSkill(skillUnitView)) {
			return;
		}
		MYArrayList<PLMSUnitView> targetArray = getTargetUnitViewArray(skillUnitView, battleUnit);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case BATTLE_BUFF: {
				if (!targetArray.contains(battleUnit.getUnitView())) {
					break;
				}
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
			case CHASE_ATTACK:
				battleUnit.incrementChasePoint();
				break;
			case DISABLE_CHASE_ATTACK:
				battleUnit.decrementChasePoint();
				enemyUnit.decrementChasePoint();
				break;
			case KILL_WEAPON:
				battleUnit.incrementChasePoint();
				enemyUnit.decrementChasePoint();
				break;
			case THREE_WAY_INTENSIFICATION:
				battleResult.setThreeWayRatio(mSkillModel.getEffectValue());
				break;
			case CHASE_ATTACK_IF_HAS_COUNTER:
			case CONTINUOUSLY_CHASE_ATTACK:
			case PREEMPTIVE_ATTACK:
			case ALL_RANGE_COUNTER:
				battleUnit.addSkill(this);
				break;
			default:
				MYLogUtil.showErrorToast("未実装 Battle スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeFinishBattleSkill(PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		boolean isAttacker = battleResult.getLeftUnit().equals(battleUnit);
		if (mTimingType != TimingType.FINISH_BATTLE
				&& !(mTimingType == TimingType.FINISH_ATTACK_BATTLE && isAttacker)
				&& !(mTimingType == TimingType.FINISH_DEFENCE_BATTLE && !isAttacker)) {
			return;
		}

		PLMSUnitView unitView = battleUnit.getUnitView();
		if (!canExecuteSkill(unitView) || battleUnit.getResultHP() <= 0) {
			return;
		}
		MYArrayList<PLMSUnitView> targetArray = getTargetUnitViewArray(battleUnit.getUnitView(), battleUnit);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case FLUCTUATE_HP: {
				for (PLMSUnitView targetUnit : targetArray) {
					int currentHP = getCurrentHPForBattle(targetUnit, battleResult);
					int diffHP = mSkillModel.getEffectValue();
					int remainingHP = targetUnit.getUnitData().calculateSkillRemainingHP(currentHP, diffHP);
					PLMSAnimationManager animationManager = mArgument.getAnimationManager();
					Animator animator = animationManager.getFluctuateHPAnimation(targetUnit, remainingHP, diffHP);
					animationManager.addTogetherAnimator(animator);
				}
				break;
			}
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case PUSH_ONE_SQUARE: {
				PLMSUnitView enemyUnitView = battleUnit.getEnemyUnit().getUnitView();
				Point enemyPoint = MYPointUtil.getMovePoint(unitView.getLandView().getPoint(), enemyUnitView.getLandView().getPoint(), 1);
				moveUnit(unitView, null, enemyUnitView, enemyPoint);
				break;
			}
			case GO_BACKWARD: {
				PLMSUnitView enemyUnitView = battleUnit.getEnemyUnit().getUnitView();
				Point selfPoint = MYPointUtil.getMovePoint(enemyUnitView.getLandView().getPoint(), unitView.getLandView().getPoint(), 1);
				Point enemyPoint = MYPointUtil.getMovePoint(unitView.getLandView().getPoint(), enemyUnitView.getLandView().getPoint(), -1);
				moveUnit(unitView, selfPoint, enemyUnitView, enemyPoint);
				break;
			}
			case SWAP_POSITION: {
				PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
				PLMSUnitView enemyUnitView = enemyUnit.getUnitView();
				Point enemyPoint = (enemyUnit.isAlive()) ? unitView.getCurrentPoint() : null;
				moveUnit(unitView, enemyUnitView.getCurrentPoint(), enemyUnitView, enemyPoint);
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 FinishBattle スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeMovementSkill(PLMSUnitView skillUnitView, PLMSUnitView moveUnitView) {
		if (!(mTimingType == TimingType.MY_MOVEMENT && moveUnitView == skillUnitView)
				&& !(mTimingType == TimingType.ENEMY_MOVEMENT && skillUnitView.isEnemy(moveUnitView))) {
			return;
		}
		if (!canExecuteSkill(skillUnitView)) {
			return;
		}

		switch (mEffectType) {
			case SLIP_MOVE:
				mArgument.getAreaManager().setSlipMove(true);
				break;
			case WARP_TO_TEAM:
				for (PLMSUnitView unitView : moveUnitView.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					mArgument.getAreaManager().addWarpUnitView(unitView, moveUnitView);
				}
				break;
			case WARP_TO_TEAM_OF_LESS_HP:
				for (PLMSUnitView unitView : moveUnitView.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					if (getRemainingHPRatio(unitView) <= mSkillModel.getEffectValue()) {
						mArgument.getAreaManager().addWarpUnitView(unitView, moveUnitView);
					}
				}
				break;
			case BLOCK_ENEMY_MOVE:
				mArgument.getAreaManager().addBlockUnitView(skillUnitView);
				break;
			default:
				break;
		}
	}

	private boolean canExecuteSkill(PLMSUnitView unitView) {
		if (mSkillModel == null) {
			return false;
		}
		switch (mRequirementType) {
			case NONE:
				return true;
			case LESS_THAN_MY_HP: {
				return getRemainingHPRatio(unitView) <= mSkillModel.getRequirementValue();
			}
			case GREATER_THAN_MY_HP: {
				return getRemainingHPRatio(unitView) >= mSkillModel.getRequirementValue();
			}
			default:
				// 各 execute メソッドで判定
				return true;
		}
	}

	private float getRemainingHPRatio(PLMSUnitView unitView) {
		PLMSUnitData unitData = unitView.getUnitData();
		return unitData.getCurrentHP() / unitData.getMaxHP() * 100;
	}

	private float getRemainingHPRatio(PLMSBattleUnit battleUnit) {
		PLMSUnitData unitData = battleUnit.getUnitData();
		return battleUnit.getResultHP() / unitData.getMaxHP() * 100;
	}

	private MYArrayList<PLMSUnitView> getTargetUnitViewArray(PLMSUnitView skillUnitView, PLMSBattleUnit battleUnit) {
		MYArrayList<PLMSUnitView> resultArray = new MYArrayList<>();
		switch (mTargetType) {
			case NONE:
				break;
			case SELF:
				resultArray.add(skillUnitView);
				break;
			case TEAM_IN_RANGE: {
				PLMSLandView landView = (battleUnit != null && skillUnitView.equals(battleUnit.getUnitView())) ?
						battleUnit.getLandView() : skillUnitView.getLandView();
				PLMSArmyStrategy army = skillUnitView.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(landView, army.getAliveUnitViewArray());
				break;
			}
			case TEAM_OTHER_THAN_ME:
				break;
			case ALL_TEAM_MEMBER:
				break;
			case ENEMY: {
				PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
				if (enemyUnit.getResultHP() <= 0) {
					break;
				}
				resultArray.add(enemyUnit.getUnitView());
				break;
			}
			case ENEMY_IN_ENEMY_RANGE: {
				PLMSBattleUnit enemy = battleUnit.getEnemyUnit();
				PLMSArmyStrategy army = enemy.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(enemy.getLandView(), army.getAliveUnitViewArray());
				break;
			}
			case ENEMY_IN_MY_RANGE: {
				PLMSLandView landView = (battleUnit != null && skillUnitView.equals(battleUnit.getUnitView())) ?
						battleUnit.getLandView() : skillUnitView.getLandView();
				PLMSArmyStrategy army = skillUnitView.getUnitData().getArmyStrategy().getEnemyArmy();
				resultArray = getInRangeUnitArray(landView, army.getAliveUnitViewArray());
				break;
			}
		}
		int targetWeapon = mSkillModel.getTargetWeapon();
		int targetBranch = mSkillModel.getTargetBranch();
		if (targetWeapon == 0 && targetBranch == 0) {
			return resultArray;
		}

		MYArrayList<PLMSUnitView> filteredArray = new MYArrayList<>();
		for (PLMSUnitView unitView : resultArray) {
			PLMSUnitData unitData = unitView.getUnitData();
			if (unitData.getWeapon().getNo() == targetWeapon || unitData.getBranch().getNo() == targetBranch) {
				filteredArray.add(unitView);
			}
		}
		return filteredArray;
	}

	private MYArrayList<PLMSUnitView> getInRangeUnitArray(PLMSLandView skillLandView,
														  MYArrayList<PLMSUnitView> baseUnitArray) {
		Point skillPoint = skillLandView.getPoint();
		MYArrayList<PLMSUnitView> resultArray = new MYArrayList<>(baseUnitArray.size());
		int range = mSkillModel.getTargetRange();
		for (PLMSUnitView unitView : baseUnitArray) {
			PLMSLandView unitLandView = unitView.getLandView();
			int difference = MYMathUtil.difference(skillPoint, unitLandView.getPoint());
			// 同じ位置は除く
			if (difference != 0 && difference <= range) {
				resultArray.add(unitView);
			}
		}
		return resultArray;
	}

	private int getCurrentHPForBattle(PLMSUnitView targetUnitView, PLMSBattleResult battleResult) {
		if (battleResult.getLeftUnit().getUnitView().equals(targetUnitView)) {
			return battleResult.getLeftUnit().getResultHP();
		} else if (battleResult.getRightUnit().getUnitView().equals(targetUnitView)) {
			return battleResult.getRightUnit().getResultHP();
		}
		return targetUnitView.getUnitData().getCurrentHP();
	}

	// ユニットの移動。片方のユニットしか移動しない場合は Point が null
	private void moveUnit(PLMSUnitView skillUnitView, Point skillPoint,
						  PLMSUnitView targetUnitView, Point targetPoint) {
		PLMSLandView skillLandView = mArgument.getFieldView().getLandViewForPoint(skillPoint);
		PLMSLandView targetLandView = mArgument.getFieldView().getLandViewForPoint(targetPoint);
		Boolean canMoveSkillUnit = skillLandView != null && canMoveUnit(skillUnitView, skillLandView, targetUnitView);
		Boolean canMoveTargetUnit = targetLandView != null && canMoveUnit(targetUnitView, targetLandView, skillUnitView);
		if ((skillPoint != null && skillUnitView.getLandView().equals(targetLandView) && !canMoveSkillUnit)
				|| (targetPoint != null && targetUnitView.getLandView().equals(skillLandView) && !canMoveTargetUnit)
				|| (!canMoveSkillUnit && !canMoveTargetUnit)) {
			// 移動先にいる一方のユニットが移動不可であるため、もう一方のユニットも移動不可
			return;
		}

		PLMSAnimationManager animationManager = mArgument.getAnimationManager();
		MYArrayList<Animator> animatorArray = new MYArrayList<>();
		if (canMoveSkillUnit) {
			animatorArray.add(animationManager.getMovementAnimation(skillUnitView, skillUnitView.getLandView(), skillLandView));
			skillUnitView.moveToLand(skillLandView);
		}
		if (canMoveTargetUnit) {
			animatorArray.add(animationManager.getMovementAnimation(targetUnitView, targetUnitView.getLandView(), targetLandView));
			targetUnitView.moveToLand(targetLandView);
		}
		if (animatorArray.size() == 0) {
			return;
		}
		animationManager.addTogetherAnimatorArray(animatorArray);
	}

	// ignoreUnitView : 移動時に位置を無視する UnitView
	private boolean canMoveUnit(PLMSUnitView moveUnitView, PLMSLandView targetLandView, PLMSUnitView ignoreUnitView) {
		PLMSUnitData unitData = moveUnitView.getUnitData();
		if (unitData.moveCost(targetLandView.getLandData()) >= 9) {
			// 侵入不可の地形
			return false;
		}
		PLMSUnitView landUnitView = targetLandView.getUnitView();
		if (landUnitView != null && !ignoreUnitView.equals(landUnitView)) {
			// 他のユニットが移動先にいる
			return false;
		}
		PLMSLandView currentLandView = moveUnitView.getLandView();
		MYArrayList<Point> halfwayPointArray = MYPointUtil.getHalfwayPointArray(
				currentLandView.getPoint(), targetLandView.getPoint());
		for (Point point : halfwayPointArray) {
			PLMSLandView landView = mArgument.getFieldView().getLandViewForPoint(point);
			PLMSUnitView unitView = landView.getUnitView();
			if (moveUnitView.isEnemy(unitView) && !ignoreUnitView.equals(unitView)) {
				// 移動経路の途中に敵ユニット
				return false;
			}
		}
		return true;
	}

	private void setBuffToUnitArray(MYArrayList<PLMSUnitView> targetUnitArray) {
		int statusType = mSkillModel.getStatusType();
		int value = mSkillModel.getEffectValue();
		for (PLMSUnitView targetUnit : targetUnitArray) {
			PLMSUnitData unitData = targetUnit.getUnitData();
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
		}
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
		START_BATTLE(11), ATTACK_TO_ENEMY(12), ATTACK_TO_ME(13), START_TEAM_BATTLE(14),
		FINISH_BATTLE(16), FINISH_ATTACK_BATTLE(17), FINISH_DEFENCE_BATTLE(18),
		MY_MOVEMENT(21), ENEMY_MOVEMENT(22),
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
		LESS_THAN_MY_HP(1), GREATER_THAN_MY_HP(2),
		NUMBER_OF_TURN(11);

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
		SELF(1), TEAM_IN_RANGE(2), TEAM_OTHER_THAN_ME(3), ALL_TEAM_MEMBER(4),
		ENEMY(11), ENEMY_IN_ENEMY_RANGE(12), ENEMY_IN_MY_RANGE(13);

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
		FLUCTUATE_HP(11), HEAL_ME_TOO(12),
		CHASE_ATTACK_IF_HAS_COUNTER(21), CHASE_ATTACK(22), CONTINUOUSLY_CHASE_ATTACK(23),
		DISABLE_CHASE_ATTACK(26), KILL_WEAPON(27), PREEMPTIVE_ATTACK(28),
		SLIP_MOVE(31),WARP_TO_TEAM(32), WARP_TO_TEAM_OF_LESS_HP(33), BLOCK_ENEMY_MOVE(36),
		THREE_WAY_INTENSIFICATION(41), ALL_RANGE_COUNTER(42), PROTECT_WEAKNESS_ATTACK(43),
		PUSH_ONE_SQUARE(51), GO_BACKWARD(52), SWAP_POSITION(53);

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
