package com.wdc.plugins.mdnsdiscovery.mdns;

import java.util.HashMap;
import java.util.Map;

public class DNSRecord extends DNSEntry 
{
	int TTL;
	
	public DNSRecord(String domain, int ttl)
	{
		super(domain);
	}
	
	static public class DNSAddressRecord extends DNSRecord
	{
		public String Address;
		
		public DNSAddressRecord(String address, String domain, int ttl)
		{
			super(domain, ttl);
			Address = address;
		}
	}
	
	static public class DNSPointerRecord extends DNSRecord
	{
		String Name;
		
		public DNSPointerRecord(String name, String domain, int ttl)
		{
			super(domain, ttl);
			Name = name;
		}
		
	}
	
	static public class DNSServiceRecord extends DNSRecord
	{
		int Priority;
		int Weight;
		int Port;
		String Target;
		
		public DNSServiceRecord(int priority, int weight, int port, String target, String domain, int ttl)
		{
			super(domain, ttl);
			Priority = priority;
			Weight = weight;
			Port = port;
			Target = target;
		}
		
	}
	
	static public class DNSTextRecord extends DNSRecord
	{
		Map<String, String> PropertyMap = new HashMap<String, String>();
		
		public DNSTextRecord(Map<String, String> propertyMap, String domain, int ttl)
		{
			super(domain, ttl);
			PropertyMap = propertyMap;
		}
	}
}
