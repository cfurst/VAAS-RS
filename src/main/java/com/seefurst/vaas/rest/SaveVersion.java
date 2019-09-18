package com.seefurst.vaas.rest;

import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.version.VersionManager;
import javax.jcr.version.Version;
import javax.jcr.Node;
import javax.jcr.Workspace;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;


import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;
import static com.seefurst.vaas.utils.VaasConstants.VERSION_NODE_MIXIN;



@Path("content")
public class SaveVersion implements VaasRestBase{
	@Context
	private HttpServletRequest req;
	private static final Logger LOG = Logger.getLogger(SaveVersion.class.getName());
	
	
	/**
	 * TODO: Posting from curl from a file somehow removed the first character of the post... need to test post with a different client.
	 * 
	 * @param contentName
	 * @return
	 */
	@POST
	@Path("commit/{contentName}")
	@Produces("application/json")
	public Response commitVersion(@PathParam("contentName") String contentName) {
		Session repoSess = getSession(req);
		Workspace ws = repoSess.getWorkspace();
		LOG.finest("Got workspace... name..." + ws.getName());
		try {
			Node root = repoSess.getRootNode();
			VersionManager vm = ws.getVersionManager();
			Node contentNode = null;
			if (root.hasNode(contentName)) {
				LOG.fine("repository has node.. fetching node...");
				contentNode = root.getNode(contentName);
			} 
			else {
				LOG.fine("creating new node... with key:" + contentName);
				contentNode = root.addNode(contentName);
				LOG.finest("adding mixin..." + VERSION_NODE_MIXIN);
				contentNode.addMixin(VERSION_NODE_MIXIN);
				repoSess.save();
				
			}
			LOG.fine("checking out: " + contentNode.getPath());
			vm.checkout(contentNode.getPath());
			LOG.finest("extracting json...");
			String content = extractJsonFromRequest(req); 
			LOG.finest("content: " + content);
			LOG.fine("adding content...");
			contentNode.setProperty(NODE_CONTENT_PROPERTY_NAME, content);
			LOG.finest("saving session...");
			repoSess.save();
			LOG.finest("checking in....");
			Version v = vm.checkin(contentNode.getPath());
			LOG.fine("done... sending response...");
			
			return Response.ok().encoding("utf-8").entity("{\"version\":\"" + v.getName() + "\"}").build();
		} catch (RepositoryException e) {
			System.err.println(e.getLocalizedMessage());
			e.printStackTrace(System.err);
			return Response.serverError().entity("{\"error\": \"" + e.getLocalizedMessage() + "\"}").build();
		}
		
	}
	
	@PUT
	@Produces("application/json")
	@Path("commit/{contentName}")
	public Response commitVersionPut(@PathParam("contentName") String contentName) {
		return commitVersion(contentName);
	}
	
	
}
