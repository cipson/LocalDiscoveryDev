package com.wdc.plugins.mdnsdiscovery.mdns;

import com.wdc.plugins.mdnsdiscovery.events.EventBase;
import com.wdc.plugins.mdnsdiscovery.events.EventTask;

import java.util.ArrayList;
import java.util.List;

public class MDNSFinderService
{
	private MDNSFinderService()
	{
	}
	
	private static MDNSFinderService _instance;
	public static MDNSFinderService GetInstance()
	{
		if(_instance == null)
		{
			synchronized(MDNSFinderService.class)
			{
				if(_instance == null)
				{
					_instance = new MDNSFinderService();
				}				
			}
		}
		return _instance;
	}
	
	private List<MDNSDeviceItem> _foundDeviceList;
	
	private MDNSFinder _finder = null;
	
	synchronized public boolean Start(EventTask<DeviceFoundEventArgs> deviceFoundHandler, EventTask<FinishedEventArgs> finishedHandler)
	{
		DeviceFoundEvent.AddListener(deviceFoundHandler);
		FinishedEvent.AddListener(finishedHandler);
		
		if(_finder != null)
		{
			final List<MDNSDeviceItem> itemList = new ArrayList<MDNSDeviceItem>();
			itemList.addAll(_foundDeviceList);
			final EventTask<DeviceFoundEventArgs> handler = deviceFoundHandler;
			new Thread()
			{
		        public void run()
		        {
		        	handler.run(new DeviceFoundEventArgs(null, itemList));
		        }
			}.start();
			return true;
		}
		
		_foundDeviceList = new ArrayList<MDNSDeviceItem>();
		
		_finder = new MDNSFinder();
		
		_finder.DeviceFoundEvent.AddListener(new EventTask<MDNSFinder.DeviceFoundEventArgs>()
		{
			@Override
			public void run(MDNSFinder.DeviceFoundEventArgs args) 
			{
				DeviceFoundHandler(args);
			}
		});
		
		_finder.FinishedEvent.AddListener(new EventTask<MDNSFinder.FinishedEventArgs>()
		{
			@Override
			public void run(MDNSFinder.FinishedEventArgs args) 
			{
				DeviceFinderFinishedHandler(args);
			}
		});
		
		return _finder.Start();
	}
	
	synchronized private void DeviceFoundHandler(MDNSFinder.DeviceFoundEventArgs args)
	{
		_foundDeviceList.add(args.DeviceItem);
		DeviceFoundEvent.RaiseEvent(new DeviceFoundEventArgs(args.DeviceItem, CloneList(_foundDeviceList)));
	}

	synchronized private void DeviceFinderFinishedHandler(MDNSFinder.FinishedEventArgs args)
	{
		_finder.DeviceFoundEvent.RemoveAllListeners();
		_finder.FinishedEvent.RemoveAllListeners();
		_finder = null;
		
		FinishedEvent.RaiseEvent(new FinishedEventArgs(args.Success, CloneList(_foundDeviceList)));
		
		DeviceFoundEvent.RemoveAllListeners();
		FinishedEvent.RemoveAllListeners();
	}
	
	List<MDNSDeviceItem> CloneList(List<MDNSDeviceItem> list)
	{
		List<MDNSDeviceItem> newList = new ArrayList<MDNSDeviceItem>();
		for(MDNSDeviceItem item : list)
		{
			newList.add(item);
		}
		return newList;
	}
	
	//
	// event code
	//
	public EventBase<DeviceFoundEventArgs> DeviceFoundEvent = new EventBase<DeviceFoundEventArgs>();
	public EventBase<FinishedEventArgs> FinishedEvent = new EventBase<FinishedEventArgs>();

	public class DeviceFoundEventArgs
	{
		public DeviceFoundEventArgs(MDNSDeviceItem foundDevice, List<MDNSDeviceItem> foundDeviceList)
		{
			FoundDevice = foundDevice;
			FoundDeviceList = foundDeviceList;
		}
		public MDNSDeviceItem FoundDevice;
		public List<MDNSDeviceItem> FoundDeviceList;
	}
	
	public class FinishedEventArgs
	{
		public FinishedEventArgs(boolean success, List<MDNSDeviceItem> foundDeviceList)
		{
			Success = success;
			FoundDeviceList = foundDeviceList;
		}
		public boolean Success;
		public List<MDNSDeviceItem> FoundDeviceList;
	}
	
	
	
}
