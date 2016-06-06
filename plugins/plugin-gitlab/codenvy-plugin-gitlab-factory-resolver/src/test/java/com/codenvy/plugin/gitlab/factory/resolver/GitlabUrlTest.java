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
package com.codenvy.plugin.gitlab.factory.resolver;

import org.mockito.InjectMocks;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Test of {@Link GitlabUrl}
 * Note: The parser is also testing the object
 *
 * @author Florent Benoit
 */
@Listeners(MockitoTestNGListener.class)
public class GitlabUrlTest {

    /**
     * Parser used to create the url.
     */
    @InjectMocks
    private GitlabUrlParser gitlabUrlParser;

    /**
     * Instance of the url created
     */
    private GitlabUrl gitlabUrl;

    /**
     * Setup objects/
     */
    @BeforeClass
    protected void init() {
        this.gitlabUrl = this.gitlabUrlParser.parse("https://gitlab.com/eclipse/che");
        assertNotNull(this.gitlabUrl);
    }

    /**
     * Check when there is .codenvy.dockerfile in the repository
     */
    @Test
    public void checkDockerfileLocation() {
        assertEquals(gitlabUrl.codenvyDockerFileLocation(), "https://gitlab.com/eclipse/che/raw/master/.codenvy.dockerfile");
    }

    /**
     * Check when there is .codenvy.json file in the repository
     */
    @Test
    public void checkCodenvyFactoryJsonFileLocation() {
        assertEquals(gitlabUrl.codenvyFactoryJsonFileLocation(), "https://gitlab.com/eclipse/che/raw/master/.codenvy.json");
    }

    /**
     * Check the original repository
     */
    @Test
    public void checkRepositoryLocation() {
        assertEquals(gitlabUrl.repositoryLocation(), "https://gitlab.com/eclipse/che.git");
    }
}
