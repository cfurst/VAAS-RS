package com.seefurst.vaas.utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.Oak;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_FILEPATH;


public class VaasListener implements ServletContextListener {
	private FileStore fs;
	
	public void contextInitialized(ServletContextEvent ctx) {
		try {
			// this might create a new repo every time deleting the old one.. we might want to tweak this a bit.
			fs = FileStoreBuilder.fileStoreBuilder(new File(REPOSITORY_FILEPATH)).build();
			SegmentNodeStore ns = SegmentNodeStoreBuilders.builder(fs).build();
			new Jcr(new Oak(ns)).createRepository();
			
		} catch (InvalidFileStoreVersionException|IOException a) {
			//TODO: MAKE THIS LOG4J
			System.err.println("something went wrong with creating the repo: " + a.getMessage());
			a.printStackTrace(System.err);
		}
	}
	
	public void contextDestroyed(ServletContextEvent ctx) {
		if (fs != null) 
			fs.close();
	}
}
