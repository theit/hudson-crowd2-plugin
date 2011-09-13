/*
 * @(#)ErrorMessages.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import org.jvnet.localizer.ResourceBundleHolder;

/**
 * This class delivers localized error messages.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 06.09.2011
 * @version $Id$
 */
public class ErrorMessages {
	/** Contains the localized messages. */
	private final static ResourceBundleHolder holder = ResourceBundleHolder
			.get(ErrorMessages.class);

	/**
	 * Returns the localized error message when no URL is specified.
	 * 
	 * @return The localized error message for a missing Crowd URL.
	 */
	public static String specifyCrowdUrl() {
		return holder.format("pleaseSpecifyCrowdUrl");
	}

	/**
	 * Returns the localized error message when no application name is given.
	 * 
	 * @return The localized error message for a missing application name.
	 */
	public static String specifyApplicationName() {
		return holder.format("specifyApplicationName");
	}

	/**
	 * Returns the localized error message when no application password is
	 * given.
	 * 
	 * @return The localized error message for a missing application password.
	 */
	public static String specifyApplicationPassword() {
		return holder.format("specifyApplicationPassword");
	}

	/**
	 * Returns the localized error message when no group name is given.
	 * 
	 * @return The localized error message for a missing group name.
	 */
	public static String specifyGroup() {
		return holder.format("specifyGroup");
	}

	/**
	 * Returns the localized error message when the connection check failed.
	 * 
	 * @return The localized error message for a failed connection check.
	 */
	public static String operationFailed() {
		return holder.format("operationFailed");
	}

	/**
	 * Returns the localized error message when the configuration file
	 * crowd.properties cannot be loaded.
	 * 
	 * @return The localized error message when the configuration file
	 *         crowd.properties cannot be loaded.
	 */
	public static String cannotLoadCrowdProperties() {
		return holder.format("cannotLoadCrowdProperties");
	}

	/**
	 * Returns the localized error message text when the application name and
	 * password are not valid.
	 * 
	 * @return The localized error message for invalid application name and
	 *         password.
	 */
	public static String invalidAuthentication() {
		return holder.format("invalidAuthentication");
	}

	/**
	 * Returns the localized error message when the application has no
	 * permission to perform a connection check to the Crowd server.
	 * 
	 * @return The localized error message for missing permission to perform a
	 *         connection check to the Crowd server.
	 */
	public static String applicationPermission() {
		return holder.format("applicationPermission");
	}

	/**
	 * Returns the localized error message when the user was not found on the
	 * remote Crowd server.
	 * 
	 * @return The localized error message when the user was not found on the
	 *         remote Crowd server.
	 */
	public static String userNotFound() {
		return holder.format("userNotFound");
	}

	/**
	 * Returns the localized error message when the group was not found on the
	 * remote Crowd server.
	 * 
	 * @return The localized error message when the group was not found on the
	 *         remote Crowd server.
	 */
	public static String groupNotFound() {
		return holder.format("groupNotFound");
	}

	/**
	 * Returns the localized error message when the specified group does not
	 * exist on the remote Crowd server or is not active.
	 * 
	 * @return The localized error message when the specified group cannot be
	 *         validated against the remote Crowd server.
	 */
	public static String cannotValidateGroup() {
		return holder.format("cannotValidateGroup");
	}

	/**
	 * Returns the localized error message when the user password has expired
	 * and must be changed.
	 * 
	 * @return The localized error message when the user password has expired.
	 */
	public static String expiredCredentials() {
		return holder.format("expiredCredentials");
	}

	/**
	 * Returns the localized error message when the account is inactive.
	 * 
	 * @return The localized error message when the account is inactive.
	 */
	public static String accountExpired() {
		return holder.format("accountExpired");
	}

	/**
	 * Returns the localized error message when an invalid SSO token was found.
	 * 
	 * @return The localized error message for an invalid SSO token.
	 */
	public static String invalidToken() {
		return holder.format("invalidToken");
	}

	/**
	 * Returns the localized error message when a user does not have access to
	 * authenticate against an application.
	 * 
	 * @return The localized error message for denied application access.
	 */
	public static String applicationAccessDenied() {
		return holder.format("applicationAccessDenied");
	}

	/**
	 * Returns the localized error message when the group of users that are
	 * allowed to login into Hudson does not exist or is not active.
	 * 
	 * @return The localized error message for a non-existing or non-active
	 *         group.
	 */
	public static String hudsonUserGroupNotFound() {
		return holder.format("hudsonUserGroupNotFound");
	}

	/**
	 * Returns the localized error message when a user does not have the
	 * permission to login into Hudson.
	 * 
	 * @return The localized error message when a user does not have the
	 *         permission to login into Hudson.
	 */
	public static String hudsonUserNotValid() {
		return holder.format("hudsonUserNotValid");
	}
}
