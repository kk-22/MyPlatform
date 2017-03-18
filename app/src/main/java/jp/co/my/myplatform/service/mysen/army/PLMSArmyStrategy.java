package jp.co.my.myplatform.service.mysen.army;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;
import jp.co.my.myplatform.service.mysen.PLMSUserInterface;

public abstract class PLMSArmyStrategy {

	private MYArrayList<PLMSUnitView> mUnitViewArray;
	private PLMSUserInterface mUnitInterface;

	public PLMSArmyStrategy() {
		mUnitViewArray = new MYArrayList<>();
	}

	public void addUnitView(PLMSUnitView unitView) {
		mUnitViewArray.add(unitView);
	}

	public boolean hasUnitView(PLMSUnitView unitView) {
		return mUnitViewArray.contains(unitView);
	}

	public abstract int getHitPointColor();
	public abstract int getAvailableAreaColor();
	public abstract int getInformationColor();
	public abstract int getIconGravity();

	// getter
	public MYArrayList<PLMSUnitView> getUnitViewArray() {
		return mUnitViewArray;
	}

	public PLMSUserInterface getUnitInterface() {
		return mUnitInterface;
	}

	// setter
	public void setUnitInterface(PLMSUserInterface unitInterface) {
		mUnitInterface = unitInterface;
	}
}
