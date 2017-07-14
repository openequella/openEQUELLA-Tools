package com.pearson.equella.support.ping.report;

import java.util.List;

import com.pearson.equella.support.ping.direct.ResultsRow;

public class ResultComparison {
	private List<ResultsRow> onlyInFirst;
	private List<ResultsRow> onlyInSecond;
	private String firstName;
	private String secondName;

	public ResultComparison(String firstName, List<ResultsRow> onlyInFirst,
			String secondName, List<ResultsRow> onlyInSecond) {
		this.onlyInFirst = onlyInFirst;
		this.firstName = firstName;
		this.onlyInSecond = onlyInSecond;
		this.secondName = secondName;
	}

	public List<ResultsRow> getOnlyInFirst() {
		return onlyInFirst;
	}

	public List<ResultsRow> getOnlyInSecond() {
		return onlyInSecond;
	}

	public boolean areReportsEqual() {
		return onlyInFirst.isEmpty() && onlyInSecond.isEmpty();
	}

	public String getFirstName() {
		return firstName;
	}

	public String getSecondName() {
		return secondName;
	}
}
