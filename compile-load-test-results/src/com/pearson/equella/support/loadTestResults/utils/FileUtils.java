package com.pearson.equella.support.loadTestResults.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileUtils {
	//TODO externalize FILE_ROOT
	public static final String FILE_ROOT = "/path/to/my/file/parent/directory/";
	private static BufferedWriter currentWriter = null;
	private static String currentFilename = "";

	public static void closeWriter() throws IOException {
		currentWriter.newLine();
		currentWriter.flush();
		currentWriter.close();
		System.out.println("Finalized writing to " + currentFilename);
		currentWriter = null;
	}

	public static void setupWriter(String filename) throws IOException {
		currentFilename = filename;
		System.out.println("Writing to " + currentFilename);
		if (currentWriter != null) {
			closeWriter();
			currentWriter = null;
		}
		File d = new File(FILE_ROOT+"/reports");
		d.mkdir();
		File f = new File(d.getAbsolutePath() + "/" + currentFilename);
		f.createNewFile();
		currentWriter = new BufferedWriter(new FileWriter(f));
	}

	public static void outComma(String s) throws IOException {
		out(false, ",%s", s);
	}

	public static void out(boolean postWriteNewLine, String msg,
			Object... args) throws IOException {
		if ((args != null) && (args.length > 0))
			currentWriter.write(String.format(msg, args));
		else
			currentWriter.write(msg);
		if (postWriteNewLine)
			currentWriter.newLine();
		if (postWriteNewLine)
			currentWriter.flush();
	}

	public static void outln() throws IOException {
		out(true, "");
	}
}
