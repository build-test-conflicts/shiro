/*
 * Copyright (C) 2005-2008 Les Hazlewood
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place, Suite 330
 * Boston, MA 02111-1307
 * USA
 *
 * Or, you may view it online at
 * http://www.opensource.org/licenses/lgpl-license.php
 */
package org.jsecurity.web;

import org.jsecurity.codec.Base64;
import org.jsecurity.crypto.Cipher;
import org.jsecurity.subject.AbstractRememberMeManager;
import org.jsecurity.util.Serializer;
import org.jsecurity.util.ThreadContext;
import org.jsecurity.web.attr.CookieAttribute;
import org.jsecurity.web.attr.WebAttribute;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * @author Les Hazlewood
 * @since 0.9
 */
public class WebRememberMeManager extends AbstractRememberMeManager {

    public static final String DEFAULT_REMEMBER_ME_COOKIE_NAME = "rememberMe";

    protected WebAttribute<String> identityAttribute = null;

    public WebRememberMeManager() {
        super();
    }

    public WebRememberMeManager(Serializer serializer) {
        super(serializer);
    }

    public WebRememberMeManager(Serializer serializer, Cipher cipher) {
        super(serializer, cipher);    
    }

    public WebAttribute<String> getIdentityAttribute() {
        return identityAttribute;
    }

    public void setIdentityAttribute(WebAttribute<String> identityAttribute) {
        this.identityAttribute = identityAttribute;
    }

    protected void onInit() {
        ensureWebAttribute();
    }

    protected void ensureWebAttribute() {
        if (getIdentityAttribute() == null) {
            //uses cookies by default.
            CookieAttribute<String> cookieStore = new CookieAttribute<String>(DEFAULT_REMEMBER_ME_COOKIE_NAME);
            cookieStore.setCheckRequestParams(false);
            setIdentityAttribute(cookieStore);
        }
    }

    protected void rememberSerializedIdentity(byte[] serialized) {
        ServletRequest request = ThreadContext.getServletRequest();
        ServletResponse response = ThreadContext.getServletResponse();
        //base 64 encode it and store as a cookie:
        String base64 = Base64.encodeBase64ToString(serialized);
        getIdentityAttribute().storeValue(base64, request, response);
    }

    protected byte[] getSerializedRememberedIdentity() {
        ServletRequest request = ThreadContext.getServletRequest();
        ServletResponse response = ThreadContext.getServletResponse();
        String base64 = getIdentityAttribute().retrieveValue(request, response);
        if ( base64 != null ) {
            return Base64.decodeBase64( base64 );
        } else {
            //no cookie set - new site visitor?
            return null;
        }
    }

    protected void forgetIdentity() {
        ServletRequest request = ThreadContext.getServletRequest();
        ServletResponse response = ThreadContext.getServletResponse();
        getIdentityAttribute().removeValue( request, response );
    }
}
