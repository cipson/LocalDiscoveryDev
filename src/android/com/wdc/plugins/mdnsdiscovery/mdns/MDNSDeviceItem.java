package com.wdc.plugins.mdnsdiscovery.mdns;

import java.util.ArrayList;
import java.util.List;

public class MDNSDeviceItem
{
	private String IPAddress;//must get from response
	private String Name;//must get from domain
	private String TXTVersion;
	private String Vendor;
	private String modelURL;
	private String manufacturer;
	private String modelDescription;
	private String modelName;
	private String modelNumber;
	private String serialNumber;
	private String UDN;
	
	public String getIPAddress(){return IPAddress;}
	public String getName(){return Name;}
	public String getTXTVersion(){return TXTVersion;}
	public String getVendor(){return Vendor;}
	public String getmodelURL(){return modelURL;}
	public String getmanufacturer(){return manufacturer;}
	public String getmodelDescription(){return modelDescription;}
	public String getmodelName(){return modelName;}
	public String getmodelNumber(){return modelNumber;}
	public String getserialNumber(){return serialNumber;}
	public String getUDN(){return UDN;}
	
	public static MDNSDeviceItem Create(DNSMessage dnsMessage)
	{
		try
		{
			List<DNSRecord> list = new ArrayList<DNSRecord>();
			list.addAll(dnsMessage.Additionals);
			list.addAll(dnsMessage.Answers);
			
			//parse txt record
			for(DNSRecord r : list)
			{
				if(r instanceof DNSRecord.DNSTextRecord)
				{
					MDNSDeviceItem item = new MDNSDeviceItem();
					item.IPAddress = dnsMessage.IPAddress;
					
					//get device name
					String[] parts = r.Domain.split("[.]");
					if(parts.length > 0)
					{
						item.Name = parts[0];
					}

					DNSRecord.DNSTextRecord tr = (DNSRecord.DNSTextRecord) r;
					
					item.TXTVersion = tr.PropertyMap.get("TXTVersion");
					item.Vendor = tr.PropertyMap.get("Vendor");
					item.modelURL = tr.PropertyMap.get("modelURL");
					item.manufacturer = tr.PropertyMap.get("manufacturer");
					item.modelDescription = tr.PropertyMap.get("modelDescription");
					item.modelName = tr.PropertyMap.get("modelName");
					item.modelNumber = tr.PropertyMap.get("modelNumber");
					item.serialNumber = tr.PropertyMap.get("serialNumber");
					item.UDN = tr.PropertyMap.get("UDN");
					
					//if no device name, then set it to model name
					if(item.Name == null)
					{
						item.Name = item.modelName;
					}
					if(item.UDN == null)//avatar returns no UDN
					{
						item.UDN = item.serialNumber;
					}
					
					return item;
				}
			}
		}
		catch(Exception e)
		{
		}
		return null;
	}

	@Override
	public String toString() {
		return "MDNSDeviceItem{" +
				"IPAddress='" + IPAddress + '\'' +
				", Name='" + Name + '\'' +
				", TXTVersion='" + TXTVersion + '\'' +
				", Vendor='" + Vendor + '\'' +
				", modelURL='" + modelURL + '\'' +
				", manufacturer='" + manufacturer + '\'' +
				", modelDescription='" + modelDescription + '\'' +
				", modelName='" + modelName + '\'' +
				", modelNumber='" + modelNumber + '\'' +
				", serialNumber='" + serialNumber + '\'' +
				", UDN='" + UDN + '\'' +
				'}';
	}
}
