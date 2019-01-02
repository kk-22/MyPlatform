package jp.co.my.myplatform.simulator;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.core.PLCoreService;
import jp.co.my.myplatform.database.PLModelContainer;
import jp.co.my.myplatform.popover.PLListPopover;

public class PLDamageSimulateContent extends PLContentView implements AdapterView.OnItemClickListener {

	private ListView mListView;

	private PLResultAdapter mAdapter;

	public PLDamageSimulateContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_damage_simulate, this);
		mListView = findViewById(R.id.list_view);
		mAdapter = new PLResultAdapter(getContext());

		initViewEvent();
	}

	@Override
	public void viewWillComeBack(PLContentView from) {
		super.viewWillComeBack(from);

		mAdapter.notifyDataSetChanged();
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
				// 自キャラ選択
				loadUnitList(true, new OnUnitSelectListener() {
					@Override
					public void onUnitSelected(final PLUnitModel mineUnit) {
						// 敵キャラ選択
						// TODO: isMine はfalseに
						loadUnitList(true, new OnUnitSelectListener() {
							@Override
							public void onUnitSelected(PLUnitModel enemyUnit) {
								PLCombatResultContent content = PLCoreService.getNavigationController().pushView(PLCombatResultContent.class);
								content.setUnits(mineUnit, enemyUnit);
								mAdapter.add(content);
								mListView.setAdapter(mAdapter);
							}
						});
					}
				});
			}
		});
		mListView.setOnItemClickListener(this);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		PLCombatResultContent content = mAdapter.getItem(position);
		PLCoreService.getNavigationController().pushView(content);
	}

	private interface OnUnitSelectListener {
		void onUnitSelected(PLUnitModel unitModel);
	}

	private class PLResultAdapter extends ArrayAdapter<PLCombatResultContent> {
		private PLResultAdapter(@NonNull Context context) {
			super(context, 0);
		}

		@Override
		public @NonNull View getView(int position, View convertView,
									 @NonNull ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getContext());
				convertView = inflater.inflate(R.layout.cell_simple_result, parent, false);
			}
			TextView mineNameText = convertView.findViewById(R.id.mine_name_text);
			TextView mineHpText = convertView.findViewById(R.id.mine_hp_text);
			TextView enemyHpText = convertView.findViewById(R.id.enemy_hp_text);
			TextView enemyNameText = convertView.findViewById(R.id.enemy_name_text);
			TextView memoText = convertView.findViewById(R.id.memo_text);

			PLCombatResultContent result = getItem(position);
			if (result != null) {
				mineNameText.setText(result.getMineUnit().getName());
				mineHpText.setText(result.getMineHpString());
				mineHpText.setTextColor(result.getMineRemainingHpText().getCurrentTextColor());
				enemyHpText.setText(result.getEnemyHpString());
				enemyHpText.setTextColor(result.getEnemyRemainingHpText().getCurrentTextColor());
				enemyNameText.setText(result.getEnemyUnit().getName());
				String memo = result.getMemoString();
				if (memo.length() == 0) {
					memoText.setVisibility(View.GONE);
				} else {
					memoText.setVisibility(View.GONE);
					memoText.setText(memo);
				}
			}
			return convertView;
		}
	}

}
