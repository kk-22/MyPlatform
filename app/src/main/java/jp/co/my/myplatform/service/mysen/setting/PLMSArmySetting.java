package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;


public class PLMSArmySetting extends LinearLayout {

	private TextView mNameText;
	private Switch mInterfaceSwitch;

	private PLMSArmyStrategy mArmyStrategy;

	public PLMSArmySetting(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_army_setting, this);
		mNameText = (TextView) findViewById(R.id.name_text);
		mInterfaceSwitch = (Switch) findViewById(R.id.interface_switch);
	}

	public PLMSArmySetting(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSArmySetting(Context context) {
		this(context, null);
	}

	// getter
	public int getNextInterfaceNo() {
		if (mInterfaceSwitch.isChecked()) {
			return PLMSArmyStrategy.INTERFACE_COMPUTER;
		} else {
			return PLMSArmyStrategy.INTERFACE_USER;
		}
	}

	// setter
	public void setArmyStrategy(PLMSArmyStrategy armyStrategy) {
		mArmyStrategy = armyStrategy;

		mNameText.setText(mArmyStrategy.getName());
		mInterfaceSwitch.setChecked(mArmyStrategy.getInterfaceNo() == PLMSArmyStrategy.INTERFACE_COMPUTER);
		setBackgroundColor(mArmyStrategy.getAvailableAreaColor());
	}
}
