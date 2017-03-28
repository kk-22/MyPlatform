package jp.co.my.myplatform.service.mysen.army;

import android.graphics.Color;
import android.view.Gravity;

public class PLMSBlueArmy extends PLMSArmyStrategy {
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
