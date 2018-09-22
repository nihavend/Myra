package com.likya.myra.jef.model;

public enum Forwarder {

	// Sample usage
	// Forwarder forwarder = Forwarder.CALENDAR_CALCULATED;
	// forwarder.setObject(null);
	// return forwarder;

	MAX_COUNT_EXCEEDED(1000, "Execution count exceeded the value defined for maxCount !"),
	CALENDAR_CALCULATED(2000, null), CALENDAR_NOT_CALCULATED(3000, null);

	private int value;
	private Object object;

	private Forwarder(int value, Object o) {
		this.value = value;
		this.object = o;
	}

	public Object getObject() {
		return object;
	}

	public int getValue() {
		return value;
	}

	public void setObject(Object object) {
		this.object = object;
	}

}
