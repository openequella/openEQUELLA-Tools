package com.pearson.eqas.utilities.open_file_count;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

import com.sun.management.UnixOperatingSystemMXBean;

public class Driver {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.out.println("To invoke, please give the following parameters:  ");
			System.out.println("[base directory for test file creation] [number of max files]");
			System.exit(0);
		}
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
        if(!(os instanceof UnixOperatingSystemMXBean)){
        	System.out.println("Your OS doesn't support the Java UnixOperatingSystemMXBean.  Try a Unix OS.");
			System.exit(0);
        }
        String base = args[0];
		long maxFiles = Long.parseLong(args[1]);
		System.out.println("Attempting to open up ["+maxFiles+"] files (note - the resulting open file count will be higher).");
		
		for(long i = 1; i <= maxFiles; i++) {
			File f = new File(base+"test_"+i+".txt");
			f.createNewFile();
			FileInputStream fis = new FileInputStream(f);
		}

		System.out.println("Number of open fd: " + ((UnixOperatingSystemMXBean) os).getOpenFileDescriptorCount());
        	System.out.println("Number of max fd: " + ((UnixOperatingSystemMXBean) os).getMaxFileDescriptorCount());
    }
}
