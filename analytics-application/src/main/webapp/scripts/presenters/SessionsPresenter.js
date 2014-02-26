/*
 * CODENVY CONFIDENTIAL
 * __________________
 *
 *  [2012] - [2014] Codenvy, S.A.
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
if (typeof analytics === "undefined") {
    analytics = {};
}

analytics.presenter = analytics.presenter || {};

analytics.presenter.SessionsPresenter = function SessionsPresenter() {};

analytics.presenter.SessionsPresenter.prototype = new EntryViewPresenter();

analytics.presenter.SessionsPresenter.prototype.TARGET_PAGE_LINK = "sessions-view.jsp";

analytics.presenter.SessionsPresenter.prototype.mapColumnNameToSortValue = {
    "Id": "session_id",
    "User": "user",
    "Workspace": "ws",
    "Start Time": "start_time",
    "End Time": "end_time",
    "Duration": "time",
    
};