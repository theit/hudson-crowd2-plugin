/*
 * @(#)CrowdServletFilter.java
 * Copyright (C)2011 Thorsten Heit
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import static de.theit.hudson.crowd.ErrorMessages.operationFailed;
import static org.acegisecurity.ui.rememberme.TokenBasedRememberMeServices.ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY;
import hudson.EnvVars;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContext;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.ui.rememberme.RememberMeServices;

import com.atlassian.crowd.exception.OperationFailedException;

/**
 * This class realizes a servlet filter that checks on each request the status
 * of the SSO session. If the session isn't valid anymore, the user is logged
 * out automatically, and vice-versa: If there's a SSO session but the user
 * isn't logged in into Hudson, (s)he is automatically logged in.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 09.09.2011
 * @version $Id$
 */
public class CrowdServletFilter implements Filter {
	/** Used for logging purposes. */
	private static final Logger LOG = Logger.getLogger(CrowdServletFilter.class
			.getName());

	/**
	 * The configuration data necessary for accessing the services on the remote
	 * Crowd server.
	 */
	private CrowdConfigurationService configuration;

	/** The default servlet filter. */
	private Filter defaultFilter;

	/**
	 * The Crowd security realm. Used for logging out users when the SSO session
	 * isn't valid anymore.
	 */
	private CrowdSecurityRealm securityRealm;

	/** Holds the {@link RememberMeServices} that is used for auto-login. */
	private CrowdRememberMeServices rememberMe;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param pSecurityRealm
	 *            The Crowd security realm. Necessary for logging out users when
	 *            the SSO session isn't valid anymore. May not be
	 *            <code>null</code>.
	 * @param pConfiguration
	 *            The configuration to access the services on the remote Crowd
	 *            server. May not be <code>null</code>.
	 * @param pDefaultFilter
	 *            The default filter to use when the Crowd security filter is
	 *            not used during runtime. May not be <code>null</code>.
	 */
	public CrowdServletFilter(CrowdSecurityRealm pSecurityRealm,
			CrowdConfigurationService pConfiguration, Filter pDefaultFilter) {
		this.securityRealm = pSecurityRealm;
		this.configuration = pConfiguration;
		this.defaultFilter = pDefaultFilter;

		if (this.securityRealm.getSecurityComponents().rememberMe instanceof CrowdRememberMeServices) {
			this.rememberMe = (CrowdRememberMeServices) this.securityRealm
					.getSecurityComponents().rememberMe;
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.defaultFilter.init(filterConfig);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		// check whether the SSO session is valid

		if (request instanceof HttpServletRequest
				&& response instanceof HttpServletResponse) {
			HttpServletRequest req = (HttpServletRequest) request;
			HttpServletResponse res = (HttpServletResponse) response;

			// if the user is logged in, check whether the session is still
			// valid
			try {
				SecurityContext sc = SecurityContextHolder.getContext();
				boolean isValidated = this.configuration.crowdHttpAuthenticator
						.isAuthenticated(req, res);
				if (!isValidated
						&& sc.getAuthentication() instanceof CrowdAuthenticationToken) {
					// close the SSO session
					if (null != this.rememberMe) {
						this.rememberMe.logout(req, res);
					}

					// invalidate the current session
					// (see SecurityRealm#doLogout())
					HttpSession session = req.getSession(false);
					if (session != null) {
						session.invalidate();
					}
					SecurityContextHolder.clearContext();

					// Clear env property.
					EnvVars.clearHudsonUserEnvVar();

					// reset remember-me cookie
					Cookie cookie = new Cookie(
							ACEGI_SECURITY_HASHED_REMEMBER_ME_COOKIE_KEY, "");
					cookie.setPath(req.getContextPath().length() > 0 ? req
							.getContextPath() : "/");
					res.addCookie(cookie);
				} else if (!(sc.getAuthentication() instanceof CrowdAuthenticationToken)) {
					// user not logged in via Crowd
					// => try to auto-login the user
					if (null != this.rememberMe) {
						Authentication auth = this.rememberMe.autoLogin(req,
								res);
						if (null != auth) {
							sc.setAuthentication(auth);
							// invalidate the current session
							HttpSession session = req.getSession(false);
							if (session != null) {
								session.invalidate();
							}
						}
					}
				}
			} catch (OperationFailedException ex) {
				LOG.log(Level.SEVERE, operationFailed(), ex);
			}

		}

		this.defaultFilter.doFilter(request, response, chain);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		this.defaultFilter.destroy();
	}
}
