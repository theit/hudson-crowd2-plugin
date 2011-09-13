/*
 * @(#)CrowdMailAddressResolverImpl.java
 * Copyright (C)2011 Thorsten Heit.
 * All rights reserved.
 */
package de.theit.hudson.crowd;

import hudson.Extension;
import hudson.model.Hudson;
import hudson.model.User;
import hudson.security.SecurityRealm;
import hudson.tasks.MailAddressResolver;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.springframework.dao.DataAccessException;

/**
 * This class resolves email addresses via lookup in Crowd.
 * 
 * @author <a href="mailto:theit@gmx.de">Thorsten Heit (theit@gmx.de)</a>
 * @since 08.09.2011
 * @version $Id$
 */
@Extension
public class CrowdMailAddressResolverImpl extends MailAddressResolver {
	/** For logging purposes. */
	private static final Logger LOG = Logger
			.getLogger(CrowdMailAddressResolverImpl.class.getName());

	/**
	 * {@inheritDoc}
	 * 
	 * @see hudson.tasks.MailAddressResolver#findMailAddressFor(hudson.model.User)
	 */
	@Override
	public String findMailAddressFor(User u) {
		String mail = null;
		SecurityRealm realm = Hudson.getInstance().getSecurityRealm();

		if (realm instanceof CrowdSecurityRealm) {
			try {
				CrowdUser details = (CrowdUser) realm.getSecurityComponents().userDetails
						.loadUserByUsername(u.getId());
				mail = details.getEmailAddress();
			} catch (UsernameNotFoundException ex) {
				LOG.log(Level.INFO, "Failed to look up email address in Crowd",
						ex);
			} catch (DataAccessException ex) {
				LOG.log(Level.SEVERE,
						"Access exception trying to look up email address in Crowd",
						ex);
			}
		}

		return mail;
	}
}
