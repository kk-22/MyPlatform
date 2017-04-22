package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
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

	private ArrayList<PLMSUnitData> mUnitDataArray;
	private boolean mFinishedLayout;			// OnGlobalLayoutListener が呼ばれたら true
	private PLMSArgument mArgument;

	public PLMSWarContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_war, this);
		mArgument = new PLMSArgument();
		mArgument.setInformationView((PLMSInformationView) findViewById(R.id.information_view));
		mArgument.setFieldView((PLMSFieldView) findViewById(R.id.field_view));
		mArgument.setAnimationManager(new PLMSAnimationManager(mArgument));

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				mFinishedLayout = true;
				startChildLayoutIfNeeded();
			}
		});

		initNaviBar();
		loadUnitModels();
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
				int x = 0, y = 0;
				mUnitDataArray = new ArrayList<>();
				PLMSBlueArmy blueArmy = new PLMSBlueArmy();
				PLMSRedArmy redArmy = new PLMSRedArmy();
				blueArmy.setEnemyArmy(redArmy);
				redArmy.setEnemyArmy(blueArmy);
				mArgument.setArmyArray(new MYArrayList<>(blueArmy, redArmy));
				for (PLMSUnitModel unitModel : modelLists) {
					Point point = new Point((x % 6), y);
					PLMSArmyStrategy armyStrategy = (x %2 == 0) ? blueArmy : redArmy;
					PLMSUnitData unitData = new PLMSUnitData(unitModel, point, armyStrategy, mArgument);
					x = x + 1;
					y = x / 2;
					mUnitDataArray.add(unitData);
				}
				startChildLayoutIfNeeded();
			}
		});
	}

	private void startChildLayoutIfNeeded() {
		if (mUnitDataArray == null || !mFinishedLayout) {
			return;
		}
		mArgument.getFieldView().layoutChildViews(mUnitDataArray);
		mArgument.setAllUnitViewArray(mArgument.getFieldView().getUnitViewArray());
		mArgument.setTurnManager(new PLMSTurnManager(mArgument));
	}
}
