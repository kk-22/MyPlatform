package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.transaction.ITransaction;
import com.raizlabs.android.dbflow.structure.database.transaction.Transaction;

import java.util.List;

@Database(name = PLDatabase.NAME, version = PLDatabase.VERSION, generatedClassSeparator = "_")
public class PLDatabase {
	public static final String NAME = "MySupportDatabase";
	public static final int VERSION = 2;

	public static void saveAllModel(final List<? extends BaseModel>... args) {
		DatabaseDefinition database = FlowManager.getDatabase(PLDatabase.class);
		Transaction transaction = database.beginTransactionAsync(new ITransaction() {
			@Override
			public void execute(DatabaseWrapper databaseWrapper) {
				for (List<? extends BaseModel> list : args) {
					for (BaseModel model : list) {
						model.save();
					}
				}
			}
		}).build();
		transaction.execute();
	}
}
