package com.pearson.equella.support.ping.direct;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WhereClauseExpression {
	private static final Logger logger = LogManager
			.getLogger(WhereClauseExpression.class);

	private String expression;
	private int intParm = Integer.MIN_VALUE;
	private int index;
	
	public WhereClauseExpression(String expression, int value, int index) {
		this.expression = expression;
		this.intParm = value;
		this.index = index;
	}
	
	public String getExpression() {
		return expression;
	}
	
	public void setParm(PreparedStatement stmt) throws SQLException {
		stmt.setInt(index, intParm);
		logger.info("Setting parameter.  Index=[{}], value=[{}].", index, intParm);
	}
	
	public static String makeWhereClause(List<WhereClauseExpression> exprs) {
		StringBuilder whereClause = new StringBuilder();
		if (exprs.size() > 0) {
			whereClause.append("where");
		}
		for (int i = 0; i < exprs.size(); i++) {
			WhereClauseExpression wce = exprs.get(i);
			whereClause.append(" ").append(wce.getExpression());
			if (i != (exprs.size() - 1)) {
				whereClause.append(" AND");
			} else {
				whereClause.append(" ");
			}
		}
		return whereClause.toString();
	}
	
	public static void setParms(PreparedStatement stmt, List<WhereClauseExpression> exprs) throws SQLException {
		for (WhereClauseExpression wce : exprs) {
			wce.setParm(stmt);
		}
		
	}
}
