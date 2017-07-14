package com.pearson.equella.support.logreview.utils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Summarizes Equella HTML log files into a report series.
 */
public class Utils {

	public static JSONObject parseJsonFile(String filename, Charset encoding) throws JSONException, IOException {
		byte[] allBytes = Files.readAllBytes(Paths.get(filename));
		return new JSONObject(new String(allBytes, encoding));
	}

	public static void outlnf(String msg, Object... args) {
		System.out.println(String.format(msg, args));
	}

	public static void outf(String msg, Object... args) {
		System.out.print(String.format(msg, args));
	}
}
