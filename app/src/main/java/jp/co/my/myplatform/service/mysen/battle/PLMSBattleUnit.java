package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSBranchData;
import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;

public class PLMSBattleUnit extends PLMSForecastUnit {

	private PLMSBattleUnit mAnotherUnit;
	private int[] mBattleBuffs;
	private int mTotalAttack; // 3すくみ・スキル補正後の値（奥義スキルは除く）
	private int mChasePoint; // 追撃補正値（0の時速さ参照。1以上で絶対追撃）

	private MYArrayList<PLMSSkillData.EffectType> mSkillEffectArray;

	public PLMSBattleUnit(PLMSUnitView unitView, PLMSLandView landView) {
		super(unitView, landView);

		mBattleBuffs = new int[PLMSUnitData.PARAMETER_NUMBER];
		mSkillEffectArray = new MYArrayList<>();
		mChasePoint = 0;
	}

	public boolean canChaseAttack() {
		if (mChasePoint > 0) {
			return true;
		} else if (mChasePoint < 0) {
			return false;
		}
		return mAnotherUnit.getBattleSpeed() + 5 <= getBattleSpeed();
	}

	public int getDefenseForEnemyAttack() {
		if (mAnotherUnit.getUnitView().getUnitData().getBranch().isPhysicalAttack()) {
			return getBattleDefense();
		} else {
			return getBattleMagicDefense();
		}
	}

	public void initParamsWithEnemyUnit(int threeWayRatio) {
		PLMSBranchData enemyBranch = mAnotherUnit.getUnitView().getUnitData().getBranch();
		int compatibility = mUnitView.getUnitData().getBranch().threeWayCompatibility(enemyBranch);
		// 正負どちらでも0に近い値を採用する
		// Math.floor は負の値の時にも低い値を採用するため使用不可
		int plusDamage = getBattleAttack() * threeWayRatio / 100 * compatibility;
		mTotalAttack = getBattleAttack() + plusDamage;
	}

	private int getBattleParameterOfNo(int no) {
		return mUnitView.getUnitData().getCurrentParameterOfNo(no) + mBattleBuffs[no];
	}

	public void incrementChasePoint() {
		mChasePoint += 1;
	}

	public void decrementChasePoint() {
		mChasePoint -= 1;
	}

	// getter
	@Override
	public PLMSBattleUnit getAnotherUnit() {
		return mAnotherUnit;
	}

	public int getBattleAttack() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_ATTACK);
	}

	public int getBattleSpeed() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_SPEED);
	}

	public int getBattleDefense() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_DEFENSE);
	}

	public int getBattleMagicDefense() {
		return getBattleParameterOfNo(PLMSUnitData.PARAMETER_MAGIC_DEFENSE);
	}

	public int getTotalAttack() {
		return mTotalAttack;
	}

	public MYArrayList<PLMSSkillData.EffectType> getSkillEffectArray() {
		return mSkillEffectArray;
	}

	// setter
	public void setAnotherUnit(PLMSBattleUnit anotherUnit) {
		mAnotherUnit = anotherUnit;
	}

	public void setRemainingHP(int remainingHP) {
		mRemainingHP = remainingHP;
	}

	// TODO: battleBuffは最大値採用方式ではなく加算方式
	public void setBattleBuffOfNo(int parameterNo, int buff) {
		if (mBattleBuffs[parameterNo] < buff) {
			mBattleBuffs[parameterNo] = buff;
		}
	}

	public void addSkill(PLMSSkillData skillData) {
		mSkillEffectArray.add(skillData.getEffectType());
	}
}
