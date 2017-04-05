package jp.co.my.myplatform.service.mysen.army;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.PLMSUserInterface;

public abstract class PLMSArmyStrategy {

	private MYArrayList<PLMSUnitView> mAllUnitViewArray;
	private PLMSUserInterface mUnitInterface;
	private PLMSArmyStrategy mEnemyArmy;

	public PLMSArmyStrategy() {
		mAllUnitViewArray = new MYArrayList<>();
	}

	public void addUnitView(PLMSUnitView unitView) {
		mAllUnitViewArray.add(unitView);
	}

	public boolean hasUnitView(PLMSUnitView unitView) {
		return mAllUnitViewArray.contains(unitView);
	}

	public abstract int getHitPointColor();
	public abstract int getAvailableAreaColor();
	public abstract int getInformationColor();
	public abstract int getIconGravity();

	// getter
	public MYArrayList<PLMSUnitView> getAllUnitViewArray() {
		return mAllUnitViewArray;
	}

	public PLMSUserInterface getUnitInterface() {
		return mUnitInterface;
	}

	public MYArrayList<PLMSUnitView> getAliveUnitViewArray() {
		MYArrayList<PLMSUnitView> resultArray = new MYArrayList<>();
		for (PLMSUnitView unitView : mAllUnitViewArray) {
			if (unitView.getUnitData().isAlive()) {
				resultArray.add(unitView);
			}
		}
		return resultArray;
	}

	public PLMSArmyStrategy getEnemyArmy() {
		return mEnemyArmy;
	}

	// setter
	public void setUnitInterface(PLMSUserInterface unitInterface) {
		mUnitInterface = unitInterface;
	}

	public void setEnemyArmy(PLMSArmyStrategy enemyArmy) {
		mEnemyArmy = enemyArmy;
	}
}
