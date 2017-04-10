package jp.co.my.myplatform.service.mysen.unit;

import jp.co.my.myplatform.service.mysen.PLMSLandView;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitView;

// BattleUnit と UnitView を共通化するための interface
public interface PLMSUnitInterface {

	// TODO: interfaceからAbstractへ変更
	// TODO: UnitDataはこのクラス内でgetUnitView().getUnitData()で返す
	// TODO: getRemainingHPを使用してisAlive()実装
	PLMSUnitView getUnitView();
	PLMSLandView getLandView();
	PLMSUnitData getUnitData();
	int getRemainingHP();
}
