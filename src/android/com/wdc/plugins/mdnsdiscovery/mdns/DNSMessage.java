package com.wdc.plugins.mdnsdiscovery.mdns;

import java.io.IOException;
import java.net.DatagramPacket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@SuppressLint("UseSparseArrays")
public class DNSMessage
{
	public int ID;
	public int Flags;
	
	public String IPAddress; 
	
	List<DNSQuestion> Questions = new ArrayList<DNSQuestion>();
	List<DNSRecord> Answers = new ArrayList<DNSRecord>();
	List<DNSRecord> Authorities = new ArrayList<DNSRecord>();
	List<DNSRecord> Additionals = new ArrayList<DNSRecord>();
	
    static public DNSMessage Create(DatagramPacket dp)
    {
    	DNSMessage item = new DNSMessage();
    	item.IPAddress = dp.getAddress().toString();
    	item.ParseResponse(dp);
    	
//    	for(DNSRecord r : item.Additionals)
//    	{
//    		if(r instanceof DNSRecord.DNSTextRecord)
//    		{
//    			//parse device info
//    		}
//    	}
    	
    	return item;
    }
    
    private void ParseResponse(DatagramPacket dp)
	{
		try
		{
	    	byte[] response = dp.getData();
	    	
			ByteReader d = new ByteReader(response);
			
			ID = d.readShort();
			Flags = d.readUnsignedShort();
			
			if( !((Flags & 0x8000) != 0 && (Flags & 0x000f) == 0) )
			{
				//error
				return;
			}
			
			int numQuestions = d.readUnsignedShort();
			int numAnswers = d.readUnsignedShort();
			int numAuthorities = d.readUnsignedShort();
			int numAdditionals = d.readUnsignedShort();

			for(int i=0; i<numQuestions; i++)
			{
				DNSQuestion r = ReadQuestion(d);
				if(r != null)
				{
					Questions.add(r);
				}
			}

			for(int i=0; i<numAnswers; i++)
			{
				DNSRecord r = ReadAnswer(d); 
				if(r != null)
				{
					Answers.add(r);
				}
			}
			
			for(int i=0; i<numAuthorities; i++)
			{
				DNSRecord r = ReadAnswer(d); 
				if(r != null)
				{
					Authorities.add(r);
				}
			}
			
			for(int i=0; i<numAdditionals; i++)
			{
				DNSRecord r = ReadAnswer(d); 
				if(r != null)
				{
					Additionals.add(r);
				}
			}
		} 
		catch (Exception e) 
		{
			System.out.println("MDNSDeviceItem.ParseResponse EXCEPTION " + e.getMessage());
		}
	}

	
	private DNSQuestion ReadQuestion(ByteReader br) throws IOException
	{
		String domain = readName(br);
		int type = br.readUnsignedShort();
		int clas = br.readUnsignedShort();
		DNSQuestion q = new DNSQuestion(type, clas, domain);
		return q;
	}

	private DNSRecord ReadAnswer(ByteReader d) throws IOException
	{
		DNSRecord r = null;
		
		String domain = readName(d);
		int type = d.readUnsignedShort();
		int clas = d.readUnsignedShort();
		int ttl = d.readInt();
		int length = d.readUnsignedShort();

		if((clas & 0x7fff) != 0x01) 
		{
			// Class is not IN, we can't parse that, skip it
			d.skipBytes(length);
			return null;
		}

		switch(type)
		{
			case 0x0c://PTR IN
				String name = readName(d);
				r = new DNSRecord.DNSPointerRecord(name, domain, ttl);
				break;
		
			case 0x01://A IN 
				StringBuffer buf = new StringBuffer();
				for(int j=0; j<4; j++) 
				{
					if(j!=0) buf.append(".");
					buf.append(Integer.toString(d.readUnsignedByte()));
				}
				r = new DNSRecord.DNSAddressRecord(buf.toString(), domain, ttl);
				break;
			
			case 0x21://SRV IN 
				int priority = d.readUnsignedShort();
				int weight = d.readUnsignedShort();
				int port = d.readUnsignedShort();
				String target = readName(d);
				r = new DNSRecord.DNSServiceRecord(priority, weight, port, target, domain, ttl);
				break;
				
			case 0x10://TXT IN
				
				Map<String, String> propertyMap = new HashMap<String, String>();
				int totalRead = 0;
				while(totalRead < length)
				{
					int stringLength = d.readUnsignedByte();
					String s = new String(d.readBytes(stringLength));
					String[] parts = s.split("=");
					if(parts.length == 2)
					{
						propertyMap.put(parts[0], parts[1]);
					}
					totalRead += stringLength + 1;
				}
				
				r = new DNSRecord.DNSTextRecord(propertyMap, domain, ttl);
				break;
		
			default://ignore
				d.skipBytes(length);
		}
		
		return r;
	}

//	private static void readName(ByteReader d, List<String> results, int offset) throws IOException
//	{
//		int labelLength;
//		int savedLength = d.getLength();
//
//
//		/* (Possibly recursive) handling of compressed names:
//		 * Jump to offset, read the name, append parts to results vector,
//		 * if reading a pointer: recursively call with new offset
//		 */
//		
//		d.setPosition(offset);
//
//		while( (labelLength = d.readUnsignedByte()) != 0 )
//		{
//			if( (labelLength & 0xc0) == 0xc0) 
//			{
//				/* Special casing for a compressed name. */
//				int pointer = d.readUnsignedByte();
//				pointer = pointer | ((labelLength & 0x3F) << 8);
//				int savedPosition = d.getPosition();
//				
//				readName(d, results, pointer);
//				
//				d.setLength(savedLength);
//				d.setPosition(savedPosition);
//				break; /* End the loop, a pointer is always the end of the label list */
//			}
//			byte b[] = new byte[labelLength];
//			d.readFully(b);
//			if(results != null)
//			{
//				results.add(new String(b));
//			}
//		}
//	}
	
//	private static String[] readName(ByteReader d) throws IOException
//	{
//		List<String> v = new ArrayList<String>();
//		readName(d, v, d.getPosition());
//		return v.toArray(new String[v.size()]);
//	}

