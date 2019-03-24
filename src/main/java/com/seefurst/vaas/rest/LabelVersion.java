/**
 * 
 */
package com.seefurst.vaas.rest;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionManager;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;


import static com.seefurst.vaas.utils.VaasConstants.UTF8;
import static com.seefurst.vaas.utils.VaasConstants.NODE_CONTENT_PROPERTY_NAME;

import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;


/**
 * @author Carl Yamamoto-Furst
 *
 */
@Path("content/label")
public class LabelVersion implements VaasRestBase {

	private static final Logger LOG = Logger.getLogger(LabelVersion.class.getName());
	
	@Context
	private HttpServletRequest req;
	
	@Path("add/{contentName}/{version}/{labelName}")
	@GET
	@Produces("application/json")
	public Response setLabel(@PathParam("contentName") String contentName, @PathParam("version") String version, @PathParam("labelName") String labelName) {
		try {
			Session repoSess = getSession(req);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm;
			vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
			
			if (vh.hasVersionLabel(labelName)) {
					return Response.status(Status.BAD_REQUEST).encoding(UTF8).entity("{\"error\": \"the Label: " + labelName + " is already assigned to a version for " + contentName + "\"}").build();
			}
			else {
				vh.addVersionLabel(version, labelName, false);
				return Response.ok("{\"success\": \"The label " + labelName +" was added to version "+ version + " of " + contentName + ".\"}").build();
			}
		} catch (RepositoryException e) {
			return Response.serverError().encoding(UTF8).entity("{\"error\": \"Repository error encountered: " + e.getMessage() + "\"}").build();
		}
		
	}
	
	@Path("remove/{contentName}/{labelName}")
	@GET
	@Produces("application/json")
	public Response removeLabel(@PathParam("contentName") String contentName, @PathParam("labelName") String labelName) {
		try {
			Session repoSess = getSession(req);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				
				VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
				if (vh.hasVersionLabel(labelName)) {
					vh.removeVersionLabel(labelName);
					return Response.ok().encoding(UTF8).entity("{\"success\": \"Version Label: " + labelName + " was removed from " + contentName).build();
				} else {
					return Response.status(Status.BAD_REQUEST).encoding(UTF8).entity("{\"error\": \"The label: " + labelName +" doesn't exist for " + contentName + "\"}").build();
				}
			} else {
				return Response.status(Status.NOT_FOUND).encoding(UTF8).entity("{\"error\":\"" + contentName + " is not found\"}").build();
			}
			
		} catch (RepositoryException a) {
			return logErrorAndRespond(a);
		}
	}
	/**
	 * TODO: Maybe consolidate this with add-label above and just move it by default?
	 * @param contentName
	 * @param destVersion
	 * @param labelName
	 * @return
	 */
	@Path("move/{contentName}/{toVersion}/{labelName}")
	@GET
	@Produces("application/json")
	public Response moveLabel(@PathParam("contentName") String contentName, @PathParam("toVersion") String destVersion, @PathParam("labelName") String labelName) {
		try {
			Session repoSess = getSession(req);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				
				VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
				vh.addVersionLabel(destVersion, labelName, true);
				return Response.ok("{\"success\": \"The label " + labelName +" was moved to version "+ destVersion + " of " + contentName + ".\"}").build();
				
			} else {
				return Response.status(Status.NOT_FOUND).encoding(UTF8).entity("{\"error\":\"" + contentName + " is not found\"}").build();
			}
		} catch (RepositoryException a) {
			return logErrorAndRespond(a);
		}
	}
	
	@Path("get/{contentName}/{labelName}")
	@GET
	@Produces("application/json")
	public Response getByLabel(@PathParam("contentName") String contentName, @PathParam("labelName") String labelName) {
		try {
			Session repoSess = getSession(req);
			Workspace wrkSp = repoSess.getWorkspace();
			VersionManager vm = wrkSp.getVersionManager();
			Node root = repoSess.getRootNode();
			if (root.hasNode(contentName)) {
				
				VersionHistory vh = vm.getVersionHistory(root.getNode(contentName).getPath());
				if (vh.hasVersionLabel(labelName)) {
					Version v = vh.getVersionByLabel(labelName);
					return Response.ok(v.getFrozenNode().getProperty(NODE_CONTENT_PROPERTY_NAME)).build();
				} else {
					return Response.status(Status.NOT_FOUND).encoding(UTF8).entity("{\"error\":\"" + labelName + " for content name: " + contentName + " was not found. Check the label and try again.\"}").build();
				}
				
				
			} else {
				return Response.status(Status.NOT_FOUND).encoding(UTF8).entity("{\"error\":\"" + contentName + " was not found\"}").build();
			}
		}
		catch (RepositoryException a) {
			return logErrorAndRespond(a);
		}
	}
}
