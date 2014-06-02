Ext.define('HighCharts.model.TempSummary', {
    extend: 'Ext.data.Model',
    fields: [
        {
            name: 'time',
            type: 'string'
        },
        {
            name: 'temperature',
            type: 'int'
        }
    ]
});
