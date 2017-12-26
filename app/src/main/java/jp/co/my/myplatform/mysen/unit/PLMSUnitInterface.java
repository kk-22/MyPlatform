package jp.co.my.myplatform.mysen.unit;

import jp.co.my.myplatform.mysen.PLMSLandView;
import jp.co.my.myplatform.mysen.PLMSUnitData;
import jp.co.my.myplatform.mysen.PLMSUnitView;

// BattleUnit と UnitView を共通化するための interface
public interface PLMSUnitInterface {

	PLMSUnitView getUnitView();
	PLMSLandView getLandView();
	PLMSUnitData getUnitData();
	int getRemainingHP();
	boolean isAlive();
	PLMSUnitInterface getAnotherUnit();
}
