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
package com.codenvy.auth.sso.client;

import com.codenvy.api.dao.authentication.AccessTicket;
import com.codenvy.api.dao.authentication.TicketManager;
import com.codenvy.auth.sso.server.ticket.InMemoryTicketManager;
import com.codenvy.auth.sso.shared.dto.SubjectDto;
import com.codenvy.machine.authentication.server.MachineTokenRegistry;

import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.HttpJsonRequest;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.commons.test.SelfReturningAnswer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static java.util.Collections.singleton;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link MachineSsoServerClient}.
 *
 * @author Yevhenii Voevodin
 */
public class MachineSsoServerClientTest {

    private static final String ENDPOINT = "http://localhost";

    private MachineTokenRegistry   registrySpy;
    private HttpJsonResponse       responseMock;
    private UserManager            userManagerMock;
    private TicketManager          ticketManager;
    private MachineSsoServerClient ssoClient;

    @BeforeMethod
    public void initClient() throws Exception {
        ticketManager = new InMemoryTicketManager();

        registrySpy = spy(new MachineTokenRegistry());

        userManagerMock = mock(UserManager.class);

        responseMock = mock(HttpJsonResponse.class);
        when(responseMock.asDto(SubjectDto.class)).thenReturn(newDto(SubjectDto.class));

        final HttpJsonRequest requestMock = mock(HttpJsonRequest.class, new SelfReturningAnswer());
        when(requestMock.request()).thenReturn(responseMock);

        final HttpJsonRequestFactory requestFactoryMock = mock(HttpJsonRequestFactory.class);
        when(requestFactoryMock.fromLink(any())).thenReturn(requestMock);
        when(requestFactoryMock.fromUrl(any())).thenReturn(requestMock);

        ssoClient = new MachineSsoServerClient(ENDPOINT,
                                               requestFactoryMock,
                                               registrySpy,
                                               ticketManager,
                                               userManagerMock);
    }

    @Test
    public void getUserMustDelegateCallToTheSuperWhenTokenIsNotPrefixedWithMachine() throws NotFoundException {
        ssoClient.getSubject("123456789", "client");

        verify(registrySpy, never()).getUserId(any());
    }

    @Test
    public void getUserMustReturnNullWhenTokenIsMachinePrefixedButUserForSuchTokenDoesNotExist() throws NotFoundException {
        final Subject user = ssoClient.getSubject("machine123456789", "client");

        assertNull(user);
        verify(registrySpy).getUserId("machine123456789");
    }

    @Test
    public void getUserMustReturnTheUserRetrievedFromTheApiRequest() throws NotFoundException, ServerException {
        // machine token
        final String token = registrySpy.generateToken("user123", "workspace1234");
        // mocking the user descriptor which will be returned from the user api
        final org.eclipse.che.api.user.server.dao.User user =
                new org.eclipse.che.api.user.server.dao.User().withName("name")
                                                              .withEmail("mail")
                                                              .withId("user123");
        when(userManagerMock.getById(any())).thenReturn(user);
        // creating the ticket for figuring out the master's token
        ticketManager.putAccessTicket(new AccessTicket("master_token",
                                                       new SubjectImpl("user", "user123", "master_token", singleton("user")),
                                                       "auth_type"));

        final Subject sessionUser = ssoClient.getSubject(token, "client");

        assertNotNull(sessionUser);
        assertEquals(sessionUser.getUserName(), user.getName());
        assertEquals(sessionUser.getUserId(), user.getId());
        assertFalse(sessionUser.isTemporary());
        assertEquals(sessionUser.getToken(), "master_token");
        assertTrue(sessionUser.isMemberOf("user"));
    }
}
