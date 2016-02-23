package com.wdc.plugins.mdnsdiscovery.concurrency;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ManualResetEvent extends WaitHandle
{
    private final Integer mutex;
    private volatile CountDownLatch event;
 
    public ManualResetEvent(boolean signalled)
    {
        mutex = Integer.valueOf(-1);
        if (signalled)
        {
            event = new CountDownLatch(0);
        }
        else 
        {
            event = new CountDownLatch(1);
        }
    }
 
	@Override
    public void set()
    {
        event.countDown();
    }
 
	@Override
    public void reset() 
    {
        synchronized (mutex) 
        {
            if (event.getCount() == 0) 
            {
                event = new CountDownLatch(1);
            }
        }
    }
 
	@Override
    public void waitOne() throws InterruptedException 
    {
        event.await();
    }
 
	@Override
    public boolean waitOne(long timeout, TimeUnit unit) throws InterruptedException 
    {
        return event.await(timeout, unit);
    }
 
	@Override
    public boolean isSignalled()
    {
        return event.getCount() == 0;
    }
}
