package jp.co.my.myplatform.service.mysen.army;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
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
	private MYArrayList<PLMSUnitData> mUnitDataArray;
	private MYArrayList<PLMSUnitView> mAliveUnitViewArray;
	private MYArrayList<PLMSUnitView> mAllUnitViewArray;
	private PLMSWarInterface mWarInterface;
	private PLMSArmyStrategy mEnemyArmy;

	public PLMSArmyStrategy(PLMSArgument argument, String name, int interfaceNo) {
		mArgument = argument;
		mName = name;
		mInterfaceNo = interfaceNo;
		mUnitDataArray = new MYArrayList<>();
		mAllUnitViewArray = new MYArrayList<>();
		mAliveUnitViewArray = new MYArrayList<>();
	}

	// ユニットの登録
	public void addUnitView(PLMSUnitView unitView) {
		mAllUnitViewArray.add(unitView);
		mAliveUnitViewArray.add(unitView);
	}

	// ユニットの離脱
	public void withdrawalUnitView(PLMSUnitView unitView) {
		mAliveUnitViewArray.remove(unitView);
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

	public MYArrayList<PLMSUnitData> getUnitDataArray() {
		return mUnitDataArray;
	}

	public MYArrayList<PLMSUnitView> getAliveUnitViewArray() {
		return mAliveUnitViewArray;
	}

	public MYArrayList<PLMSUnitView> getAliveUnitViewArray(PLMSUnitView ignoreUnitView) {
		MYArrayList<PLMSUnitView> resultArray = new MYArrayList<>();
		for (PLMSUnitView unitView : mAliveUnitViewArray) {
			if (!unitView.equals(ignoreUnitView)) {
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
