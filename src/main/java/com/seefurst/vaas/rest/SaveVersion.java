package com.seefurst.vaas.rest;

import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.Consumes;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;
import javax.jcr.version.Version;
import javax.jcr.Node;
import javax.jcr.Workspace;

import javax.servlet.http.HttpServletRequest;


import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;
import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;
import static com.seefurst.vaas.utils.VaasConstants.VERSION_NODE_MIXIN;

public class SaveVersion {
	@Context
	private HttpServletRequest req;
	
	
	@POST
	@Path("content/commit/{contentName}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response commitVersion(@PathParam("contentName") String contentName, String content) {
		Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
		Workspace ws = repoSess.getWorkspace();
		try {
			Node root = repoSess.getRootNode();
			VersionManager vm = ws.getVersionManager();
			Node contentNode = null;
			if (root.hasNode(contentName)) {
				contentNode = root.getNode(contentName);
			} 
			else {
				contentNode = root.addNode(contentName);
				contentNode.addMixin(VERSION_NODE_MIXIN);
			}
			vm.checkout(contentNode.getPath());
			contentNode.setProperty(NODE_CONTENT_PROPERTY_NAME, content);
			repoSess.save();
			Version v = vm.checkin(contentNode.getPath());
			
			return Response.ok().encoding("utf-8").entity("{\"version\":\"" + v.getName() + "\"").build();
		} catch (RepositoryException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace(System.err);
			return Response.serverError().entity("{\"error\": \"" + e.getLocalizedMessage() + "\"}").build();
		}
		
	}
}
