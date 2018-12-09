package com.seefurst.vaas.utils;

import java.util.Map;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.oak.jcr.OakRepositoryFactory;
import java.net.URLEncoder;
import java.io.IOException;

import java.util.logging.Logger;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_FILEPATH;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_USERNAME;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_PASSWORD;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;


public class VaasRepoFilter implements Filter {
	private static final Logger LOG = Logger.getLogger(VaasRepoFilter.class.getName());
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException {
		Session repoSess = null;
		HttpServletRequest httpReq = (HttpServletRequest) req;
		HttpServletResponse httpRes = (HttpServletResponse) res;
		try {
			String repositoryUrl =  req.getServletContext().getRealPath("/vaas");
			LOG.finest("got repourl: " + repositoryUrl);
			Map<String, String> repoOptions = new HashMap<>();
			//repoOptions.put("repository.home", repositoryUrl);
			LOG.finest("getting repo with null map...");
			Repository repo = (new OakRepositoryFactory()).getRepository(null);
			LOG.fine("Logging in...");
			repoSess = repo.login(new SimpleCredentials(REPOSITORY_USERNAME,REPOSITORY_PASSWORD.toCharArray()));
			LOG.fine("done.. setting attribute...");
			httpReq.setAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME, repoSess);
			LOG.finest("donig filter...");
			chain.doFilter(req, res);
			LOG.finest("done filter... no errors...");
		} catch (ClassCastException|ServletException|IOException|RepositoryException a){
			
			LOG.log(java.util.logging.Level.SEVERE, "Couldn't log into repository", a);
			httpRes.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Couldn't login to responsitory");
			
		} finally {
			
			LOG.fine("logging out..");
			if (repoSess != null) {
				LOG.finest("repoSess is not null and therefore calling logout()");
				repoSess.logout();
			}
		}
	}
}
