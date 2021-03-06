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
package com.codenvy.ext.java.server;

import com.codenvy.api.permission.server.PermissionChecker;
import com.codenvy.auth.sso.client.TokenHandler;
import com.codenvy.auth.sso.client.token.RequestTokenExtractor;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import org.eclipse.che.ApiEndpointProvider;
import org.eclipse.che.EventBusURLProvider;
import org.eclipse.che.UserTokenProvider;
import org.eclipse.che.api.auth.oauth.OAuthTokenProvider;
import org.eclipse.che.api.core.notification.WSocketEventBusClient;
import org.eclipse.che.api.core.rest.ApiInfoService;
import org.eclipse.che.api.core.rest.CoreRestModule;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.util.FileCleaner.FileCleanerModule;
import org.eclipse.che.api.git.GitConnectionFactory;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.api.git.LocalGitUserResolver;
import org.eclipse.che.api.project.server.ProjectApiModule;
import org.eclipse.che.api.ssh.server.HttpSshServiceClient;
import org.eclipse.che.api.ssh.server.SshServiceClient;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.everrest.CheAsynchronousJobPool;
import org.eclipse.che.git.impl.jgit.JGitConnectionFactory;
import org.eclipse.che.ide.ext.microsoft.server.inject.MicrosoftModule;
import org.eclipse.che.inject.DynaModule;
import org.eclipse.che.plugin.github.server.inject.GitHubModule;
import org.eclipse.che.plugin.maven.generator.archetype.ArchetypeGenerator;
import org.eclipse.che.plugin.maven.server.inject.MavenModule;
import org.eclipse.che.security.oauth.RemoteOAuthTokenProvider;
import org.everrest.core.impl.async.AsynchronousJobPool;
import org.everrest.core.impl.async.AsynchronousJobService;
import org.everrest.guice.ServiceBindingHelper;

import javax.inject.Named;

/**
 * @author Evgen Vidolob
 * @author Sergii Kabashniuk
 * @author Max Shaposhnik
 * @author Alexander Garagatyi
 * @author Anton Korneta
 * @author Vitaly Parfonov
 */
@DynaModule
public class MachineModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ApiInfoService.class);

        bind(SshServiceClient.class).to(HttpSshServiceClient.class);
        bind(OAuthTokenProvider.class).to(RemoteOAuthTokenProvider.class);
        bind(org.eclipse.che.plugin.ssh.key.script.SshKeyProvider.class)
                .to(org.eclipse.che.plugin.ssh.key.script.SshKeyProviderImpl.class);

        install(new CoreRestModule());
        install(new FileCleanerModule());
        install(new ProjectApiModule());
        install(new MavenModule());
        install(new GitHubModule());
        install(new MicrosoftModule());
        install(new org.eclipse.che.swagger.deploy.DocsModule());
        install(new org.eclipse.che.api.debugger.server.DebuggerModule());

        bind(ArchetypeGenerator.class);

        bind(GitUserResolver.class).to(LocalGitUserResolver.class);
        bind(GitConnectionFactory.class).to(JGitConnectionFactory.class);

        bind(AsynchronousJobPool.class).to(CheAsynchronousJobPool.class);
        bind(ServiceBindingHelper.bindingKey(AsynchronousJobService.class, "/async/{ws-id}")).to(AsynchronousJobService.class);

        bind(String.class).annotatedWith(Names.named("api.endpoint")).toProvider(ApiEndpointProvider.class);
        bind(String.class).annotatedWith(Names.named("user.token")).toProvider(UserTokenProvider.class);

        bind(PermissionChecker.class).to(com.codenvy.api.permission.server.HttpPermissionCheckerImpl.class);
        bind(TokenHandler.class).to(com.codenvy.api.permission.server.PermissionTokenHandler.class);
        bind(TokenHandler.class).annotatedWith(Names.named("delegated.handler"))
                                .to(com.codenvy.auth.sso.client.NoUserInteractionTokenHandler.class);

        bindConstant().annotatedWith(Names.named("auth.sso.cookies_disabled_error_page_url"))
                      .to("/site/error/error-cookies-disabled");
        bindConstant().annotatedWith(Names.named("auth.sso.login_page_url")).to("/site/login");

        bind(PreferenceDao.class).to(org.eclipse.che.RemotePreferenceDao.class);

        bind(WSocketEventBusClient.class).asEagerSingleton();

        bind(String.class).annotatedWith(Names.named("event.bus.url")).toProvider(EventBusURLProvider.class);

        bind(HttpJsonRequestFactory.class).to(AuthorizeTokenHttpJsonRequestFactory.class);

        bind(com.codenvy.workspace.websocket.WorkspaceWebsocketConnectionListener.class);
        install(new org.eclipse.che.commons.schedule.executor.ScheduleModule());

        bind(RequestTokenExtractor.class).to(com.codenvy.auth.sso.client.token.ChainedTokenExtractor.class);

        bind(String.class).annotatedWith(Names.named("wsagent.endpoint"))
                          .toProvider(com.codenvy.api.agent.WsAgentURLProvider.class);
    }

    //it's need for WSocketEventBusClient and in the future will be replaced with the property
    @Named("notification.client.event_subscriptions")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] eventSubscriptionsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[] {Pair.of(eventBusURL, "")};
    }

    //it's need for EventOriginClientPropagationPolicy and in the future will be replaced with the property
    @Named("notification.client.propagate_events")
    @Provides
    @SuppressWarnings("unchecked")
    Pair<String, String>[] propagateEventsProvider(@Named("event.bus.url") String eventBusURL) {
        return new Pair[] {Pair.of(eventBusURL, "")};
    }
}
