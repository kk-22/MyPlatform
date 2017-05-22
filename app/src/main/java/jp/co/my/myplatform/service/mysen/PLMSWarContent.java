package jp.co.my.myplatform.service.mysen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.army.PLMSBlueArmy;
import jp.co.my.myplatform.service.mysen.army.PLMSRedArmy;
import jp.co.my.myplatform.service.mysen.setting.PLMSWarSettingContent;
import jp.co.my.myplatform.service.popover.PLConfirmationPopover;
import jp.co.my.myplatform.service.popover.PLPopoverView;


public class PLMSWarContent extends PLContentView {

	private PLMSArgument mArgument;
	private int mLoadCount;

	public PLMSWarContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war, this);
		mArgument = new PLMSArgument();
		mArgument.setInformationView((PLMSInformationView) findViewById(R.id.information_view));
		mArgument.setFieldView((PLMSFieldView) findViewById(R.id.field_view));
		mArgument.setAnimationManager(new PLMSAnimationManager(mArgument));

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

	private void initNaviBar() {
		LinearLayout naviBar = new LinearLayout(getContext());
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_war_navibar, naviBar);
		setNavigationBar(naviBar);

		naviBar.findViewById(R.id.back_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().popView();
			}
		});
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
				for (PLPopoverView popoverView : getPopoverViews()) {
					if (popoverView instanceof PLConfirmationPopover) {
						// 表示中
						return;
					}
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
				PLMSWarSettingContent settingContent = new PLMSWarSettingContent(mArgument);
				PLCoreService.getNavigationController().pushView(settingContent);
			}
		});
	}

	private void loadUnitModels() {
		PLModelContainer<PLMSUnitModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLMSUnitModel.class)
				.orderBy(PLMSUnitModel_Table.no, false));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLMSUnitModel>() {
			@Override
			public void onLoad(List<PLMSUnitModel> modelLists) {
				if (modelLists.size() == 0) {
					MYLogUtil.showErrorToast("unit model array is null");
					return;
				}

				// TODO: delete dummy code
				PLMSBlueArmy blueArmy = new PLMSBlueArmy(mArgument, "青軍", PLMSArmyStrategy.INTERFACE_USER);
				PLMSRedArmy redArmy = new PLMSRedArmy(mArgument, "赤軍", PLMSArmyStrategy.INTERFACE_COMPUTER);
				blueArmy.setEnemyArmy(redArmy);
				redArmy.setEnemyArmy(blueArmy);
				mArgument.setArmyArray(new MYArrayList<>(blueArmy, redArmy));
				int i = 0;
				for (PLMSUnitModel unitModel : modelLists) {
					PLMSArmyStrategy armyStrategy = (i / 4 == 0) ? blueArmy : redArmy;
					PLMSUnitData unitData = new PLMSUnitData(unitModel, armyStrategy, mArgument);
					armyStrategy.getUnitDataArray().add(unitData);
					i++;
				}
				startChildLayoutIfNeeded();
			}
		});
	}

	private void startChildLayoutIfNeeded() {
		mLoadCount++;
		if (mLoadCount < 2) {
			return;
		}
		PLMSFieldModel fieldModel = SQLite.select().from(PLMSFieldModel.class).querySingle();
		mArgument.getFieldView().initForWar(mArgument, fieldModel);
		mArgument.setAllUnitViewArray(mArgument.getFieldView().getUnitViewArray());
		mArgument.setTurnManager(new PLMSTurnManager(mArgument));
	}
}
