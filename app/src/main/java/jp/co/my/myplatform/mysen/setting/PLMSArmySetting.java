package jp.co.my.myplatform.mysen.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.mysen.PLMSArgument;
import jp.co.my.myplatform.mysen.PLMSUnitData;
import jp.co.my.myplatform.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.mysen.army.PLMSBlueArmy;
import jp.co.my.myplatform.mysen.army.PLMSRedArmy;


public class PLMSArmySetting extends LinearLayout implements PLMSUnitSelectContent.PLMSOnSelectUnitListener {

	private TextView mNameText;
	private Switch mInterfaceSwitch;
	private PLMSUnitListView mUnitListView;

	private PLMSArmyStrategy mArmyStrategy;
	private PLMSArgument mArgument;
	private MYArrayList<PLMSUnitData> mSelectingUnitArray;
	private PLMSWarSettingListener mListener;

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

	public void initProperties(PLMSArmyStrategy armyStrategy, PLMSArgument argument,
							   PLMSWarSettingListener listener) {
		mArgument = argument;
		mArmyStrategy = armyStrategy;
		mListener = listener;

		mNameText.setText(mArmyStrategy.getName());
		mInterfaceSwitch.setChecked(mArmyStrategy.getInterfaceNo() == PLMSArmyStrategy.INTERFACE_COMPUTER);
		setBackgroundColor(mArmyStrategy.getAvailableAreaColor());

		mSelectingUnitArray = armyStrategy.getUnitDataArray();
		mUnitListView.loadUnitList(mSelectingUnitArray);
	}

	public PLMSArmyStrategy makeArmyInstance(PLMSArgument argument) {
		PLMSArmyStrategy newArmy;
		// TODO: ifなしでインスタンス分ける。Strategyパターンやめる？copyする？
		if (mArmyStrategy instanceof PLMSBlueArmy) {
			newArmy = new PLMSBlueArmy(argument, mArmyStrategy.getName(), getNextInterfaceNo());
		} else {
			newArmy = new PLMSRedArmy(argument, mArmyStrategy.getName(), getNextInterfaceNo());
		}
		for (PLMSUnitData unitData : mSelectingUnitArray) {
			// TODO: mSelectingUnitArray は UnitMode にする？
			PLMSUnitData newUnitData = new PLMSUnitData(unitData.getUnitModel(), newArmy, argument);
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
		mListener.setNeedMakeNewWar();
	}

	interface PLMSWarSettingListener {
		void setNeedMakeNewWar();
	}
}