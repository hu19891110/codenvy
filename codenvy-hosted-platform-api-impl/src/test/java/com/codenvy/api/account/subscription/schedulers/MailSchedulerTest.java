/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2015] Codenvy, S.A.
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
package com.codenvy.api.account.subscription.schedulers;

import com.codenvy.api.account.billing.BillingService;
import com.codenvy.api.account.billing.InvoiceFilter;
import com.codenvy.api.account.billing.PaymentState;
import com.codenvy.api.account.billing.TemplateProcessor;
import com.codenvy.api.account.impl.shared.dto.Invoice;
import com.codenvy.api.account.subscription.service.util.SubscriptionMailSender;

import org.eclipse.che.dto.server.DtoFactory;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link com.codenvy.api.account.subscription.schedulers.MailSchedulerTest}
 *
 * @author Sergii Leschenko
 */
@Listeners(value = {MockitoTestNGListener.class})
public class MailSchedulerTest {
    private static final int INVOICES_LIMIT = 20;

    private static final String ACCOUNT_ID = "acc213";

    final DtoFactory dto = DtoFactory.getInstance();

    @Mock
    SubscriptionMailSender subscriptionMailSender;
    @Mock
    BillingService         billingService;
    @Mock
    TemplateProcessor      templateProcessor;

    MailScheduler mailScheduler;

    @BeforeMethod
    public void setUp() {
        mailScheduler = new MailScheduler(subscriptionMailSender, billingService, templateProcessor, INVOICES_LIMIT);
    }

    @Test
    public void shouldSendEmailForInvoiceWithStatePaymentSuccessfully() throws Exception {
        final Invoice invoice = dto.createDto(Invoice.class)
                                   .withAccountId(ACCOUNT_ID)
                                   .withTotal(0D)
                                   .withId(1L)
                                   .withPaymentState(PaymentState.PAID_SUCCESSFULLY.getState());
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Arrays.asList(invoice));

        mailScheduler.sendEmails();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final List<String> states = new ArrayList<>(Arrays.asList(invoiceFilter.getStates()));
                return states.size() == 3
                       && states.contains(PaymentState.PAYMENT_FAIL.getState())
                       && states.contains(PaymentState.PAID_SUCCESSFULLY.getState())
                       && states.contains(PaymentState.NOT_REQUIRED.getState())
                       && invoiceFilter.getIsMailNotSend()
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(templateProcessor).processTemplate((Invoice)anyObject(), (Writer)anyObject());
        verify(subscriptionMailSender).sendInvoice(eq(ACCOUNT_ID), eq(invoice.getPaymentState()), anyString());
        verify(billingService).markInvoiceAsSent(eq(1L));
    }

    @Test
    public void shouldSendEmailForInvoiceWithStatePaymentFailed() throws Exception {
        final Invoice invoice = dto.createDto(Invoice.class)
                                   .withAccountId(ACCOUNT_ID)
                                   .withTotal(0D)
                                   .withId(1L)
                                   .withPaymentState(PaymentState.PAYMENT_FAIL.getState());
        when(billingService.getInvoices((InvoiceFilter)anyObject())).thenReturn(Arrays.asList(invoice));

        mailScheduler.sendEmails();

        verify(billingService).getInvoices(argThat(new ArgumentMatcher<InvoiceFilter>() {
            @Override
            public boolean matches(Object o) {
                final InvoiceFilter invoiceFilter = (InvoiceFilter)o;

                final List<String> states = new ArrayList<>(Arrays.asList(invoiceFilter.getStates()));
                return states.size() == 3
                       && states.contains(PaymentState.PAYMENT_FAIL.getState())
                       && states.contains(PaymentState.PAID_SUCCESSFULLY.getState())
                       && states.contains(PaymentState.NOT_REQUIRED.getState())
                       && invoiceFilter.getIsMailNotSend()
                       && INVOICES_LIMIT == invoiceFilter.getMaxItems()
                       && invoiceFilter.getSkipCount() == null;
            }
        }));

        verify(templateProcessor).processTemplate((Invoice)anyObject(), (Writer)anyObject());
        verify(subscriptionMailSender).sendInvoice(eq(ACCOUNT_ID), eq(invoice.getPaymentState()), anyString());
        verify(billingService).markInvoiceAsSent(eq(1L));
    }
}