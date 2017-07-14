package com.pearson.equella.support.logreview.utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ReviewWriter implements AutoCloseable {
	private static Charset charset = Charset.forName("UTF-8");

	private BufferedWriter w = null;
	private Path p = null;

	private ReviewWriter condensed = null;

	public ReviewWriter(String targetFilename) throws IOException {
		p = Paths.get(targetFilename);
		w = Files.newBufferedWriter(p, charset);
	}

	public void attachedCondensedWriter(ReviewWriter con) {
		this.condensed = con;
	}

	private void writelnf(String s, Object... args) throws IOException {
		writelnf(false, s, args);
	}

	public void writelnf(boolean writeToCondensed, String s, Object... args) throws IOException {
		writef(s, args);
		newln();
		if (condensed != null && writeToCondensed) {
			condensed.writelnf(s, args);
		}
	}

	private void newln() throws IOException {
		newln(false);
	}

	public void newln(boolean writeToCondensed) throws IOException {
		writef("\n");
		w.flush();
		if (condensed != null && writeToCondensed) {
			condensed.newln();
		}
	}

	private void writef(String s, Object... args) throws IOException {
		writef(false, s, args);
	}

	public void writeln(String s) throws IOException {
		w.write(s);
		newln();
	}

	public void writef(boolean writeToCondensed, String s, Object... args) throws IOException {
		w.write(String.format(s, args));
		if (condensed != null && writeToCondensed) {
			condensed.writef(String.format(s, args));
		}
	}

	@Override
	public void close() throws Exception {
		w.flush();
		w.close();
	}

	public Path getReportPath() {
		return p;
	}

}
