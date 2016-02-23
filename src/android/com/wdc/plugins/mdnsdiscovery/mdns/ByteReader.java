package com.wdc.plugins.mdnsdiscovery.mdns;

import java.nio.ByteBuffer;

public class ByteReader
{
	private ByteBuffer buf;
	
	public ByteReader(byte[] data)
	{
		buf = ByteBuffer.wrap(data);
	}
	
	public void reset()
	{
		buf.position(0);
	}
	
	public int getPosition()
	{
		return buf.position();
	}
	
	public void setPosition(int newPosition)
	{
		buf.position(newPosition);
	}
	
	public void skipBytes(int count)
	{
		int newPosition = getPosition() + count;
		buf.position(newPosition);
	}
	
	public int getLength()
	{
		return buf.limit();
	}
	
	public void setLength(int newLength)
	{
		buf.limit(newLength);
	}
	
	public void readFully(byte[] b)
	{
		buf.get(b);
	}
	
	public byte[] readBytes(int count)
	{
		byte[] b = new byte[count];
		buf.get(b);
		return b;
	}
	
	public int readUnsignedByte()
	{
		return buf.get() & 0xff;
	}
	
	public int readShort()
	{
		return buf.getShort();
	}
	
	public int readUnsignedShort()
	{
		return buf.getShort() & 0xffff;
	}
	
	public int readInt()
	{
		return buf.getInt();
	}
	
}
