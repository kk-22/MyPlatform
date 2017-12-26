package jp.co.my.myplatform.database;

import android.os.Handler;

import com.raizlabs.android.dbflow.sql.language.BaseModelQueriable;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.ArrayList;
import java.util.List;

public class PLModelContainer<TModel extends BaseModel> {

	private List<TModel> mModelList;
	private BaseModelQueriable<TModel> mQuery;

	public PLModelContainer(BaseModelQueriable<TModel> query) {
		mQuery = query;
	}

	public void addModel(TModel model) {
		if (mModelList == null) {
			mModelList = new ArrayList<>();
		}
		mModelList.add(model);
	}

	public long count() {
		if (mModelList != null) {
			return mModelList.size();
		}
		return mQuery.count();
	}

	public void clear() {
		mModelList = null;
	}

	public void loadList(final PLOnModelLoadThreadListener<TModel> threadListener, final PLOnModelLoadMainListener<TModel> mainListener) {
		if (mModelList == null) {
			loadListInThread(true, threadListener, mainListener);
			return;
		}

		if (threadListener == null) {
			executeMainListenerIfNecessary(null, mainListener);
			return;
		}
		loadListInThread(false, threadListener, mainListener);
	}

	private void loadListInThread(final boolean needQuery
			, final PLOnModelLoadThreadListener<TModel> threadListener
			, final PLOnModelLoadMainListener<TModel> mainListener) {
		final Handler mainHandler = createHandlerIfNecessary(mainListener);
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (needQuery) {
					mModelList = mQuery.queryList();
				}
				executeThreadListenerIfNecessary(true, threadListener);
				executeMainListenerIfNecessary(mainHandler, mainListener);
			}
		}).start();
	}

	private Handler createHandlerIfNecessary(PLOnModelLoadMainListener mainListener) {
		if (mainListener == null) {
			return null;
		}
		return new Handler();
	}

	private void executeThreadListenerIfNecessary(boolean isThread, final PLOnModelLoadThreadListener<TModel> threadListener) {
		if (threadListener == null) {
			return;
		}
		if (isThread) {
			threadListener.onLoad(mModelList);
			return;
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				threadListener.onLoad(mModelList);
			}
		}).start();
	}

	private void executeMainListenerIfNecessary(Handler handler, final PLOnModelLoadMainListener<TModel> mainListener) {
		if (mainListener == null) {
			return;
		}
		if (handler == null) {
			// In main thread
			mainListener.onLoad(mModelList);
			return;
		}
		handler.post(new Runnable() {
			@Override
			public void run() {
				mainListener.onLoad(mModelList);
			}
		});
	}

	public static abstract class PLOnModelLoadThreadListener<TModel> {
		public abstract void onLoad(List<TModel> modelList);
	}
	public static abstract class PLOnModelLoadMainListener<TModel> {
		public abstract void onLoad(List<TModel> modelList);
	}

	public List<TModel> getModelList() {
		return mModelList;
	}

	public void setModelList(List<TModel> modelList) {
		mModelList = modelList;
	}
}
