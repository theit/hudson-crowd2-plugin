/*
 * @(#)CrowdAuthenticationManager.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import static de.theit.hudson.crowd.ErrorMessages.accountExpired;
import static de.theit.hudson.crowd.ErrorMessages.applicationPermission;
import static de.theit.hudson.crowd.ErrorMessages.expiredCredentials;
import static de.theit.hudson.crowd.ErrorMessages.hudsonUserGroupNotFound;
import static de.theit.hudson.crowd.ErrorMessages.hudsonUserNotValid;
import static de.theit.hudson.crowd.ErrorMessages.invalidAuthentication;
import static de.theit.hudson.crowd.ErrorMessages.operationFailed;
import static de.theit.hudson.crowd.ErrorMessages.userNotFound;
import hudson.security.SecurityRealm;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.AccountExpiredException;
import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.AuthenticationServiceException;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.CredentialsExpiredException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.InsufficientAuthenticationException;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.ExpiredCredentialException;
import com.atlassian.crowd.exception.InactiveAccountException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;

/**
 * This class implements the authentication manager for Hudson.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 07.09.2011
 * @version $Id$
 */
public class CrowdAuthenticationManager implements AuthenticationManager {
	/** Used for logging purposes. */
	private static final Logger LOG = Logger
			.getLogger(CrowdAuthenticationManager.class.getName());

	/**
	 * The configuration data necessary for accessing the services on the remote
	 * Crowd server.
	 */
	private CrowdConfigurationService configuration;

	/**
	 * Creates a new instance of this class.
	 * 
	 * @param pConfiguration
	 *            The configuration to access the services on the remote Crowd
	 *            server. May not be <code>null</code>.
	 */
	public CrowdAuthenticationManager(CrowdConfigurationService pConfiguration) {
		this.configuration = pConfiguration;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.AuthenticationManager#authenticate(org.acegisecurity.Authentication)
	 */
	@Override
	public Authentication authenticate(Authentication authentication)
			throws AuthenticationException {
		String username = authentication.getPrincipal().toString();

		// checking whether there's already a SSO token
		if (null == authentication.getCredentials()
				&& authentication instanceof CrowdAuthenticationToken
				&& null != ((CrowdAuthenticationToken) authentication)
						.getSSOToken()) {
			// SSO token available => user already authenticated
			return authentication;
		}

		String password = authentication.getCredentials().toString();

		// ensure that the group is available, active and that the user
		// is a member of it
		if (!this.configuration.isGroupActive()) {
			throw new InsufficientAuthenticationException(
					hudsonUserGroupNotFound());
		}

		if (!this.configuration.isGroupMember(username)) {
			throw new InsufficientAuthenticationException(hudsonUserNotValid());
		}

		try {
			// authenticate user
			this.configuration.crowdClient.authenticateUser(username, password);
		} catch (UserNotFoundException ex) {
			LOG.log(Level.INFO, userNotFound(), ex);
			throw new BadCredentialsException(userNotFound(), ex);
		} catch (ExpiredCredentialException ex) {
			LOG.log(Level.WARNING, expiredCredentials(), ex);
			throw new CredentialsExpiredException(expiredCredentials(), ex);
		} catch (InactiveAccountException ex) {
			LOG.log(Level.WARNING, accountExpired(), ex);
			throw new AccountExpiredException(accountExpired(), ex);
		} catch (ApplicationPermissionException ex) {
			LOG.log(Level.WARNING, applicationPermission(), ex);
			throw new AuthenticationServiceException(applicationPermission(),
					ex);
		} catch (InvalidAuthenticationException ex) {
			LOG.log(Level.WARNING, invalidAuthentication(), ex);
			throw new AuthenticationServiceException(invalidAuthentication(),
					ex);
		} catch (OperationFailedException ex) {
			LOG.log(Level.SEVERE, operationFailed(), ex);
			throw new AuthenticationServiceException(operationFailed(), ex);
		}

		// user successfully authenticated
		// => retrieve the list of groups the user is a member of
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

		// add the "authenticated" authority to the list of granted
		// authorities...
		authorities.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
		// ..and finally all authorities retrieved from the Crowd server
		authorities.addAll(this.configuration.getAuthoritiesForUser(username));

		// user successfully authenticated => create authentication
		// token
		return new CrowdAuthenticationToken(username, password, authorities);
	}
}
