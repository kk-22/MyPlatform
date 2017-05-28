package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.army.PLMSBlueArmy;
import jp.co.my.myplatform.service.mysen.army.PLMSRedArmy;


public class PLMSArmySetting extends LinearLayout implements PLMSUnitSelectContent.PLMSOnSelectUnitListener {

	private TextView mNameText;
	private Switch mInterfaceSwitch;
	private PLMSUnitListView mUnitListView;

	private PLMSArmyStrategy mArmyStrategy;
	private PLMSArgument mArgument;
	private MYArrayList<PLMSUnitData> mSelectingUnitArray;

	public PLMSArmySetting(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		LayoutInflater.from(context).inflate(R.layout.mysen_view_army_setting, this);
		mNameText = (TextView) findViewById(R.id.name_text);
		mInterfaceSwitch = (Switch) findViewById(R.id.interface_switch);
		mUnitListView = (PLMSUnitListView) findViewById(R.id.unit_list);

		mUnitListView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLMSUnitSelectContent selectContent = new PLMSUnitSelectContent(
						mArgument, mArmyStrategy,
						mSelectingUnitArray, PLMSArmySetting.this);
				PLCoreService.getNavigationController().pushView(selectContent);
			}
		});
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

		mSelectingUnitArray = armyStrategy.getUnitDataArray();
		mUnitListView.loadUnitList(mSelectingUnitArray);
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

	@Override
	public void onSelectUnit(MYArrayList<PLMSUnitData> selectingUnitArray) {
		mSelectingUnitArray = selectingUnitArray;
		mUnitListView.loadUnitList(mSelectingUnitArray);
	}
}
