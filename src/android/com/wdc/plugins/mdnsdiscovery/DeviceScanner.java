package com.wdc.plugins.mdnsdiscovery;

import android.content.Context;
import android.util.Log;

import com.wdc.plugins.mdnsdiscovery.events.EventBase;
import com.wdc.plugins.mdnsdiscovery.events.EventTask;

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * Copyright 2015 Western Digital Corporation. All rights reserved.
 */
public class DeviceScanner {
    private static final String tag = DeviceScanner.class.getName();
    private Context mContext;
    private WDDeviceFinder _finder = null;
    private DeviceScannerListener mDeviceScannerListener;

    private ArrayList<DNSDevice> foundDevices;
    private boolean _discoveryRunning = false;

    public interface DeviceScannerListener {
        void onSuccess(DNSDevice[] deviceList); // making it array to support plugin (javascript)
        void onFailure(String reason);
    }

    public void openDmc(final Context app, DeviceScannerListener listener)
    {
        if (app == null || listener == null){
            throw new IllegalArgumentException("Context and listener cannot be null");
        }
        mDeviceScannerListener = listener;
        foundDevices = new ArrayList<DNSDevice>();
        new Thread()
        {
            public void run()
            {
                openDmcWorker(app);
            }
        }.start();
    }

    private synchronized void openDmcWorker(Context app)
    {
        Log.d(tag, "openDmc");

        if(app == null)
        {
            Log.d(tag, "openDmc - null app parameter passed in, exiting");
            return;
        }

        try
        {
            if(_discoveryRunning)
            {
                Log.d(tag, "openDmc - already running, return");
                return;
            }
            _discoveryRunning = true;

            mContext = app;



            //
            // start finder
            //

            _finder = new WDDeviceFinder();

            _finder.DeviceFoundEvent.AddListener(new EventTask<DeviceFinder.DeviceFoundEventArgs>()
            {
                @Override
                public void run(DeviceFinder.DeviceFoundEventArgs args)
                {
                    DeviceFound(args);
                }
            });

            _finder.FinishedEvent.AddListener(new EventTask<DeviceFinder.FinishedEventArgs>()
            {
                @Override
                public void run(DeviceFinder.FinishedEventArgs args)
                {
                    Log.d(tag, "Discovery finished");
                    if (mDeviceScannerListener != null){
                        mDeviceScannerListener.onSuccess(foundDevices.toArray(new DNSDevice[foundDevices.size()]));
                    }
                }
            });

            _finder.Start();
        }
        catch(Exception e)
        {
            Log.w(tag, "mDNS Discovery exception "+e.getMessage());
            mDeviceScannerListener.onFailure(e.getMessage());
        }
    }

    public void closeDmc()
    {
        mDeviceScannerListener = null;
        foundDevices = null;
        new Thread()
        {
            public void run()
            {
                closeDmcWorker();
            }
        }.start();
    }

    private synchronized void closeDmcWorker()
    {
        Log.d(tag, "closeDmc");

        if(!_discoveryRunning)
        {
            Log.d(tag, "closeDmc - not running, return");
            return;
        }

        try
        {
            if(_finder != null)
            {
                _finder.DeviceFoundEvent.RemoveAllListeners();
                _finder.FinishedEvent.RemoveAllListeners();
                _finder = null;
            }


        }
        catch (Exception e)
        {
            Log.w(tag, e.getStackTrace()[0].getMethodName(), e);
        }
        finally
        {
            _finder = null;
            _discoveryRunning = false;
            Log.d(tag, "closeDmc - FINISHED");
        }
    }


    private static Object _deviceFoundLock = new Object();
    private void DeviceFound(DeviceFinder.DeviceFoundEventArgs args)
    {
        synchronized(_deviceFoundLock)
        {
            try
            {
                Log.d(tag, "##### getmodelName" + args.Device.getmodelName());
                Log.d(tag, "##### device" + args.Device);
                DNSDevice newDevice = new DNSDevice();
                newDevice.setIpAddress(args.Device.getIPAddress().replace("/", ""));
                newDevice.setVendor(args.Device.getmanufacturer());
                newDevice.setName(args.Device.getName());
                newDevice.setModelNumber(args.Device.getmodelNumber());
                newDevice.setSerialNumner(args.Device.getserialNumber());
                newDevice.setModelName(args.Device.getmodelName());
                foundDevices.add(newDevice);
            }
            catch (Exception e)
            {
            }
        }
    }
}
