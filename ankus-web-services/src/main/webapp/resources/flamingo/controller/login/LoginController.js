/*
 * Copyright (C) 2011  Flamingo Project (http://www.opencloudengine.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

Ext.define('Flamingo.controller.login.LoginController', {
    extend: 'Ext.app.Controller',
    alias: 'controller.loginController',

    requires: [
        'Flamingo.view.desktop.Login',
        'Flamingo.view.login.LoginPanel'
    ],

    init: function () {
        log('Initializing Login Controller');

        this.control({
            'login #signInButton': {
                click: this.onSignInButtonClick
            },
            'login #signUpButton': {
                click: this.onSignUpButtonClick
            },
            'login #resetButton': {
                click: this.onResetButtonClick
            },
            'loginPanel #password': {
                specialkey: this.onEnterSpecialkey
            }
        });

        this.onLaunch();
    },

    onLaunch: function () {
        log('Launched Login Controller');
    },

    /**
     * Do login when press enter key on password field.
     *
     * @param f Field
     * @param e Event
     */
    onEnterSpecialkey: function (f, e) {
        if (e.getKey() == e.ENTER) {
            this.onSignInButtonClick();
        }
    },

    /**
     * Clear username and password field when press reset button.
     */
    onResetButtonClick: function () {
        var panel = Ext.ComponentQuery.query('login')[0];
        var username = panel.query('#username')[0].setValue('');
        var password = panel.query('#password')[0].setValue('');
    },

    /**
     * Do login when press signin button.
     */
    onSignInButtonClick: function () {
        var panel = Ext.ComponentQuery.query('login')[0];
        var username = panel.query('#username')[0].getValue();
        var password = panel.query('#password')[0].getValue();

        var params = {
            username: username,
            password: password
        };

        invokePostByMap(CONSTANTS.CONTEXT_PATH + CONSTANTS.USER.AUTH, params,
            function (response) {
                var obj = Ext.decode(response.responseText);

                if (obj.success) {
                    // 로그인 사용자 계정 활성화 체크를 위한 response parameter
                    if (obj.object) {
                        window.location.href = CONSTANTS.CONTEXT_PATH + '/login.do';
                    } else {
                        query('loginPanel #error').setValue(obj.error.message);
                    }

                } else {
                    query('loginPanel #error').setValue(obj.error.message);
                }
            },
            function (response) {
                console.log(response.statusText);
            }
        )
    },

    /**
     * Do sign up when press signup button.
     * @auth shjeon
     * @date 2014.2.3
     */
    onSignUpButtonClick: function () {
//        var signUp = Ext.get('signUpWindow');
//        if (!signUp) {
//            signUp = Ext.create('Flamingo.view.login.popup.SignUpWindow');
//        }
//        signUp.show();

        var popWindow = Ext.create('Ext.Window', {
            title: 'Join us',
            width: 380,
            height: 250,
            modal: true,
            resizable: false,
            constrain: true,
            layout: 'fit',
            items: {
                xtype: 'form',
                itemId: 'signUpForm',
                border: false,
                bodyPadding: 10,
                defaults: {
                    anchor: '100%',
                    labelWidth: 120
                },
                items: [
                    {
                        xtype: 'textfield',
                        name: 'username',
                        itemId: 'username',
                        fieldLabel: 'Username',
                        emptyText: 'Username',
                        allowBlank: false,
                        minLength: 5,
                        vtype: 'alphanum'
                    },
                    {
                        xtype: 'textfield',
                        name: 'email',
                        itemId: 'email',
                        fieldLabel: 'Email',
                        vType: 'email',
                        emptyText: 'Email',
                        allowBlank: false
                    },
                    {
                        xtype: 'textfield',
                        name: 'check_email',
                        itemId: 'check_email',
                        fieldLabel: 'Re Email',
                        vType: 'email',
                        emptyText: 'Re Email',
                        allowBlank: false
                    },
                    {
                        xtype: 'textfield',
                        name: 'password',
                        itemId: 'password',
                        fieldLabel: 'Password',
                        inputType: 'password',
                        emptyText: 'Password',
                        allowBlank: false,
                        minLength: 4
                    },
                    {
                        xtype: 'textfield',
                        name: 'check_password',
                        itemId: 'check_password',
                        fieldLabel: 'Re password',
                        inputType: 'password',
                        emptyText: 'Re Password',
                        allowBlank: false,
                        minLength: 4
                    },
                    // view message
                    {
                        xtype: 'displayfield',
                        name: 'error',
                        itemId: 'error',
                        fieldStyle: 'color: #FF0000'
                    }
                ]
            },
            buttons: [
                {
                    text: MSG.COMMON_OK,
                    iconCls: 'common-confirm',
                    handler: function () {
                        var url = CONSTANTS.CONTEXT_PATH + CONSTANTS.SIGNUP;

                        var username = Flamingo.Util.String.trim(popWindow.down('#username').getValue());
                        var email = Flamingo.Util.String.trim(popWindow.down('#email').getValue());
                        var check_email = Flamingo.Util.String.trim(popWindow.down('#check_email').getValue());
                        var password = Flamingo.Util.String.trim(popWindow.down('#password').getValue());
                        var check_password = Flamingo.Util.String.trim(popWindow.down('#check_password').getValue());
                        var error = Flamingo.Util.String.trim(popWindow.down('#error').getValue());

                        var param = {
                            "username": username,
                            "email": email,
                            "password": password
                        };

                        if (username == '') {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('What is your name?'));
                        }
                        if (email == '') {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('You will this email if you ever need to find your password.'));
                        }

                        if (check_email == '') {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('Please re enter your email address.'));
                        }
                        else if (email != check_email) {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('Your emails do not match. Please try again.'));
                        }

                        if (password == '') {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('Please enter your password.'));
                        }

                        if (check_password == '') {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('Please re enter your password.'));
                        }
                        else if (password != check_password) {
                            Flamingo.Util.String.trim(popWindow.down('#error').setValue('Your passwords do not match. Please try again.'));
                        }


                        Flamingo.Ajax.Request.invokePostByMap(url, param,
                            function (response) {
                                var obj = Ext.decode(response.responseText);

                                if (obj.success) {

                                    alert("Congratulations! Welcome to the ankus framework! ['" + param.username + "'] ");
//                                    Flamingo.Util.String.trim(popWindow.down('#error').setValue("Congratulation! '"+param.username+"' became a member of this site!"));
                                    popWindow.close();

                                } else {
                                    if (obj.error.message.indexOf('Username') >= 0) {
                                        Flamingo.Util.String.trim(popWindow.down('#error').setValue(obj.error.message));
//                                                                                query('signUpPanel #username_error').setValue(obj.error.message);
                                    }
                                    else if (obj.error.message.indexOf('Email') >= 0) {
                                        Flamingo.Util.String.trim(popWindow.down('#error').setValue(obj.error.message));
//                                                                                query('signUpPanel #email_error').setValue(obj.error.message);
//                                                                                query('signUpPanel #username_error').setValue('');
                                    }
                                    else {
                                        Flamingo.Util.String.trim(popWindow.down('#error').setValue(obj.error.message));
                                    }
                                }
                            },
                            function (response) {
                                Flamingo.Util.String.trim(popWindow.down('#error').setValue(response.statusText));
//                                console.log(response.statusText);
                            }
                        );
                    }
                },
                {
                    text: MSG.COMMON_CANCEL,
                    iconCls: 'common-cancel',
                    handler: function () {
                        var win = this.up('window');
                        win.close();
                    }
                }
            ]

        }).show();
    },

    redundancyCheckOnUserName: function (userNm) {

    },
    validateEmail: function () {

    },
    validatePass: function () {

    },
    validateSignUp: function () {

    }
});