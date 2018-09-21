package com.seefurst.vaas.utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.jackrabbit.oak.segment.file.FileStore;
import org.apache.jackrabbit.oak.segment.file.FileStoreBuilder;
import org.apache.jackrabbit.oak.segment.SegmentNodeStoreBuilders;
import org.apache.jackrabbit.oak.segment.SegmentNodeStore;
import org.apache.jackrabbit.oak.segment.file.InvalidFileStoreVersionException;
import org.apache.jackrabbit.oak.jcr.Jcr;
import org.apache.jackrabbit.oak.Oak;




public class VaasListener implements ServletContextListener {
	private FileStore fs;
	private static final Logger LOG = Logger.getLogger(VaasListener.class.getName());
	public void contextInitialized(ServletContextEvent ctx) {
		try {
			// this might create a new repo every time deleting the old one.. we might want to tweak this a bit.
			ServletContext sc = ctx.getServletContext();
			sc.log("servlet path: ========= : " + sc.getRealPath("/vaas"));
			fs = FileStoreBuilder.fileStoreBuilder(new File(sc.getRealPath("/vaas"))).build();
			SegmentNodeStore ns = SegmentNodeStoreBuilders.builder(fs).build();
			new Jcr(new Oak(ns)).createRepository();
			
		} catch (InvalidFileStoreVersionException|IOException a) {
			//TODO: MAKE THIS LOG4J
			
			LOG.log(java.util.logging.Level.SEVERE, "Couldn't create repository!", a);
		}
	}
	
	public void contextDestroyed(ServletContextEvent ctx) {
		if (fs != null) 
			fs.close();
	}
}
