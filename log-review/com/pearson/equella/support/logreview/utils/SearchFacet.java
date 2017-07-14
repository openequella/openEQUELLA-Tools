package com.pearson.equella.support.logreview.utils;

import org.json.JSONObject;

/**
 * There's currently no check for type or severity consistency.
 */
public class SearchFacet {
	public enum SearchType {
		IGNORE("/"), LIKE("~"), EQUALS("=");

		private final String symbol;

		SearchType(String symbol) {
			this.symbol = symbol;
		}

		String getSymbol() {
			return this.symbol;
		}
	}

	private String severity;
	private SearchType msgType = SearchType.IGNORE;
	private String msgQuery = "";
	private SearchType stackTraceType = SearchType.IGNORE;
	private String stackTraceQuery = "";
	private SearchType levelType = SearchType.IGNORE;
	private String levelQuery = "";
	private SearchType categoryType = SearchType.IGNORE;
	private String categoryQuery = "";

	public SearchFacet(JSONObject facetJson) {
		if (facetJson.has("message")) {
			JSONObject jObj = facetJson.getJSONObject("message");
			try {
				msgType = SearchType.valueOf(jObj.getString("type").toUpperCase());
			} catch (Exception e) {
				System.err.println(
						"Failed to parse the stack-trace type [" + jObj.getString("type") + "] into a SearchType.");
				throw e;
			}
			msgQuery = jObj.getString("query");
		}

		if (facetJson.has("stack-trace")) {
			JSONObject jObj = facetJson.getJSONObject("stack-trace");
			try {
				stackTraceType = SearchType.valueOf(jObj.getString("type").toUpperCase());
			} catch (Exception e) {
				System.err.println(
						"Failed to parse the stack-trace type [" + jObj.getString("type") + "] into a SearchType.");
				throw e;
			}
			stackTraceQuery = jObj.getString("query");
		}

		if (facetJson.has("level")) {
			JSONObject jObj = facetJson.getJSONObject("level");
			levelType = SearchType.valueOf(jObj.getString("type").toUpperCase());
			levelQuery = jObj.getString("query");
		}

		if (facetJson.has("category")) {
			JSONObject jObj = facetJson.getJSONObject("category");
			categoryType = SearchType.valueOf(jObj.getString("type").toUpperCase());
			categoryQuery = jObj.getString("query");
		}

		severity = facetJson.getString("severity");
	}

	public String getSeverity() {
		return severity;
	}

	public String toString() {
		return "SearchFacet: severity=[" + severity + "], " + prettyPrint();
	}

	public String prettyPrint() {
		return String.format("M%s'%s', ST%s'%s', LEV%s'%s', CAT%s'%s'", msgType.getSymbol(), msgQuery,
				stackTraceType.getSymbol(), stackTraceQuery, levelType.getSymbol(), levelQuery,
				categoryType.getSymbol(), categoryQuery);

	}

	public SearchType getMsgType() {
		return msgType;
	}

	public String getMsgQuery() {
		return msgQuery;
	}

	public SearchType getStackTraceType() {
		return stackTraceType;
	}

	public String getStackTraceQuery() {
		return stackTraceQuery;
	}

	public SearchType getLevelType() {
		return levelType;
	}

	public String getLevelQuery() {
		return levelQuery;
	}

	public SearchType getCategoryType() {
		return categoryType;
	}

	public String getCategoryQuery() {
		return categoryQuery;
	}

}
