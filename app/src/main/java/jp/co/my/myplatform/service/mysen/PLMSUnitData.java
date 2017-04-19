package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;

import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillData;
import jp.co.my.myplatform.service.mysen.unit.PLMSSkillModel;

public class PLMSUnitData {

	public static final int PARAMETER_ATTACK = 0;
	public static final int PARAMETER_SPEED = 1;
	public static final int PARAMETER_DEFENSE = 2;
	public static final int PARAMETER_MAGIC_DEFENSE = 3;
	public static final int PARAMETER_NUMBER = 4; // パラメータ数

	public static final int SKILL_WEAPON = 0;
	public static final int SKILL_SUPPORT = 1;
	public static final int SKILL_SECRET = 2;
	public static final int SKILL_A = 3;
	public static final int SKILL_B = 4;
	public static final int SKILL_C = 5;
	public static final int SKILL_NUMBER = 6;

	private PLMSUnitModel mUnitModel;
	private Point mFirstPoint;				// 初期位置
	private PLMSArmyStrategy mArmyStrategy;
	private PLMSArgument mArgument;

	private PLMSBranchData mBranch;
	private PLMSSkillData[] mAllSkills;
	private MYArrayList<PLMSSkillData> mPassiveSkillArray;

	private int mMaxHP;
	private int mCurrentHP;
	private int[] mBaseParams; // バフ適用前の値
	private int[] mBuffParams; // バフの加算値
	private int[] mDebuffParams; // デバフの減算値(正の値)

	private int mMoveCount; // 同一ターン内での移動回数

	public PLMSUnitData(PLMSUnitModel unitModel, Point firstPoint,
						PLMSArmyStrategy armyStrategy, PLMSArgument argument) {
		mUnitModel = unitModel;
		mFirstPoint = firstPoint;
		mArmyStrategy = armyStrategy;
		mArgument = argument;
		mAllSkills = new PLMSSkillData[SKILL_NUMBER];

		mBranch = new PLMSBranchData(mUnitModel.getBranchType());
		mAllSkills[SKILL_WEAPON] = createSkillData(mUnitModel.getWeaponSkillForeign());
		mAllSkills[SKILL_SUPPORT] = createSkillData(mUnitModel.getSupportSkillForeign());
		mAllSkills[SKILL_SECRET] = createSkillData(mUnitModel.getSecretSkillForeign());
		mAllSkills[SKILL_A] = createSkillData(mUnitModel.getPassiveASkillForeign());
		mAllSkills[SKILL_B] = createSkillData(mUnitModel.getPassiveBSkillForeign());
		mAllSkills[SKILL_C] = createSkillData(mUnitModel.getPassiveCSkillForeign());

		mPassiveSkillArray = new MYArrayList<>(3);
		mPassiveSkillArray.add(mAllSkills[SKILL_WEAPON]);
		mPassiveSkillArray.add(mAllSkills[SKILL_A]);
		mPassiveSkillArray.add(mAllSkills[SKILL_B]);
		mPassiveSkillArray.add(mAllSkills[SKILL_C]);

		mBaseParams = new int[PARAMETER_NUMBER];
		mBaseParams[PARAMETER_ATTACK] = mUnitModel.getAttackPoint();
		mBaseParams[PARAMETER_SPEED] = mUnitModel.getSpeedPoint();
		mBaseParams[PARAMETER_DEFENSE] = mUnitModel.getDefensePoint();
		mBaseParams[PARAMETER_MAGIC_DEFENSE] = mUnitModel.getMagicDefensePoint();
		mBuffParams = new int[PARAMETER_NUMBER];
		mDebuffParams = new int[PARAMETER_NUMBER];
		
		mMaxHP = mUnitModel.getHitPoint();
		mCurrentHP = mMaxHP;
		resetParamsForNewTurn();
	}

	public int moveCost(PLMSLandData landData) {
		return 1;
	}

	public String getSmallImagePath() {
		return "unit/" +mUnitModel.getNo() +".png";
	}

	public void resetParamsForNewTurn() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mBuffParams[i] = 0;
		}
		mMoveCount = 0;
	}

	public void resetDebuffParams() {
		for (int i = 0; i < PARAMETER_NUMBER; i++) {
			mDebuffParams[i] = 0;
		}
	}

	// 戦闘後のHP
	public int calculateBattleRemainingHP(int currentHP, int diffHP) {
		return Math.max(0, Math.min(mMaxHP, currentHP + diffHP));
	}

	// スキル効果後のHP(HPが最低1残る)
	public int calculateSkillRemainingHP(int currentHP, int diffHP) {
		return Math.max(1, Math.min(mMaxHP, currentHP + diffHP));
	}

	public boolean isAlive() {
		return (mCurrentHP > 0);
	}

	private PLMSSkillData createSkillData(ForeignKeyContainer<PLMSSkillModel> foreign) {
		if (foreign == null) {
			return new PLMSSkillData(mArgument, null);
		}
		return new PLMSSkillData(mArgument, foreign.load());
	}

	// getter
	public PLMSUnitModel getUnitModel() {
		return mUnitModel;
	}

	public PLMSBranchData getBranch() {
		return mBranch;
	}

	public int getMaxHP() {
		return mMaxHP;
	}

	public int getCurrentHP() {
		return mCurrentHP;
	}

	public PLMSArmyStrategy getArmyStrategy() {
		return mArmyStrategy;
	}

	public int getMoveCount() {
		return mMoveCount;
	}

	public Point getFirstPoint() {
		return mFirstPoint;
	}

	public PLMSSkillData getSkillOfNo(int no) {
		return mAllSkills[no];
	}

	public PLMSSkillData getSupportSkillData() {
		return mAllSkills[SKILL_SUPPORT];
	}

	public MYArrayList<PLMSSkillData> getPassiveSkillArray() {
		return mPassiveSkillArray;
	}

	public int getCurrentParameterOfNo(int no) {
		return mBaseParams[no] + mBuffParams[no] - mDebuffParams[no];
	}

	public int getBaseParameterOfNo(int no) {
		return mBaseParams[no];
	}

	public int getBuffParameterOfNo(int no) {
		return mBuffParams[no];
	}

	public int getDebuffParameterOfNo(int no) {
		return mDebuffParams[no];
	}

	// setter
	public void setCurrentHP(int currentHP) {
		mCurrentHP = currentHP;
	}

	public void setBuffOfNo(int parameterNo, int buff) {
		if (buff > 0) {
			if (mBuffParams[parameterNo] < buff) {
				mBuffParams[parameterNo] = buff;
			}
		} else {
			int num = Math.abs(buff);
			if (mDebuffParams[parameterNo] < num) {
				mDebuffParams[parameterNo] = num;
			}
		}
	}
}
