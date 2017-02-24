package jp.co.my.myplatform.service.mysen;

import android.graphics.Point;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.ArrayList;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.mysen.Army.PLMSArmyStrategy;
import jp.co.my.myplatform.service.mysen.Army.PLMSBlueArmy;
import jp.co.my.myplatform.service.mysen.Army.PLMSRedArmy;


public class PLMSBattleView extends PLContentView {

	private PLMSInformationView mInformation;
	private PLMSFieldView mField;
	private PLMSUserInterface mUserInterface;

	private ArrayList<PLMSUnitData> mUnitDataArray;
	private boolean mFinishedLayout;			// OnGlobalLayoutListener が呼ばれたら true

	public PLMSBattleView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_battle, this);
		mInformation = (PLMSInformationView) findViewById(R.id.information_view);
		mField = (PLMSFieldView) findViewById(R.id.field_view);

		getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				getViewTreeObserver().removeOnGlobalLayoutListener(this);
				mFinishedLayout = true;
				startChildLayoutIfNeeded();
			}
		});

		loadUnitModels();
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
				for (PLMSUnitModel unitModel : modelLists) {
					Point point = new Point(x, y);
					PLMSArmyStrategy armyStrategy = (x %2 == 0) ? blueArmy : redArmy;
					PLMSUnitData unitData = new PLMSUnitData(unitModel, point, armyStrategy);
					x += 1;
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
		mField.layoutChildViews(mUnitDataArray);
		mUserInterface = new PLMSUserInterface(mInformation, mField, mField.getUnitViewArray());
	}
}
