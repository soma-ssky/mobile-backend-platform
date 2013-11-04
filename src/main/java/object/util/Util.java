package main.java.object.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Util {
	private static final String DATE_PATTERN = "EEE MMM dd HH:mm:ss zz yyyy";

	public static Date convertDateToString(String str) {
		try {
			DateFormat df = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
			return df.parse(str);
		} catch (ParseException e) {
			return null;
		}
	}

}
