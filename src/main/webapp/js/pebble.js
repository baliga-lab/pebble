/*
 * pebble.js - Javascript API for the Pebble service.
 * Assumes availability of
 * - jQuery
 * - Highcharts API
 */
var pebble;
if (!pebble) {
    pebble = {};
}
(function() {
    "use strict";
    // This web service's application context
    var pebbleContext = 'http://druid.systemsbiology.net:8080';

    function makeIntListString(intArray) {
        var result = '';
        var i;
        for (i = 0; i < intArray.length; i++) {
            if (i > 0) {
                result += ',';
            }
            result += intArray[i];
        }
        return result;
    }

    function dmvTable(id, dataurl) {
        $.ajax({
            url: dataurl,
            dataType: 'html',
            success: function (htmlText) {
                $(htmlText).replaceAll('#' + id);
            }
        });
    }

    function highChartsLineChart(id, dataurl) {
        $.ajax({
            url: dataurl,
            dataType: 'json',
            success: function (jsonData) {
                new Highcharts.Chart(jsonData);
                $('#' + id).show();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                console.debug('error, jqXHR = ' + jqXHR + ' textStatus: ' + textStatus + ' errorThrown: ' + errorThrown);
            }
        });
    }

    function makeDataUrlFromQuery(serviceURI, chartId, query) {
        return pebbleContext + '/' + serviceURI + '?rows=' + makeIntListString(query.genes) +
            '&chartId=' + chartId + '&query=' + JSON.stringify(query.data);
    }

    // Public API
    pebble.lambdaLineChart = function(id, query) {
        highChartsLineChart(id, makeDataUrlFromQuery('highcharts/lambdas', id, query));
    };
    pebble.ratioLineChart = function(id, query) {
        highChartsLineChart(id, makeDataUrlFromQuery('highcharts/ratios', id, query));
    };

    pebble.dmvTable = function(id, query) {
        dmvTable(id, pebbleContext + '/highcharts/dummy?bla=0');
    };
}());