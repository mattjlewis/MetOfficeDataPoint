package com.diozero.weather.metoffice.datapoint;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateTest {
	public static void main(String[] args) {
		String date_time = "2011-12-03T10:15:30Z";
		Instant instant = DateTimeFormatter.ISO_INSTANT.parse(date_time, Instant::from);
		System.out.println("instant: " + instant);
		LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
		System.out.println("ldt: " + ldt);
		ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.systemDefault());
		System.out.println("zdt: " + zdt);
		
		date_time = "2016-08-02T07:00:00Z";
		zdt = DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(date_time, ZonedDateTime::from);
		System.out.println("zdt: " + zdt);
		
		String DATE_FORMAT = "yyyy-MM-ddz";
		DateTimeFormatter date_formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
		date_time = "2016-08-02Z";
		TemporalAccessor ta = date_formatter.parse(date_time);
		System.out.println("ta: " + ta);
		LocalDate ld = date_formatter.parse(date_time, LocalDate::from);
		System.out.println("ld: " + ld);
		ldt = ld.atStartOfDay();
		System.out.println("ldt: " + ldt);
		zdt = ld.atStartOfDay(ZoneId.systemDefault());
		System.out.println("zdt: " + zdt);
	}
}
