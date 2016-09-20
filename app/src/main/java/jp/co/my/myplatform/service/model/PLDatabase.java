package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.Model;
import com.raizlabs.android.dbflow.structure.database.transaction.FastStoreModelTransaction;

import java.util.List;

import jp.co.my.common.util.MYLogUtil;

@Database(name = PLDatabase.NAME, version = PLDatabase.VERSION, generatedClassSeparator = "_")
public class PLDatabase {
	public static final String NAME = "MySupportDatabase";
	public static final int VERSION = 9;

	@SuppressWarnings("unchecked") // klassがTではないため不定であるため警告発生
	public static <T extends Model> void saveModelList(List<T> modelList) {
		if (modelList.size() == 0) {
			MYLogUtil.showErrorToast("saveModelList list size is 0");
			return;
		}
		Class klass = modelList.get(0).getClass();
		FastStoreModelTransaction.insertBuilder(
				FlowManager.getModelAdapter(klass)).addAll(modelList).build();
	}
}
