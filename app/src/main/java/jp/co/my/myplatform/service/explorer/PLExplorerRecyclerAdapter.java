package jp.co.my.myplatform.service.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.util.LruCache;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.view.PLLoadImageView;

public class PLExplorerRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private Context mContext;
	private Handler mHandler;

	private LayoutInflater mInflater;
	private List<File> mFileList;
	private LruCache<String, Bitmap> mImageCache;
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
		if (MYStringUtil.isImageFileName(fileName)) {
			holder.mLoadImage.loadImageFile(file, mImageCache);
			holder.mTextView.setVisibility(View.GONE);
			return;
		}

		holder.mTextView.setVisibility(View.VISIBLE);
		if (file.isDirectory()) {
			holder.mLoadImage.loadImageResource(R.drawable.directory);
			holder.mTextView.setText(fileName);
			return;
		} else {
			holder.mLoadImage.loadImageResource(R.drawable.file);
		}
	}

	public interface PLOnClickFileListener {
		void onClickFile(View view, File file);
		void onLongClickFile(View view, File file);
	}

	private static class PLImageViewHolder extends RecyclerView.ViewHolder {
		File mFile;
		PLLoadImageView mLoadImage;
		TextView mTextView;

		public PLImageViewHolder(View itemView) {
			super(itemView);
			mLoadImage = (PLLoadImageView) itemView.findViewById(R.id.load_image_view);
			mTextView = (TextView) itemView.findViewById(R.id.image_title);
		}
	}

	public void setFileList(List<File> fileList) {
		mFileList = fileList;
		mImageCache = new LruCache<>(1024 * 1024 * 10);
	}

	public List<File> getFileList() {
		return mFileList;
	}

	public LruCache<String, Bitmap> getImageCache() {
		return mImageCache;
	}
}