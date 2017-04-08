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

	public void executeStartTurnSkill(PLMSUnitInterface skillUnit, int numberOfTurn) {
		if (mTimingType != TimingType.START_TURN) {
			return;
		}
		if (!canExecuteSkill(skillUnit)) {
			return;
		}
		if (mRequirementType == RequirementType.NUMBER_OF_TURN
				&& numberOfTurn % mSkillModel.getRequirementValue() != 1) {
			return;
		}

		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(skillUnit, null);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case FLUCTUATE_HP: {
				PLMSUnitData unitData = skillUnit.getUnitData();
				int diffHP = mSkillModel.getEffectValue();
				int remainingHP = unitData.calculateSkillRemainingHP(unitData.getCurrentHP(), diffHP);
				PLMSAnimationManager animationManager = mArgument.getAnimationManager();
				Animator animator = animationManager.getFluctuateHPAnimation(skillUnit.getUnitView(), remainingHP, diffHP);
				animationManager.addTogetherAnimator(animator);
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 StartTurn スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeStartBattleSkill(PLMSUnitInterface skillUnit,
										PLMSBattleUnit battleUnit, PLMSBattleResult battleResult) {
		if (mTimingType == null) {
			return;
		}
		boolean isAttacker = battleResult.getLeftUnit().equals(battleUnit);
		boolean isBattleUnit = battleUnit.getUnitView().equals(skillUnit);
		switch (mTimingType) {
			case START_BATTLE: if (!isBattleUnit) {return;} break;
			case ATTACK_TO_ENEMY: if (!isBattleUnit || !isAttacker) {return;} break;
			case ATTACK_TO_ME: if (!isBattleUnit || isAttacker) {return;} break;
			case START_TEAM_BATTLE: if (isBattleUnit) {return;} break;
			default: return;
		}

		PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
		if (!canExecuteSkill(skillUnit)) {
			return;
		}
		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(skillUnit, battleResult);
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
		MYArrayList<PLMSUnitInterface> targetArray = getTargetUnitViewArray(battleUnit, battleResult);
		if (mTargetType != TargetType.NONE && targetArray.size() == 0) {
			return;
		}

		switch (mEffectType) {
			case FLUCTUATE_HP: {
				for (PLMSUnitInterface targetUnit : targetArray) {
					// TODO: UnitInterfaceで共通化
					int currentHP = getCurrentHPForBattle(targetUnit, battleResult);
					int diffHP = mSkillModel.getEffectValue();
					int remainingHP = targetUnit.getUnitData().calculateSkillRemainingHP(currentHP, diffHP);
					PLMSAnimationManager animationManager = mArgument.getAnimationManager();
					Animator animator = animationManager.getFluctuateHPAnimation(targetUnit.getUnitView(), remainingHP, diffHP);
					animationManager.addTogetherAnimator(animator);
				}
				break;
			}
			case ONE_TURN_BUFF: {
				setBuffToUnitArray(targetArray);
				break;
			}
			case PUSH_ONE_SQUARE: {
				PLMSUnitInterface enemyUnit = battleUnit.getEnemyUnit();
				Point enemyPoint = MYPointUtil.getMovePoint(unitView.getLandView().getPoint(), enemyUnit.getLandView().getPoint(), 1);
				moveUnit(unitView, null, enemyUnit, enemyPoint);
				break;
			}
			case GO_BACKWARD: {
				PLMSUnitInterface enemyUnit = battleUnit.getEnemyUnit();
				Point selfPoint = MYPointUtil.getMovePoint(enemyUnit.getLandView().getPoint(), unitView.getLandView().getPoint(), 1);
				Point enemyPoint = MYPointUtil.getMovePoint(unitView.getLandView().getPoint(), enemyUnit.getLandView().getPoint(), -1);
				moveUnit(unitView, selfPoint, enemyUnit, enemyPoint);
				break;
			}
			case SWAP_POSITION: {
				PLMSBattleUnit enemyUnit = battleUnit.getEnemyUnit();
				Point enemyPoint = (enemyUnit.isAlive()) ? unitView.getCurrentPoint() : null;
				moveUnit(unitView, enemyUnit.getUnitView().getCurrentPoint(), enemyUnit, enemyPoint);
				break;
			}
			default:
				MYLogUtil.showErrorToast("未実装 FinishBattle スキル " +mSkillModel.getName() +" " +mEffectType.getInt());
				break;
		}
	}

	public void executeMovementSkill(PLMSUnitInterface skillUnit, PLMSUnitInterface moveUnit) {
		if (!(mTimingType == TimingType.MY_MOVEMENT && moveUnit == skillUnit)
				&& !(mTimingType == TimingType.ENEMY_MOVEMENT && skillUnit.getUnitView().isEnemy(moveUnit))) {
			return;
		}
		if (!canExecuteSkill(skillUnit)) {
			return;
		}

		switch (mEffectType) {
			case SLIP_MOVE:
				mArgument.getAreaManager().setSlipMove(true);
				break;
			case WARP_TO_TEAM:
				for (PLMSUnitView unitView : moveUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					mArgument.getAreaManager().addWarpUnitView(unitView, moveUnit.getUnitView());
				}
				break;
			case WARP_TO_TEAM_OF_LESS_HP:
				for (PLMSUnitView unitView : moveUnit.getUnitData().getArmyStrategy().getAliveUnitViewArray()) {
					if (getRemainingHPRatio(unitView) <= mSkillModel.getEffectValue()) {
						mArgument.getAreaManager().addWarpUnitView(unitView, moveUnit.getUnitView());
					}
				}
				break;
			case BLOCK_ENEMY_MOVE:
				mArgument.getAreaManager().addBlockUnitView(skillUnit.getUnitView());
				break;
			default:
				break;
		}
	}

	private boolean canExecuteSkill(PLMSUnitInterface skillUnit) {
		if (mSkillModel == null) {
			return false;
		}
		switch (mRequirementType) {
			case NONE:
				return true;
			case LESS_THAN_MY_HP: {
				return getRemainingHPRatio(skillUnit) <= mSkillModel.getRequirementValue();
			}
			case GREATER_THAN_MY_HP: {
				return getRemainingHPRatio(skillUnit) >= mSkillModel.getRequirementValue();
			}
			default:
				// 各 execute メソッドで判定
				return true;
		}
	}

	private float getRemainingHPRatio(PLMSUnitInterface unitView) {
		PLMSUnitData unitData = unitView.getUnitData();
		return unitData.getCurrentHP() / unitData.getMaxHP() * 100;
	}

	// battleUnit : 戦闘を行った自身もしくは味方ユニット
	private MYArrayList<PLMSUnitInterface> getTargetUnitViewArray(PLMSUnitInterface skillUnit, PLMSBattleResult battleResult) {
		MYArrayList<PLMSUnitInterface> resultArray = new MYArrayList<>();
		switch (mTargetType) {
			case NONE:
				break;
			case SELF:
				resultArray.add(skillUnit);
				break;
			case TEAM_IN_RANGE: {
				PLMSArmyStrategy army = skillUnit.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(skillUnit, battleResult, army.getAliveUnitViewArray());
				break;
			}
			case TEAM_OTHER_THAN_ME:
				break;
			case ENEMY: {
				PLMSBattleUnit enemyUnit = battleResult.getBattleUnitOfUnitTeam(skillUnit).getEnemyUnit();
				if (enemyUnit.getResultHP() <= 0) {
					break;
				}
				resultArray.add(enemyUnit.getUnitView());
				break;
			}
			case ENEMY_IN_ENEMY_RANGE: {
				PLMSBattleUnit enemy = battleResult.getBattleUnitOfUnitTeam(skillUnit).getEnemyUnit();
				PLMSArmyStrategy army = enemy.getUnitData().getArmyStrategy();
				resultArray = getInRangeUnitArray(enemy, battleResult, army.getAliveUnitViewArray());
				break;
			}
			case ENEMY_IN_MY_RANGE: {
				PLMSArmyStrategy army = skillUnit.getUnitData().getArmyStrategy().getEnemyArmy();
				resultArray = getInRangeUnitArray(skillUnit, battleResult, army.getAliveUnitViewArray());
				break;
			}
		}
		int targetWeapon = mSkillModel.getTargetWeapon();
		int targetBranch = mSkillModel.getTargetBranch();
		if (targetWeapon == 0 && targetBranch == 0) {
			return resultArray;
		}

		MYArrayList<PLMSUnitInterface> filteredArray = new MYArrayList<>();
		for (PLMSUnitInterface unitView : resultArray) {
			PLMSUnitData unitData = unitView.getUnitData();
			if (unitData.getWeapon().getNo() == targetWeapon || unitData.getBranch().getNo() == targetBranch) {
				filteredArray.add(unitView);
			}
		}
		return filteredArray;
	}

	private MYArrayList<PLMSUnitInterface> getInRangeUnitArray(PLMSUnitInterface centerUnit,
															   PLMSBattleResult battleResult,
															   MYArrayList<PLMSUnitView> baseUnitArray) {
		Point centerPoint;
		if (battleResult == null) {
			centerPoint = centerUnit.getLandView().getPoint();
		} else {
			// 戦闘時の UnitView と BattleUnit の位置の違いを吸収
			centerPoint= battleResult.getUnitOfBattle(centerUnit).getLandView().getPoint();
		}

		MYArrayList<PLMSUnitInterface> resultArray = new MYArrayList<>(baseUnitArray.size());
		int range = mSkillModel.getTargetRange();
		for (PLMSUnitView unitView : baseUnitArray) {
			PLMSLandView unitLandView;
			if (battleResult == null) {
				unitLandView = unitView.getLandView();
			} else {
				unitLandView = battleResult.getUnitOfBattle(unitView).getLandView();
			}
			int difference = MYMathUtil.difference(centerPoint, unitLandView.getPoint());
			// 同じ位置は除く
			if (difference != 0 && difference <= range) {
				resultArray.add(unitView);
			}
		}
		return resultArray;
	}

	private int getCurrentHPForBattle(PLMSUnitInterface targetUnitView, PLMSBattleResult battleResult) {
		if (battleResult.getLeftUnit().getUnitView().equals(targetUnitView)) {
			return battleResult.getLeftUnit().getResultHP();
		} else if (battleResult.getRightUnit().getUnitView().equals(targetUnitView)) {
			return battleResult.getRightUnit().getResultHP();
		}
		return targetUnitView.getUnitData().getCurrentHP();
	}

	// ユニットの移動。片方のユニットしか移動しない場合は Point が null
	private void moveUnit(PLMSUnitInterface skillUnit, Point skillPoint,
						  PLMSUnitInterface targetUnit, Point targetPoint) {
		PLMSLandView skillLandView = mArgument.getFieldView().getLandViewForPoint(skillPoint);
		PLMSLandView targetLandView = mArgument.getFieldView().getLandViewForPoint(targetPoint);
		Boolean canMoveSkillUnit = skillLandView != null && canMoveUnit(skillUnit, skillLandView, targetUnit);
		Boolean canMoveTargetUnit = targetLandView != null && canMoveUnit(targetUnit, targetLandView, skillUnit);
		if ((skillPoint != null && skillUnit.getLandView().equals(targetLandView) && !canMoveSkillUnit)
				|| (targetPoint != null && targetUnit.getLandView().equals(skillLandView) && !canMoveTargetUnit)
				|| (!canMoveSkillUnit && !canMoveTargetUnit)) {
			// 移動先にいる一方のユニットが移動不可であるため、もう一方のユニットも移動不可
			return;
		}

		PLMSAnimationManager animationManager = mArgument.getAnimationManager();
		MYArrayList<Animator> animatorArray = new MYArrayList<>();
		if (canMoveSkillUnit) {
			PLMSUnitView skillUnitView = skillUnit.getUnitView();
			animatorArray.add(animationManager.getMovementAnimation(skillUnitView, skillUnit.getLandView(), skillLandView));
			skillUnitView.moveToLand(skillLandView);
		}
		if (canMoveTargetUnit) {
			PLMSUnitView targetUnitView = targetUnit.getUnitView();
			animatorArray.add(animationManager.getMovementAnimation(targetUnitView, targetUnit.getLandView(), targetLandView));
			targetUnitView.moveToLand(targetLandView);
		}
		if (animatorArray.size() == 0) {
			return;
		}
		animationManager.addTogetherAnimatorArray(animatorArray);
	}

	// ignoreUnit : 移動時に位置を無視する UnitView
	private boolean canMoveUnit(PLMSUnitInterface moveUnit, PLMSLandView targetLandView, PLMSUnitInterface ignoreUnit) {
		PLMSUnitData unitData = moveUnit.getUnitData();
		if (unitData.moveCost(targetLandView.getLandData()) >= 9) {
			// 侵入不可の地形
			return false;
		}
		PLMSUnitView landUnitView = targetLandView.getUnitView();
		PLMSUnitView ignoreUnitView = ignoreUnit.getUnitView();
		if (landUnitView != null && !ignoreUnitView.equals(landUnitView)) {
			// 他のユニットが移動先にいる
			return false;
		}
		PLMSLandView currentLandView = moveUnit.getLandView();
		PLMSUnitView moveUnitView = moveUnit.getUnitView();
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

	private void setBuffToUnitArray(MYArrayList<PLMSUnitInterface> targetUnitArray) {
		int statusType = mSkillModel.getStatusType();
		int value = mSkillModel.getEffectValue();
		for (PLMSUnitInterface targetUnit : targetUnitArray) {
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
		SELF(1), TEAM_IN_RANGE(2), TEAM_OTHER_THAN_ME(3),
		TARGET(6), TARGET_AND_SELF(7),
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
		ONE_TURN_BUFF(1), BATTLE_BUFF(2),
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
