package jp.co.my.myplatform.service.model;

import java.util.List;

import jp.co.my.common.util.MYArrayList;

public class PLAllModelFetcher {

	private List<Class> mClassList;
	private PLAllModelFetcherListener mListener;

	private boolean mIsCanceled;
	private MYArrayList<MYArrayList<PLBaseModel>> mResultArrays;

	public PLAllModelFetcher(List<Class> classList, PLAllModelFetcherListener listener) {
		mListener = listener;
		mClassList = classList;
	}

	@SuppressWarnings("unchecked") // PLModelFetchTaskの型チェックをしない
	public void startAllModelFetch() {
		mIsCanceled = false;
		int numberOfClass = mClassList.size();
		mResultArrays = new MYArrayList<>(numberOfClass);

		for (int i = 0; i < numberOfClass; i++) {
			final Class klass = mClassList.get(i);
			PLModelFetchTask<PLBaseModel> fetchTask = new PLModelFetchTask<>(klass, new PLModelFetchTask.PLModelFetchTaskListener() {
				@Override
				public void finishedFetchModels(MYArrayList<PLBaseModel> modelArray) {
					if (mIsCanceled) {
						return;
					}
					if (modelArray == null) {
						mIsCanceled = true;
						mListener.finishedAllFetchModels(null);
						return;
					}
					int index = mClassList.indexOf(klass);
					mResultArrays.add(index, modelArray);
					if (mClassList.size() == mResultArrays.size()) {
						mListener.finishedAllFetchModels(mResultArrays);
					}
				}
			});
			fetchTask.execute();
		}
	}

	public interface PLAllModelFetcherListener {
		void finishedAllFetchModels(MYArrayList<MYArrayList<PLBaseModel>> modelArrays);
	}
}
