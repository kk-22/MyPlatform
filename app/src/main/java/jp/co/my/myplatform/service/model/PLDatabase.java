package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;

@Database(name = PLDatabase.NAME, version = PLDatabase.VERSION, generatedClassSeparator = "_")
public class PLDatabase {
	public static final String NAME = "MySupportDatabase";
	public static final int VERSION = 11;
	public static final Object sLock = new Object();

	public static <T extends Model> void saveModelList(List<T> modelList) {
		saveModelList(modelList, false);
	}

	@SuppressWarnings("unchecked") // klassがTではないため不定であるため警告発生
	public static <T extends Model> void saveModelList(List<T> modelList, boolean isSync) {
		if (modelList == null || modelList.size() == 0) {
			MYLogUtil.showErrorToast("saveModelList list size is 0");
			return;
		}
		Class klass = modelList.get(0).getClass();
		MYLogUtil.outputLog("saveModelList count=" + modelList.size() + " class=" + klass);

		FastStoreModelTransaction<T> fast = FastStoreModelTransaction.saveBuilder(
				FlowManager.getModelAdapter(klass)).addAll(modelList).build();
		DatabaseDefinition database = FlowManager.getDatabase(PLDatabase.class);
		if (isSync) {
			database.executeTransaction(fast);
			return;
		}
		Transaction transaction = database.beginTransactionAsync(fast)
				.success(new Transaction.Success() {
					@Override
					public void onSuccess(Transaction transaction) {
						MYLogUtil.outputLog("saveModelList onSuccess");
					}
				}).error(new Transaction.Error() {
					@Override
					public void onError(Transaction transaction, Throwable error) {
						MYLogUtil.outputLog("saveModelList onError" + error.toString());
					}
				}).build();
		executeTransaction(transaction);
	}

	public static void executeTransaction(final Transaction transaction) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				synchronized (sLock) {
					transaction.execute();
				}
			}
		}).start();
	}
}
