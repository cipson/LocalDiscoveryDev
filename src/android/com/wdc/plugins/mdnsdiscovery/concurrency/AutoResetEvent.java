package com.wdc.plugins.mdnsdiscovery.concurrency;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class AutoResetEvent extends WaitHandle
{
    private final Semaphore event;
    private final Integer mutex;
 
    public AutoResetEvent(boolean signalled) 
    {
        event = new Semaphore(signalled ? 1 : 0);
        mutex = Integer.valueOf(-1);
    }
 
	@Override
    public void set() 
    {
        synchronized (mutex) 
        {
            if (event.availablePermits() == 0)
            {
                event.release();        
            }
        }
    }
 
	@Override
    public void reset() 
    {
        event.drainPermits();
    }
 
	@Override
    public void waitOne() throws InterruptedException 
    {
       event.acquire();
    }
 
	@Override
    public boolean waitOne(long timeout, TimeUnit unit) throws InterruptedException 
    {
        return event.tryAcquire(timeout, unit);
    }       
 
	@Override
    public boolean isSignalled() 
    {
        return event.availablePermits() > 0;
    }       
 
}
