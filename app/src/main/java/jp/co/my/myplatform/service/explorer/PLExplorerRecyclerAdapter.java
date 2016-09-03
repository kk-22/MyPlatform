package jp.co.my.myplatform.service.explorer;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;

public class PLExplorerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private LayoutInflater mInflater;
	private List<File> mFiles;
	private PLOnClickFileListener mListener;

	public PLExplorerRecyclerAdapter(Context context, List<File> data, PLOnClickFileListener listener) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mFiles = data;
		mListener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		return new PLImageViewHolder(mInflater.inflate(R.layout.cell_explorer, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		PLImageViewHolder customHolder = (PLImageViewHolder) viewHolder;
		final File file = mFiles.get(position);
		customHolder.setFile(file);
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onClickFile(file);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mFiles.size();
	}

	private static class PLImageViewHolder extends RecyclerView.ViewHolder {

		private ImageView mImageView;
		private TextView mTextView;

		public PLImageViewHolder(View itemView) {
			super(itemView);
			mImageView = (ImageView) itemView.findViewById(R.id.icon_image);
			mTextView = (TextView) itemView.findViewById(R.id.image_title);
		}

		public void setFile(File file) {
			String fileName = file.getName();
			String extension = MYStringUtil.getSuffix(fileName);
			if (extension != null && (extension.equals("png") || extension.equals("jpg"))) {
				Uri uri = Uri.fromFile(file);
				mImageView.setImageURI(uri);
				mTextView.setVisibility(View.GONE);
				return;
			}

			mTextView.setVisibility(View.VISIBLE);
			if (file.isDirectory()) {
				mImageView.setImageResource(R.drawable.directory);
				mTextView.setText(fileName);
				return;
			} else {
				mImageView.setImageResource(R.drawable.file);
			}
		}
	}

	public interface PLOnClickFileListener {
		void onClickFile(File file);
	}
}