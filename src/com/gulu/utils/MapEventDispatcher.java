package com.gulu.utils;

import java.util.LinkedList;
import java.util.List;

import android.view.View;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMapLongClickListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;

public class MapEventDispatcher implements OnMarkerClickListener,
		OnMarkerDragListener, OnInfoWindowClickListener, InfoWindowAdapter,
		OnMapClickListener, OnMapLoadedListener, OnMapLongClickListener {
	
	private AMap mAMap;

	private List<OnMapLoadedListener> mOnMapLoadedObservers = new LinkedList<OnMapLoadedListener>();
	private List<OnMapClickListener> mOnMapClickObservers = new LinkedList<OnMapClickListener>();
	private List<OnMapLongClickListener> mOnMapLongClickObservers = new LinkedList<OnMapLongClickListener>();
	
	private List<OnMarkerClickListener> mOnMarkerClickObservers = new LinkedList<OnMarkerClickListener>();
	private List<OnMarkerDragListener> mOnMarkerDragObservers = new LinkedList<OnMarkerDragListener>();
	private List<OnInfoWindowClickListener> mOnInfoWindowClickObservers = new LinkedList<OnInfoWindowClickListener>();
	private List<InfoWindowAdapter> mInfoWindowAdpters = new LinkedList<InfoWindowAdapter>();

	public void associateWith(AMap amap) {
		mAMap = amap;
		mAMap.setOnMapLoadedListener(this);
		mAMap.setOnMapClickListener(this);
		mAMap.setOnMapLongClickListener(this);
		
		mAMap.setOnMarkerClickListener(this);
		mAMap.setOnMarkerDragListener(this);
		mAMap.setOnInfoWindowClickListener(this);
		mAMap.setInfoWindowAdapter(this);
	}

	public AMap getAMap() {
		return mAMap;
	}

	public void unassociateWithAMap() {
		
		mOnMapLoadedObservers.clear();
		mAMap.setOnMapLoadedListener(null);
		
		mOnMapClickObservers.clear();
		mAMap.setOnMapClickListener(null);
		
		mOnMapLongClickObservers.clear();
		mAMap.setOnMapLongClickListener(null);
		
		mOnMarkerClickObservers.clear();
		mAMap.setOnMarkerClickListener(null);
		
		mOnMarkerDragObservers.clear();
		mAMap.setOnMarkerDragListener(null);
		
		mOnInfoWindowClickObservers.clear();
		mAMap.setOnInfoWindowClickListener(null);
		
		mInfoWindowAdpters.clear();
		mAMap.setInfoWindowAdapter(null);
		
		mAMap = null;
	}

	public void addOnMapLoadedListener(OnMapLoadedListener l) {
		mOnMapLoadedObservers.add(l);
	}
	
	public void removeOnMapLoadedListener(OnMapLoadedListener l) {
		mOnMapLoadedObservers.remove(l);
	}
	
	public void addOnMapClickListener(OnMapClickListener l) {
		mOnMapClickObservers.add(l);
	}
	
	public void removeOnMapClickListener(OnMapClickListener l) {
		mOnMapClickObservers.remove(l);
	}
	
	public void addOnMapLongClickListener(OnMapLongClickListener l) {
		mOnMapLongClickObservers.add(l);
	}
	
	public void removeOnMapLongClickListener(OnMapLongClickListener l) {
		mOnMapLongClickObservers.remove(l);
	}

	public void addOnMarkerClickListener(OnMarkerClickListener l) {
		mOnMarkerClickObservers.add(l);
	}
	
	public void removeOnMarkerClickListener(OnMarkerClickListener l) {
		mOnMarkerClickObservers.remove(l);
	}

	public void addOnMarkerDragListener(OnMarkerDragListener l) {
		mOnMarkerDragObservers.add(l);
	}
	
	public void removeOnMarkerDragListener(OnMarkerDragListener l) {
		mOnMarkerDragObservers.remove(l);
	}
	
	public void addOnInfoWindowClickListener(OnInfoWindowClickListener l) {
		mOnInfoWindowClickObservers.add(l);
	}
	
	public void removeOnInfoWindowClickListener(OnInfoWindowClickListener l) {
		mOnInfoWindowClickObservers.remove(l);
	}
	
	public void addInfoWindowAdapter(InfoWindowAdapter a) {
		mInfoWindowAdpters.add(a);
	}
	
	public void removeInfoWindowAdapter(InfoWindowAdapter a) {
		mInfoWindowAdpters.remove(a);
	}

	@Override
	public void onMapClick(LatLng position) {
		if (mOnMapClickObservers.isEmpty()) {
			return;
		}

		for (OnMapClickListener listener : mOnMapClickObservers) {
			listener.onMapClick(position);
		}
		
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		
		if (mInfoWindowAdpters.isEmpty())
			return null;
		
		for (InfoWindowAdapter adapter : mInfoWindowAdpters) {
			View temp = adapter.getInfoContents(marker);
			if (temp != null) {
				return temp;
			}
		}
		
		return null;
	}
	
	@Override
	public View getInfoWindow(Marker marker) {
		if (mInfoWindowAdpters.isEmpty())
			return null;
		
		for (InfoWindowAdapter adapter : mInfoWindowAdpters) {
			View temp = adapter.getInfoWindow(marker);
			if (temp != null) {
				return temp;
			}
		}
		
		return null;
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		if (mOnInfoWindowClickObservers.isEmpty()) {
			return;
		}
		
		for (OnInfoWindowClickListener listener : mOnInfoWindowClickObservers) {
			listener.onInfoWindowClick(marker);
		}
		
	}
	
	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMarkerDragStart(Marker marker) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		if (mOnMarkerClickObservers.isEmpty())
			return false;
		
		for (OnMarkerClickListener listener : mOnMarkerClickObservers) {
			if (listener.onMarkerClick(marker)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onMapLongClick(LatLng position) {
		if (mOnMapLongClickObservers.isEmpty()) {
			return;
		}
		
		for (OnMapLongClickListener listener : mOnMapLongClickObservers) {
			listener.onMapLongClick(position);
		}
		
	}
	
	@Override
	public void onMapLoaded() {
		if (mOnMapLoadedObservers.isEmpty()) {
			return;
		}
		
		for (OnMapLoadedListener listener : mOnMapLoadedObservers) {
			listener.onMapLoaded();
		}
		
	}
	
}
