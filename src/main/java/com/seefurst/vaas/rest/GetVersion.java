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
import javax.jcr.version.VersionIterator;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.Node;
import javax.jcr.Workspace;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.Arrays;
import java.util.logging.Level;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;
import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;

import org.json.JSONObject;
import org.json.JSONArray;

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
			LOG.finest("got workspace... name..." + wrkSp.getName());
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
	
	@Path("get/{contentName}/versionList")
	@GET
	@Produces("application/json")
	public Response getVersionList(@PathParam("contentName") String contentName) {
		try {
			Session repoSess = (Session) req.getAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
			VersionIterator vi = vh.getAllVersions();
			Version v = null;
			JSONObject versionObject = new JSONObject();
			while (vi.hasNext()) {
				v = vi.nextVersion();
				if (v.getName().equals("jcr:rootVersion"))
					continue;
				String[] labels = vh.getVersionLabels(v);
				JSONArray labelJsonArray = new JSONArray();
				Arrays.stream(labels).forEach(label -> {
					labelJsonArray.put(label);
				});
				versionObject.put(v.getName(), labelJsonArray);
			}
		
			return Response.ok(versionObject.toString()).encoding("utf-8").build();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			return logErrorAndRespond(e);
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
				return Response.ok(f.getProperty(NODE_CONTENT_PROPERTY_NAME).getString()).encoding("utf-8").build();
			} else {
				return Response.status(404, "{\"error\": \"The content " + contentName + " was not found\"}").build();
			}
			
		} catch(VersionException a) {
			return Response.status(404, "{\"error\": \"The content " + contentName + " does not have version " + versionName + "\"}").encoding("utf-8").build();
		}catch (RepositoryException b) {
			return logErrorAndRespond(b);
		}
	}
	
	
	
	/**
	 * TODO: move this to base interface as default and public.
	 * @param e - Error to log
	 * @return - server error response.
	 */
	private Response logErrorAndRespond(Throwable e) {
		LOG.log(Level.SEVERE, e.getLocalizedMessage(), e);
		return Response.serverError().encoding("utf-8").entity("{\"error\": {\"" + e.getLocalizedMessage() + "\"}").build();
	}
}
	

	

