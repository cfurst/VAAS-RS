package com.seefurst.vaas.rest;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.POST;
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
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;
import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;

@Path("content")
public class GetVersion implements VaasRestBase{
	@Context
	private HttpServletRequest req;
	private static final Logger LOG = Logger.getLogger(GetVersion.class.getName());
	
	
	
	
	@Path("test")
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
	@Path("get/{contentName}")
	@GET
	@Produces("application/json")
	public Response getLatestVersion(@PathParam("contentName") String contentName)  {
		try {
			LOG.fine("Attempting to get latest version of " + contentName +"...");
			LOG.finest("getting session...");
			Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
			LOG.finest("getting workspace...");
			Workspace wrkSp = repoSess.getWorkspace();
			LOG.finest("getting version manager...");
			VersionManager vm = wrkSp.getVersionManager();
			LOG.finest("getting root node...");
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				LOG.finest("getting base version...");
				Version v = vm.getBaseVersion(root.getNode(contentName).getPath());
				LOG.finest("getting frozen node...");
				Node vn = v.getFrozenNode();
				LOG.fine("Found Latest version and returning content...");
				return Response.ok().encoding("utf-8").entity(vn.getProperty(NODE_CONTENT_PROPERTY_NAME).getString()).build();
				 
			} else {
				LOG.fine(contentName + " is not here! Sending 404...");
				return Response.status(404, "{\"error\": \"The content " + contentName + " was not found or does not have any versions yet\"}").build();
			}
			
		} catch (ClassCastException|UnsupportedRepositoryOperationException a) {
			
			return Response.serverError().encoding("utf-8").entity("{" +
					"\"error:\"" + a.getLocalizedMessage() + "\"" +
					"}").build();
		} catch (RepositoryException b) {
			return logErrorAndRespond(b);
		}
		
		
	}
	
	@Path("get/{contentName}/{versionName}")
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
			return logErrorAndRespond(b);
		}
	}
	
	@Path("commit/{contentName}")
	@POST
	@Produces("application/json")
	public Response commitVersion(@PathParam("contentName") String contentName) {
		Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
		Workspace wrkSp = repoSess.getWorkspace();
		LOG.fine("attempting to save");
		try {
			Node root = repoSess.getRootNode();
			LOG.finest("Got root node. Attempting to read request...");
			String json = extractJsonFromRequest(req); 
			LOG.finest("got json... =========" + System.lineSeparator() +json + System.lineSeparator() + "=========" );
			VersionManager vm = wrkSp.getVersionManager();
			Node newContent = null;
			if (root.hasNode(contentName)) {
				LOG.finest("root has node:" + contentName);
				newContent = root.getNode(contentName);
				LOG.finest("got root node.. setting property");
				newContent.setProperty(NODE_CONTENT_PROPERTY_NAME, json);
				LOG.finest("Set content property");
				
			} else {
				//save a new node
				LOG.finest("creating new node with contentName: " + contentName + "....");
				newContent = root.addNode(contentName);
				LOG.finest("adding the mixin...");
				newContent.addMixin("mix:versionable");
				LOG.finest("setting property...");
				newContent.setProperty(NODE_CONTENT_PROPERTY_NAME, json);
				
				
			}
			LOG.finest("saving session...");
			repoSess.save();
			LOG.fine("checking in new content...");
			vm.checkin(newContent.getPath());
			
			return Response.accepted(json).build();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			
			return logErrorAndRespond(e);
		}
		
	}
	
	@PUT
	@Produces("application/json")
	@Path("commit/{contentName}")
	public Response commitVersionPut(@PathParam("contentName") String contentName) {
		return commitVersion(contentName);
	}
	
	private String extractJsonFromRequest(HttpServletRequest req) {
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
	
	private Response logErrorAndRespond(Throwable e) {
		LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		return Response.serverError().encoding("utf-8").entity("{\"error\": {\"" + e.getLocalizedMessage() + "\"}").build();
	}
}
	

	

