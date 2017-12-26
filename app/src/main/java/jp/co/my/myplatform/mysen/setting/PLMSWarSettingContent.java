package jp.co.my.myplatform.mysen.setting;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.layout.PLAbsoluteLayoutController;
import jp.co.my.myplatform.mysen.PLMSArgument;
import jp.co.my.myplatform.mysen.PLMSFieldModel;
import jp.co.my.myplatform.mysen.PLMSFieldView;
import jp.co.my.myplatform.mysen.PLMSTurnManager;
import jp.co.my.myplatform.mysen.PLMSUnitData;
import jp.co.my.myplatform.mysen.PLMSWarContent;
import jp.co.my.myplatform.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.mysen.userinterface.PLMSWarInterface;


public class PLMSWarSettingContent extends PLContentView implements PLMSArmySetting.PLMSWarSettingListener {

	private PLMSFieldView mPreviewFieldView;
	private TextView mFieldNameText;
	private PLMSArmySetting mLeftArmyView;
	private PLMSArmySetting mRightArmyView;
	private Button mExecuteButton;
	private Switch mRestartSwitch;

	private PLMSArgument mArgument;
	private PLMSWarContent mWarContent;
	private MYArrayList<PLMSArmySetting> mArmyViewArray;

	private PLMSFieldModel mSelectingFieldMode;

	public PLMSWarSettingContent(PLMSArgument argument, PLMSWarContent warContent) {
		super();
		mArgument = argument;
		mWarContent = warContent;

		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war_setting, this);
		mPreviewFieldView = (PLMSFieldView) findViewById(R.id.preview_field);
		mFieldNameText = (TextView) findViewById(R.id.field_name_text);
		mLeftArmyView = (PLMSArmySetting) findViewById(R.id.left_army);
		mRightArmyView = (PLMSArmySetting) findViewById(R.id.right_army);
		mArmyViewArray = new MYArrayList<>(mLeftArmyView, mRightArmyView);
		mExecuteButton = (Button) findViewById(R.id.execute_button);
		mRestartSwitch = (Switch) findViewById(R.id.restart_switch);

		MYArrayList<PLMSArmyStrategy> armyArray = mArgument.getArmyArray();
		if (armyArray == null) {
			return;
		}
		for (int i = 0; i < 2; i++) {
			PLMSArmySetting armyView = mArmyViewArray.get(i);
			PLMSArmyStrategy armyStrategy = armyArray.get(i);
			armyView.initProperties(armyStrategy, argument, this);
		}

		initEvent();

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				loadFieldView(mArgument.getFieldView().getFieldModel());
			}
		});
	}

	private void initEvent() {
		mExecuteButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				execute();
			}
		});
		mPreviewFieldView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				View targetView = findViewById(R.id.common_setting_layout);
				new PLMSFieldListPopover(new PLMSFieldRecyclerAdapter.PLMSOnClickFieldListener() {
					@Override
					public void onClickField(View view, PLMSFieldModel fieldModel) {
						PLCoreService.getNavigationController().getCurrentView().removeTopPopover();
						if (!fieldModel.equals(mSelectingFieldMode)) {
							loadFieldView(fieldModel);
							setNeedMakeNewWar();
						}
					}
				}).showPopover(new PLAbsoluteLayoutController(
						// mPreviewFieldView を隠さないように表示
						targetView.getWidth(),
						getHeight() - targetView.getHeight(),
						new Point(0, targetView.getHeight())));
			}
		});
		mRestartSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mExecuteButton.setText("再スタート");
				} else {
					mExecuteButton.setText("更新");
				}
			}
		});
	}

	private void loadFieldView(PLMSFieldModel fieldModel) {
		if (fieldModel == null) return;

		mSelectingFieldMode = fieldModel;
		mPreviewFieldView.initForPreview(fieldModel);
		mFieldNameText.setText(fieldModel.getName());
	}

	private void execute() {
		MYArrayList<PLMSArmyStrategy> armyArray = mArgument.getArmyArray();
		if (mRestartSwitch.isChecked()) {
			PLMSArgument newArgument = new PLMSArgument();
			mWarContent.initArgument(newArgument);

			PLMSArmyStrategy leftArmy = mLeftArmyView.makeArmyInstance(newArgument);
			PLMSArmyStrategy rightArmy = mRightArmyView.makeArmyInstance(newArgument);
			leftArmy.setEnemyArmy(rightArmy);
			rightArmy.setEnemyArmy(leftArmy);
			newArgument.setArmyArray(new MYArrayList<>(leftArmy, rightArmy));

			PLMSFieldView fieldView = mArgument.getFieldView();
			fieldView.initForWar(newArgument, mSelectingFieldMode);
			// TODO: WarContentと共通化？
			newArgument.setFieldView(fieldView);
			newArgument.setAllUnitViewArray(newArgument.getFieldView().getUnitViewArray());
			newArgument.setTurnManager(new PLMSTurnManager(newArgument));

			SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
			editor.putInt(PLMSWarContent.KEY_FIELD_NO, mSelectingFieldMode.getNo());
			editor.putString(PLMSWarContent.KEY_LEFT_UNIT_NOS, getUnitNosString(leftArmy.getUnitDataArray()));
			editor.putString(PLMSWarContent.KEY_RIGHT_UNIT_NOS, getUnitNosString(rightArmy.getUnitDataArray()));
			editor.commit();
		} else {
			for (int i = 0; i < 2; i++) {
				PLMSArmyStrategy armyStrategy = armyArray.get(i);
				PLMSArmySetting armyView = mArmyViewArray.get(i);

				if (armyStrategy.getInterfaceNo() != armyView.getNextInterfaceNo()) {
					// インターフェースの切り替え
					PLMSWarInterface prevInterface = armyStrategy.getWarInterface();
					armyStrategy.setInterfaceNo(armyView.getNextInterfaceNo());
					PLMSWarInterface nextInterface = armyStrategy.makeInterface();
					if (mArgument.getTurnManager().getCurrentArmy() == armyStrategy) {
						prevInterface.disableInterface();
						nextInterface.enableInterface();
					}
				}
			}
		}
		PLCoreService.getNavigationController().popView();
	}

	private String getUnitNosString(MYArrayList<PLMSUnitData> unitDataArray) {
		StringBuilder str = new StringBuilder();
		for (PLMSUnitData unitData : unitDataArray) {
			str.append(unitData.getUnitModel().getNo()).append(",");
		}
		return str.toString();
	}

	@Override
	public void setNeedMakeNewWar() {
		mRestartSwitch.setChecked(true);
	}
}
