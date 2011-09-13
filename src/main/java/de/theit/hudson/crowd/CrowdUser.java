/*
 * @(#)CrowdUser.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import java.util.List;

import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.userdetails.UserDetails;

import com.atlassian.crowd.model.user.User;

/**
 * This class provides the information about a user that was authenticated
 * successfully against a remote Crowd server.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 07.09.2011
 * @version $Id$
 */
public class CrowdUser implements UserDetails {
	/** Necessary for serialisation. */
	private static final long serialVersionUID = -907996070755427899L;

	/** Stores the granted authorities. */
	private List<GrantedAuthority> grantedAuthorities;

	/** Holds the Crowd user object. */
	private User user;

	/**
	 * Creates a new instance.
	 * 
	 * @param pUser
	 *            The user object. May not be <code>null</code>.
	 * @param authorities
	 *            The granted authorities of the user. May not be
	 *            <code>null</code>.
	 */
	public CrowdUser(User pUser, List<GrantedAuthority> authorities) {
		this.grantedAuthorities = authorities;
		this.user = pUser;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#getAuthorities()
	 */
	@Override
	public GrantedAuthority[] getAuthorities() {
		return this.grantedAuthorities
				.toArray(new GrantedAuthority[this.grantedAuthorities.size()]);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#getPassword()
	 */
	@Override
	public String getPassword() {
		throw new UnsupportedOperationException("Not giving you the password");
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#getUsername()
	 */
	@Override
	public String getUsername() {
		return this.user.getName();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isAccountNonExpired()
	 */
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isAccountNonLocked()
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.acegisecurity.userdetails.UserDetails#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return this.user.isActive();
	}

	/**
	 * Returns the users email address.
	 * 
	 * @return The users email address.
	 */
	public String getEmailAddress() {
		return this.user.getEmailAddress();
	}
}
