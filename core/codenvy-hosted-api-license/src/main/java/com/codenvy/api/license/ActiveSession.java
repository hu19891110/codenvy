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
package com.codenvy.api.license;

import com.codenvy.api.license.model.Resource;

import java.util.Set;

/**
 * @author gazarenkov
 */
public class ActiveSession {
    private final String        id;
    private final String        user;
    private final long          startTime;
    private final License       license;
    private final Set<Resource> resources;

    public ActiveSession(String id, String user, License license, long startTime, Set<Resource> resources) {
        this.id = id;
        this.user = user;
        this.license = license;
        this.startTime = startTime;
        this.resources = resources;
    }

    public String getId() {
        return id;
    }

    public String getUser() {
        return user;
    }

    public long getStartTime() {
        return startTime;
    }

    public License getLicense() {
        return license;
    }

    public Set<Resource> getResources() {
        return resources;
    }
}
