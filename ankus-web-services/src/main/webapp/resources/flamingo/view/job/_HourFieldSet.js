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

/**
 * Hour Cron Expression Builder FieldSet
 *
 * @class Job.view._HourFieldSet
 * @extends Ext.form.FieldSet
 * @author <a href="mailto:hrkenshin@gmail.com">Seungbaek Lee</a>
 */
Ext.define('Flamingo.view.job._HourFieldSet', {
    extend: 'Ext.form.FieldSet',
    alias: 'widget._hourFieldSet',

    title: '시간단위 설정',
    collapsible: false,
    items: [
        {
            xtype: 'fieldcontainer',
            layout: 'hbox',
            items: [
                {
                    itemId: 'HOUR_TYPE',
                    name: 'hour_type',
                    xtype: 'combo',
                    store: Ext.create('Ext.data.Store', {
                        fields: ['value', 'name', 'desc'],
                        data: [
                            {value: 'ALL', name: '매시간 마다', desc: ''},
                            {value: 'RANGE', name: '범위로 지정', desc: ''},
                            {value: 'INCREMENT', name: '증분으로 지정', desc: ''},
                            {value: 'SPECIFIC', name: '명시적으로 지정', desc: ''}
                        ]
                    }),
                    tpl: '<tpl for="."><div class="x-boundlist-item" data-qtip="{desc}">{name}</div></tpl>',
                    queryMode: 'local',
                    displayField: 'name',
                    valueField: 'value',
                    value: 'ALL',
                    width: 150,
                    padding: '0 10 0 0',
                    editable: false,
                    listeners: {
                        change: function (combo, newValue, oldValue, eOpts) {
                            var cronTrigger = combo.up('cronTrigger'),
                                container = combo.up('fieldcontainer'),
                                range = container.getComponent('HOUR_RANGE'),
                                increment = container.getComponent('HOUR_INCREMENT'),
                                specific = container.getComponent('HOUR_SPECIFIC');

                            // hide all
                            range.setDisabled(true);
                            range.hide();
                            increment.setDisabled(true);
                            increment.hide();
                            specific.setDisabled(true);
                            specific.hide();

                            // show by type
                            switch (newValue) {
                                case 'RANGE' :
                                    range.setDisabled(false);
                                    range.show();
                                    break;
                                case 'INCREMENT' :
                                    increment.setDisabled(false);
                                    increment.show();
                                    break;
                                case 'SPECIFIC' :
                                    specific.setDisabled(false);
                                    specific.show();
                                    break;
                                case 'NTH' :
                                    nth.setDisabled(false);
                                    nth.show();
                                    break;
                                default :
                                    break;
                            }

                            // Cron Expression 적용
                            if (cronTrigger) cronTrigger.toCronExpression();
                        }
                    }
                },
                {
                    itemId: 'HOUR_RANGE',
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    disabled: true,
                    hidden: true,
                    items: [
                        {
                            name: 'hour_from',
                            xtype: 'numberfield',
                            vtype: 'alphanum',
                            minValue: 0,
                            maxValue: 23,
                            width: 80,
                            allowBlank: false,
                            emptyText: '시작 시간',
                            listeners: {
                                change: function (field, newValue, oldValue, eOpts) {
                                    var cronTrigger = field.up('cronTrigger');
                                    if (cronTrigger) cronTrigger.toCronExpression();
                                }
                            }
                        },
                        {
                            xtype: 'displayfield',
                            padding: '0 5 0 5',
                            value: '-'
                        },
                        {
                            name: 'hour_to',
                            xtype: 'numberfield',
                            vtype: 'alphanum',
                            minValue: 0,
                            maxValue: 23,
                            width: 80,
                            allowBlank: false,
                            emptyText: '끝 시간',
                            listeners: {
                                change: function (field, newValue, oldValue, eOpts) {
                                    var cronTrigger = field.up('cronTrigger');
                                    if (cronTrigger) cronTrigger.toCronExpression();
                                }
                            }
                        }
                    ]
                },
                {
                    itemId: 'HOUR_INCREMENT',
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    disabled: true,
                    hidden: true,
                    items: [
                        {
                            name: 'hour_start',
                            xtype: 'numberfield',
                            vtype: 'alphanum',
                            minValue: 0,
                            maxValue: 23,
                            width: 80,
                            allowBlank: false,
                            emptyText: '시작 시간',
                            listeners: {
                                change: function (field, newValue, oldValue, eOpts) {
                                    var cronTrigger = field.up('cronTrigger');
                                    if (cronTrigger) cronTrigger.toCronExpression();
                                }
                            }
                        },
                        {
                            xtype: 'displayfield',
                            padding: '0 5 0 5',
                            value: '/'
                        },
                        {
                            name: 'hour_increment',
                            xtype: 'numberfield',
                            vtype: 'alphanum',
                            minValue: 1,
                            width: 80,
                            allowBlank: false,
                            emptyText: '증분',
                            listeners: {
                                change: function (field, newValue, oldValue, eOpts) {
                                    var cronTrigger = field.up('cronTrigger');
                                    if (cronTrigger) cronTrigger.toCronExpression();
                                }
                            }
                        }
                    ]
                },
                {
                    itemId: 'HOUR_SPECIFIC',
                    xtype: 'fieldcontainer',
                    layout: 'hbox',
                    disabled: true,
                    hidden: true,
                    items: [
                        {
                            name: 'hour_value',
                            xtype: 'textfield',
                            vtype: 'cronHourComma',
                            width: 175,
                            allowBlank: false,
                            emptyText: '콤마(,)로 구분된 시간(시)',
                            listeners: {
                                change: function (field, newValue, oldValue, eOpts) {
                                    var cronTrigger = field.up('cronTrigger');
                                    if (cronTrigger) cronTrigger.toCronExpression();
                                }
                            }
                        }
                    ]
                }
            ]
        }
    ],

    /**
     * 설정된 Cron Expression 값을 반환한다.
     *
     * @return {String} cron expression
     */
    getValue: function () {
        var fieldContainer = this.down('fieldcontainer'),
            typeCombo = fieldContainer.getComponent('HOUR_TYPE'),
            rangeContainer = fieldContainer.getComponent('HOUR_RANGE'),
            incrementContainer = fieldContainer.getComponent('HOUR_INCREMENT'),
            specificContainer = fieldContainer.getComponent('HOUR_SPECIFIC'),
            value1 = '_', value2 = '_';

        switch (typeCombo.getValue()) {
            case 'ALL':
                return '*';
            case 'RANGE':
                value1 = rangeContainer.down('numberfield[name=hour_from]').getValue();
                value2 = rangeContainer.down('numberfield[name=hour_to]').getValue();
                return (value1 = Ext.isEmpty(value1) ? '_' : value1) + '-' + (value2 = Ext.isEmpty(value2) ? '_' : value2);
            case 'INCREMENT':
                value1 = incrementContainer.down('numberfield[name=hour_start]').getValue();
                value2 = incrementContainer.down('numberfield[name=hour_increment]').getValue();
                return (value1 = Ext.isEmpty(value1) ? '_' : value1) + '/' + (value2 = Ext.isEmpty(value2) ? '_' : value2);
            case 'SPECIFIC':
                value1 = specificContainer.down('textfield[name=hour_value]').getValue();
                return Ext.isEmpty(value1) ? '_' : value1;
        }

        return '_';
    },

    /**
     * 주어진 파싱된 Cron Object 을 form 에 설정한다.
     *
     * @param {Object} cronObject 파싱된 Object
     */
    setValue: function (cronObject) {
        var fieldContainer = this.down('fieldcontainer'),
            typeCombo = fieldContainer.getComponent('HOUR_TYPE'),
            rangeContainer = fieldContainer.getComponent('HOUR_RANGE'),
            incrementContainer = fieldContainer.getComponent('HOUR_INCREMENT'),
            specificContainer = fieldContainer.getComponent('HOUR_SPECIFIC');

        switch (cronObject.type) {
            case 'ALL':
                typeCombo.setValue('ALL');
                break;
            case 'RANGE':
                typeCombo.setValue('RANGE');
                rangeContainer.down('numberfield[name=hour_from]').setValue(cronObject.value[0]);
                rangeContainer.down('numberfield[name=hour_to]').setValue(cronObject.value[1]);
                break;
            case 'INCREMENT':
                typeCombo.setValue('INCREMENT');
                incrementContainer.down('numberfield[name=hour_start]').setValue(cronObject.value[0]);
                incrementContainer.down('numberfield[name=hour_increment]').setValue(cronObject.value[1]);
                break;
            case 'SPECIFIC':
                typeCombo.setValue('SPECIFIC');
                specificContainer.down('textfield[name=hour_value]').setValue(cronObject.orgValue);
                break;
        }
    }
});

/**
 * cron vtype
 */
Ext.apply(Ext.form.field.VTypes, {
    cronHourComma: function (v) {
        var hourExp = /^(([0-9]|1[0-9]|2[0-3])|((([0-9]|1[0-9]|2[0-3]),)([0-9]|1[0-9]|2[0-3])(,([0-9]|1[0-9]|2[0-3]))*))$/;
        return hourExp.test(v);
    },
    cronHourCommaText: 'Must be a Quartz Cron Expression!'
});