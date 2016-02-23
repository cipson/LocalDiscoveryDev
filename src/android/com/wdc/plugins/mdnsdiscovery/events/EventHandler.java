package com.wdc.plugins.mdnsdiscovery.events;

public abstract interface EventHandler<T>
{
	public abstract void run(T args);
}
