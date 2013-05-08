package me.timothy.coupons;

public class NamedString {
	private String name;
	private String val;

	public NamedString(String str, int i) {
		this.name = str;
		val = Integer.toString(i);
	}

	public NamedString(String str, double d) {
		this.name = str;
		val = Double.toString(d);
	}

	public NamedString(String str, String val) {
		this.name = str;
		this.val = val;
	}

	public String getValue() {
		return val;
	}

	public String getName() {
		return name;
	}
}
