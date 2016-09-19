package jp.co.my.myplatform.service.news;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import jp.co.my.common.util.MYLogUtil;
import jp.co.my.myplatform.service.model.PLNewsPageModel;
import jp.co.my.myplatform.service.model.PLNewsSiteModel;

public class PLRSSParser {

	// TODO: delete this value
	private static final int MAX_SITE_NUM = 5;		// 1つのサイトからの最大ページ取得数

	private PLRSSParser() {}

	public static ArrayList<PLNewsPageModel> getPagaDatasForInputStream(PLNewsSiteModel site, InputStream inputStream) {
		ArrayList<PLNewsPageModel> pageArray = null;
		BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
		try {
			XmlPullParserFactory xmlPullParserFactory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = xmlPullParserFactory.newPullParser();
			parser.setInput(bufferedInputStream, "UTF-8");
			pageArray = parseXmlPLNewsPageModels(site, parser);
		} catch (XmlPullParserException | IOException e) {
			MYLogUtil.showExceptionToast(e);
		} finally {
			if (bufferedInputStream != null) {
				try {
					bufferedInputStream.close();
				} catch (IOException e) {
					MYLogUtil.showExceptionToast(e);
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					MYLogUtil.showExceptionToast(e);
				}
			}
		}
		if (pageArray == null) {
			MYLogUtil.showErrorToast("pageArray=null siteName" +site.getName() +" url=" +site.getUrl());
		}
		return pageArray;
	}

	private static ArrayList<PLNewsPageModel> parseXmlPLNewsPageModels(PLNewsSiteModel siteData, XmlPullParser parser) throws XmlPullParserException, IOException {
		ArrayList<PLNewsPageModel> pageArray = new ArrayList<>();

		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			eventType = parser.next();
			if (eventType != XmlPullParser.START_TAG || MAX_SITE_NUM <= pageArray.size()) {
				continue;
			}
			String tag = parser.getName();
			if (tag.equals("title")) {
				// サイト名
				String siteName =  parser.nextText();
				if (!siteName.equals(siteData.getName())) {
					siteData.setName(siteName);
					siteData.save();
				}
			} else if (tag.equals("item")) {
				// ページ情報
				PLNewsPageModel page = parseItemTagPLNewsPageModel(parser);
				page.associateSite(siteData);
				pageArray.add(page);
			}
		}
		return pageArray;
	}

	private static PLNewsPageModel parseItemTagPLNewsPageModel(XmlPullParser parser) throws XmlPullParserException, IOException {
		PLNewsPageModel pageData = new PLNewsPageModel();
		while (true) {
			int eventType = parser.next();
			String tag = parser.getName();

			if (eventType == XmlPullParser.END_TAG && tag.equals("item")) {
				break;
			} else if (eventType != XmlPullParser.START_TAG) {
				continue;
			}

			if (tag.equals("title")) {
				pageData.setTitle(parser.nextText());
			} else if (tag.equals("link")) {
				pageData.setUrl(parser.nextText());
			} else if (tag.equals("dc:date")) {
				// 投稿日（RSS1.0）
				String dateText = parser.nextText();
				String tempText = dateText.replace("T", " ").substring(0, 19);
				Calendar calendar = convertStringToCalendar(tempText, "yyyy-MM-dd hh:mm:ss");
				pageData.setPostedDate(calendar);
			} else if (tag.equals("pubDate")) {
				// 投稿日（RSS2.0）
				String dateText = parser.nextText();
				Calendar calendar = convertStringToCalendar(dateText, "EEE, dd MMM yyyy HH:mm:ss Z");
				pageData.setPostedDate(calendar);
			}
		}
		return pageData;
	}

	private static Calendar convertStringToCalendar(String text, String template) {
		SimpleDateFormat format = new SimpleDateFormat(template, Locale.US);
		Date date;
		try {
			date = format.parse(text);
		} catch (ParseException e) {
			date = new Date();
			MYLogUtil.showExceptionToast(e);
		}

		Calendar calendar = Calendar.getInstance();
		Date nowDate = new Date();
		if (date.before(nowDate)) {
			calendar.setTime(date);
		} else {
			// 現在日時より先のものは書き換える
			calendar.set(1970, 0, 1, 0, 0);
		}
		return calendar;
	}
}
