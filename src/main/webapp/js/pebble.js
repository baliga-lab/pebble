/*
 * pebble.js - Javascript API for the Pebble service.
 * Assumes availability of
 * - jQuery
 * - Highcharts API
 * - JSON API
 */
var pebble;
if (!pebble) {
    pebble = {};
}
(function() {
    "use strict";
    // This web service's application context
    //var pebbleContext = 'http://druid.systemsbiology.net:8080';
    var pebbleContext = 'http://edgar.systemsbiology.net:8081/pebble';

    function makeStringListString(stringArray) {
        var result = '';
        var i;
        for (i = 0; i < stringArray.length; i++) {
            if (i > 0) {
                result += ',';
            }
            result += "'" + stringArray[i] + "'";
        }
        return result;
    }

    function dmvTable(id, dataurl) {
        console.debug('dmvTable, dataurl = ' + dataurl);
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
        return pebbleContext + '/' + serviceURI + '?vngNames=' + makeStringListString(query.vngNames) +
            '&chartId=' + chartId + '&query=' + JSON.stringify(query.data);
    }
    function makeUrlFromQuery(serviceURI, query) {
        return pebbleContext + '/' + serviceURI + '?query=' + JSON.stringify(query);
    }

    // Public API
    pebble.lambdaLineChart = function(id, query) {
        highChartsLineChart(id, makeDataUrlFromQuery('highcharts/lambdas', id, query));
    };
    pebble.ratioLineChart = function(id, query) {
        highChartsLineChart(id, makeDataUrlFromQuery('highcharts/ratios', id, query));
    };
    pebble.dmvTable = function(id, query) {
        dmvTable(id, makeUrlFromQuery('datatable', query));
    };
}());
