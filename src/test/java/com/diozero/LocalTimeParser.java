package com.diozero;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class LocalTimeParser {
	public static void main(String[] args) {
		LocalTime t = LocalTime.parse("22 11 30", DateTimeFormatter.ofPattern("HH mm ss"));
		System.out.println(t);
		System.out.println(t.format(DateTimeFormatter.ofPattern("K:mm a")));

		t = LocalTime.parse("10 11 30 pm", DateTimeFormatter.ofPattern("hh mm ss a"));
		System.out.println(t);

		DateTimeFormatter formatter = new DateTimeFormatterBuilder().appendPattern("K:mm a")
				.parseDefaulting(ChronoField.HOUR_OF_AMPM, 0).toFormatter();
		t = LocalTime.parse("8:28 pm", formatter);
		t = LocalTime.parse("8:28 pm", DateTimeFormatter.ofPattern("K:mm a"));
		System.out.println(t);
	}
}
