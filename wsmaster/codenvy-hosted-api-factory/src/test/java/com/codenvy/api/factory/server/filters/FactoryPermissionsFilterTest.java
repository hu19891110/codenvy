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
package com.codenvy.api.factory.server.filters;

import com.jayway.restassured.response.Response;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.factory.server.FactoryService;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.everrest.core.resource.GenericResourceMethod;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.api.workspace.server.WorkspaceDomain.DOMAIN_ID;
import static com.codenvy.api.workspace.server.WorkspaceDomain.READ;
import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link FactoryPermissionsFilter}.
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class FactoryPermissionsFilterTest {

    @SuppressWarnings("unused")
    private static final EnvironmentFilter FILTER = new EnvironmentFilter();

    @SuppressWarnings("unused")
    @InjectMocks
    FactoryPermissionsFilter permissionsFilter;

    @Mock
    private static Subject subject;

    @Mock
    FactoryService service;

    @Test
    public void shouldCheckPermissionsOnGettingFactoryJson() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/factory/workspace/workspace123");

        assertEquals(response.getStatusCode(), 204);
        verify(service).getFactoryJson(eq("workspace123"), anyString());
        verify(subject).checkPermission(DOMAIN_ID, "workspace123", READ);
    }

    @Test
    public void shouldThrowExceptionWhenUserDoesHavePermissionsToReadWorkspaceOnGettingFactoryJson() throws Exception {
        doThrow(new ForbiddenException("User in not authorized"))
                .when(subject).checkPermission(anyString(), anyString(), anyString());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/factory/workspace/workspace123");

        assertEquals(response.getStatusCode(), 403);
    }

    @Test
    public void shouldNotCheckPermissionsWhenUnlistedMethodIsCalled() throws Exception {
        GenericResourceMethod genericResourceMethod = mock(GenericResourceMethod.class);
        when(genericResourceMethod.getMethod()).thenReturn(FactoryService.class.getMethod("getServiceDescriptor"));

        permissionsFilter.filter(genericResourceMethod, new Object[0]);
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {
        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(subject);
        }
    }
}
