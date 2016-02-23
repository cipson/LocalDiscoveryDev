package com.wdc.plugins.mdnsdiscovery;

import com.wdc.plugins.mdnsdiscovery.events.EventBase;
import com.wdc.plugins.mdnsdiscovery.events.EventTask;
import com.wdc.plugins.mdnsdiscovery.mdns.MDNSDeviceItem;
import com.wdc.plugins.mdnsdiscovery.mdns.MDNSFinderService;

import java.util.List;

public class DeviceFinder
{
	MDNSFinderService _finder;
	
	public DeviceFinder()
	{
	}
	
	boolean _started = false;
	synchronized public boolean Start()
	{
		if(_started)
		{
			return true;
		}
		_started = true;

		try
		{
			_finder = MDNSFinderService.GetInstance();
			return _finder.Start(DeviceFoundHandler, FinishedHandler);
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	EventTask<MDNSFinderService.DeviceFoundEventArgs> DeviceFoundHandler = new EventTask<MDNSFinderService.DeviceFoundEventArgs>()
	{
		@Override
		public void run(MDNSFinderService.DeviceFoundEventArgs args) 
		{
			DeviceFoundHandlerWorker(args);
		}
	};
	
	EventTask<MDNSFinderService.FinishedEventArgs> FinishedHandler = new EventTask<MDNSFinderService.FinishedEventArgs>()
	{
		@Override
		public void run(MDNSFinderService.FinishedEventArgs args) 
		{
			DeviceFinderFinishedHandlerWorker(args);
		}
	};
	
	boolean _firstEvent = true;
	synchronized void DeviceFoundHandlerWorker(MDNSFinderService.DeviceFoundEventArgs args)
	{
		if(_firstEvent)
		{
			_firstEvent = false;
			RaiseEventsForAlreadyFoundDevices(args.FoundDeviceList);
		}
		else
		{
			if(ValidateDevice(args.FoundDevice))
			{
				//raise found event
				DeviceFoundEvent.RaiseEvent(new DeviceFoundEventArgs(args.FoundDevice));
			}
		}
	}
	
	synchronized void DeviceFinderFinishedHandlerWorker(MDNSFinderService.FinishedEventArgs args)
	{
		if(_firstEvent)
		{
			_firstEvent = false;
			RaiseEventsForAlreadyFoundDevices(args.FoundDeviceList);
		}
		
		//raise finished event
		FinishedEvent.RaiseEvent(new FinishedEventArgs(args.Success));
	}
	
	void RaiseEventsForAlreadyFoundDevices(List<MDNSDeviceItem> foundDeviceList)
	{
		for(MDNSDeviceItem item : foundDeviceList)
		{
			if(ValidateDevice(item))
			{
				//raise found event
				DeviceFoundEvent.RaiseEvent(new DeviceFoundEventArgs(item));
			}
		}
	}
	
	protected boolean ValidateDevice(MDNSDeviceItem device)
	{
		//return true;
		return device != null;
	}

	
	
	//
	// event code
	//
	public EventBase<DeviceFoundEventArgs> DeviceFoundEvent = new EventBase<DeviceFoundEventArgs>();
	public EventBase<FinishedEventArgs> FinishedEvent = new EventBase<FinishedEventArgs>();

	public class DeviceFoundEventArgs
	{
		public DeviceFoundEventArgs(MDNSDeviceItem device)
		{
			Device = device;
		}
		public MDNSDeviceItem Device; 
	}
	
	public class FinishedEventArgs
	{
		public FinishedEventArgs(boolean success)
		{
			Success = success;
		}
		public boolean Success;
	}
	
}
