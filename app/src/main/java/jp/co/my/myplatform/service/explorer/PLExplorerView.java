package jp.co.my.myplatform.service.explorer;

import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.common.util.MYStringUtil;
import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.navigation.PLNavigationView;

public class PLExplorerView extends PLNavigationView implements PLExplorerRecyclerAdapter.PLOnClickFileListener {

	private File mCurrentFile;
	private TextView mPathText;
	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter mAdapter;

	public PLExplorerView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_explorer, this);
		mPathText = (TextView) findViewById(R.id.path_text);
		mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

		loadPicturesDirectory();
		initClickEvent();
	}

	@Override
	public void onClickFile(File file) {
		if (file.isDirectory()) {
			loadDirectory(file);
			return;
		}

		String extension = MYStringUtil.getSuffix(file.getName());
		if (extension.equals("png") || extension.equals("jpg")) {
			new PLImagePopover(file).showPopover();
		} else {
			MYLogUtil.showToast("Sorry, No action");
		}
	}

	private void loadDirectory(File file) {
		mCurrentFile = file;
		String path = file.getPath();
		mPathText.setText(path);

		File[] allFiles = new File(path).listFiles();
		List<File> fileList = Arrays.asList(allFiles);
		mAdapter = new PLExplorerRecyclerAdapter(getContext(), fileList, this);
		mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
		mRecyclerView.setAdapter(mAdapter);
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
}
