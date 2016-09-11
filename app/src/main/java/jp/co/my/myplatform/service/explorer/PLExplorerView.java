package jp.co.my.myplatform.service.explorer;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.content.PLContentView;
import jp.co.my.myplatform.service.popover.PLListPopover;

public class PLExplorerView extends PLContentView implements PLExplorerRecyclerAdapter.PLOnClickFileListener
		, PLImagePopover.PLOnSetImageListener {

	private static final String KEY_LAST_PATH = "KEY_LAST_PATH";
	private static final String KEY_LAST_IMAGE = "KEY_LAST_IMAGE";

	private File mCurrentFile;
	private TextView mPathText;
	private RecyclerView mRecyclerView;
	private PLExplorerRecyclerAdapter mAdapter;

	public PLExplorerView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_explorer, this);
		mPathText = (TextView) findViewById(R.id.path_text);
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler);
		mAdapter = new PLExplorerRecyclerAdapter(getContext(), this);

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
			// Popoverの追加先がContentViewではなくNavigationなので、コンストラク時にはaddできない
//			File imageFile = new File(lastPath);
//			showImageFile(imageFile);
		}

		initClickEvent();
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
				PLExplorerView.this.removeTopPopover();
			}
		}).showPopover();
	}

	@Override
	public void onSetImage(File file) {
		SharedPreferences.Editor editor = MYLogUtil.getPreferenceEditor();
		editor.putString(KEY_LAST_IMAGE, file.getPath());
		editor.commit();
	}

	private void loadDirectory(File file) {
		String path = file.getPath();
		File[] allFiles = new File(path).listFiles();
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
		findViewById(R.id.parent_button).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
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
				loadPicturesDirectory();
			}
		});
	}

	private void loadPicturesDirectory() {
		loadDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
	}

	private void showImageFile(File file) {
		PLImagePopover imagePopover = new PLImagePopover(mAdapter.getFileList(), file, mAdapter.getImageCache());
		imagePopover.showPopover(this);
		imagePopover.setListener(this);
	}
}
