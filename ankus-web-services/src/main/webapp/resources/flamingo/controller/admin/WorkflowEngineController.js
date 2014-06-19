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

Ext.define('Flamingo.controller.admin.WorkflowEngineController', {
    extend: 'Ext.app.Controller',
    alias: 'controller.workflowEngineAdminController',

    init: function () {
        log('Initializing Workflow Engine Admin Controller');
        this.control({
            '#enginesGrid': {
                itemclick: this.onItemClick
            },
            '#showFileSystemBrowserButton': {
                click: this.onShowFileSystemBrowserButton
            },
            '#refreshEngineButton': {
                click: this.onRefreshEngineButton
            },
            '#registJobButton': {
                click: this.onRegistJobButton
            },
            '#deleteEngineButton': {
                click: this.onDeleteEngineButton
            },
            '#addEngineButton': {
                click: this.onAddEngineButton
            },
            '#refreshTriggers': {
                click: this.onRefreshTriggers
            },
            '#refreshRunningJobs': {
                click: this.onRefreshRunningJobs
            }
        });
        log('Initialized Workflow Engine Admin Controller');

        this.onLaunch();
    },

    onLaunch: function () {
        log('Launched Workflow Engine Admin Controller');
    },

    /**
     * Workflow Engine의 Trigger 정보를 갱신한다.
     */
    onRefreshTriggers: function () {
        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
        var selection = grid.getView().getSelectionModel().getSelection()[0];
        var serverUrl = selection.data.serverUrl;

        var triggersGrid = Ext.ComponentQuery.query('workflowEngineTriggersPanel #triggersGrid')[0];
        triggersGrid.getStore().load(
            {
                params: {
                    'serverUrl': serverUrl
                }
            }
        );
    },

    /**
     * Workflow Engine의 Running Job 정보를 갱신한다.
     */
    onRefreshRunningJobs: function () {
        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
        var selection = grid.getView().getSelectionModel().getSelection()[0];
        var serverUrl = selection.data.serverUrl;

        var runningTrigger = Ext.ComponentQuery.query('workflowEngineRunningJobsPanel #runningTrigger')[0];
        runningTrigger.getStore().load(
            {
                params: {
                    'serverUrl': serverUrl
                }
            }
        );
    },

    /**
     * 새로운 Workflow Engine을 등록한다.
     */
    onAddEngineButton: function () {
        var popWindow = Ext.create('Ext.Window', {
            title: MSG.ADMIN_REG_WORKFLOW_ENG,
            width: 320,
            height: 230,
            modal: true,
            resizable: true,
            constrain: true,
            layout: 'fit',
            items: [
                {
                    xtype: 'form',
                    itemId: 'workflowEngineForm',
                    border: false,
                    bodyPadding: 10,
                    defaults: {
                        anchor: '100%',
                        labelWidth: 80
                    },
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'name',
                            itemId: 'name',
                            fieldLabel: 'Engine Name',
                            allowBlank: false,
                            minLength: 6
                        },
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            margin: '0 0 0',
                            items: [
                                {
                                    xtype: 'fieldset',
                                    flex: 1,
                                    title: 'Connection',
                                    layout: 'anchor',
                                    defaults: {
                                        anchor: '100%',
                                        labelWidth: 100,
                                        hideEmptyLabel: false
                                    },
                                    items: [
                                        {
                                            xtype: 'fieldcontainer',
                                            fieldLabel: 'Engine',
                                            layout: 'hbox',
                                            combineErrors: true,
                                            defaultType: 'textfield',
                                            defaults: {
                                                hideLabel: 'true'
                                            },
                                            items: [
                                                {
                                                    name: 'engineIP',
                                                    itemId: 'engineIP',
                                                    fieldLabel: MSG.COMMON_IP,
                                                    flex: 4,
                                                    emptyText: '192.168.0.1',
                                                    allowBlank: false
                                                },
                                                {
                                                    name: 'enginePort',
                                                    itemId: 'enginePort',
                                                    fieldLabel: MSG.COMMON_PORT,
                                                    flex: 2,
                                                    margins: '0 0 0 6',
                                                    emptyText: '9090',
                                                    allowBlank: false
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        },
                        {
                            xtype: 'container',
                            layout: 'hbox',
                            margin: '0 0 0',
                            items: [
                                {
                                    xtype: 'fieldset',
                                    flex: 1,
                                    title: 'Assignment',
                                    layout: 'anchor',
                                    defaults: {
                                        anchor: '100%',
                                        labelWidth: 100,
                                        hideEmptyLabel: false
                                    },
                                    items: [
                                        {
                                            xtype: 'fieldcontainer',
                                            fieldLabel: 'Hadoop Custer',
                                            layout: 'hbox',
                                            combineErrors: true,
                                            defaults: {
                                                hideLabel: 'true'
                                            },
                                            items: [
                                                {
                                                    xtype: '_hadoopClusterCombo'
                                                }
                                            ]
                                        },
                                        {
                                            xtype: 'fieldcontainer',
                                            fieldLabel: 'Hive Server',
                                            layout: 'hbox',
                                            combineErrors: true,
                                            defaults: {
                                                hideLabel: 'true'
                                            },
                                            items: [
                                                {
                                                    xtype: '_hiveServerCombo'
                                                }
                                            ]
                                        }
                                    ]
                                }
                            ]
                        }
                    ]

                }
            ],
            buttons: [
                {
                    text: MSG.COMMON_OK,
                    iconCls: 'common-confirm',
                    handler: function () {
                        var name = popWindow.down('#name').getValue();
                        var engineIP = popWindow.down('#engineIP').getValue();
                        var enginePort = popWindow.down('#enginePort').getValue();
                        var hadoopClusterCombo = popWindow.down('_hadoopClusterCombo');
                        var hiverServerCombo = popWindow.down('_hiveServerCombo');

                        var url = CONSTANTS.CONTEXT_PATH + CONSTANTS.ADMIN.WE.ADD_ENGINE;
                        var param = {
                            'name': name,
                            'ip': engineIP,
                            'port': enginePort,
                            'hadoopClusterId': hadoopClusterCombo.getValue(),
                            'hiveServerId': hiverServerCombo.getValue()
                        };

                        var win = popWindow;
                        Flamingo.Ajax.Request.invokePostByMap(url, param,
                            function (response) {
                                var result = Ext.decode(response.responseText);
                                var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
                                grid.getStore().load();

                                var popup = win;
                                popup.close();
                            },
                            function (response) {
                                var result = Ext.decode(response.responseText);
                                var popup = win;
                                Ext.MessageBox.show({
                                    title: MSG.ADMIN_REG_WORKFLOW_ENG,
                                    msg: result.error.message,
                                    buttons: Ext.MessageBox.OK,
                                    icon: Ext.MessageBox.WARNING,
                                    fn: function handler(btn) {
                                        popup.close();
                                    }
                                });
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

    /**
     * 등록한 Workflow Engine을 삭제한다.
     */
    onDeleteEngineButton: function () {
        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
        var selection = grid.getView().getSelectionModel().getSelection()[0];
        if (selection) {
            Ext.MessageBox.show({
                title: MSG.ADMIN_DELETE_WORKFLOW_ENG,
                msg: MSG.ADMIN_DELETE_WORKFLOW_ENG_YN,
                buttons: Ext.MessageBox.YESNO,
                icon: Ext.MessageBox.WARNING,
                fn: function handler(btn) {
                    if (btn == 'yes') {
                        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
                        var store = grid.getStore();
                        var selection = grid.getView().getSelectionModel().getSelection()[0];

                        var url = CONSTANTS.CONTEXT_PATH + CONSTANTS.ADMIN.WE.DEL_ENGINE;
                        var param = {
                            "id": selection.data.id
                        };

                        Ext.Msg.hide();
                        Flamingo.Ajax.Request.invokePostByMap(url, param,
                            function (response) {
                                store.remove(selection);

                                Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0].getStore().removeAll();
                                Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0].getStore().load();
                                Ext.ComponentQuery.query('workflowEngineEnvsPanel #environmentGrid')[0].getStore().removeAll();
                                Ext.ComponentQuery.query('workflowEnginePropsPanel #propsGrid')[0].getStore().removeAll();
                                Ext.ComponentQuery.query('workflowEngineTriggersPanel #triggersGrid')[0].getStore().removeAll();
                                Ext.ComponentQuery.query('workflowEngineRunningJobsPanel #runningTrigger')[0].getStore().removeAll();
                            },
                            function (response) {
                                var msg = Ext.decode(response.responseText);
                                Ext.MessageBox.show({
                                    title: MSG.ADMIN_DELETE_WORKFLOW_ENG,
                                    msg: msg.error.message,
                                    buttons: Ext.MessageBox.OK,
                                    icon: Ext.MessageBox.WARNING,
                                    fn: function handler(btn) {
                                        Ext.Msg.hide();
                                    }
                                });
                            }
                        );
                    }
                }
            });
        } else {
            msg(MSG.DIALOG_TITLE_WARN, MSG.ADMIN_SELECT_WORKFLOW_ENG);
        }
    },

    /**
     * Workflow Engine에 로그 수집 작업을 등록한다.
     */
    onRegistJobButton: function () {
        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
        var selection = grid.getView().getSelectionModel().getSelection()[0];
        if (selection) {
            var popWindow = Ext.create('Ext.Window', {
                title: MSG.ADMIN_REG_LOG_COLLECT,
                width: 700,
                height: 450,
                modal: true,
                resizable: true,
                constrain: true,
                padding: '5 5 5 5',
                layout: 'fit',
                items: [
                    {
                        xtype: 'textareafield',
                        itemId: 'xmlTextArea',
                        grow: false,
                        name: 'xml',
                        fieldStyle: {
                            'fontFamily': 'Monaco',
                            'fontSize': '12px',
                            padding: '5 5 5 5'
                        },
                        anchor: '100%'
                    }
                ],
                buttons: [
                    {
                        text: 'Register',
                        iconCls: 'common-confirm',
                        handler: function () {
                            var textarea = Ext.ComponentQuery.query("textareafield[name='xml']")[0];
                            var xml = textarea.getValue();
                            var serverId = selection.data.id;

                            var params = {
                                serverId: serverId
                            };

                            var p = popWindow;
                            Flamingo.Ajax.Request.invokePostByXML(CONSTANTS.ADMIN.WE.REGIST_JOB, params, xml,
                                function (response) {
                                    // FIXME 에러 대응 필요.
                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success) {
                                        p.close();
                                    }
                                },
                                function (response) {
                                }
                            );
                        }
                    },
                    {
                        text: MSG.COMMON_CANCEL,
                        iconCls: 'common-cancel',
                        handler: function () {
                            popWindow.close();
                        }
                    }
                ]
            }).show();
        } else {
            msg(MSG.DIALOG_TITLE_WARN, 'Workflow Engine을 선택하십시오.');
        }
    },

    /**
     * Workflow Engine 목록 및 관련 화면을 갱신한다.
     */
    onRefreshEngineButton: function () {
        Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0].getStore().removeAll();
        Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0].getStore().load();
        Ext.ComponentQuery.query('workflowEngineEnvsPanel #environmentGrid')[0].getStore().removeAll();
        Ext.ComponentQuery.query('workflowEnginePropsPanel #propsGrid')[0].getStore().removeAll();
        Ext.ComponentQuery.query('workflowEngineTriggersPanel #triggersGrid')[0].getStore().removeAll();
        Ext.ComponentQuery.query('workflowEngineRunningJobsPanel #runningTrigger')[0].getStore().removeAll();
    },

    /**
     * 파일 시스템 브라우저를 화면에 표시한다.
     */
    onShowFileSystemBrowserButton: function () {
        var grid = Ext.ComponentQuery.query('workflowEnginesPanel #enginesGrid')[0];
        var selection = grid.getView().getSelectionModel().getSelection()[0];
        if (selection) {
            var popWindow = Ext.create('Ext.Window', {
                title: 'Local File System Browser',
                width: 350,
                height: 600,
                modal: true,
                resizable: true,
                constrain: true,
                padding: '5 5 5 5',
                layout: 'fit',
                items: [
                    Ext.create('Flamingo.view.component._AIOLocalFileSystemBrowser', {
                        engine: selection.data
                    })
                ],
                buttons: [
                    {
                        text: MSG.COMMON_OK,
                        iconCls: 'common-confirm',
                        handler: function () {
                            popWindow.close();
                        }
                    }
                ]
            }).show();
        } else {
            msg(MSG.DIALOG_TITLE_WARN, MSG.ADMIN_SELECT_WORKFLOW_ENG);
        }
    },

    /**
     * Workflow Engine Grid를 선택했을 때 하단의 Detail Panel의 정보를 갱신한다.
     */
    onItemClick: function (dv, selectedRecord, item, index, e) {
        if (selectedRecord) {
            var serverUrl = selectedRecord.data.serverUrl;

            var envGrid = Ext.ComponentQuery.query('workflowEngineEnvsPanel #environmentGrid')[0];
            envGrid.getStore().load(
                {
                    params: {
                        'serverUrl': serverUrl
                    }
                }
            );

            var propsGrid = Ext.ComponentQuery.query('workflowEnginePropsPanel #propsGrid')[0];
            propsGrid.getStore().load(
                {
                    params: {
                        'serverUrl': serverUrl
                    }
                }
            );

            var triggersGrid = Ext.ComponentQuery.query('workflowEngineTriggersPanel #triggersGrid')[0];
            triggersGrid.getStore().load(
                {
                    params: {
                        'serverUrl': serverUrl
                    }
                }
            );

            var runningTrigger = Ext.ComponentQuery.query('workflowEngineRunningJobsPanel #runningTrigger')[0];
            runningTrigger.getStore().load(
                {
                    params: {
                        'serverUrl': serverUrl
                    }
                }
            );
        }
    }
});