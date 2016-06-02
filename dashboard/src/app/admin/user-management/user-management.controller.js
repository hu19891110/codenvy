/*
 *  [2015] - [2016] Codenvy, S.A.
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
'use strict';

/**
 * This class is handling the controller for the admins user management
 * @author Oleksii Orel
 */
export class AdminsUserManagementCtrl {

  /**
   * Default constructor.
   * @ngInject for Dependency injection
   */
  constructor($document, $mdMedia, $mdDialog, codenvyAPI, cheNotification) {
    'ngInject';

    this.$document = $document;
    this.$mdMedia = $mdMedia;
    this.$mdDialog = $mdDialog;
    this.codenvyAPI = codenvyAPI;
    this.cheNotification = cheNotification;

    this.isLoading = false;

    this.maxItems = 12;
    this.skipCount = 0;

    this.users = [];
    this.usersMap = codenvyAPI.getUser().getUsersMap();

    if (this.usersMap && this.usersMap.size > 1) {
      this.updateUsers();
    } else {
      this.isLoading = true;
      codenvyAPI.getUser().fetchUsers(this.maxItems, this.skipCount).then(() => {
        this.isLoading = false;
        this.updateUsers();
      }, (error) => {
        this.isLoading = false;
        if (error && error.status !== 304) {
          this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Failed to retrieve the list of users.');
        }
      });
    }

    this.pagesInfo = codenvyAPI.getUser().getPagesInfo();
  }

  /**
   * Update users array
   */
  updateUsers() {
    //update users array
    this.users.length = 0;
    this.usersMap.forEach((user) => {
      this.users.push(user);
    });
  }

  /**
   * Ask for loading the users page in asynchronous way
   * @param pageKey - the key of page
   */
  fetchUsersPage(pageKey) {
    let promise = this.codenvyAPI.getUser().fetchUsersPage(pageKey);

    promise.then(() => {
      this.updateUsers();
    }, (error) => {
      if (error.status === 304) {
        this.updateUsers();
      } else {
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Update information failed.');
      }
    });
  }

  /**
   * Returns true if the next page is exist.
   * @returns {boolean}
   */
  hasNextPage() {
    return this.pagesInfo.currentPageNumber < this.pagesInfo.countOfPages;
  }

  /**
   * Returns true if the previous page is exist.
   * @returns {boolean}
   */
  hasPreviousPage() {
    return this.pagesInfo.currentPageNumber > 1;
  }

  /**
   * Returns true if we have more then one page.
   * @returns {boolean}
   */
  isPagination() {
    return this.pagesInfo.countOfPages > 1;
  }

  /**
   * Admin clicked on the + button to add a new user. Show the dialog
   * @param  event - the $event
   */
  showAddUserDialog(event) {
    let parentEl = angular.element(this.$document.body);

    this.$mdDialog.show({
      targetEvent: event,
      bindToController: true,
      clickOutsideToClose: true,
      controller: 'AdminsAddUserCtrl',
      controllerAs: 'adminsAddUserCtrl',
      locals: {callbackController: this},
      parent: parentEl,
      templateUrl: 'app/admin/user-management/add-user/add-user.html'
    });
  }

  /**
   * User clicked on the - button to remove the user. Show the dialog
   * @param  event - the $event
   * @param user - the selected user
   */
  removeUser(event, user) {
    let confirm = this.$mdDialog.confirm()
      .title('Would you like to remove user ' + user.email + ' ?')
      .content('Please confirm for the user removal.')
      .ariaLabel('Remove user')
      .ok('Remove')
      .cancel('Cancel')
      .clickOutsideToClose(true)
      .targetEvent(event);
    this.$mdDialog.show(confirm).then(() => {
      this.isLoading = true;
      let promise = this.codenvyAPI.getUser().deleteUserById(user.id);
      promise.then(() => {
        this.isLoading = false;
        this.updateUsers();
      }, (error) => {
        this.isLoading = false;
        this.cheNotification.showError(error.data && error.data.message ? error.data.message : 'Delete user failed.');
      });
    });
  }
}
