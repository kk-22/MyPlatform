package jp.co.my.myplatform.service.mysen.setting;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import java.util.List;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.mysen.PLMSFieldModel;
import jp.co.my.myplatform.service.mysen.PLMSFieldView;

public class PLMSFieldRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
	private LayoutInflater mInflater;
	private List<PLMSFieldModel> mFieldModelList;
	private PLMSOnClickFieldListener mListener;

	public PLMSFieldRecyclerAdapter(Context context,  PLMSOnClickFieldListener listener) {
		mInflater = LayoutInflater.from(context);
		mListener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		final PLMSFieldHolder viewHolder = new PLMSFieldHolder(mInflater.inflate(R.layout.mysen_cell_field, viewGroup, false));
		viewHolder.mFieldView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onClickField(v, viewHolder.mFieldModel);
			}
		});
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		final PLMSFieldHolder holder = (PLMSFieldHolder) viewHolder;
		final PLMSFieldModel fieldModel = mFieldModelList.get(position);
		holder.mFieldModel = fieldModel;
		holder.mTextView.setText(fieldModel.getName());
		holder.itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				if (fieldModel.equals(holder.mFieldModel)) {
					holder.mFieldView.initForPreview(fieldModel);
					holder.itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
				}
			}
		});
	}

	@Override
	public int getItemCount() {
		return mFieldModelList.size();
	}

	interface PLMSOnClickFieldListener {
		void onClickField(View view, PLMSFieldModel fieldModel);
	}

	private static class PLMSFieldHolder extends RecyclerView.ViewHolder {
		PLMSFieldModel mFieldModel;
		PLMSFieldView mFieldView;
		TextView mTextView;

		PLMSFieldHolder(View itemView) {
			super(itemView);
			mFieldView = (PLMSFieldView) itemView.findViewById(R.id.preview_field);
			mTextView = (TextView) itemView.findViewById(R.id.field_name_text);
		}
	}

	public void setFieldModelList(List<PLMSFieldModel> fieldModelList) {
		mFieldModelList = fieldModelList;
	}
}
