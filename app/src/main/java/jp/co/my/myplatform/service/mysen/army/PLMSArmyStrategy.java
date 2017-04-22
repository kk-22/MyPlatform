package jp.co.my.myplatform.service.mysen.army;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.userinterface.PLMSComputerInterface;
import jp.co.my.myplatform.service.mysen.userinterface.PLMSUserInterface;
import jp.co.my.myplatform.service.mysen.userinterface.PLMSWarInterface;

public abstract class PLMSArmyStrategy {

	public static final int INTERFACE_USER = 1;
	public static final int INTERFACE_COMPUTER = 2;

	private String mName;
	private int mInterfaceNo;
	private PLMSArgument mArgument;
	private MYArrayList<PLMSUnitView> mAllUnitViewArray;
	private PLMSWarInterface mWarInterface;
	private PLMSArmyStrategy mEnemyArmy;

	public PLMSArmyStrategy(PLMSArgument argument, String name, int interfaceNo) {
		mArgument = argument;
		mName = name;
		mInterfaceNo = interfaceNo;
		mAllUnitViewArray = new MYArrayList<>();
	}

	public void addUnitView(PLMSUnitView unitView) {
		mAllUnitViewArray.add(unitView);
	}

	public boolean hasUnitView(PLMSUnitView unitView) {
		return mAllUnitViewArray.contains(unitView);
	}

	public PLMSWarInterface makeInterface() {
		switch (mInterfaceNo) {
			case 1:
				mWarInterface = new PLMSUserInterface(mArgument, this);
				break;
			case 2:
				mWarInterface = new PLMSComputerInterface(mArgument, this);
				break;
			default:
				MYLogUtil.showErrorToast("未対応のmInterfaceNo=" +mInterfaceNo);
				break;
		}
		return mWarInterface;
	}

	public abstract int getHitPointColor();
	public abstract int getAvailableAreaColor();
	public abstract int getInformationColor();
	public abstract int getIconGravity();

	// getter
	public MYArrayList<PLMSUnitView> getAllUnitViewArray() {
		return mAllUnitViewArray;
	}

	public PLMSWarInterface getWarInterface() {
		return mWarInterface;
	}

	public MYArrayList<PLMSUnitView> getAliveUnitViewArray() {
		return getAliveUnitViewArray(null);
	}

	public MYArrayList<PLMSUnitView> getAliveUnitViewArray(PLMSUnitView ignoreUnitView) {
		MYArrayList<PLMSUnitView> resultArray = new MYArrayList<>();
		for (PLMSUnitView unitView : mAllUnitViewArray) {
			if (!unitView.equals(ignoreUnitView) && unitView.getUnitData().isAlive()) {
				resultArray.add(unitView);
			}
		}
		return resultArray;
	}

	public PLMSArmyStrategy getEnemyArmy() {
		return mEnemyArmy;
	}

	public String getName() {
		return mName;
	}

	public int getInterfaceNo() {
		return mInterfaceNo;
	}

	// setter
	public void setEnemyArmy(PLMSArmyStrategy enemyArmy) {
		mEnemyArmy = enemyArmy;
	}

	public void setInterfaceNo(int interfaceNo) {
		mInterfaceNo = interfaceNo;
	}
}
