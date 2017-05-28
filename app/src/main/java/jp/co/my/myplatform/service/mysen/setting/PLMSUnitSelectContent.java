package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;
import java.util.Random;

import jp.co.my.common.util.MYArrayList;
import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.core.PLCoreService;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.mysen.PLMSArgument;
import jp.co.my.myplatform.service.mysen.PLMSUnitData;
import jp.co.my.myplatform.service.mysen.PLMSUnitModel;
import jp.co.my.myplatform.service.mysen.PLMSUnitModel_Table;
import jp.co.my.myplatform.service.mysen.army.PLMSArmyStrategy;

import static jp.co.my.myplatform.service.mysen.setting.PLMSUnitListView.MAX_NUMBER_UNIT;


public class PLMSUnitSelectContent extends PLContentView {

	private PLMSArgument mArgument;
	private PLMSArmyStrategy mArmyStrategy;
	private PLMSUnitListView mUnitListView;
	private RecyclerView mUnitRecyclerView;

	private MYArrayList<PLMSUnitData> mSelectingUnitArray;
	private List<PLMSUnitModel> mAllUnitModels;
	private PLMSOnSelectUnitListener mListener;

	public PLMSUnitSelectContent(PLMSArgument argument, PLMSArmyStrategy armyStrategy,
								 MYArrayList<PLMSUnitData> selectingUnitArray, PLMSOnSelectUnitListener listener) {
		super();
		mArgument = argument;
		mArmyStrategy = armyStrategy;
		mSelectingUnitArray = new MYArrayList<>(selectingUnitArray);
		mListener = listener;

		LayoutInflater.from(getContext()).inflate(R.layout.mysen_content_select_unit, this);
		mUnitListView = (PLMSUnitListView) findViewById(R.id.unit_list);
		mUnitRecyclerView = (RecyclerView) findViewById(R.id.recycler);
		findViewById(R.id.decision_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onSelectUnit(mSelectingUnitArray);
				PLCoreService.getNavigationController().popView();
			}
		});
		findViewById(R.id.random_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int selectedCount = mSelectingUnitArray.size();
				int allUnitCount = mAllUnitModels.size();
				if (selectedCount == MAX_NUMBER_UNIT || selectedCount == allUnitCount) {
					MYLogUtil.showToast("これ以上選択不可");
					return;
				}
				Random random = new Random();
				while (true) {
					int index = random.nextInt(allUnitCount);
					PLMSUnitModel unitModel = mAllUnitModels.get(index);
					if (getSelectingUnitDataOfModel(unitModel) == null) {
						addSelectingUnitModel(unitModel);
						return;
					}
				}
			}
		});

		mUnitListView.loadUnitList(selectingUnitArray);

		PLModelContainer<PLMSUnitModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLMSUnitModel.class)
				.orderBy(PLMSUnitModel_Table.no, false));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLMSUnitModel>() {
			@Override
			public void onLoad(List<PLMSUnitModel> modelLists) {
				mAllUnitModels = modelLists;
				PLMSUnitRecyclerAdapter adapter = new PLMSUnitRecyclerAdapter(getContext());
				mUnitRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
				mUnitRecyclerView.setAdapter(adapter);
			}
		});
	}

	private void addSelectingUnitModel(PLMSUnitModel unitModel) {
		PLMSUnitData unitData = new PLMSUnitData(unitModel, mArmyStrategy, mArgument);
		mSelectingUnitArray.add(unitData);
		reloadList();
	}

	private void reloadList() {
		mUnitListView.loadUnitList(mSelectingUnitArray);
		mUnitRecyclerView.getAdapter().notifyDataSetChanged();
	}

	private void toggleUnitMode(PLMSUnitModel unitModel) {
		PLMSUnitData unitData = getSelectingUnitDataOfModel(unitModel);
		if (unitData != null) {
			mSelectingUnitArray.remove(unitData);
			reloadList();
		} else if (mSelectingUnitArray.size() < MAX_NUMBER_UNIT) {
			addSelectingUnitModel(unitModel);
		} else {
			MYLogUtil.showToast("人数が限界");
		}
	}

	private PLMSUnitData getSelectingUnitDataOfModel(PLMSUnitModel unitModel) {
		for (PLMSUnitData unitData : mSelectingUnitArray) {
			if (unitData.getUnitModel().equals(unitModel)) {
				return unitData;
			}
		}
		return null;
	}

	interface PLMSOnSelectUnitListener {
		void onSelectUnit(MYArrayList<PLMSUnitData> selectingUnitArray);
	}

	private class PLMSUnitRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
		private LayoutInflater mInflater;

		public PLMSUnitRecyclerAdapter(Context context) {
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
			final PLMSUnitHolder viewHolder = new PLMSUnitHolder(mInflater.inflate(R.layout.mysen_cell_unit, viewGroup, false));
			viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					toggleUnitMode(viewHolder.mUnitModel);
				}
			});
			return viewHolder;
		}

		@Override
		public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
			final PLMSUnitHolder holder = (PLMSUnitHolder) viewHolder;
			final PLMSUnitModel unitModel = mAllUnitModels.get(position);
			holder.mUnitModel = unitModel;
			holder.mImageView.setImageBitmap(unitModel.getImage(getContext()));
			holder.mTextView.setText(unitModel.getName());
			if (getSelectingUnitDataOfModel(unitModel) != null) {
				holder.mImageView.setBackgroundColor(Color.parseColor("#9DCCE0"));
			} else {
				holder.mImageView.setBackgroundColor(Color.parseColor("#ffffff"));
			}
		}

		@Override
		public int getItemCount() {
			return mAllUnitModels.size();
		}

		private class PLMSUnitHolder extends RecyclerView.ViewHolder {
			PLMSUnitModel mUnitModel;
			ImageView mImageView;
			TextView mTextView;

			PLMSUnitHolder(View itemView) {
				super(itemView);
				mImageView = (ImageView) itemView.findViewById(R.id.unit_image);
				mTextView = (TextView) itemView.findViewById(R.id.unit_name_text);
				mTextView.setTextSize(10);
			}
		}
	}
}
