package jp.co.my.myplatform.mysen;

import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.raizlabs.android.dbflow.sql.language.Condition;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.HashMap;
import java.util.List;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.database.PLModelContainer;
import jp.co.my.myplatform.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.mysen.army.PLMSBlueArmy;
import jp.co.my.myplatform.mysen.army.PLMSRedArmy;
import jp.co.my.myplatform.mysen.setting.PLMSWarSettingContent;
import jp.co.my.myplatform.popover.PLConfirmationPopover;


public class PLMSWarContent extends PLContentView {

	public static final String KEY_FIELD_NO = "KEY_FIELD_NO";
	public static final String KEY_LEFT_UNIT_NOS = "KEY_LEFT_UNIT_NOS";
	public static final String KEY_RIGHT_UNIT_NOS = "KEY_RIGHT_UNIT_NOS";

	private PLMSArgument mArgument;
	private int mLoadCount;

	public PLMSWarContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war, this);
		mArgument = new PLMSArgument();
		initArgument(new PLMSArgument());

		mLoadCount = 0;

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				startChildLayoutIfNeeded();
			}
		});

		initNaviBar();
		loadUnitModels();
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		// 戦闘を再開
		if (mArgument != null && mArgument.getTurnManager() != null) {
			mArgument.getTurnManager().getCurrentArmy().getWarInterface().resumeInterface();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();

		// 戦闘を停止
		if (mArgument != null && mArgument.getTurnManager() != null) {
			mArgument.getTurnManager().getCurrentArmy().getWarInterface().suspendInterface();
		}
	}

	public void initArgument(PLMSArgument argument) {
		mArgument = argument;
		argument.setInformationView((PLMSInformationView) findViewById(R.id.information_view));
		argument.setFieldView((PLMSFieldView) findViewById(R.id.field_view));
		argument.setAnimationManager(new PLMSAnimationManager(argument));
	}

	private void initNaviBar() {
		LinearLayout naviBar = new LinearLayout(getContext());
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_war_navibar, naviBar);
		setNavigationBar(naviBar);

		naviBar.findViewById(R.id.danger_area_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mArgument.getTurnManager() == null) {
					return;
				}
				mArgument.getTurnManager().getCurrentArmy().getWarInterface().toggleAllDangerArea();
			}
		});
		naviBar.findViewById(R.id.turn_end_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mArgument.getTurnManager() == null) {
					return;
				}
				new PLConfirmationPopover("ターンを終了しますか？", new PLConfirmationPopover.PLConfirmationListener() {
					@Override
					public void onClickButton(boolean isYes) {
						mArgument.getTurnManager().finishTurn();
					}
				}, null);
			}
		});
		naviBar.findViewById(R.id.setting_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLMSWarSettingContent settingContent = new PLMSWarSettingContent(mArgument, PLMSWarContent.this);
				PLCoreService.getNavigationController().pushView(settingContent);
			}
		});
	}

	private void loadUnitModels() {
		SharedPreferences preferences = MYLogUtil.getPreference();
		final String[] leftNos = preferences.getString(KEY_LEFT_UNIT_NOS, "").split(",");
		final String[] rightNos = preferences.getString(KEY_RIGHT_UNIT_NOS, "").split(",");

		// in に配列ごと渡すと検索できない
		Condition.In conditionIn = Condition.column(PLMSUnitModel_Table.no.getNameAlias()).in(leftNos[0]);
		for (int i = 1; i < leftNos.length; i++) {
			conditionIn.and(leftNos[i]);
		}
		for (String rightNo : rightNos) {
			conditionIn.and(rightNo);
		}
		PLModelContainer<PLMSUnitModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLMSUnitModel.class)
				.where(conditionIn));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLMSUnitModel>() {
			@Override
			public void onLoad(List<PLMSUnitModel> modelLists) {
				if (modelLists.size() == 0) {
					MYLogUtil.showErrorToast("unit model array is null");
					return;
				}
				HashMap<String, PLMSUnitModel> unitHashMap = new HashMap<>();
				for (PLMSUnitModel unitModel : modelLists) {
					unitHashMap.put(String.valueOf(unitModel.getNo()), unitModel);
				}

				PLMSBlueArmy blueArmy = new PLMSBlueArmy(mArgument, "青軍", PLMSArmyStrategy.INTERFACE_USER);
				PLMSRedArmy redArmy = new PLMSRedArmy(mArgument, "赤軍", PLMSArmyStrategy.INTERFACE_COMPUTER);
				blueArmy.setEnemyArmy(redArmy);
				redArmy.setEnemyArmy(blueArmy);
				mArgument.setArmyArray(new MYArrayList<>(blueArmy, redArmy));

				for (String str : leftNos) {
					addUnitToArmy(blueArmy, unitHashMap.get(str));
				}
				for (String str : rightNos) {
					addUnitToArmy(redArmy, unitHashMap.get(str));
				}
				startChildLayoutIfNeeded();
			}
		});
	}

	private void addUnitToArmy(PLMSArmyStrategy armyStrategy, PLMSUnitModel unitModel) {
		if (unitModel == null) {
			MYLogUtil.showErrorToast("該当Noのユニットなし");
			return;
		}
		armyStrategy.getUnitDataArray().add(new PLMSUnitData(unitModel, armyStrategy, mArgument));
	}

	private void startChildLayoutIfNeeded() {
		mLoadCount++;
		if (mLoadCount < 2) {
			return;
		}

		SharedPreferences preferences = MYLogUtil.getPreference();
		int fieldNo = preferences.getInt(KEY_FIELD_NO, 1);
		PLMSFieldModel fieldModel = SQLite.select().from(PLMSFieldModel.class)
				.where(PLMSFieldModel_Table.no.eq(fieldNo)).querySingle();
		if (fieldModel != null) {
			mArgument.getFieldView().initForWar(mArgument, fieldModel);
			mArgument.setAllUnitViewArray(mArgument.getFieldView().getUnitViewArray());
			mArgument.setTurnManager(new PLMSTurnManager(mArgument));
		}
	}
}
