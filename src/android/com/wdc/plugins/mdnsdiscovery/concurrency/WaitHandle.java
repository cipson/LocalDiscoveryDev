package com.wdc.plugins.mdnsdiscovery.concurrency;

import java.util.concurrent.TimeUnit;

abstract public class WaitHandle
{
	abstract public void set();
	abstract public void reset();
	abstract public void waitOne() throws InterruptedException;
	abstract public boolean waitOne(long timeout, TimeUnit unit) throws InterruptedException;
	abstract public boolean isSignalled();
	
	static public int WaitAny(WaitHandle[] waitHandleArray) throws InterruptedException
	{
		return WaitAny(waitHandleArray, 3600, TimeUnit.SECONDS);
	}
	static public int WaitAny(WaitHandle[] waitHandleArray, long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		long wait = 100;
		long msTimeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
		long startTick = System.currentTimeMillis();//SystemClock.uptimeMillis();
		while(true)
		{
			for(int i=0;i<waitHandleArray.length;++i)
			{
				if(waitHandleArray[i].isSignalled())
				{
					//reset waitHandle if AutoResetEvent
					if(waitHandleArray[i] instanceof AutoResetEvent)
					{
						waitHandleArray[i].reset();
					}
					return i;
				}
			}
            if((/*SystemClock.uptimeMillis()*/System.currentTimeMillis() - startTick) > msTimeout)
            {
            	return -1;
            }
			Thread.sleep(wait);
		}
	}
	
	static public Boolean WaitAll(WaitHandle[] waitHandleArray) throws InterruptedException
	{
		//for infinity set to one hour
		return WaitAll(waitHandleArray, 3600, TimeUnit.SECONDS);
	}
	static public Boolean WaitAll(WaitHandle[] waitHandleArray, long timeout, TimeUnit timeUnit) throws InterruptedException
	{
		long wait = 100;
		long msTimeout = TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
		long startTick = System.currentTimeMillis();//SystemClock.uptimeMillis();
        for (WaitHandle wh : waitHandleArray)
        {
            while(!wh.waitOne(wait, TimeUnit.MILLISECONDS))
            {
                if((/*SystemClock.uptimeMillis()*/System.currentTimeMillis() - startTick) > msTimeout)
                {
                    return false;
                }
                
				//reset waitHandle if AutoResetEvent
				if(wh instanceof AutoResetEvent)
				{
					wh.reset();
				}
                
            }
        }
        return true;		
	}
} 
