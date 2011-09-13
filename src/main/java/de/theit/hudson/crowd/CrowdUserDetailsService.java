/*
 * @(#)CrowdUserDetailsService.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import static de.theit.hudson.crowd.ErrorMessages.applicationPermission;
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

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.model.user.User;

/**
 * This class provides the service to load a user object from the remote Crowd
 * server.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 07.09.2011
 * @version $Id$
 */
public class CrowdUserDetailsService implements UserDetailsService {
	/** Used for logging purposes. */
	private static final Logger LOG = Logger
			.getLogger(CrowdUserDetailsService.class.getName());

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
	public CrowdUserDetailsService(CrowdConfigurationService pConfiguration) {
		this.configuration = pConfiguration;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetailsService#loadUserByUsername(java.lang.String)
	 */
	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		// check whether the Hudson user group in Crowd exists and is active
		if (!this.configuration.isGroupActive()) {
			throw new DataRetrievalFailureException(hudsonUserGroupNotFound());
		}

		if (!this.configuration.isGroupMember(username)) {
			throw new DataRetrievalFailureException(hudsonUserNotValid());
		}

		User user;
		try {
			// load the user object from the remote Crowd server
			user = this.configuration.crowdClient.getUser(username);
		} catch (UserNotFoundException ex) {
			LOG.log(Level.INFO, userNotFound(), ex);
			throw new UsernameNotFoundException(userNotFound(), ex);
		} catch (ApplicationPermissionException ex) {
			LOG.log(Level.WARNING, applicationPermission(), ex);
			throw new DataRetrievalFailureException(applicationPermission(), ex);
		} catch (InvalidAuthenticationException ex) {
			LOG.log(Level.WARNING, invalidAuthentication(), ex);
			throw new DataRetrievalFailureException(invalidAuthentication(), ex);
		} catch (OperationFailedException ex) {
			LOG.log(Level.SEVERE, operationFailed(), ex);
			throw new DataRetrievalFailureException(operationFailed(), ex);
		}

		// create the list of granted authorities
		List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		// add the "authenticated" authority to the list of granted
		// authorities...
		authorities.add(SecurityRealm.AUTHENTICATED_AUTHORITY);
		// ..and all authorities retrieved from the Crowd server
		authorities.addAll(this.configuration.getAuthoritiesForUser(username));

		return new CrowdUser(user, authorities);
	}
}
