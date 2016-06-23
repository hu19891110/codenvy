/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.auth.sso.client.token;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

/** Extract sso token from request headers. */
public class HeaderRequestTokenExtractor implements RequestTokenExtractor {
    @Override
    public String getToken(HttpServletRequest req) {
        return req.getHeader(HttpHeaders.AUTHORIZATION);
    }

}
