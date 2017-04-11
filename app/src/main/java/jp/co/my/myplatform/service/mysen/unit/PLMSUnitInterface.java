package jp.co.my.myplatform.service.mysen.unit;

import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

// BattleUnit と UnitView を共通化するための interface
public interface PLMSUnitInterface {

	PLMSUnitView getUnitView();
	PLMSLandView getLandView();
	PLMSUnitData getUnitData();
	int getRemainingHP();
	boolean isAlive();
}
