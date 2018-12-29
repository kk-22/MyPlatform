package jp.co.my.myplatform.core;

/*
 gitでファイル差分を無視する設定にしたクラス。
 ローカルリポジトリ内でのみ実装したい開発用コードを置く。

 設定方法
 git update-index --skip-worktree app/src/main/java/jp/co/my/myplatform/core/PLDevelopmentUtil.java

 解除方法
 git update-index --no-skip-worktree app/src/main/java/jp/co/my/myplatform/core/PLDevelopmentUtil.java
 */
class PLDevelopmentUtil {

	private PLDevelopmentUtil(){};

	static void openScreenInDevelopment() {
//		PLCoreService.getNavigationController().pushView(null);
	}

	static boolean isWriteLog() {
		return false;
	}
}
