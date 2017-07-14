package com.pearson.equella.support.ping.utils;

import java.io.File;
import java.io.FilenameFilter;

public class OnlyErrorStatsFileFilter implements FilenameFilter {

	@Override
	public boolean accept(File dir, String name) {
		String part = "_"+Config.getInstance().getClientName()+"_error_stats_";
		return (new File(dir.getAbsolutePath()+"/"+name)).isFile() && name.contains(part);
	}
}
