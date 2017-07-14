package com.pearson.equella.support.loadTestResults.utils;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class StrUtils {
	private static DecimalFormat df = new DecimalFormat("#.#");
	{
		df.setRoundingMode(RoundingMode.CEILING);
	}
	
	public static DecimalFormat getDecimalFormatter() {
		return df;
	}
}
