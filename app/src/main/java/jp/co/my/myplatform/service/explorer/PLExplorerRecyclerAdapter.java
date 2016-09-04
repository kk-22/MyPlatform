package jp.co.my.myplatform.service.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;

public class PLExplorerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private Handler mHandler;

	private LayoutInflater mInflater;
	private List<File> mFileList;
	private PLOnClickFileListener mListener;

	public PLExplorerRecyclerAdapter(Context context,  PLOnClickFileListener listener) {
		mInflater = LayoutInflater.from(context);
		mContext = context;
		mListener = listener;
		mHandler = new Handler();
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		return new PLImageViewHolder(mInflater.inflate(R.layout.cell_explorer, viewGroup, false));
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		PLImageViewHolder customHolder = (PLImageViewHolder) viewHolder;
		final File file = mFileList.get(position);
		updateHolderByFile(customHolder, file);
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onClickFile(v, file);
			}
		});
		viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mListener.onLongClickFile(v, file);
				return true;
			}
		});
	}

	@Override
	public int getItemCount() {
		return mFileList.size();
	}

	public void updateHolderByFile(PLImageViewHolder holder, final File file) {
		holder.mFile = file;
		String fileName = file.getName();
		String extension = MYStringUtil.getSuffix(fileName);
		if (extension != null && (extension.equals("png") || extension.equals("jpg"))) {
			loadImageOnBackground(holder, file);
			holder.mTextView.setVisibility(View.GONE);
			return;
		}

		holder.mTextView.setVisibility(View.VISIBLE);
		if (file.isDirectory()) {
			holder.mImageView.setImageResource(R.drawable.directory);
			holder.mTextView.setText(fileName);
			return;
		} else {
			holder.mImageView.setImageResource(R.drawable.file);
		}
	}

	private void loadImageOnBackground(final PLImageViewHolder holder, final File file) {
		holder.mImageView.setImageResource(R.drawable.file);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					FileInputStream stream = new FileInputStream(file);
					Bitmap bitmap = BitmapFactory.decodeStream(stream);
					setImageOnMainThread(holder, file, bitmap);
				} catch (Exception e) {
					MYLogUtil.showExceptionToast(e);
				}
			}
		}).start();
	}

	private void setImageOnMainThread(final PLImageViewHolder holder, final File file, final Bitmap bitmap) {
		if (!holder.mFile.equals(file)) {
			return;
		}
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				if (holder.mFile.equals(file)) {
					holder.mImageView.setImageBitmap(bitmap);
				}
			}
		});
	}

	public void setFileList(List<File> fileList) {
		mFileList = fileList;
	}

	public interface PLOnClickFileListener {
		void onClickFile(View view, File file);
		void onLongClickFile(View view, File file);
	}

	private static class PLImageViewHolder extends RecyclerView.ViewHolder {
		 File mFile;
		ImageView mImageView;
		TextView mTextView;

		public PLImageViewHolder(View itemView) {
			super(itemView);
			mImageView = (ImageView) itemView.findViewById(R.id.icon_image);
			mTextView = (TextView) itemView.findViewById(R.id.image_title);
		}
	}
}