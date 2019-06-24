package jp.co.my.myplatform.explorer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.content.PLContentView;
import jp.co.my.myplatform.popover.PLListPopover;

public class PLExplorerContent extends PLContentView implements PLExplorerRecyclerAdapter.PLOnClickFileListener
		, PLImagePopover.PLOnSetImageListener {

	private static final String KEY_LAST_PATH = "KEY_LAST_PATH";
	private static final String KEY_LAST_IMAGE = "KEY_LAST_IMAGE";

	private File mCurrentFile;
	private TextView mPathText;
	private RecyclerView mRecyclerView;
	private PLExplorerRecyclerAdapter mAdapter;
	private PLImagePopover mImagePopover;
	private Button mCloseButton;

	public PLExplorerContent() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_explorer, this);
		mPathText = findViewById(R.id.path_text);
		mRecyclerView = findViewById(R.id.recycler);
		mAdapter = new PLExplorerRecyclerAdapter(getContext(), this);
		initClickEvent();

		SharedPreferences pref = MYLogUtil.getPreference();
		String lastPath = pref.getString(KEY_LAST_PATH, null);
		if (lastPath != null) {
			File directory = new File(lastPath);
			loadDirectory(directory);
		} else {
			loadPicturesDirectory();
		}
		String imagePath = pref.getString(KEY_LAST_IMAGE, null);
		if (imagePath != null) {
			File imageFile = new File(imagePath);
			showImageFile(imageFile);
		}
	}

	@Override
	public void onClickFile(View view, File file) {
		if (file.isDirectory()) {
			loadDirectory(file);
			return;
		}

		if (MYStringUtil.isImageFileName(file.getName())) {
			showImageFile(file);
		} else {
			MYLogUtil.showToast("Sorry, No action");
		}
	}

	@Override
	public void onLongClickFile(final View view, final File file) {
		String[] titles = {"削除"};
		new PLListPopover(titles, new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View cellView, int position, long id) {
				if (file.delete()) {
					MYLogUtil.showToast(file.getName() +"を削除");
					view.setBackgroundColor(Color.GRAY);
				} else {
					MYLogUtil.showErrorToast(file.getName() +"の削除に失敗");
				}
				PLExplorerContent.this.removeTopPopover();
			}
		}).showPopover();
	}

	@Override
	public void onSetImage(File file) {
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		if (file == null) {
			editor.putString(KEY_LAST_IMAGE, null);
		} else {
			editor.putString(KEY_LAST_IMAGE, file.getPath());
		}
		editor.commit();
	}

	private void loadDirectory(File file) {
		String path = file.getPath();
		File[] allFiles = new File(path).listFiles();
		Arrays.sort(allFiles, new Comparator<File>() {
			@Override
			public int compare(File f1, File f2) {
				// 日付順
				return (int)(f1.lastModified() - f2.lastModified());
			}
		});
		List<File> fileList = Arrays.asList(allFiles);
		if (fileList.size() == 0) {
			MYLogUtil.showToast("ファイルなし");
			return;
		}

		mCurrentFile = file;
		mPathText.setText(path);

		mAdapter.setFileList(fileList);
		mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
		mRecyclerView.setAdapter(mAdapter);

		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putString(KEY_LAST_PATH, path);
		editor.commit();
	}

	private void initClickEvent() {
		MYLogUtil.outputLog("initClick");
		findViewById(R.id.parent_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.outputLog("onClick1");
				File parentFile = mCurrentFile.getParentFile();
				if (parentFile == null) {
					MYLogUtil.showErrorToast("parentFile is null");
					return;
				}
				loadDirectory(parentFile);
			}
		});
		findViewById(R.id.pictures_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MYLogUtil.outputLog("initClick2");
				loadPicturesDirectory();
			}
		});
		MYLogUtil.outputLog("initClick end");

		mCloseButton = addNavigationButton("閉じる", false, new OnClickListener() {
			@Override
			public void onClick(View v) {
				mImagePopover.removeFromContentView();
				mCloseButton.setEnabled(false);
			}
		});
	}

	private void loadPicturesDirectory() {
		loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
	}

	private void showImageFile(File file) {
		if (!file.exists()) {
			return;
		}
		mImagePopover = new PLImagePopover(mAdapter.getFileList(), file, mAdapter.getImageCache());
		mImagePopover.showPopover(this);
		mImagePopover.setListener(this);
		mCloseButton.setEnabled(true);
	}
}
