Ext.define('Highcharts.model.BubbleSingle', {
    extend: 'Ext.data.Model',
    config: {
        fields: [
            {
                name: 'x',
                type: 'integer'
            },
            {
                name: 'y',
                type: 'integer'
            },
            {
                name: 'r',
                type: 'integer'
            }
        ]
    }
});
