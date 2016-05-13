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
package com.codenvy.machine;

import org.eclipse.che.api.machine.server.model.impl.ServerImpl;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * @author Alexander Garagatyi
 */
public class BaseServerModifierTest {
    private BaseServerModifier serverModifier;

    @Test
    public void shouldBeAbleToNotChangeServer() throws Exception {
        serverModifier = new BaseServerModifier("http://%2$s:%3$s/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "some/path",
                                                 "http://my-server.com:32589/some/path");
        ServerImpl expectedServer = new ServerImpl(originServer.getRef(),
                                                   "http",
                                                   originServer.getAddress(),
                                                   '/' + originServer.getPath(),
                                                   "http://my-server.com:32589/some/path");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldRemoveLeadingSlashFromArgumentServerPath() throws Exception {
        serverModifier = new BaseServerModifier("http://%2$s:%3$s/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "/some/path",
                                                 "http://my-server.com:32589/some/path");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer.getUrl(), originServer.getUrl());
    }

    @Test
    public void shouldReturnUnchangedServerIfCreatedUriIsInvalid() throws Exception {
        serverModifier = new BaseServerModifier(":::://:%3$s`%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "/some/path",
                                                 "http://my-server.com:32589/some/path");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer, originServer);
    }

    @Test
    public void shouldNotAdd80PortToUrl() throws Exception {
        serverModifier = new BaseServerModifier("http://proxy-host/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "/some/path",
                                                 "http://my-server.com:32589/some/path");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer.getUrl(), "http://proxy-host/some/path");
    }

    @Test
    public void shouldBeAbleToChangeServerAttributes() throws Exception {
        serverModifier = new BaseServerModifier("https://%3$s-%2$s.proxy-host:444/%1$s/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 "/some/path",
                                                 "http://my-server.com:32589/some/path");
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "https",
                                                   "32589-my-server.com.proxy-host:444",
                                                   "/myRef/some/path",
                                                   "https://32589-my-server.com.proxy-host:444/myRef/some/path");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer, expectedServer);
    }

    @Test
    public void shouldWorkProperlyIfPathIsNull() throws Exception {
        serverModifier = new BaseServerModifier("https://%3$s-%2$s.proxy-host:444/%4$s") {};
        ServerImpl originServer = new ServerImpl("myRef",
                                                 "http",
                                                 "my-server.com:32589",
                                                 null,
                                                 "http://my-server.com:32589/");
        ServerImpl expectedServer = new ServerImpl("myRef",
                                                   "https",
                                                   "32589-my-server.com.proxy-host:444",
                                                   "/",
                                                   "https://32589-my-server.com.proxy-host:444/");

        ServerImpl modifiedServer = serverModifier.proxy(originServer);

        assertEquals(modifiedServer, expectedServer);
    }
}
