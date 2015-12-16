package org.matt.metoffice.datapoint;

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
}
