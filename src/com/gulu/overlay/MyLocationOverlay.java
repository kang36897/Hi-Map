package com.gulu.overlay;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.Circle;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.gulu.manager.CleverLocationManager;
import com.gulu.utils.IconGenerator;
import com.gulu.utils.LatLonUtil;
import com.gulu.utils.MapEventDispatcher;

public class MyLocationOverlay extends Overlay implements
		OnMarkerClickListener, InfoWindowAdapter, OnLocationChangedListener {
	
	private Context mContext;
	private MapEventDispatcher mDispatcher;
	private AMap mAMap;
	private CleverLocationManager mCleverLocationManager;
	
	private Location mLastLocation;
	private Marker myLocateMarker;
	private Circle mAccuracyCircle;
	
	private Bitmap mActiveIcon;

	private int type = AMap.LOCATION_TYPE_LOCATE;
	private int ticket = 1;

	public MyLocationOverlay(Context ctx, MapEventDispatcher mapEventDispatcher) {
		mContext = ctx;
		mDispatcher = mapEventDispatcher;
		mAMap = mDispatcher.getAMap();
		mCleverLocationManager = CleverLocationManager.getInstance(mContext);
		
		mActiveIcon = IconGenerator.generateBlueCircleIcon(20);
	}
	
	protected void registEventListener() {
		mCleverLocationManager.registLocationChangedEvent(this);
		mDispatcher.addOnMarkerClickListener(this);
		mDispatcher.addInfoWindowAdapter(this);
	}
	
	protected void unregistEventListener() {
		mDispatcher.removeInfoWindowAdapter(this);
		mDispatcher.removeOnMarkerClickListener(this);
		mCleverLocationManager.unregistLocationChangedEvent(this);
	}

	public boolean isOnThisOverlay(Marker marker) {
		return false;
	}
	
	public void setMyLocationType(int t) {
		type = t;
		
		if (t != AMap.LOCATION_TYPE_LOCATE) {
			ticket = 1;
		}
	}

	@Override
	public View getInfoContents(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public View getInfoWindow(Marker arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (!isOnThisOverlay(marker)) {
			return false;
		}
		
		return false;
	}

	@Override
	public void doAddToMapInternal() {
		if (myLocateMarker == null) {
			
			MarkerOptions options = new MarkerOptions();
			options.anchor(0.5f, 0.5f).title("dummy title")
					.icon(BitmapDescriptorFactory.fromBitmap(mActiveIcon));
			myLocateMarker = mAMap.addMarker(options);
		}
		
		if (mAccuracyCircle == null) {
			mAccuracyCircle = mAMap.addCircle(new CircleOptions()
					.visible(false).fillColor(Color.argb(60, 131, 182, 222))
					.strokeWidth(0.5f)
					.strokeColor(Color.argb(100, 54, 114, 227)).zIndex(20.0f));
		}

		moveMyLocationMarker();
		adjustCamera();

	}

	private void moveMyLocationMarker() {
		if (mLastLocation == null) {
			return;
		}
		
		LatLng target = LatLonUtil.generateFromLocation(mLastLocation);
		
		myLocateMarker.setPosition(target);
		myLocateMarker.setRotateAngle(mLastLocation.getBearing());
		
		if (mLastLocation.getAccuracy() > 0) {
			mAccuracyCircle.setCenter(target);
			mAccuracyCircle.setRadius(mLastLocation.getAccuracy());
			mAccuracyCircle.setVisible(true);
		}
	}
	
	@Override
	public void doRemoveFromMapInternal() {
		
		if (myLocateMarker != null) {
			myLocateMarker.remove();
			myLocateMarker.destroy();
			myLocateMarker = null;
		}
		
		if (mAccuracyCircle != null) {
			mAccuracyCircle.remove();
			mAccuracyCircle = null;
		}
	}

	@Override
	public void onLocationChanged(Location l) {
		if (l == null) {
			return;
		}
		
		mLastLocation = l;
		moveMyLocationMarker();
		
		if (ticket > 0) {
			
			adjustCamera();
			
			if (type == AMap.LOCATION_TYPE_LOCATE) {
				ticket--;
			}
		}
		
	}
	
	private void adjustCamera() {
		if (mLastLocation == null) {
			return;
		}
		LatLng target = LatLonUtil.generateFromLocation(mLastLocation);
		CameraPosition position = new CameraPosition.Builder(
				mAMap.getCameraPosition()).target(target).build();
		CameraUpdate update = CameraUpdateFactory.newCameraPosition(position);
		mAMap.animateCamera(update);
	}

}
