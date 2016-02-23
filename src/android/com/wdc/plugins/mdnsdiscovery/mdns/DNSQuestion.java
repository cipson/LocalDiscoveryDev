package com.wdc.plugins.mdnsdiscovery.mdns;

public class DNSQuestion extends DNSEntry
{
	public int Type;
	public int Clas;
	
	public DNSQuestion(int type, int clas, String domain)
	{
		super(domain);
		Type = type;
		Clas = clas;
	}
}
