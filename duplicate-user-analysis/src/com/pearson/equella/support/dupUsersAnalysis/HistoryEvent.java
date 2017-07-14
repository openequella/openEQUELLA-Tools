package com.pearson.equella.support.dupUsersAnalysis;

import java.sql.ResultSet;
import java.sql.SQLException;

public class HistoryEvent implements GenericEntity {
	private String id;
	private String applies;
	private String comment;
	private String date;
	private String state;
	private String step;
	private String stepName;
	private String toStep;
	private String toStepName;
	private String type;
	private String user;
	
	public HistoryEvent(ResultSet rs) throws SQLException {
		this.id = rs.getString(1);
		this.applies = rs.getString(2);
		this.comment = rs.getString(3);
		this.date = rs.getString(4);
		this.state = rs.getString(5);
		this.step = rs.getString(6);
		this.stepName = rs.getString(7);
		this.toStep = rs.getString(8);
		this.toStepName = rs.getString(9);
		this.type = rs.getString(10);
		this.user = rs.getString(11);
	}
	public String toString() {
		return String.format("Id=[%s], Applies=[%s], Comment=[%s], Date=[%s], " +
				"State=[%s], Step=[%s], StepName=[%s], ToStep=[%s], ToStepName=[%s], " +
				"Type=[%s], User=[%s]", 
				id, applies, comment, date, state, step, stepName, toStep, toStepName, type, user);
	}
}
