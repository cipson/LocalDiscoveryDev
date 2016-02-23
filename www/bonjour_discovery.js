/*global cordova, module*/

/*
module.exports = {
    bonjour_discovery: function (successCallback, errorCallback, action) {
        cordova.exec(successCallback, errorCallback, "bonjour_discovery", action, []);
    }
};
*/

var cordova = require('cordova');

/**
 * BonjourDiscovery plugin for Cordova
 * 
 * @constructor
 */
function BonjourDiscovery () {}

/**
 * Starts BonjourDiscovery scan
 *
 * @param {String}   text      The content to copy to the clipboard
 * @param {Function} onSuccess The function to call in case of success (takes the copied text as argument)
 * @param {Function} onFail    The function to call in case of error
 */
BonjourDiscovery.prototype.startScan = function (onSuccess, onFail) {
    cordova.exec(onSuccess, onFail, "BonjourDiscovery", "start_scan", []);
};

/**
 * Stops BonjourDiscovery scan
 *
 * @param {Function} onSuccess The function to call in case of success
 * @param {Function} onFail    The function to call in case of error
 */
BonjourDiscovery.prototype.stopScan = function (onSuccess, onFail) {
	cordova.exec(onSuccess, onFail, "BonjourDiscovery", "stop_scan", []);
};

// Register the plugin
var bonjourDiscovery = new BonjourDiscovery();
module.exports = bonjourDiscovery;
