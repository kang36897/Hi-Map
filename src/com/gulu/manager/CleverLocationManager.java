package com.gulu.manager;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;

public class CleverLocationManager implements AMapLocationListener {
	
	private List<OnLocationChangedListener> mOnLocationChangedObservers = new LinkedList<OnLocationChangedListener>();
	
	private Context mContext;
	private LocationManagerProxy mLocationManagerOne;
	
	private static Object mLock = new Object();
	private static CleverLocationManager myself;

	private CleverLocationManager(Context ctx) {
		mContext = ctx.getApplicationContext();
		mLocationManagerOne = LocationManagerProxy.getInstance(mContext);
		
		mLocationManagerOne.requestLocationUpdates(
				LocationProviderProxy.AMapNetwork, 2000, 100, this);
	}
	
	public static CleverLocationManager getInstance(Context context) {
		if (myself != null) {
			return myself;
		}
		
		synchronized (mLock) {
			if (myself == null) {
				myself = new CleverLocationManager(context);
			}
		}
		return myself;
	}

	public void registLocationChangedEvent(OnLocationChangedListener l) {
		mOnLocationChangedObservers.add(l);
	}
	
	public void unregistLocationChangedEvent(OnLocationChangedListener l) {
		mOnLocationChangedObservers.remove(l);
	}
	
	private void onLocationChangedInternal(Location location) {
		if (mOnLocationChangedObservers.isEmpty()) {
			return;
		}
		
		for (OnLocationChangedListener l : mOnLocationChangedObservers) {
			l.onLocationChanged(location);
		}
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onLocationChanged(AMapLocation amLocation) {
		onLocationChangedInternal(amLocation);
	}
	
	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}
}
