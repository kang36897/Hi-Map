package com.gulu.utils;

import android.location.Location;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.core.LatLonPoint;

public class LatLonUtil {
	
	public static LatLonPoint translateToPoint(LatLng latlng) {
		return new LatLonPoint(latlng.latitude, latlng.longitude);
	}
	
	public static LatLng translateFromPoint(LatLonPoint point) {
		return new LatLng(point.getLatitude(), point.getLongitude());
	}
	
	public static LatLng generateFromLocation(Location l) {
		return new LatLng(l.getLatitude(), l.getLongitude());
	}
}
