package jp.co.my.myplatform.service.mysen.army;

import android.graphics.Color;
import android.view.Gravity;

import jp.co.my.myplatform.service.mysen.PLMSArgument;

public class PLMSRedArmy extends PLMSArmyStrategy {

	public PLMSRedArmy(PLMSArgument argument, String name, int interfaceNo) {
		super(argument, name, interfaceNo);
	}

	@Override
	public int getHitPointColor() {
		return Color.parseColor("#EA5532");
	}

	@Override
	public int getAvailableAreaColor() {
		return Color.parseColor("#40ff0000");
	}

	@Override
	public int getInformationColor() {
		return Color.parseColor("#FA6964");
	}

	@Override
	public int getIconGravity() {
		return Gravity.RIGHT;
	}
}
