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
package com.codenvy.onpremises.factory.deploy;

import com.codenvy.onpremises.factory.filter.RemoveIllegalCharactersFactoryURLFilter;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;

import org.eclipse.che.inject.DynaModule;

import javax.inject.Singleton;

/**
 *  Servlet module composer for factory war.
 *  @author Sergii Kabashniuk
 */
@DynaModule
public class FactoryServletModule extends ServletModule {

    private static final String PASS_RESOURCES_REGEXP = "^(?!/resources/)/?.*$";

    @Override
    protected void configureServlets() {
        filterRegex(PASS_RESOURCES_REGEXP).through(com.codenvy.onpremises.factory.filter.BrowserCheckerFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(RemoveIllegalCharactersFactoryURLFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(com.codenvy.onpremises.factory.filter.FactoryParamsFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(com.codenvy.auth.sso.client.LoginFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(com.codenvy.onpremises.factory.filter.FactoryRetrieverFilter.class);
        filterRegex(PASS_RESOURCES_REGEXP).through(com.codenvy.onpremises.factory.filter.ReferrerCheckerFilter.class);


        bind(com.codahale.metrics.servlets.ThreadDumpServlet.class).in(Singleton.class);
        bind(com.codahale.metrics.servlets.PingServlet.class).in(Singleton.class);
        serve("/metrics/ping").with(com.codahale.metrics.servlets.PingServlet.class);
        serve("/metrics/threaddump").with(com.codahale.metrics.servlets.ThreadDumpServlet.class);

        serveRegex(PASS_RESOURCES_REGEXP).with(com.codenvy.onpremises.factory.FactoryServlet.class);

        bindConstant().annotatedWith(Names.named("page.creation.error")).to("/resources/error-factory-creation.html");
        bindConstant().annotatedWith(Names.named("page.invalid.factory")).to("/resources/error-invalid-factory-url.jsp");
        bindConstant().annotatedWith(Names.named("page.too.many.factories")).to("/resources/too-many-factories.html");

        install(new com.codenvy.auth.sso.client.deploy.SsoClientServletModule());
    }
}
