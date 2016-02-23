//
//  BonjourDiscovery.swift
//  GuessGame
//
//  Created by Cipson Chiriyankandath on 2/22/16.
//  Copyright © 2016 Cipson Chiriyankandath. All rights reserved.
//

import Foundation

class BonjourDiscoveryBrowser:NSObject, NSNetServiceBrowserDelegate, NSNetServiceDelegate {
    
    let smbType:String
    var smbBrowser:NSNetServiceBrowser
    var serviceList:[NSNetService]
    var callbackDeviceFound: ((String, String) -> Void)?
    var callbackSearchFinished: (() -> Void)?
    var bonjourDeviceArray = [String]()
    
    override init() {
        self.smbType = "_wd-mycloud._tcp." //"_wd-2go._tcp." //"_smb._tcp."
        self.smbBrowser = NSNetServiceBrowser()
        self.serviceList = [NSNetService]()
        super.init()
        self.smbBrowser.delegate = self
    }
    
    func netServiceBrowser(aNetServiceBrowser: NSNetServiceBrowser,
        didFindService aNetService: NSNetService, moreComing: Bool) {
            NSLog("Found: \(aNetService)")
            NSLog("moreComing: \(moreComing)")
            NSLog("hostName: \(aNetService.name)")
            bonjourDeviceArray.append(aNetService.name)
            aNetService.delegate = self
            aNetService.resolveWithTimeout(5)
            serviceList.append(aNetService)
            
    }
    
    func searchForOnboarding(onDeviceFound: (String, String) -> Void, onSearchFinished: () -> Void){
        NSLog("Starting search..")
        callbackDeviceFound = onDeviceFound
        callbackSearchFinished = onSearchFinished
        smbBrowser.searchForServicesOfType(smbType, inDomain: "")
    }
    
    func stopSearchForOnboarding(){
        smbBrowser.stop()
    }
    
    func netServiceDidResolveAddress(sender: NSNetService) {
        if let addresses = sender.addresses
        {
            for address in addresses
            {
                let ptr = UnsafePointer<sockaddr_in>(address.bytes)
                var addr = ptr.memory.sin_addr
                let buf = UnsafeMutablePointer<Int8>.alloc(Int(INET6_ADDRSTRLEN))
                var family = ptr.memory.sin_family
                var ipc = UnsafePointer<Int8>()
                if family == __uint8_t(AF_INET)
                {
                    ipc = inet_ntop(Int32(family), &addr, buf, __uint32_t(INET6_ADDRSTRLEN))
                }
                else if family == __uint8_t(AF_INET6)
                {
                    let ptr6 = UnsafePointer<sockaddr_in6>(address.bytes)
                    var addr6 = ptr6.memory.sin6_addr
                    family = ptr6.memory.sin6_family
                    ipc = inet_ntop(Int32(family), &addr6, buf, __uint32_t(INET6_ADDRSTRLEN))
                }
                
                if let ip = String.fromCString(ipc)
                {
                    NSLog("IP Address \(ip)")
                    NSLog("Host name \(sender.hostName)")
                    NSLog("Host name \(sender.name)")
                    callbackDeviceFound!(sender.hostName!, ip)
                    let index = bonjourDeviceArray.indexOf(sender.name)
                    bonjourDeviceArray.removeAtIndex(index!)
                    if bonjourDeviceArray.isEmpty
                    {
                        callbackSearchFinished!()
                    }
                    break
                }
            }
        }
    }

}


