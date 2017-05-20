package com.diozero.weather.metoffice.datapoint;

import java.time.ZonedDateTime;
import java.util.Map;

public class Forecast {
	private Map<String, Param> params;
	private DataValue dataValue;
	
	public Forecast() {
	}

	public Map<String, Param> getParams() {
		return params;
	}

	public void setParams(Map<String, Param> params) {
		this.params = params;
	}

	public DataValue getDataValue() {
		return dataValue;
	}

	public void setDataValue(DataValue dataValue) {
		this.dataValue = dataValue;
	}

	@Override
	public String toString() {
		return "Forecast [params=" + params + ", dataValue=" + dataValue + "]";
	}

	public Report getClosestReport(ZonedDateTime dateTime) {
		Report previous_report = null;
		for (Period period : dataValue.getLocation().getPeriods()) {
			for (Report report : period.getReports()) {
				if (report.getReportDateTime().isAfter(dateTime)) {
					long current_delta = Math.abs(report.getReportDateTime().toEpochSecond() - dateTime.toEpochSecond());
					long previous_delta;
					if (previous_report == null) {
						previous_delta = Long.MAX_VALUE;
					} else {
						previous_delta = Math.abs(previous_report.getReportDateTime().toEpochSecond() - dateTime.toEpochSecond());
					}
					
					if (previous_delta < current_delta) {
						return previous_report;
					}
					
					return report;
				}
				
				previous_report = report;
			}
		}
		
		return previous_report;
	}
}
