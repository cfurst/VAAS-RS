package com.seefurst.vaas.rest;

import javax.ws.rs.GET;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.VersionManager;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.Node;
import javax.jcr.Workspace;

import javax.servlet.http.HttpServletRequest;


import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;
import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;


public class GetVersion implements VaasRestBase{
	@Context
	private HttpServletRequest req;
	
	
	
	@Path("content/get/{contentName}/{versionName}")
	@GET
	public void getVersionByName(@PathParam("contentName") String contentName,
			@PathParam("versionName") String versionName) {
		
	}
	
	
	@Path("admin/test")
	@Produces("text/html")
	@GET
	public Response getTest() {
		return Response.ok("Yep, I'm here!").build();
	}
	/**
	 * getLatestVersion 
	 * @description gets the base version of the content node at contentName
	 * @param contentName
	 * @throws RepositoryException 
	 * @throws UnsupportedRepositoryOperationException 
	 */
	@Path("content/get/{contentName}")
	@GET
	@Produces("application/json")
	public Response getLatestVersion(@PathParam("contentName") String contentName)  {
		try {
			Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				Version v = vm.getBaseVersion(root.getNode(contentName).getPath());
				Node vn = v.getFrozenNode();
				return Response.ok().encoding("utf-8").entity(vn.getProperty(NODE_CONTENT_PROPERTY_NAME).getString()).build();
				 
			} else {
				return Response.status(404, "{\"error\": \"The content " + contentName + " was not found or does not have any versions yet\"}").build();
			}
			
		} catch (ClassCastException|UnsupportedRepositoryOperationException a) {
			
			return Response.serverError().encoding("utf-8").entity("{" +
					"\"error:\"" + a.getLocalizedMessage() + "\"" +
					"}").build();
		} catch (RepositoryException b) {
			return Response.serverError().encoding("utf-8").entity("{\"error\": \"" + b.getLocalizedMessage() + "\"}").build();
		}
		
		
	}
	
	@Path("content/get/{contentName}/{versionName}")
	@GET
	@Produces("application/json")
	public Response getVersion(@PathParam("contentName") String contentName, @PathParam("versionName") String versionName) {
		try {
			Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
			Workspace wrkSp = repoSess.getWorkspace();
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				
				VersionManager vm = wrkSp.getVersionManager();
				VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
				Version v = vh.getVersion(versionName);
				Node f = v.getFrozenNode();
				return Response.ok(f.getProperty(NODE_CONTENT_PROPERTY_NAME)).encoding("utf-8").build();
			} else {
				return Response.status(404, "{\"error\": \"The content " + contentName + " was not found\"}").build();
			}
			
		} catch(VersionException a) {
			return Response.status(404, "{\"error\": \"The content " + contentName + " does not have version " + versionName + "\"}").encoding("utf-8").build();
		}catch (RepositoryException b) {
			return Response.serverError().encoding("utf-8").entity("{\"error\": {\"" + b.getLocalizedMessage() + "\"}").build();
		}
	}
	
	
	
	
}
