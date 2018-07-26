package com.seefurst.vaas.rest;

import javax.servlet.http.HttpServletRequest;
import javax.jcr.Session;


public interface VaasRestBase {
	public static Session getRepositorySession(HttpServletRequest req) {
		try {
			return (Session) req.getAttribute("repositorySession");
		} catch (ClassCastException a) {
			System.err.println("Session is not a valid jcr session: " );
			a.printStackTrace(System.err);
		}
		return null;
	}
	
	
}
