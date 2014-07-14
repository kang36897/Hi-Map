package com.gulu.utils;

import java.util.HashMap;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.amap.api.maps.model.LatLng;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.gulu.R;
import com.gulu.adapter.OnGeocodeSearchListenerAdapter;

public class GeocodeSearchHelper {
	
	private Handler mHandler;
	private GeocodeSearch mGeoSearch;
	
	private HashMap<LatLng, RegeocodeQuery> mReGeoSearchQueryCached = new HashMap<LatLng, RegeocodeQuery>();
	private HashMap<RegeocodeQuery, RegeocodeResult> mReGeoSearchResultCached = new HashMap<RegeocodeQuery, RegeocodeResult>();
	
	public GeocodeSearchHelper(Context ctx, Handler handler) {
		this.mHandler = handler;
		this.mGeoSearch = new GeocodeSearch(ctx);
		
	}
	
	private OnGeocodeSearchListener mOnGeocodeSearchListener = new OnGeocodeSearchListenerAdapter() {
		
		@Override
		public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
			
			if (rCode == 0) {
				
				if (result != null) {
					cacheRegeocodeResult(result.getRegeocodeQuery(), result);
				}
				reGeoSearchCallback(MsgConstants.RE_GEOCODE_SEARCH_SUCCEED,
						result.getRegeocodeQuery(), result, rCode);
			} else {
				if (result != null) {
					reGeoSearchCallback(MsgConstants.RE_GEOCODE_SEARCH_FAILED,
							result.getRegeocodeQuery(), result, rCode);
				} else {
					reGeoSearchCallback(MsgConstants.RE_GEOCODE_SEARCH_FAILED,
							null, null, rCode);
				}
			}
		}
		
	};
	
	private void cacheRegeocodeResult(RegeocodeQuery query,
			RegeocodeResult result) {
		if (result == null) {
			return;
		}
		
		mReGeoSearchResultCached.put(query, result);
	}
	
	private void cacheRegeocodeQuery(RegeocodeQuery query) {
		if (query == null) {
			return;
		}
		mReGeoSearchQueryCached.put(
				LatLonUtil.translateFromPoint(query.getPoint()), query);
	}
	
	public RegeocodeQuery doGeodeocodeSearchFromLocation(LatLng latlng,
			float radius, String latLonType) {
		
		RegeocodeQuery query = mReGeoSearchQueryCached.get(latlng);
		
		if (query == null) {
			
			query = new RegeocodeQuery(LatLonUtil.translateToPoint(latlng),
					radius, latLonType);
			cacheRegeocodeQuery(query);
			mGeoSearch.setOnGeocodeSearchListener(mOnGeocodeSearchListener);
			mGeoSearch.getFromLocationAsyn(query);
			
			return query;
		}
		
		RegeocodeResult result = mReGeoSearchResultCached.get(query);
		
		if (result == null) {
			mGeoSearch.getFromLocationAsyn(query);
			return query;
		}
		
		reGeoSearchCallback(MsgConstants.RE_GEOCODE_SEARCH_SUCCEED, query,
				result, 0);
		
		return query;
	}
	
	private void reGeoSearchCallback(int what, RegeocodeQuery query,
			RegeocodeResult result, int responseCode) {
		Message msg = mHandler.obtainMessage(what);
		QuestionAndAnswer qa = new QuestionAndAnswer();
		qa.reGeoQuery = query;
		qa.reGeoResult = result;
		qa.responseCode = responseCode;
		msg.obj = qa;
		msg.sendToTarget();
	}
	
	public static void defaultGeoSearchFailedCallback(Context context,
			int responseCode) {
		if (responseCode == 27) {
			ToastUtil.show(context, R.string.error_network);
		} else if (responseCode == 32) {
			ToastUtil.show(context, R.string.error_key);
		} else {
			ToastUtil.show(context,
					context.getResources().getString(R.string.error_other)
							+ responseCode);
		}
	}
}
