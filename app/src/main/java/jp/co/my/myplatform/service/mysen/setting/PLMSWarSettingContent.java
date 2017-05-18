package jp.co.my.myplatform.service.mysen.setting;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSFieldModel;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.userinterface.PLMSWarInterface;


public class PLMSWarSettingContent extends PLContentView {

	private PLMSFieldView mPreviewFieldView;
	private TextView mFieldNameText;
	private PLMSArmySetting mLeftArmyView;
	private PLMSArmySetting mRightArmyView;

	private PLMSArgument mArgument;
	private MYArrayList<PLMSArmySetting> mArmyViewArray;

	public PLMSWarSettingContent(PLMSArgument argument) {
		super();
		mArgument = argument;

		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war_setting, this);
		mPreviewFieldView = (PLMSFieldView) findViewById(R.id.preview_field);
		mFieldNameText = (TextView) findViewById(R.id.field_name_text);
		mLeftArmyView = (PLMSArmySetting) findViewById(R.id.left_army);
		mRightArmyView = (PLMSArmySetting) findViewById(R.id.right_army);
		mArmyViewArray = new MYArrayList<>(mLeftArmyView, mRightArmyView);

		MYArrayList<PLMSArmyStrategy> armyArray = mArgument.getArmyArray();
		if (armyArray == null) {
			return;
		}
		for (int i = 0; i < 2; i++) {
			PLMSArmySetting armyView = mArmyViewArray.get(i);
			PLMSArmyStrategy armyStrategy = armyArray.get(i);
			armyView.setArmyStrategy(armyStrategy);
		}

		initEvent();

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);

				PLMSFieldModel fieldModel = SQLite.select().from(PLMSFieldModel.class).querySingle();
				loadFieldView(fieldModel);
			}
		});
	}

	private void initEvent() {
		findViewById(R.id.execute_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				execute();
			}
		});
		mPreviewFieldView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.outputLog("TODO");
			}
		});
	}

	private void loadFieldView(PLMSFieldModel fieldModel) {
		if (fieldModel == null) return;

		mPreviewFieldView.initForPreview(fieldModel);
		mFieldNameText.setText(fieldModel.getName());
	}

	private void execute() {
		MYArrayList<PLMSArmyStrategy> armyArray = mArgument.getArmyArray();
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
		PLCoreService.getNavigationController().popView();
	}
}
