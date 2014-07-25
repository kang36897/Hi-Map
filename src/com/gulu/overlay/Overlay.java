package com.gulu.overlay;

import com.amap.api.maps.model.Marker;

public abstract class Overlay {
	
	public abstract boolean isOnThisOverlay(Marker marker);
	
	protected abstract void registEventListener();
	
	protected abstract void unregistEventListener();
	
	public abstract void doAddToMapInternal();
	
	public abstract void doRemoveFromMapInternal();
	
	public void addToMap() {
		registEventListener();
		doAddToMapInternal();
	}
	
	public void removeFromMap() {
		doRemoveFromMapInternal();
		unregistEventListener();
	}
}
