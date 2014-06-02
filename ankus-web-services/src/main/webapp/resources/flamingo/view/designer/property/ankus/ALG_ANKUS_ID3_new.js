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

Ext.ns('Flamingo.view.designer.property.ankus');
Ext.define('Flamingo.view.designer.property.ankus.ALG_ANKUS_ID3', {
    extend: 'Flamingo.view.designer.property._NODE_ALG',
    alias: 'widget.ALG_ANKUS_ID3',

    requires: [
        'Flamingo.view.designer.property._ConfigurationBrowserField',
        'Flamingo.view.designer.property._BrowserField',
        'Flamingo.view.designer.property._ColumnGrid',
        'Flamingo.view.designer.property._DependencyGrid',
        'Flamingo.view.designer.property._NameValueGrid',
        'Flamingo.view.designer.property._KeyValueGrid',
        'Flamingo.view.designer.property._EnvironmentGrid',
        'Flamingo.model.designer.Preview'
    ],

    controllers: ['Flamingo.controller.designer.DesignerController'],

    width: 500,
    height: 640,

    items: [
        {
            title: MSG.DESIGNER_TITLE_PARAMETER,
            xtype: 'form',
            border: false,
            autoScroll: true,
            defaults: {
                labelWidth: 200
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                // Ankus MapReduce가 동작하는데 필요한 입력 경로를 지정한다.  이 경로는 N개 지정가능하다.
                {
                    xtype: '_inputGrid',
                    title: MSG.DESIGNER_TITLE_INPUT_PATH,
                    flex: 1
                },
                {
                    xtype: 'tbspacer',
                    height: 10
                },
                {
                    xtype: 'fieldcontainer',
                    fieldLabel: MSG.DESIGNER_COL_DELIMITER,
                    tooltip: MSG.DESIGNER_MSG_COL_DELIMITER,
                    layout: 'hbox',
                    items: [
                        {
                            xtype: 'fieldcontainer',
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: 'combo',
                                    name: 'delimiter',
                                    value: '\\t',
                                    flex: 1,
                                    forceSelection: true,
                                    multiSelect: false,
                                    editable: false,
                                    readOnly: this.readOnly,
                                    displayField: 'name',
                                    valueField: 'value',
                                    mode: 'local',
                                    queryMode: 'local',
                                    triggerAction: 'all',
                                    tpl: '<tpl for="."><div class="x-boundlist-item" data-qtip="{description}">{name}</div></tpl>',
                                    store: Ext.create('Ext.data.Store', {
                                        fields: ['name', 'value', 'description'],
                                        data: [
                                            {name: MSG.COMMON_DOUBLE_COLON, value: '::', description: '::'},
                                            {name: MSG.COMMON_COMMA, value: ',', description: ','},
                                            {name: MSG.COMMON_TAB, value: '\\t', description: '\\t'},
                                            {name: MSG.COMMON_BLANK, value: '\\s', description: '\\s'},
                                            {name: MSG.COMMON_USER_DEFINED, value: 'CUSTOM', description: MSG.COMMON_USER_DEFINED}
                                        ]
                                    }),
                                    listeners: {
                                        change: function (combo, newValue, oldValue, eOpts) {
                                            // 콤보 값에 따라 관련 textfield 를 enable | disable 처리한다.
                                            var customValueField = combo.nextSibling('textfield');
                                            if (newValue === 'CUSTOM') {
                                                customValueField.enable();
                                                customValueField.isValid();
                                            } else {
                                                customValueField.disable();
                                                if (newValue) {
                                                    customValueField.setValue(newValue);
                                                } else {
                                                    customValueField.setValue('::');
                                                }
                                            }
                                        }
                                    }
                                },
                                {
                                    xtype: 'textfield',
                                    name: 'delimiterValue',
                                    vtype: 'exceptcommaspace',
                                    flex: 1,
                                    disabled: true,
                                    readOnly: this.readOnly,
                                    allowBlank: false,
                                    value: '\\t'
                                }
                            ]
                        }
                    ]
                },
                {
                    xtype: 'button',
                    text: 'Preview file from HDFS',
//                    iconCls: 'common-confirm',
                    handler: function (grid, rowIndex, colIndex) {

                        // Parameter form
                        var canvas = Ext.ComponentQuery.query('form')[1];
                        var form = canvas.getForm();

                        // Preview grid
                        var previewGrid = Ext.ComponentQuery.query('#previewGrid')[0];

//                        previewGrid.setSource({
//                            fir
//                        })

                        // Input paths grid
                        var inputGrid = Ext.ComponentQuery.query('_inputGrid')[0];
                        var selectedInputPath = inputGrid.getView().getSelectionModel().getSelection();

                        if (selectedInputPath[0] === undefined) {
                            msg('Select input path', 'Please select input path from File Path grid.');
                            return;
                        }

                        var inputPath = selectedInputPath[0].data.input;
                        var delimiter = form.getValues()['delimiter'];

                        var store = Ext.create('Ext.data.Store', {
                            fields: [
                                {name: 'columnIndex'},
                                {name: 'rowData'}
                            ],
                            autoLoad: true,
                            proxy: {
                                type: 'ajax',
                                url: CONSTANTS.CONTEXT_PATH + CONSTANTS.DESIGNER.LOAD_HDFS_FILE,
                                headers: {
                                    'Accept': 'application/json'
                                },
                                reader: {
                                    type: 'json',
                                    root: 'list'
                                },
                                extraParams: {
                                    'inputPath': inputPath,
                                    'delimiter': delimiter
                                }
                            }
                        });

                        // Set grid row to preview file from hdfs
                        var rec;
                        var columnIndexList = new Array();
                        var rowDataList = new Array();
                        var cIndexList = ['Field', 'Row Data', 'Target', 'Exception', 'Class'];

                        store.on('load', function (store, records) {

                            console.log('records....')
                            console.log(records)
                            console.log(cIndexList)
                            console.log(records[0].data.columnIndex)
                            console.log(records[0].data.rowData)

                            var columnIndexList = records[0].data.columnIndex;
                            var rowDataList = records[0].data.rowData;

                            columnIndexList.push(111);
                            columnIndexList.push(333);
//                            columnIndexList.push(records[0].get('columnIndex'));
//                            rowDataList.push(records[0].get('rowData'));

                            for (var i = 0; i < cIndexList.length; i++) {
                                rec = new Flamingo.model.designer.Preview({
//                                            rIndex: columnIndexList[i] + '|||||' + rowDataList[i],
//                                    rIndex: columnIndexList[i],
                                    rIndex: rowDataList[i],
//                                        rData: rowDataList[k],
                                    cIndex: cIndexList[i]
                                });


                                console.log('for----------')
                                console.log(columnIndexList.pop())

                                store.insert(0, rec);
                                //Remove get list from ajax
                                store.remove(records);
                                previewGrid.store.sort('rIndex', 'ASC');
                            }
//                                for (var j = 0; j < records.length; j++) {
//                                columnIndexList.push(records[j].get('columnIndex'));
//                                rowDataList.push(records[j].get('rowData'));
//

//                                    console.log(columnIndexList)
//                                    console.log(rowDataList)
//
////                                    for (var k = 0; k < columnIndexList.length; k++) {
//                                        rec = new Flamingo.model.designer.Preview({
////                                            rIndex: columnIndexList[i] + '|||||' + rowDataList[i],
//                                            rIndex: columnIndexList[i],
//    //                                        rData: rowDataList[k],
//                                            cIndex: cIndexList[i]
//                                        });
//
//                                        store.add();
//                                        store.insert(1, rec);
//                                        //Remove get list from ajax
//                                        store.remove(records);
//                                        previewGrid.store.sort('rIndex', 'ASC');
////                                    }
//                                }
//                            }
                        });
                        console.log('columnIndexList....')
                        console.log(columnIndexList)
                        console.log(rowDataList)

//                        store.on('load', function (store, records) {
//                            for (var i = 0; i < records.length; i++) {
//                                columnIndexList = records[i].get('columnIndex');
//                                rowDataList = records[i].get('rowData');
////                                cIndexList.set('field', 'Field');
////                                cIndexList.set('data', 'Data');
////                                cIndexList.set('taraget', 'Target');
//
//                                for (var k = 0; k < columnIndexList.length; k++) {
//                                    rec = new Flamingo.model.designer.Preview({
//                                        rIndex: columnIndexList[k],
//                                        rData: rowDataList[k],
//                                        cIndex: cIndexList[k]
//                                    });
//
//
//                                    console.log(rowDataList[k])
//                                    console.log(cIndexList[k])
//
//                                    store.insert(0, rec);
//                                    //Remove get list from ajax
//                                    store.remove(records);
//                                    previewGrid.store.sort('rIndex', 'ASC');
//                                }
//                            }
//                        });

                        Ext.suspendLayouts();
                        previewGrid.reconfigure(store, [
                            {
                                text: '',
                                dataIndex: 'cIndex',
                                id: 'cIndex',
                                width: 80
                            },
                            {
                                text: 'Field',
                                dataIndex: 'rIndex',
                                id: 'rIndex',
                                width: 80
                            },
                            {
                                text: 'Row Data',
                                dataIndex: 'rData',
                                flex: 1
                            },
                            {
                                xtype: 'checkcolumn',
                                width: 55,
                                header: 'Target',
                                dataIndex: 'targetCheckIndex',
                                listeners: {
                                    checkchange: function (column, recordIndex, checked) {
                                        var record = previewGrid.getStore().getAt(recordIndex);
                                        var dataIndex = this.dataIndex;
                                        checked = !record.get(dataIndex);

                                        record.set('exceptionCheckIndex', checked);
                                    }
                                }
                            },
                            {
                                xtype: 'checkcolumn',
                                width: 65,
                                header: 'Exception',
                                dataIndex: 'exceptionCheckIndex',
                                listeners: {
                                    checkchange: function (column, recordIndex, checked) {
                                        var record = previewGrid.getStore().getAt(recordIndex);
                                        var dataIndex = this.dataIndex;
                                        checked = !record.get(dataIndex);

                                        record.set('targetCheckIndex', checked);
                                    }
                                }
                            },
                            {
                                xtype: 'checkcolumn',
                                width: 65,
                                header: 'Class',
                                dataIndex: 'classCheckIndex',
                                listeners: {
                                    checkchange: function (column, recordIndex, checked) {

                                        //TODO 클릭할 때 나머지 체크해지 되는거 뭔가 이상함.. 오류남... 나중에 확인하기 .4/23
                                        var rowCount = previewGrid.getStore().data.length;
                                        var record = previewGrid.getStore().getAt(recordIndex);
                                        var dataIndex = this.dataIndex;
                                        checked = !record.get(dataIndex);

                                        // 하나 체크할 때 나머지는 체크 해지
                                        for (var i = 0; i < rowCount; i++) {
                                            if (i != recordIndex) {
                                                previewGrid.getStore().getAt(i).set(dataIndex, checked);
                                            }
                                        }
                                        record.set('exceptionCheckIndex', checked);
                                    }
                                }
                            }
                        ]);
                        Ext.resumeLayouts(true);
                    }
                },
                {
                    margin: '10 0 0 0',
                    xtype: 'grid',
                    renderTo: 'grid-container',
                    source: {
                        "(name)": "Properties Grid",
                        "grouping": false,
                        "autoFitColumns": true,
                        "productionQuality": false,
                        "created": Ext.Date.parse('10/15/2006', 'm/d/Y'),
                        "tested": false,
                        "version": 0.01,
                        "borderWidth": 1
                    },
                    minHeight: 100,
//                    height: 130,
                    layout: 'fit',
                    itemId: 'previewGrid',
                    multiSelect: true,
                    columns: [
                        {
                            text: 'Column',
                            width: 80,
                            dataIndex: 'cIndex'
                        },
                        {
                            text: '',
                            width: 80,
                            dataIndex: 'rIndex'
                        },
                        {
                            text: '',
                            flex: 1,
                            dataIndex: 'rData'
                        },
                        {
                            xtype: 'checkcolumn',
                            width: 55,
                            header: '',
                            dataIndex: 'targetCheckIndex',
                            editor: {
                                xtype: 'checkbox',
                                cls: 'x-grid-checkheader-editor'
                            }
                        },
                        {
                            xtype: 'checkcolumn',
                            width: 65,
                            text: '',
                            dataIndex: 'exceptionCheckIndex'
                        },
                        {
                            xtype: 'checkcolumn',
                            width: 65,
                            text: '',
                            dataIndex: 'classCheckIndex'
                        }
                    ],
                    tbar: [
                        {
                            text: 'All',
                            scope: this,
                            margin: '0 0 0 300',
                            handler: function (store) {
                                var previewGrid = Ext.ComponentQuery.query('#previewGrid')[0];
                                var range = previewGrid.store.getRange();
                                for (var i = 0; i < range.length; i++) {
                                    if (range[i] != null) {
                                        var record = previewGrid.getStore().getAt(i);
                                        record.set('targetCheckIndex', true);
                                        record.set('exceptionCheckIndex', false);
                                    }
                                }
                            }
                        },
                        {

                            text: 'All',
                            scope: this,
                            margin: '0 0 0 28',
                            handler: function (store) {
                                var previewGrid = Ext.ComponentQuery.query('#previewGrid')[0];
                                var range = previewGrid.store.getRange();

                                for (var i = 0; i < range.length; i++) {
                                    if (range[i] != null) {
                                        var record = previewGrid.getStore().getAt(i);
                                        record.set('exceptionCheckIndex', true);
                                        record.set('targetCheckIndex', false);
                                    }
                                }
                            }
                        }
                    ],
                    viewConfig: {
                        enableTextSelection: true,
                        emptyText: 'Click a button to show preview data from HDFS',
                        deferEmptyText: false
                    }
                },
                {
                    xtype: 'tbspacer',
                    height: 10
                },
                {
                    xtype: 'button',
                    text: 'Select field number',
                    iconCls: 'common-confirm',
                    handler: function (grid, rowIndex, colIndex) {
                        var previewGrid = Ext.ComponentQuery.query('#previewGrid')[0];
                        var r = previewGrid.store.getRange();
                        var targetCount = 0
                        var exceptionCount = 0;
                        var classCount = 0;
                        var record;

                        // Count checkbox from grid
                        for (var i = 0; i < r.length; i++) {
                            if (r[i] != null) {
                                record = previewGrid.getStore().getAt(i);
                                if (r[i].data.targetCheckIndex) targetCount++;
                                if (r[i].data.exceptionCheckIndex) exceptionCount++;
                                if (r[i].data.classCheckIndex) classCount++;
                            }
                        }

                        var targetIndexList = [];
                        var exceptionIndexList = [];
                        var classIndexList = [];

                        // Set checkbox index from gird
                        for (var i = 0; i < r.length; i++) {
                            if (r[i] != null) {
                                record = previewGrid.getStore().getAt(i);

                                // Set target attribute(index) list
                                if (targetCount != r.length && targetCount != 0) {
                                    if (r[i].data.targetCheckIndex) {
                                        targetIndexList.push(r[i].data.rIndex);
                                        if (targetCount === targetCount - 1) {
                                            targetIndexList.push(',');
                                        }
                                    }
                                }

                                // Set exception attribute(index) list
                                if (exceptionCount != r.length && exceptionCount != 0) {
                                    if (r[i].data.exceptionCheckIndex) {
                                        exceptionIndexList.push(r[i].data.rIndex);
                                        if (exceptionCount === exceptionCount - 1) {
                                            exceptionIndexList.push(',');
                                        }
                                    }
                                }

                                // Set class attribute(index)
                                if (classCount != r.length && classCount != 0) {
                                    if (r[i].data.classCheckIndex) {
                                        classIndexList.push(r[i].data.rIndex);
                                        if (classCount === classCount - 1) {
                                            classIndexList.push(',');
                                        }
                                    }
                                }
                            }
                        }

                        // Set textfiled by grid
                        if (targetCount == r.length) {
                            Ext.getCmp('indexList').setValue('-1');
                        } else if (targetCount === 0) {
                            Ext.getCmp('indexList').setValue('');
                        } else {
                            Ext.getCmp('indexList').setValue(targetIndexList);
                        }

                        if (exceptionCount == r.length) {
                            Ext.getCmp('exceptionIndexList').setValue('-1');
                        } else if (exceptionCount === 0) {
                            Ext.getCmp('exceptionIndexList').setValue('');
                        } else {
                            Ext.getCmp('exceptionIndexList').setValue(exceptionIndexList);
                        }

                        if (classCount == r.length) {
                            Ext.getCmp('classIndex').setValue('-1');
                        } else if (classCount === 0) {
                            Ext.getCmp('classIndex').setValue('');
                        } else {
                            Ext.getCmp('classIndex').setValue(classIndexList);
                        }
                    }
                },
                {
                    xtype: 'fieldset',
                    height: 100,
                    title: 'Select Parameter Option',
                    layout: 'anchor',
                    defaults: {
                        anchor: '100%',
                        labelWidth: 200,
                        hideEmptyLabel: false
                    },
                    items: [
                        // Add select index parameters
                        {
                            xtype: 'textfield',
                            name: 'indexList',
                            id: 'indexList',
                            fieldLabel: MSG.DESIGNER_ID3_INCLUDE_LIST,
                            tooltip: MSG.DESIGNER_MSG_ID3_INCLUDE_LIST,
                            vtype: 'commaseperatednum',
                            allowBlank: false
                        },
                        {
                            xtype: 'textfield',
                            name: 'exceptionIndexList',
                            id: 'exceptionIndexList',
                            fieldLabel: MSG.DESIGNER_ID3_EXCLUDE_LIST,
                            vtype: 'commaseperatednum',
                            allowBlank: true
                        },
                        {
                            xtype: 'textfield',
                            id: 'classIndex',
                            name: 'classIndex',
                            fieldLabel: MSG.DESIGNER_ID3_CLASS_ATTRIBUTE,
                            vtype: 'numeric',
                            allowBlank: false
                        }
                    ]
                },
                {
                    xtype: 'fieldset',
                    height: 140,
                    title: 'Input Parameter Option',
                    layout: 'anchor',
                    defaults: {
                        anchor: '100%',
                        labelWidth: 200,
                        hideEmptyLabel: false
                    },
                    items: [
                        {
                            xtype: 'textfield',
                            name: 'minLeafData',
                            fieldLabel: MSG.DESIGNER_ID3_PRUNING_MINIMUM,
                            value: 2,
                            vtype: 'numeric',
                            allowBlank: false
                        },
                        {
                            xtype: 'textfield',
                            name: 'purity',
                            fieldLabel: MSG.DESIGNER_ID3_PRUNING_PURITY,
                            value: 0.75,
                            allowBlank: false
                        },
                        {
                            xtype: 'radiogroup',
                            fieldLabel: MSG.DESIGNER_ID3_RESULT_GENERATION,
                            allowBlank: false,
                            columns: 2,
                            itemId: 'myRadio',
                            items: [
                                {
                                    xtype: 'radiofield',
                                    boxLabel: 'True',
                                    name: 'finalResultGen',
                                    checked: false,
                                    inputValue: 'true'
                                },
                                {
                                    xtype: 'radiofield',
                                    boxLabel: 'False',
                                    name: 'finalResultGen',
                                    checked: true,
                                    inputValue: 'false'
                                }
                            ]
                        },
                        // Ankus MapReduce가 동작하는데 필요한 출력 경로를 지정한다. 이 경로는 오직 1개만 지정가능하다.
                        {
                            xtype: 'fieldcontainer',
                            fieldLabel: MSG.DESIGNER_TITLE_OUTPUT_PATH,
                            defaults: {
                                hideLabel: true,
                                margin: "5 0 0 0"  // Same as CSS ordering (top, right, bottom, left)
                            },
                            layout: 'hbox',
                            items: [
                                {
                                    xtype: '_browserField',
                                    name: 'output',
                                    allowBlank: false,
                                    readOnly: false,
                                    flex: 1
                                }
                            ]
                        }
                    ]
                }
            ]
        },
        {
            title: MSG.DESIGNER_TITLE_MAPREDUCE,
            xtype: 'form',
            border: false,
            autoScroll: true,
            defaults: {
                labelWidth: 100
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                {
                    xtype: 'textfield',
                    name: 'jar',
                    fieldLabel: MSG.DESIGNER_MR_JAR,
                    value: 'org.ankus:ankus-core:0.1',
                    disabledCls: 'disabled-plain',
                    allowBlank: false
                },
                {
                    xtype: 'textfield',
                    name: 'driver',
                    fieldLabel: MSG.DESIGNER_DRIVER,
                    value: 'ID3',
                    disabledCls: 'disabled-plain',
                    allowBlank: false
                }
            ]
        },
        {
            title: MSG.DESIGNER_TITLE_H_CONFIG,
            xtype: 'form',
            border: false,
            autoScroll: true,
            defaults: {
                labelWidth: 100
            },
            layout: {
                type: 'vbox',
                align: 'stretch'
            },
            items: [
                /*
                 {
                 xtype: '_configurationBrowserField'
                 },
                 */
                {
                    xtype: '_keyValueGrid',
                    flex: 1
                }
            ]
        }
    ],

    /**
     * UI 컴포넌트의 Key를 필터링한다.
     *
     * ex) 다음과 같이 필터를 설정할 수 있다.
     * propFilters: {
     *     // 추가할 프라퍼티
     *     add   : [
     *         {'test1': '1'},
     *         {'test2': '2'}
     *     ],
     *
     *     // 변경할 프라퍼티
     *     modify: [
     *         {'delimiterType': 'delimiterType2'},
     *         {'config': 'config2'}
     *     ],
     *
     *     // 삭제할 프라퍼티
     *     remove: ['script', 'metadata']
     * }
     */
    propFilters: {
        add: [],
        modify: [],
        remove: ['config']
    },

    /**
     * MapReduce의 커맨드 라인 파라미터를 수동으로 설정한다.
     * 커맨드 라인 파라미터는 Flamingo Workflow Engine에서 props.mapreduce를 Key로 꺼내어 구성한다.
     *
     * @param props UI 상에서 구성한 컴포넌트의 Key Value값
     */
    afterPropertySet: function (props) {
        props.mapreduce = {
            "driver": props.driver ? props.driver : '',
            "jar": props.jar ? props.jar : '',
            "confKey": props.hadoopKeys ? props.hadoopKeys : '',
            "confValue": props.hadoopValues ? props.hadoopValues : '',
            params: []
        };

        if (props.input) {
            props.mapreduce.params.push("-input", props.input);
        }

        if (props.output) {
            props.mapreduce.params.push("-output", props.output);
        }

        if (props.indexList) {
            props.mapreduce.params.push("-indexList", props.indexList);
        }

        if (props.exceptionIndexList) {
            props.mapreduce.params.push("-exceptionIndexList", props.exceptionIndexList);
        }

        if (props.classIndex) {
            props.mapreduce.params.push("-classIndex", props.classIndex);
        }

        if (props.minLeafData) {
            props.mapreduce.params.push("-minLeafData", props.minLeafData);
        }

        if (props.purity) {
            props.mapreduce.params.push("-purity", props.purity);
        }

        if (props.finalResultGen) {
            props.mapreduce.params.push("-finalResultGen", props.finalResultGen);
        }

        if (props.delimiter) {
            if (props.delimiter == 'CUSTOM') {
                props.mapreduce.params.push("-delimiter", props.delimiterValue);
            } else {
                props.mapreduce.params.push("-delimiter", props.delimiter);
            }
        }

        this.callParent(arguments);
    }


});

