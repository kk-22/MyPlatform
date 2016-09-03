package jp.co.my.myplatform.service.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.R;

public class PLImageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private LayoutInflater mInflater;
	private ArrayList<File> mFiles;

	public PLImageRecyclerAdapter(Context context, ArrayList<File> data) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mFiles = data;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		return new PLImageViewHolder(mInflater.inflate(R.layout.cell_image_item, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		PLImageViewHolder customHolder = (PLImageViewHolder) viewHolder;
		customHolder.setFile(mFiles.get(position));
	}

	@Override
	public int getItemCount() {
		return mFiles.size();
	}

	private static class PLImageViewHolder extends RecyclerView.ViewHolder {

		private ImageButton mImageButton;
		private TextView mTextView;

		public PLImageViewHolder(View itemView) {
			super(itemView);
			mImageButton = (ImageButton) itemView.findViewById(R.id.image_button);
			mTextView = (TextView) itemView.findViewById(R.id.image_title);
		}

		public void setFile(File file) {
			mTextView.setText(file.getName());

			Uri uri = Uri.fromFile(file);
			mImageButton.setImageURI(uri);
			mImageButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					MYLogUtil.showToast("ok yes");
				}
			});
		}
	}
}