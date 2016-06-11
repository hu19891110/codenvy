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
package com.codenvy.im.service;

import com.codenvy.api.permission.server.SystemDomain;

import org.eclipse.che.api.core.ApiException;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.everrest.CheMethodInvokerFilter;
import org.everrest.core.Filter;
import org.everrest.core.resource.GenericMethodResource;

import javax.ws.rs.Path;

import static com.codenvy.api.permission.server.SystemDomain.MANAGE_CODENVY_ACTION;

/**
 * Filter that covers calls to {@link LicenseService} and {@link InstallationManagerService} with authorization
 *
 * @author Sergii Leschenko
 */
@Filter
@Path("/{serviceName:[(license)(im)]}{path:.*}")
public class InstallationManagerPermissionsFilter extends CheMethodInvokerFilter {
    @Override
    protected void filter(GenericMethodResource genericMethodResource, Object[] arguments) throws ApiException {
        if (genericMethodResource.getMethod().getName().equals("isCodenvyUsageLegal")) {
            //public method
            return;
        }

        EnvironmentContext.getCurrent().getSubject().checkPermission(SystemDomain.DOMAIN_ID, null, MANAGE_CODENVY_ACTION);
    }
}
