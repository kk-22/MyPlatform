package jp.co.my.myplatform.service.mysen.battle;

import jp.co.my.myplatform.service.mysen.unit.PLMSUnitInterface;

public abstract class PLMSBaseForecast {

	PLMSBaseForecast() {
	}

	public abstract PLMSUnitInterface getLeftUnit();
	public abstract PLMSUnitInterface getRightUnit();
	public abstract String getInformationTitle();
}
