package jp.co.my.myplatform.service.mysen.army;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public abstract class PLMSArmyStrategy {

	private MYArrayList<PLMSUnitView> mUnitViewArray;

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
	public abstract int getUnitBackgroundColor();
	public abstract int getInformationColor();
	public abstract int getIconGravity();

	// getter
	public MYArrayList<PLMSUnitView> getUnitViewArray() {
		return mUnitViewArray;
	}
}
