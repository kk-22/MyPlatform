package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.army.PLMSBlueArmy;
import jp.co.my.myplatform.service.mysen.army.PLMSRedArmy;


public class PLMSArmySetting extends LinearLayout {

	private TextView mNameText;
	private Switch mInterfaceSwitch;
	private LinearLayout mUnitListLinear;

	private PLMSArmyStrategy mArmyStrategy;
	private PLMSArgument mArgument;
	private MYArrayList<PLMSUnitData> mSelectingUnitArray;

	public PLMSArmySetting(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_army_setting, this);
		mNameText = (TextView) findViewById(R.id.name_text);
		mInterfaceSwitch = (Switch) findViewById(R.id.interface_switch);
		mUnitListLinear = (LinearLayout) findViewById(R.id.unit_linear);
	}

	public PLMSArmySetting(Context context, AttributeSet attrs){
		this(context, attrs, 0);
	}

	public PLMSArmySetting(Context context) {
		this(context, null);
	}

	public void initProperties(PLMSArmyStrategy armyStrategy, PLMSArgument argument) {
		mArgument = argument;
		mArmyStrategy = armyStrategy;

		mNameText.setText(mArmyStrategy.getName());
		mInterfaceSwitch.setChecked(mArmyStrategy.getInterfaceNo() == PLMSArmyStrategy.INTERFACE_COMPUTER);
		setBackgroundColor(mArmyStrategy.getAvailableAreaColor());

		loadUnitList(armyStrategy.getUnitDataArray());
	}

	private void loadUnitList(MYArrayList<PLMSUnitData> unitDataArray) {
		mUnitListLinear.removeAllViews();
		mSelectingUnitArray = unitDataArray;

		for (int i = 0; i < 6; i++) {
			LinearLayout linearLayout = (LinearLayout) View.inflate(getContext(), R.layout.mysen_cell_unit, null);
			if (i < unitDataArray.size()) {
				PLMSUnitData unitData = unitDataArray.get(i);
				ImageView imageView = (ImageView) linearLayout.findViewById(R.id.unit_image);
				imageView.setImageBitmap(unitData.getImage(getContext()));

				TextView textView = (TextView) linearLayout.findViewById(R.id.unit_name_text);
				textView.setText(unitData.getUnitModel().getName());
			}

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					0, LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 1;
			mUnitListLinear.addView(linearLayout, params);
		}
	}

	public PLMSArmyStrategy makeArmyInstance() {
		PLMSArmyStrategy newArmy;
		// TODO: ifなしでインスタンス分ける。Strategyパターンやめる？copyする？
		if (mArmyStrategy instanceof PLMSBlueArmy) {
			newArmy = new PLMSBlueArmy(mArgument, mArmyStrategy.getName(), getNextInterfaceNo());
		} else {
			newArmy = new PLMSRedArmy(mArgument, mArmyStrategy.getName(), getNextInterfaceNo());
		}
		for (PLMSUnitData unitData : mArmyStrategy.getUnitDataArray()) {
			PLMSUnitData newUnitData = new PLMSUnitData(unitData.getUnitModel(), newArmy, mArgument);
			newArmy.getUnitDataArray().add(newUnitData);
		}
		return newArmy;
	}

	// getter
	public int getNextInterfaceNo() {
		if (mInterfaceSwitch.isChecked()) {
			return PLMSArmyStrategy.INTERFACE_COMPUTER;
		} else {
			return PLMSArmyStrategy.INTERFACE_USER;
		}
	}
}
