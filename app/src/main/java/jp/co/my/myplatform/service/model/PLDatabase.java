package jp.co.my.myplatform.service.model;

import com.raizlabs.android.dbflow.annotation.Database;

@Database(name = PLDatabase.NAME, version = PLDatabase.VERSION, generatedClassSeparator = "_")
public class PLDatabase {
	public static final String NAME = "MySupportDatabase";
	public static final int VERSION = 1;
}
