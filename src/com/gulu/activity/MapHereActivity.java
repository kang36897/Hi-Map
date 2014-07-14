package com.gulu.activity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.AMap.OnInfoWindowClickListener;
import com.amap.api.maps.AMap.OnMapClickListener;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.AMap.OnMarkerClickListener;
import com.amap.api.maps.AMap.OnMarkerDragListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.MyTrafficStyle;
import com.amap.api.maps.model.Polyline;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.VisibleRegion;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.help.Inputtips;
import com.amap.api.services.help.Inputtips.InputtipsListener;
import com.amap.api.services.help.Tip;
import com.gulu.R;
import com.gulu.adapter.AMapLocationListenerAdapter;
import com.gulu.adapter.SimpleTextWatcher;
import com.gulu.adapter.SuggestTipsAdapter;
import com.gulu.utils.GeocodeSearchHelper;
import com.gulu.utils.InfoWindowViewHolder;
import com.gulu.utils.MsgConstants;
import com.gulu.utils.QuestionAndAnswer;
import com.gulu.utils.TakeSnapshotHelper;
import com.gulu.utils.ToastUtil;

public class MapHereActivity extends Activity implements LocationSource,
		OnMapLoadedListener, OnClickListener, OnMapClickListener,
		OnMarkerClickListener, OnMarkerDragListener, OnInfoWindowClickListener,
		InfoWindowAdapter {
	
	public static final String ORIGINAL_CAMERA_POSTION = "original_camera_postion";
	
	private MapView mMapView;
	private Projection mProjection;
	private AMap mAMap;
	
	private Marker mPickMarker;
	private Polyline mPolyline;
	
	private View mMenuView;
	
	private String mCityCode = "shanghai";
	private View mSearchPaneView;
	private AutoCompleteTextView mOriginalView;
	private AutoCompleteTextView mDestinationView;
	private Tip mOriginalSelectedTip;
	private Tip mDestinationSelectedTip;
	
	private View mInfoWindowPane;
	
	private LocationManagerProxy mLocManager;
	private OnLocationChangedListener mOnLocationChangedListener;
	private AMapLocation mLastLocation;
	
	private AMapLocationListener mAMapLocationListener = new AMapLocationListenerAdapter() {
		
		public void onLocationChanged(AMapLocation location) {
			
			mLastLocation = location;
			
			if (mOnLocationChangedListener != null) {
				mOnLocationChangedListener.onLocationChanged(location);
				
			}
		};
	};
	
	private Handler mHandler;
	
	private TakeSnapshotHelper mSnapshotHelper;
	
	private GeocodeSearchHelper mGeocodeSearchHelper;
	private RegeocodeQuery mReGeoQuery;
	private RegeocodeResult mRegeocodeResult;
	
	private void updateGeoInfoWindow(RegeocodeResult result) {
		if (result == null) {
			return;
		}
		
		RegeocodeAddress address = result.getRegeocodeAddress();
		if (address != null && address.getFormatAddress() != null) {
			String addressName = result.getRegeocodeAddress()
					.getFormatAddress() + "附近";
			
			if (mInfoWindowPane == null) {
				return;
			}
			
			InfoWindowViewHolder holder = (InfoWindowViewHolder) mInfoWindowPane
					.getTag();
			holder.geoTitle.setText(addressName);
		} else {
			ToastUtil.show(MapHereActivity.this, R.string.no_result);
		}
		
	}
	
	private ViewPropertyAnimator mSearchPaneAnimator;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_here);
		
		mMapView = (MapView) findViewById(R.id.map);
		mMapView.onCreate(savedInstanceState);
		
		initAMap();
		mAMap.setMyLocationEnabled(true);
		
		if (mLocManager == null) {
			mLocManager = LocationManagerProxy.getInstance(this);
			
			mLocManager.requestLocationUpdates(
					LocationProviderProxy.AMapNetwork, 2000, 100,
					mAMapLocationListener);
		}
		
		setActivityView();
		initInfoWindowStuff();
		initHandlerAndHelper();
		
		restoreCameraAfterConfigureChanged(savedInstanceState);
	}
	
	private void initHandlerAndHelper() {
		mHandler = new Handler() {
			
			public void handleMessage(Message msg) {
				switch (msg.what) {
				
				case MsgConstants.SNAPSHOT_SUCCEED:
					ToastUtil.show(getApplicationContext(),
							R.string.screen_snapshot_succeed);
					break;
				
				case MsgConstants.SNAPHOT_FAILED:
					ToastUtil.show(getApplicationContext(),
							R.string.screen_snapshot_failed);
					break;
				
				case MsgConstants.RE_GEOCODE_SEARCH_SUCCEED:
					QuestionAndAnswer qa = (QuestionAndAnswer) msg.obj;
					if (qa.reGeoQuery.equals(mReGeoQuery)) {
						
						if (qa.reGeoResult == null) {
							ToastUtil.show(getApplicationContext(),
									R.string.no_result);
							break;
						}
						
						updateGeoInfoWindow(qa.reGeoResult);
					}
					break;
				
				case MsgConstants.RE_GEOCODE_SEARCH_FAILED:
					QuestionAndAnswer qas = (QuestionAndAnswer) msg.obj;
					GeocodeSearchHelper.defaultGeoSearchFailedCallback(
							getApplicationContext(), qas.responseCode);
					break;
				
				default:
					break;
				}
			};
		};
		
		mSnapshotHelper = new TakeSnapshotHelper(mAMap, mHandler);
		mGeocodeSearchHelper = new GeocodeSearchHelper(this, mHandler);
	}
	
	private void initInfoWindowStuff() {
		mInfoWindowPane = getLayoutInflater().inflate(R.layout.info_window_geo,
				null);
		
		TextView title = (TextView) mInfoWindowPane
				.findViewById(R.id.info_window_title);
		ImageView detail = (ImageView) mInfoWindowPane
				.findViewById(R.id.info_window_btn_detail);
		InfoWindowViewHolder holder = new InfoWindowViewHolder();
		holder.geoTitle = title;
		holder.geoDetail = detail;
		
		mInfoWindowPane.setTag(holder);
	}
	
	private void initAMap() {
		if (mAMap == null) {
			mAMap = mMapView.getMap();
			mProjection = mAMap.getProjection();
			
			// regist amap listener
			mAMap.setOnMapLoadedListener(this);
			
			mAMap.setOnMapClickListener(this);
			mAMap.setOnMarkerDragListener(this);
			mAMap.setOnMarkerClickListener(this);
			mAMap.setOnInfoWindowClickListener(this);
			mAMap.setInfoWindowAdapter(this);
			
			initMyLocationOverlay();
			
			MyTrafficStyle myTrafficStyle = new MyTrafficStyle();
			myTrafficStyle.setSeriousCongestedColor(0xff92000a);
			myTrafficStyle.setCongestedColor(0xffea0312);
			myTrafficStyle.setSlowColor(0xffff7508);
			myTrafficStyle.setSmoothColor(0xff00a209);
			mAMap.setMyTrafficStyle(myTrafficStyle);
			
		}
	}
	
	private void initMyLocationOverlay() {
		MyLocationStyle myLocationStyle = new MyLocationStyle();
		myLocationStyle
				.anchor(0.5f, 0.5f)
				.myLocationIcon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.location_marker))
				.strokeColor(Color.parseColor("#4B8DF8")).strokeWidth(0.01f)
				.radiusFillColor(Color.parseColor("#444B8DF8"));
		mAMap.setMyLocationStyle(myLocationStyle);
		
		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
		mAMap.getUiSettings().setMyLocationButtonEnabled(true);
		mAMap.setLocationSource(this);
	}
	
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private ActionBarDrawerToggle mDrawerToggle;
	
	private void setActivityView() {
		// TODO Auto-generated method stub
		
		mMenuView = findViewById(R.id.btn_temp);
		mMenuView.setOnClickListener(this);
		
		mSearchPaneView = findViewById(R.id.search_pane);
		mOriginalView = (AutoCompleteTextView) mSearchPaneView
				.findViewById(R.id.autotextview_roadsearch_start);
		mDestinationView = (AutoCompleteTextView) mSearchPaneView
				.findViewById(R.id.autotextview_roadsearch_goals);
		
		mOriginalView.addTextChangedListener(new SimpleTextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String keyWords = s.toString().trim();
				if (keyWords == null || keyWords.length() < 2) {
					return;
				}
				
				Inputtips iTips = new Inputtips(getApplicationContext(),
						new InputtipsListener() {
							
							@Override
							public void onGetInputtips(List<Tip> inputTips,
									int rCode) {
								SuggestTipsAdapter adapter = new SuggestTipsAdapter(
										getApplicationContext(), inputTips);
								mOriginalView.setAdapter(adapter);
								mOriginalView
										.setOnItemClickListener(new OnItemClickListener() {
											
											@Override
											public void onItemClick(
													AdapterView<?> parent,
													View view, int position,
													long id) {
												
												mOriginalSelectedTip = (Tip) parent
														.getItemAtPosition(position);
												
											}
										});
								adapter.notifyDataSetChanged();
							}
						});
				
				try {
					iTips.requestInputtips(keyWords, mCityCode);
				} catch (AMapException e) {
					e.printStackTrace();
				}
			};
		});
		mDestinationView.addTextChangedListener(new SimpleTextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				String keyWords = s.toString().trim();
				if (keyWords == null || keyWords.length() < 2) {
					return;
				}
				
				Inputtips iTips = new Inputtips(getApplicationContext(),
						new InputtipsListener() {
							
							@Override
							public void onGetInputtips(List<Tip> inputTips,
									int rCode) {
								SuggestTipsAdapter adapter = new SuggestTipsAdapter(
										getApplicationContext(), inputTips);
								mDestinationView.setAdapter(adapter);
								mDestinationView
										.setOnItemClickListener(new OnItemClickListener() {
											
											@Override
											public void onItemClick(
													AdapterView<?> parent,
													View view, int position,
													long id) {
												
												mDestinationSelectedTip = (Tip) parent
														.getItemAtPosition(position);
												
											}
										});
								adapter.notifyDataSetChanged();
							}
						});
				
				try {
					iTips.requestInputtips(keyWords, mCityCode);
				} catch (AMapException e) {
					e.printStackTrace();
				}
			};
		});
		
		initDrawerAfterAMap();
	}
	
	private void initDrawerAfterAMap() {
		String[] mDrawerItems = getResources().getStringArray(
				R.array.drawer_list);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
		mDrawerList.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, mDrawerItems));
		mDrawerList.setOnItemClickListener(new OnItemClickListener() {
			
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position) {
				case 0:
					mAMap.setMapType(AMap.MAP_TYPE_NORMAL);
					break;
				
				case 1:
					mAMap.setMapType(AMap.MAP_TYPE_SATELLITE);
					break;
				
				case 2:
					mAMap.setTrafficEnabled(true);
					break;
				case 3:
					clearMap();
					break;
				
				default:
					break;
				
				}
				mDrawerList.setItemChecked(position, true);
				mDrawerLayout.closeDrawer(mDrawerList);
			}
		});
		
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().hide();
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open,
				R.string.drawer_close);
		
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
		
		// save camera postion before configuration changed
		CameraPosition cameraPostion = mAMap.getCameraPosition();
		outState.putParcelable(ORIGINAL_CAMERA_POSTION, cameraPostion);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		mMapView.onResume();
		
	}
	
	private void restoreCameraAfterConfigureChanged(Bundle savedInstanceState) {
		
		if (savedInstanceState == null) {
			return;
		}
		
		if (!savedInstanceState.containsKey(ORIGINAL_CAMERA_POSTION)) {
			return;
		}
		
		CameraPosition cameraPostion = savedInstanceState
				.<CameraPosition> getParcelable(ORIGINAL_CAMERA_POSTION);
		VisibleRegion vRegion = mProjection.getVisibleRegion();
		if (vRegion.latLngBounds.contains(cameraPostion.target)) {
			return;
		}
		
		CameraUpdate cameraUpdate = CameraUpdateFactory
				.newCameraPosition(cameraPostion);
		mAMap.animateCamera(cameraUpdate);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mMapView.onPause();
		deactivate();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mMapView.onDestroy();
	}
	
	@Override
	public void activate(OnLocationChangedListener arg0) {
		mOnLocationChangedListener = arg0;
		
	}
	
	@Override
	public void deactivate() {
		
		mOnLocationChangedListener = null;
		
		if (mLocManager != null) {
			mLocManager.removeUpdates(mAMapLocationListener);
			mLocManager.destory();
		}
		
		mLocManager = null;
		
	}
	
	@Override
	public void onMapLoaded() {
		
		if (mLastLocation != null) {
			CameraPosition.Builder builder = CameraPosition.builder(
					mAMap.getCameraPosition()).target(
					new LatLng(mLastLocation.getLatitude(), mLastLocation
							.getLongitude()));
			CameraUpdate cameraUpdate = CameraUpdateFactory
					.newCameraPosition(builder.build());
			mAMap.animateCamera(cameraUpdate);
		}
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_temp:
			toggleSearchPane();
			// showMyLocation();
			// mDrawerLayout.openDrawer(mDrawerList);
			break;
		
		default:
			break;
		}
		
	}
	
	private void showMyLocation() {
		Location location = mAMap.getMyLocation();
		if (location == null) {
			ToastUtil.show(this, R.string.show_my_location_failed);
			return;
		}
		
		Marker myLocationMarker = mAMap.addMarker(new MarkerOptions()
				.anchor(0.5f, 0.5f)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.point))
				.position(
						new LatLng(location.getLatitude(), location
								.getLongitude()))
				.title(getString(R.string.resolving_please_wait)));
		doGeocodeSearchFromLocation(myLocationMarker);
		myLocationMarker.showInfoWindow();
		
	}
	
	private void takeSnapshot() {
		mSnapshotHelper.takeSnapshot();
	}
	
	private void toggleSearchPane() {
		if (mSearchPaneAnimator != null) {
			mSearchPaneAnimator.cancel();
		}
		
		if (mSearchPaneView.isShown()) {
			mSearchPaneAnimator = mSearchPaneView.animate().setDuration(400)
					.setInterpolator(new AccelerateInterpolator())
					.translationX(mSearchPaneView.getWidth()).alpha(0)
					.setListener(new AnimatorListenerAdapter() {
						
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);
							mSearchPaneView.setVisibility(View.INVISIBLE);
						}
					});
			mSearchPaneAnimator.start();
			
		} else {
			mSearchPaneView.setTranslationX(mSearchPaneView.getWidth());
			mSearchPaneView.setAlpha(0f);
			mSearchPaneAnimator = mSearchPaneView.animate().setDuration(600)
					.setInterpolator(new DecelerateInterpolator())
					.translationX(0.0f).alpha(1.0f)
					.setListener(new AnimatorListenerAdapter() {
						
						@Override
						public void onAnimationEnd(Animator animation) {
							super.onAnimationEnd(animation);
							mSearchPaneView.setVisibility(View.VISIBLE);
						}
					});
			mSearchPaneAnimator.start();
		}
	}
	
	private List<Marker> removeableMarkers = new ArrayList<Marker>();
	
	@Override
	public void onMapClick(LatLng latLng) {
		
		if (mPickMarker != null && mPickMarker.isVisible()) {
			return;
		}
		
		// clear old marker
		mPickMarker = null;
		mPickMarker = addRemoveableMarker(new MarkerOptions()
				.draggable(true)
				.perspective(true)
				.icon(BitmapDescriptorFactory
						.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
				.visible(true).title(getString(R.string.resolving_please_wait))
				.position(latLng));
		// mPickMarker.showInfoWindow();
	}
	
	private Marker addRemoveableMarker(MarkerOptions options) {
		Marker temp = mAMap.addMarker(options);
		removeableMarkers.add(temp);
		return temp;
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onMarkerDrag(Marker marker) {
		// TODO Auto-generated method stub
		// doDrawPolyline(marker);
		
	}
	
	@Override
	public void onMarkerDragEnd(Marker marker) {
		// TODO Auto-generated method stub
		doGeocodeSearchFromLocation(marker);
		marker.showInfoWindow();
	}
	
	@Override
	public void onMarkerDragStart(Marker marker) {
		ToastUtil.showAtTop(this, "you can move it now");
		marker.hideInfoWindow();
		
		// beginDrawPolyline(marker);
	}
	
	private void beginDrawPolyline(Marker marker) {
		if (mPolyline == null) {
			
			PolylineOptions options = new PolylineOptions();
			options.add(marker.getPosition());
			options.color(Color.parseColor("#0099cc"));
			options.width(9.0f);
			mPolyline = mAMap.addPolyline(options);
			
		}
	}
	
	private void doDrawPolyline(Marker marker) {
		if (mPolyline != null) {
			
			ArrayList<LatLng> temp = new ArrayList<LatLng>();
			temp.addAll(mPolyline.getPoints());
			temp.add(marker.getPosition());
			mPolyline.setPoints(temp);
			
			return;
		}
	}
	
	@Override
	public boolean onMarkerClick(Marker marker) {
		// TODO Auto-generated method stub
		
		doGeocodeSearchFromLocation(marker);
		return false;
	}
	
	private void doGeocodeSearchFromLocation(Marker marker) {
		if (marker == null) {
			return;
		}
		
		LatLng latLng = marker.getPosition();
		if (latLng == null) {
			return;
		}
		
		mReGeoQuery = mGeocodeSearchHelper.doGeodeocodeSearchFromLocation(
				latLng, 200, GeocodeSearch.AMAP);
	}
	
	@Override
	public View getInfoContents(Marker marker) {
		
		if (mInfoWindowPane == null) {
			return null;
		}
		
		InfoWindowViewHolder holder = (InfoWindowViewHolder) mInfoWindowPane
				.getTag();
		holder.geoTitle.setText(marker.getTitle());
		
		return mInfoWindowPane;
	}
	
	@Override
	public View getInfoWindow(Marker marker) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private void clearMap() {
		if (removeableMarkers.isEmpty()) {
			return;
		}
		
		Iterator<Marker> iterator = removeableMarkers.iterator();
		while (iterator.hasNext()) {
			Marker m = iterator.next();
			m.setVisible(false);
			m.remove();
			iterator.remove();
		}
	}
}
