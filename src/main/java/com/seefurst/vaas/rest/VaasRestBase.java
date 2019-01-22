package com.seefurst.vaas.rest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.VersionManager;


public interface VaasRestBase {
	
	static final Logger LOG = Logger.getLogger(VaasRestBase.class.getName());
	public static Session getRepositorySession(HttpServletRequest req) {
		try {
			return (Session) req.getAttribute("repositorySession");
		} catch (ClassCastException a) {
			System.err.println("Session is not a valid jcr session: " );
			a.printStackTrace(System.err);
		}
		return null;
	}
	
	public default Response logErrorAndRespond(Throwable e) {
		LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		return Response.serverError().encoding("utf-8").entity("{\"error\": {\"" + e.getLocalizedMessage() + "\"}").build();
	}
	
	public default String extractJsonFromRequest(HttpServletRequest req) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(req.getInputStream()));
			StringBuffer jsonBuffer = new StringBuffer();
			String line = null;
			while ((line= br.readLine()) != null) {
				jsonBuffer.append(line);
			}
			return jsonBuffer.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return "{}";
		}
		
	}
	
	public default Session getSession(HttpServletRequest req) {
		return (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
	}
}