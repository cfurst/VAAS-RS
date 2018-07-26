package com.seefurst.vaas.utils;

import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;


import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;

import org.apache.jackrabbit.commons.JcrUtils;
import java.io.IOException;

import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_FILEPATH;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_USERNAME;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_PASSWORD;
import static com.seefurst.vaas.utils.VaasConstants.REPOSITORY_SESSION_SERVLET_ATTRB_NAME;


public class VaasRepoFilter implements Filter {
	
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) {
		Session repoSess = null;
		try {
			HttpServletRequest httpReq = (HttpServletRequest) req;
			Repository repo = JcrUtils.getRepository("file://" + REPOSITORY_FILEPATH);
			repoSess = repo.login(new SimpleCredentials(REPOSITORY_USERNAME,REPOSITORY_PASSWORD.toCharArray()));
			
			httpReq.setAttribute(REPOSITORY_SESSION_SERVLET_ATTRB_NAME, repoSess);
			
			chain.doFilter(req, res);
			
		} catch (ClassCastException|ServletException|IOException|RepositoryException a){
			System.err.println("Couldn't login to repository : " + a.getMessage());
			a.printStackTrace(System.err);
		} finally {
			if (repoSess != null)
				repoSess.logout();
			
		}
	}
}
