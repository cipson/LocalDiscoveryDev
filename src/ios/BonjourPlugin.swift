//
//  BonjourPlugin.swift
//  GuessGame
//
//  Created by Cipson Chiriyankandath on 2/22/16.
//  Copyright © 2016 Cipson Chiriyankandath. All rights reserved.
//

import Foundation
import AVFoundation

@objc(BonjourDiscovery)class BonjourDiscoveryPlugin : CDVPlugin{
    
    var bonjourBrowser:BonjourDiscoveryBrowser!
    var bonjourDeviceArray = [[String: String]]()
    var bonjourDiscoveryResult = [String: [[String: String]]]()
    var jsCallback: CDVInvokedUrlCommand!

    override func pluginInitialize () {
        NSLog("BonjourDiscovery plugin Initialize")
        bonjourDeviceArray = [[String: String]]()
        bonjourDiscoveryResult = [String: [[String: String]]]()
    }

    
    func onDeviceFound(deviceName: String, deviceIP: String) {
        var deviceObject = [String: String]()
        deviceObject["name"] = deviceName;
        deviceObject["ipAddress"] = deviceIP;
        NSLog("deviceName: \(deviceName)")
        NSLog("deviceIP: \(deviceIP)")
        bonjourDeviceArray.append(deviceObject)
        
    }

    func onSearchFinished() {
        bonjourDiscoveryResult["deviceList"] = bonjourDeviceArray
        if NSJSONSerialization.isValidJSONObject(bonjourDiscoveryResult) { // True
            do {
                let rawData = try NSJSONSerialization.dataWithJSONObject(bonjourDiscoveryResult, options: NSJSONWritingOptions(rawValue: 0))
                let jsonString = String(data: rawData, encoding: NSUTF8StringEncoding) 
                NSLog("Bonjour discovery result: \(jsonString)")
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: jsonString)
                commandDelegate!.sendPluginResult(pluginResult, callbackId:jsCallback.callbackId)
            } catch {
                let pluginResult = CDVPluginResult(status: CDVCommandStatus_OK, messageAsString: "")
                commandDelegate!.sendPluginResult(pluginResult, callbackId:jsCallback.callbackId)
            }
        }
        
    }

    func start_scan(command: CDVInvokedUrlCommand) {
        jsCallback = command
        bonjourBrowser = BonjourDiscoveryBrowser()
        bonjourBrowser.searchForOnboarding(onDeviceFound, onSearchFinished: onSearchFinished)
    }
    
    func stop_scan(command: CDVInvokedUrlCommand) {
        jsCallback = command
        if ((bonjourBrowser) != nil) {
            bonjourBrowser.stopSearchForOnboarding()
            bonjourBrowser = nil
        }
   }

}