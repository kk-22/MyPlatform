package jp.co.my.myplatform.service.mysen.setting;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.model.PLModelContainer;
import jp.co.my.myplatform.service.mysen.PLMSFieldModel;
import jp.co.my.myplatform.service.mysen.PLMSFieldModel_Table;
import jp.co.my.myplatform.service.popover.PLPopoverView;


public class PLMSFieldListPopover extends PLPopoverView {

	private RecyclerView mRecyclerView;

	private PLMSFieldRecyclerAdapter.PLMSOnClickFieldListener mListener;

	public PLMSFieldListPopover(PLMSFieldRecyclerAdapter.PLMSOnClickFieldListener listener) {
		super(R.layout.mysen_field_list_popover);
		mListener = listener;

		mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

		PLModelContainer<PLMSFieldModel> container = new PLModelContainer<>(SQLite.select()
				.from(PLMSFieldModel.class)
				.orderBy(PLMSFieldModel_Table.no, false));
		container.loadList(null, new PLModelContainer.PLOnModelLoadMainListener<PLMSFieldModel>() {
			@Override
			public void onLoad(List<PLMSFieldModel> modelLists) {
				if (modelLists.size() == 0) {
					MYLogUtil.showErrorToast("field model array is null");
					return;
				}
				PLMSFieldRecyclerAdapter adapter = new PLMSFieldRecyclerAdapter(getContext(), mListener);
				adapter.setFieldModelList(modelLists);
				mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 5));
				mRecyclerView.setAdapter(adapter);
			}
		});
	}
}
