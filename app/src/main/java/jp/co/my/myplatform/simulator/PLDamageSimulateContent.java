package jp.co.my.myplatform.simulator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.database.PLModelContainer;
import jp.co.my.myplatform.popover.PLListPopover;

public class PLDamageSimulateContent extends PLContentView {

	public PLDamageSimulateContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_damage_simulate, this);

		initViewEvent();
	}

	private void initViewEvent() {
		addNavigationButton("作成", new OnClickListener() {
			@Override
			public void onClick(View v) {
				PLCoreService.getNavigationController().pushView(PLUnitEditContent.class);
			}
		});
		addNavigationButton("編集", new OnClickListener() {
			@Override
			public void onClick(View v) {
				loadUnitList(true, new OnUnitSelectListener() {
					@Override
					public void onUnitSelected(PLUnitModel unitModel) {
						PLUnitEditContent content = PLCoreService.getNavigationController().pushView(PLUnitEditContent.class);
						content.editUnit(unitModel);
					}
				});
			}
		});
		addNavigationButton("戦闘", new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
	}

	private void loadUnitList(boolean isMine, final OnUnitSelectListener listener) {
		PLModelContainer<PLUnitModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLUnitModel.class)
				.where(PLUnitModel_Table.isMine.eq(isMine)));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLUnitModel>() {
			@Override
			public void onLoad(List<PLUnitModel> list) {
				showUnitList(list, listener);
			}
		});
	}

	private void showUnitList(final List<PLUnitModel> list, final OnUnitSelectListener listener) {
		int numberOfUnit = list.size();
		if (numberOfUnit == 0) {
			MYLogUtil.showErrorToast("該当ユニットなし");
			return;
		}

		String[] titles = new String[numberOfUnit];
		for (int i = 0; i < numberOfUnit; i++) {
			titles[i] = list.get(i).getName();
		}
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				listener.onUnitSelected(list.get(position));
			}
		}).showPopover();
	}

	private interface OnUnitSelectListener {
		void onUnitSelected(PLUnitModel unitModel);
	}
}
