/*
 * Copyright (c) 2005, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.s1asdev.deployment.ejb30.ear.security;

import java.io.IOException;

import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

@DeclareRoles({ "j2ee", "nobody", "sunuser" })
public class MyFilter implements Filter {

    private @Resource(mappedName="jdbc/__default") DataSource ds1;

    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext sc = filterConfig.getServletContext();
    
        try {

            int loginTimeout = ds1.getLoginTimeout();
            sc.log("ds1-login-timeout=" + loginTimeout);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void doFilter (ServletRequest request,
                          ServletResponse response,
                          FilterChain chain)
            throws IOException, ServletException {
        int loginTimeout = 0;
        try {
            loginTimeout = ds1.getLoginTimeout();
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IOException(ex.toString());
        }
        HttpServletRequest httpRequest = (HttpServletRequest)request;
        HttpSession httpSession = httpRequest.getSession(true);
        if (httpRequest.isUserInRole("j2ee")) {
            httpSession.putValue("deployment.ejb30.ear.security",
                "filterMessage=hello world: " + loginTimeout);

        }
        chain.doFilter(request, response);
    }

    public void destroy() {
        // do nothing
    }
}
