package jp.co.my.common.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MYCalendarUtil {

	private MYCalendarUtil() {}

	public static String getDiffTimeTextForHm(Calendar from, Calendar to) {
		int[] diffTimes = getDiffTimesForHms(from, to);
		return (diffTimes[0] + "時間" + diffTimes[1] + "分");
	}

	public static String getDiffTimeTextForHms(Calendar from, Calendar to) {
		int[] diffTimes = getDiffTimesForHms(from, to);
		return String.format("%02d:%02d:%02d", diffTimes[0], diffTimes[1], diffTimes[2]);
	}

	public static int[] getDiffTimesForHms(Calendar from, Calendar to) {
		long diffMilli = to.getTimeInMillis() - from.getTimeInMillis();
		long diffSec = diffMilli / 1000;
		long diffMin = diffSec / 60;
		long diffHourMin = diffMin % (60 * 24);
		int[] diffTimes = {(int)diffHourMin / 60, (int)diffHourMin % 60, (int)diffSec % 60};
		return diffTimes;
	}

	public static String getDateTextFromCalendar(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("HH時mm分ss秒");
		return format.format(calendar.getTime());
	}
}
