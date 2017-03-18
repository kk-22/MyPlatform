package jp.co.my.myplatform.service.mysen.army;

import android.graphics.Color;
import android.view.Gravity;

public class PLMSRedArmy extends PLMSArmyStrategy {
	@Override
	public int getHitPointColor() {
		return Color.parseColor("#EA5532");
	}

	@Override
	public int getUnitBackgroundColor() {
		return Color.parseColor("#40ff0000");
	}

	@Override
	public int getInformationColor() {
		return Color.parseColor("#EA5532");
	}

	@Override
	public int getIconGravity() {
		return Gravity.RIGHT;
	}
}
