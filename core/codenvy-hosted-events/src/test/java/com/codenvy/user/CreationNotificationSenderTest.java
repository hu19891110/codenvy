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
package com.codenvy.user;

import com.codenvy.mail.MailSenderClient;
import com.codenvy.mail.shared.dto.EmailBeanDto;
import com.codenvy.service.password.RecoveryStorage;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.codenvy.user.CreationNotificationSender.EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link CreationNotificationSender}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class CreationNotificationSenderTest {
    @Captor
    private ArgumentCaptor<EmailBeanDto> argumentCaptor;

    @Mock
    private MailSenderClient mailSenderClient;

    @Mock
    private RecoveryStorage recoveryStorage;

    @InjectMocks
    CreationNotificationSender notificationSender;

    @BeforeMethod
    public void setUp() {
        notificationSender.apiEndpoint = "http://localhost/api";
        notificationSender.mailSender = "noreply@host";

        when(recoveryStorage.generateRecoverToken(anyString())).thenReturn("uuid");
    }

    @Test
    public void shouldSendEmailWhenUserWasCreatedByUserServiceWithDescriptor() throws Throwable {
        notificationSender.sendNotification("user123", "test@user.com", EMAIL_TEMPLATE_USER_CREATED_WITH_PASSWORD);

        verify(mailSenderClient).sendMail(argumentCaptor.capture());

        EmailBeanDto emailBeanDto = argumentCaptor.getValue();
        assertTrue(emailBeanDto.getAttachments().size() == 1);
        assertTrue(!emailBeanDto.getBody().isEmpty());
        assertEquals(emailBeanDto.getTo(), "test@user.com");
        assertEquals(emailBeanDto.getMimeType(), TEXT_HTML);
        assertEquals(emailBeanDto.getFrom(), "noreply@host");
        assertEquals(emailBeanDto.getSubject(), "Welcome To Codenvy");
    }
}
