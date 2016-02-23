package com.wdc.plugins.mdnsdiscovery.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class EventBase<T>
{
	private List<EventTask<T>> _listeners = new ArrayList<EventTask<T>>();

	public synchronized void AddListener(EventTask<T> l)
	{
		_listeners.add(l);
	}

	public synchronized void RemoveListener(EventTask<T> l)
	{
		_listeners.remove(l);
	}

	public synchronized void RaiseEvent(T args)
	{
	    Iterator<EventTask<T>> i = _listeners.iterator();
	    while(i.hasNext())  
	    {
	    	((EventTask<T>)i.next()).run(args);
	    }
	}
	
	public synchronized int GetListenerCount()
	{
		return _listeners.size();
	}
	
	public synchronized void RemoveAllListeners()
	{
		_listeners.clear();
	}
	
}