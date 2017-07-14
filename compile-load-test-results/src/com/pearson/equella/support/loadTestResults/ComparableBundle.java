package com.pearson.equella.support.loadTestResults;

public class ComparableBundle implements Comparable<ComparableBundle> {
	private boolean compareFirst = true;
	private String string;
	private Long num;
	
	public ComparableBundle(String s, Long l, boolean compareFirst) {
		this.compareFirst = compareFirst;
		this.string = s;
		this.num = l;
	}

	@Override
	public int compareTo(ComparableBundle o) {
		if(compareFirst) {
			return this.string.compareTo(o.string);
		} else {
			return this.num.compareTo(o.num);
		}
	}
	
	public String getString() {
		return this.string;
	}
	
	public Long getLong() {
		return this.num;
	}

}