    Map<Integer, StringBuilder> _savedNames = new HashMap<Integer, StringBuilder>();
	private String readName(ByteReader d) throws IOException
	{
        Map<Integer, StringBuilder> names = new HashMap<Integer, StringBuilder>();
        StringBuilder newName = new StringBuilder();
        //int labelLength;
        boolean done = false;
        
        while(!done)
		{
        	int labelLength = d.readUnsignedByte();
        	if(labelLength == 0)
        	{
        		break;
        	}
        	
			switch(labelLength & 0xc0)
			{
				case 0x00:

                    int offset = d.getPosition() - 1;
					byte b[] = new byte[labelLength];
					d.readFully(b);
					String label = new String(b) + ".";
					newName.append(label);
                    for (StringBuilder previousLabel : names.values()) 
                    {
                        previousLabel.append(label);
                    }
                    names.put(Integer.valueOf(offset), new StringBuilder(label));
					break;
				
				case 0xC0:
					
					int pointer = d.readUnsignedByte();
					pointer = pointer | ((labelLength & 0x3F) << 8);
                    String compressedLabel = _savedNames.get(Integer.valueOf(pointer)).toString();
                    if (compressedLabel == null) 
                    {
                    	//error
                        compressedLabel = "";
                    }
                    newName.append(compressedLabel);
                    for (StringBuilder previousLabel : names.values()) 
                    {
                        previousLabel.append(compressedLabel);
                    }
                    done = true;
                    break;
	
				default:
					break;
			}
		}
        for (Integer index : names.keySet()) {
        	_savedNames.put(Integer.valueOf(index), names.get(index));
        }
		return newName.toString();
	}
    
//    public String readUTF(ByteReader d, int len) 
//    {
//        StringBuilder buffer = new StringBuilder(len);
//        for (int index = 0; index < len; index++) {
//            int ch = d.readUnsignedByte();
//            switch (ch >> 4) {
//                case 0:
//                case 1:
//                case 2:
//                case 3:
//                case 4:
//                case 5:
//                case 6:
//                case 7:
//                    // 0xxxxxxx
//                    break;
//                case 12:
//                case 13:
//                    // 110x xxxx 10xx xxxx
//                    ch = ((ch & 0x1F) << 6) | (d.readUnsignedByte() & 0x3F);
//                    index++;
//                    break;
//                case 14:
//                    // 1110 xxxx 10xx xxxx 10xx xxxx
//                    ch = ((ch & 0x0f) << 12) | ((d.readUnsignedByte() & 0x3F) << 6) | (d.readUnsignedByte() & 0x3F);
//                    index++;
//                    index++;
//                    break;
//                default:
//                    // 10xx xxxx, 1111 xxxx
//                    ch = ((ch & 0x3F) << 4) | (d.readUnsignedByte() & 0x0f);
//                    index++;
//                    break;
//            }
//            buffer.append((char) ch);
//        }
//        return buffer.toString();
//    }

}
