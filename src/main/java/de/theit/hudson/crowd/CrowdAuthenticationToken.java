/*
 * @(#)CrowdAuthenticationToken.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.AbstractAuthenticationToken;

/**
 * This class represents an authentication token that is created after a user
 * was successfully authenticated against the remote Crowd server.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 07.09.2011
 * @version $Id$
 */
public class CrowdAuthenticationToken extends AbstractAuthenticationToken {
	/** For serialization. */
	private static final long serialVersionUID = 7685110934682676618L;

	/** The SSO token. */
	private String credentials;

	/** The authenticated Crowd user. */
	private String principal;

	/** The Crowd SSO token after a successful login. */
	private String ssoToken;

	/** The display name of the user. */
	private String displayName;

	/**
	 * Creates a new authorization token.
	 * 
	 * @param pPrincipal
	 *            The name of the authenticated Crowd user. May not be
	 *            <code>null</code>.
	 * @param pCredentials
	 *            The credentials. Normally the users password. May not be
	 *            <code>null</code>.
	 * @param authorities
	 *            The list of granted authorities for the user. May not be
	 *            <code>null</code>.
	 */
	public CrowdAuthenticationToken(String pPrincipal, String pCredentials,
			List<GrantedAuthority> authorities) {
		this(pPrincipal, pCredentials, authorities, null, null);
	}

	/**
	 * Creates a new authorization token.
	 * 
	 * @param pPrincipal
	 *            The name of the authenticated Crowd user. May not be
	 *            <code>null</code>.
	 * @param pCredentials
	 *            The credentials. Normally the users password. May only be
	 *            <code>null</code> when the SSO token is given.
	 * @param authorities
	 *            The list of granted authorities for the user. May not be
	 *            <code>null</code>.
	 * @param pSsoToken
	 *            The Crowd SSO token. May be <code>null</code> if the token is
	 *            not (yet) available.
	 * @param pDisplayName
	 *            The display name of the user. May be <code>null</code>.
	 */
	public CrowdAuthenticationToken(String pPrincipal, String pCredentials,
			List<GrantedAuthority> authorities, String pSsoToken,
			String pDisplayName) {
		super(authorities.toArray(new GrantedAuthority[authorities.size()]));
		this.principal = pPrincipal;
		this.credentials = pCredentials;
		this.ssoToken = pSsoToken;
		this.displayName = pDisplayName;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.Authentication#getCredentials()
	 */
	@Override
	public String getCredentials() {
		return this.credentials;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.Authentication#getPrincipal()
	 */
	@Override
	public String getPrincipal() {
		return this.principal;
	}

	/**
	 * Returns the SSO token.
	 * 
	 * @return The SSO token. <code>null</code> if the token is not (yet)
	 *         available.
	 */
	public String getSSOToken() {
		return this.ssoToken;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.providers.AbstractAuthenticationToken#getName()
	 */
	@Override
	public String getName() {
		if (null == this.displayName) {
			return super.getName();
		}
		return this.displayName;
	}
}
