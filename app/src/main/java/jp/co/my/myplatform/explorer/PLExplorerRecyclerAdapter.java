package jp.co.my.myplatform.explorer;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import androidx.collection.LruCache;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.view.PLLoadImageView;

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
		final PLImageViewHolder viewHolder = new PLImageViewHolder(mInflater.inflate(R.layout.cell_explorer, viewGroup, false));
		viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mListener.onClickFile(v, viewHolder.mFile);
			}
		});
		viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mListener.onLongClickFile(v, viewHolder.mFile);
				return true;
			}
		});
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
		PLImageViewHolder customHolder = (PLImageViewHolder) viewHolder;
		File file = mFileList.get(position);
		updateHolderByFile(customHolder, file);
	}

	@Override
	public int getItemCount() {
		return mFileList.size();
	}

	public void updateHolderByFile(PLImageViewHolder holder, File file) {
		holder.mFile = file;
		String fileName = file.getName();
		if (MYStringUtil.isImageFileName(fileName)) {
			holder.mLoadImage.loadImageFile(file, mImageCache);
			holder.mTextView.setVisibility(View.GONE);
			return;
		}

		holder.mTextView.setVisibility(View.VISIBLE);
		holder.mTextView.setText(fileName);
		if (file.isDirectory()) {
			holder.mLoadImage.loadImageResource(R.drawable.directory);
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