package jp.co.my.myplatform.service.navigation;

import android.os.Environment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import jp.co.my.myplatform.R;
import jp.co.my.myplatform.service.adapter.PLImageRecyclerAdapter;

public class PLImageListView extends PLNavigationView {

	private TextView mTextView;
	private RecyclerView mRecyclerView;
	private RecyclerView.Adapter  mAdapter;

	public PLImageListView() {
		super();
		LayoutInflater.from(getContext()).inflate(R.layout.content_image_list, this);
		mTextView = (TextView) findViewById(R.id.path_text);
		mRecyclerView = (RecyclerView) findViewById(R.id.image_recycler);

		mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
		

		File pictureFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		// Add to path "Screenshots"
		loadImageListFromPath(pictureFile.getPath());
	}

	private void loadImageListFromPath(String path) {
		mTextView.setText(path);

		File[] allFiles = new File(path).listFiles();
		ArrayList<File> imageFileArray = new ArrayList<>();
		for (File file : allFiles) {
			if (file.isFile() && (file.getName().endsWith(".jpg") || file.getName().endsWith(".png"))) {
				imageFileArray.add(file);
			}
		}

		mAdapter = new PLImageRecyclerAdapter(getContext(), imageFileArray);
		mRecyclerView.setAdapter(mAdapter);
	}
}
