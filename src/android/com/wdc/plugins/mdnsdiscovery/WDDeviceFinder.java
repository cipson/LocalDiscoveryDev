package com.wdc.plugins.mdnsdiscovery;

import com.wdc.plugins.mdnsdiscovery.mdns.MDNSDeviceItem;

import java.util.Locale;

public class WDDeviceFinder extends DeviceFinder
{
	protected boolean ValidateDevice(MDNSDeviceItem device)
	{
    	try
    	{
	        if (super.ValidateDevice(device))
	        {
	        	if(device.getmanufacturer().toLowerCase(Locale.US).contains("western digital"))
				{
					return true;
				}
			}
    	}
    	catch(Exception e)
    	{
    		//Log.d("WDDeviceFinder.ValidateDevice", e.getMessage());
    	}
        return false;
	}
}
