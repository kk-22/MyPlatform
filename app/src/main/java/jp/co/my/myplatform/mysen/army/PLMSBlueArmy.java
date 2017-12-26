package jp.co.my.myplatform.mysen.army;

import android.graphics.Color;
import android.view.Gravity;

import jp.co.my.myplatform.mysen.PLMSArgument;

public class PLMSBlueArmy extends PLMSArmyStrategy {

	public PLMSBlueArmy(PLMSArgument argument, String name, int interfaceNo) {
		super(argument, name, interfaceNo);
	}

	@Override
	public int getHitPointColor() {
		return Color.parseColor("#00AFEC");
	}

	@Override
	public int getAvailableAreaColor() {
		return Color.parseColor("#400000ff");
	}

	@Override
	public int getInformationColor() {
		return Color.parseColor("#00AFEC");
	}

	@Override
	public int getIconGravity() {
		return Gravity.LEFT;
	}
}
