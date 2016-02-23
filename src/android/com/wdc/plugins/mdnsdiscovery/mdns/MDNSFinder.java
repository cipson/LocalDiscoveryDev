package com.wdc.plugins.mdnsdiscovery.mdns;

import com.wdc.plugins.mdnsdiscovery.concurrency.AutoResetEvent;
import com.wdc.plugins.mdnsdiscovery.events.EventBase;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class MDNSFinder
{
    final String multicastIP = "224.0.0.251";
    final int multicastPort = 5353;
    
	MulticastSocket _socket;
    List<DatagramPacket> _responseList = new ArrayList<DatagramPacket>();
    boolean _running = false;
    int _sendCount = 3;
    int _msResendDelay = 2000;

	public MDNSFinder()
	{
	}
	
	public boolean Start()
	{
		return Start(_sendCount, _msResendDelay);
	}
	synchronized public boolean Start(int sendCount, int msResendDelay)
	{
		if(_running)
		{
			return false;
		}
		
    	if(!Init())
    	{
    		return false;
    	}
    	
		_running = true;
		
		_processedIPList.clear();
		
		_msResendDelay = msResendDelay;
		_sendCount = sendCount;
		
		StartReceiveThread();
  		StartSendThread();
  		
		return true;
	}
	
	synchronized public void Stop()
	{
		try
		{
			Uninit(true);
		}
		catch(Exception e)
		{
		}
	}
	
	private boolean Init()
	{
		try
		{
		    _socket = new MulticastSocket(5353);
	        _socket.joinGroup(InetAddress.getByName("224.0.0.251"));
	        return true;
		}
		catch(Exception e)
		{
		}
		return false;
	}
	
	synchronized private void Uninit(boolean success)
	{
		try
		{
			if(!_running)
			{
				return;
			}
			_running = false;
			
			if(_socket != null)
			{
				_socket.close();
				_socket = null;
			}
			
			FinishedEvent.RaiseEvent(new FinishedEventArgs(success));
		}
		catch(Exception e)
		{
		}
	}
	
	void StartSendThread()
	{
		final int sendCount = _sendCount;
		final int delay = _msResendDelay;
		
		new Thread()
		{
	        public void run()
	        {
	        	for(int i=0;i<sendCount;++i)
	        	{
		        	try
		        	{
		        		if(!_running)
		        		{
		        			break;
		        		}
		        		
		        		if(!Send())
		        		{
		        			Uninit(false);
		        			break;
		        		}
	        		
		        		Thread.sleep(delay);
		        	}
		        	catch(Exception e)
		        	{
		        		Uninit(false);		        		
		        		break;
		        	}
	        	}
    			Uninit(true);
	        }
		}.start();
	}

	void StartReceiveThread()
	{
		new Thread()
		{
	        public void run()
	        {
	        	Receive();
	        }
		}.start();
	}
	
	private boolean Send() 
	{
		//String type = "_wd-2go._tcp.local.";
		
		byte[] query = new byte[]{
			0x00, 0x00, // Transaction ID
			0x00, 0x00, // Flags
			0x00, 0x01, // Queries
			0x00, 0x00, // Answer RRs
			0x00, 0x00, // Authority RRs
			0x00, 0x00, // Additional RRs


			// Queries
			//0x07, '_', 'w', 'd', '-', '2', 'g', 'o',
				0x0B, '_', 'w', 'd', '-', 'm', 'y', 'c', 'l','o','u','d',
			0x04, '_', 't', 'c', 'p',
			0x05, 'l', 'o', 'c', 'a', 'l',
			0x00, /// End of name
			0x00, 0x0c, // Type: PTR
			-128, 0x01, // Class IN, "QU" question
		};
		
		try
		{
	        DatagramPacket request =
	            new DatagramPacket(query, query.length, InetAddress.getByName("224.0.0.251"), 5353);
	        _socket.send(request);
	        return true;
		}
		catch(Exception e)
		{
		}
        return false;
    }
	
	private void Receive()
	{
        try
        {
        	StartProcessResponseThread();
            while (true) 
            {
        	    while(true)
        	    {
            	    byte[] buf = new byte[1024];
            	    DatagramPacket dp = new DatagramPacket(buf, buf.length);
        	    	_socket.receive(dp);
        	    	
        	    	synchronized(_responseList)
        	    	{
        	    		_responseList.add(dp);
        	    	}
    	    		_newResponseWaitHandle.set();
        	    }
            }
        }
        catch (Exception e)
        {
        }
	}
	
	List<String> _processedIPList = new ArrayList<String>();
	AutoResetEvent _newResponseWaitHandle = new AutoResetEvent(false);
	void StartProcessResponseThread()
	{
		new Thread()
		{
	        public void run()
	        {
	        	while(true)
	        	{
	        		try
	        		{
	        			_newResponseWaitHandle.waitOne();

	        			while(true)
	        			{
	        				try
	        				{
	        					DatagramPacket response;
		        				
			        			synchronized(_responseList)
			        			{
			        				if(_responseList.size() > 0)
			        				{
			        					response = _responseList.remove(0); 
			        				}
			        				else
			        				{
			        					break;
			        				}
			        			}
			        			
			        			//only process each IP once
			        			if(!_processedIPList.contains(response.getAddress().toString()))
			        			{
			        				_processedIPList.add(response.getAddress().toString());
			        				ProcessResponse(response);
			        			}
	        				}
	        				catch(Exception e)
	        				{
	        				}
	        			}
	        		}
	        		catch(Exception e)
	        		{
	        		}
	        	}
	        }
		}.start();
	}
	
	void ProcessResponse(DatagramPacket dp)
	{
		DNSMessage msg = DNSMessage.Create(dp); 
		MDNSDeviceItem item = MDNSDeviceItem.Create(msg); 
		DeviceFoundEvent.RaiseEvent(new DeviceFoundEventArgs(item));
	}

	//
	// event code
	//
	public EventBase<DeviceFoundEventArgs> DeviceFoundEvent = new EventBase<DeviceFoundEventArgs>();
	public EventBase<FinishedEventArgs> FinishedEvent = new EventBase<FinishedEventArgs>();

	public class DeviceFoundEventArgs
	{
		public DeviceFoundEventArgs(MDNSDeviceItem deviceItem)
		{
			DeviceItem = deviceItem;
		}
		public MDNSDeviceItem DeviceItem;
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
