package jp.co.my.myplatform.service.mysen.Army;

import jp.co.my.myplatform.service.mysen.PLMSUnitView;

public abstract class PLMSArmyStrategy {

	public PLMSArmyStrategy() {
	}

	public abstract int getHitPointColor();
	public abstract int getInformationColor();

	public boolean isEnemy(PLMSUnitView unitView) {
		if (unitView == null) {
			return false;
		}
		return !equals(unitView.getUnitData().getArmyStrategy());
	}
}
