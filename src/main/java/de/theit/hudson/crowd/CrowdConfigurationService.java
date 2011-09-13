/*
 * @(#)CrowdConfigurationService.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import static de.theit.hudson.crowd.ErrorMessages.applicationPermission;
import static de.theit.hudson.crowd.ErrorMessages.groupNotFound;
import static de.theit.hudson.crowd.ErrorMessages.invalidAuthentication;
import static de.theit.hudson.crowd.ErrorMessages.operationFailed;
import static de.theit.hudson.crowd.ErrorMessages.specifyGroup;
import static de.theit.hudson.crowd.ErrorMessages.userNotFound;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.GrantedAuthorityImpl;

import com.atlassian.crowd.exception.ApplicationPermissionException;
import com.atlassian.crowd.exception.GroupNotFoundException;
import com.atlassian.crowd.exception.InvalidAuthenticationException;
import com.atlassian.crowd.exception.OperationFailedException;
import com.atlassian.crowd.exception.UserNotFoundException;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelper;
import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;

/**
 * This class contains all objects that are necessary to access the REST
 * services on the remote Crowd server. In addition to this it contains some
 * helper methods
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 08.09.2011
 * @version $Id$
 */
public class CrowdConfigurationService {
	/** Used for logging purposes. */
	private static final Logger LOG = Logger
			.getLogger(CrowdConfigurationService.class.getName());

	/**
	 * The maximum number of groups that can be fetched from the Crowd server
	 * for a user in one request.
	 */
	private static final int MAX_GROUPS = 500;

	/** Holds the Crowd client properties. */
	ClientProperties clientProperties;

	/** The Crowd client to access the REST services on the remote Crowd server. */
	CrowdClient crowdClient;

	/** The helper class for Crowd SSO token operations. */
	CrowdHttpTokenHelper tokenHelper;

	/**
	 * The interface used to manage HTTP authentication and web/SSO
	 * authentication integration.
	 */
	CrowdHttpAuthenticator crowdHttpAuthenticator;

	/** The group name a user must belong to to be allowed to login into Hudson. */
	private String groupName;

	/** Specifies whether nested groups may be used. */
	private boolean nestedGroups;

	/**
	 * Creates a new Crowd configuration object.
	 * 
	 * @param pGroupName
	 *            The group name to use when authenticating Crowd users. May not
	 *            be <code>null</code>.
	 * @param pNestedGroups
	 *            Specifies whether nested groups should be used when validating
	 *            users against the group name.
	 */
	public CrowdConfigurationService(String pGroupName, boolean pNestedGroups) {
		this.groupName = pGroupName.trim();
		if (0 == this.groupName.length()) {
			throw new IllegalArgumentException(specifyGroup());
		}

		this.nestedGroups = pNestedGroups;
	}

	/**
	 * Checks whether the user is a member of a certain Crowd group whose
	 * members are allowed to login into Hudson.
	 * 
	 * @param username
	 *            The name of the user to check. May not be <code>null</code>.
	 * @return <code>true</code> if and only if the group exists, is active and
	 *         the user is either a direct group member or, if nested groups may
	 *         be used, a nested group member. <code>false</code> else.
	 */
	public boolean isGroupMember(String username) {
		boolean retval = false;

		try {
			if (this.crowdClient.isUserDirectGroupMember(username,
					this.groupName)) {
				retval = true;
			} else if (this.nestedGroups
					&& this.crowdClient.isUserNestedGroupMember(username,
							this.groupName)) {
				retval = true;
			}
		} catch (ApplicationPermissionException ex) {
			LOG.log(Level.WARNING, applicationPermission(), ex);
		} catch (InvalidAuthenticationException ex) {
			LOG.log(Level.WARNING, invalidAuthentication(), ex);
		} catch (OperationFailedException ex) {
			LOG.log(Level.SEVERE, operationFailed(), ex);
		}

		return retval;
	}

	/**
	 * Checks if the group exists on the remote Crowd server and is active.
	 * 
	 * @return <code>true</code> if and only if:
	 *         <ul>
	 *         <li>The group name is empty or</li>
	 *         <li>The group name is not empty, does exist on the remote Crowd
	 *         server and is active.</li>
	 *         </ul>
	 *         <code>false</code> else.
	 */
	public boolean isGroupActive() {
		boolean retval = false;
		try {
			Group group = this.crowdClient.getGroup(this.groupName);
			if (null != group) {
				retval = group.isActive();
			}
		} catch (GroupNotFoundException ex) {
			LOG.log(Level.INFO, groupNotFound(), ex);
		} catch (InvalidAuthenticationException ex) {
			LOG.log(Level.WARNING, invalidAuthentication(), ex);
		} catch (ApplicationPermissionException ex) {
			LOG.log(Level.WARNING, applicationPermission(), ex);
		} catch (OperationFailedException ex) {
			LOG.log(Level.SEVERE, operationFailed(), ex);
		}

		return retval;
	}

	/**
	 * Retrieves the list of all (nested) groups from the Crowd server that the
	 * user is a member of.
	 * 
	 * @param username
	 *            The name of the user. May not be <code>null</code>.
	 * @return The list of all groups that the user is a member of. Always
	 *         non-null.
	 */
	public Collection<GrantedAuthority> getAuthoritiesForUser(String username) {
		Collection<GrantedAuthority> authorities = new TreeSet<GrantedAuthority>(
				new Comparator<GrantedAuthority>() {
					@Override
					public int compare(GrantedAuthority ga1,
							GrantedAuthority ga2) {
						return ga1.getAuthority().compareTo(ga2.getAuthority());
					}
				});

		HashSet<String> groupNames = new HashSet<String>();

		// load the names of all groups the user is a direct member of
		try {
			int index = 0;
			while (true) {
				List<Group> groups = this.crowdClient.getGroupsForUser(
						username, index, MAX_GROUPS);
				if (null == groups || groups.isEmpty()) {
					break;
				}
				for (Group group : groups) {
					if (group.isActive()) {
						groupNames.add(group.getName());
					}
				}
				index += MAX_GROUPS;
			}
		} catch (UserNotFoundException ex) {
			LOG.log(Level.INFO, userNotFound(), ex);
		} catch (InvalidAuthenticationException ex) {
			LOG.log(Level.WARNING, invalidAuthentication(), ex);
		} catch (ApplicationPermissionException ex) {
			LOG.log(Level.WARNING, applicationPermission(), ex);
		} catch (OperationFailedException ex) {
			LOG.log(Level.SEVERE, operationFailed(), ex);
		}

		// load the names of all groups the user is a nester member of
		if (this.nestedGroups) {
			try {
				int index = 0;
				while (true) {
					List<Group> groups = this.crowdClient
							.getGroupsForNestedUser(username, index, MAX_GROUPS);
					if (null == groups || groups.isEmpty()) {
						break;
					}
					for (Group group : groups) {
						if (group.isActive()) {
							groupNames.add(group.getName());
						}
					}
					index += MAX_GROUPS;
				}
			} catch (UserNotFoundException ex) {
				LOG.log(Level.INFO, userNotFound(), ex);
			} catch (InvalidAuthenticationException ex) {
				LOG.log(Level.WARNING, invalidAuthentication(), ex);
			} catch (ApplicationPermissionException ex) {
				LOG.log(Level.WARNING, applicationPermission(), ex);
			} catch (OperationFailedException ex) {
				LOG.log(Level.SEVERE, operationFailed(), ex);
			}
		}

		// now create the list of authorities
		for (String str : groupNames) {
			authorities.add(new GrantedAuthorityImpl(str));
		}

		return authorities;
	}
}
