package com.wdc.plugins.mdnsdiscovery;

import android.content.Context;
import android.util.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright 2015 Western Digital Corporation. All rights reserved.
 */
public class BonjourDiscoveryPlugin extends CordovaPlugin  {
    private static final String TAG = BonjourDiscoveryPlugin.class.getName();
    private static final String COMMAND_START_SCAN = "start_scan";
    private static final String COMMAND_STOP_SCAN = "stop_scan";

    private DeviceScanner mDeviceScanner;

    private Context mContext;
    private CallbackContext callbackContext;

    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.mContext = this.cordova.getActivity();
        mDeviceScanner = new DeviceScanner();
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackCxt) throws JSONException {
        Log.e(TAG, "Cordova action "+action);
        this.callbackContext = callbackCxt;
        if (mDeviceScanner == null){
            callbackContext.error("scanner is null");
            return false;
        }

        if (action.equals(COMMAND_START_SCAN)) {
            mDeviceScanner.openDmc(mContext, new DeviceScanner.DeviceScannerListener() {
                @Override
                public void onSuccess(DNSDevice[] deviceList) {
                    if (deviceList != null){
                    	Log.d(TAG, "Device List " + deviceList.length);
                        String jsonResponse = null;
                        for (int i = 0;i <deviceList.length;i++){
                            Log.d(TAG, "IP: "+ (deviceList[i].getIpAddress()));
                        }
                        jsonResponse = getJSONArray(deviceList);
                        if (callbackContext != null){
                            callbackContext.success(jsonResponse);
                        }
                    } else {
                    	Log.w(TAG, "Device List is empty");
                    	callbackContext.error("Device List is empty");
                    }
                    mDeviceScanner.closeDmc();
                }

                @Override
                public void onFailure(String reason) {
                    callbackContext.error(reason);
                }
            });
            return true;
        } else if (action.equals(COMMAND_STOP_SCAN)){
            mDeviceScanner.closeDmc();
            return true;
        }
        return false;
    }
    
    
    private String getJSONArray(DNSDevice[] deviceList){
        JSONObject json = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        try {
            for (int i = 0; i < deviceList.length; i++) {
                jsonArray.put(deviceList[i].getJSON());
            }
            json.put("deviceList", jsonArray);
        }catch (Exception e) {
            Log.e(TAG, "getJSONArray " +e.getMessage());
        }
        return json.toString();
    }
}
